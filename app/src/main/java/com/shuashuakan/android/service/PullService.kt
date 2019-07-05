package com.shuashuakan.android.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.os.Parcel
import android.os.Parcelable
import com.qiniu.android.common.AutoZone
import com.qiniu.android.storage.*
import com.shuashuakan.android.FishInjection
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.comment.ApiComment
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.data.api.services.UploadService
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.event.UpdatePublishFeedListEvent
import com.shuashuakan.android.event.UploadFailedEvent
import com.shuashuakan.android.event.UploadQiniuProgressEvent
import com.shuashuakan.android.event.UploadSuccessEvent
import com.shuashuakan.android.spider.Spider
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.utils.getUserId
import com.shuashuakan.android.utils.showLongToast
import com.shuashuakan.android.exts.subscribeApi
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/11/21
 * Description:
 */
class PullService : Service() {
    @Inject
    lateinit var uploadService: UploadService
    @Inject
    lateinit var apiService: ApiService

    private var uploadManager: UploadManager? = null
    @Inject
    lateinit var accountManager: AccountManager
    @Inject
    lateinit var spider: Spider

    override fun onCreate() {
        super.onCreate()
        FishInjection.inject(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            if (intent.getParcelableExtra<UploadEntity>("entity") != null) {
                if (uploadingEntity == null) {
                    uploadingEntity = intent.getParcelableExtra("entity")
                    uploadFile()
                }
            } else {
                if (intent.getStringExtra("operation") == "reply") {
                    uploadingEntity?.isReplied = true
                    uploadFile()
                }

            }
        }
        return Service.START_STICKY
    }

    companion object {
        private var uploadingEntity: UploadEntity? = null

        fun canUpload(): Boolean {
            return uploadingEntity == null
        }

        fun uploadComment(context: Context, filePath: String, fileType: String, textContent: String?, targetId: String): Boolean {
            val intent = Intent(context, PullService::class.java)
            intent.putExtra("entity", UploadEntity(filePath, fileType, UploadEntity.TYPE_ADD_COMMENT, targetId, textContent))
            context.startService(intent)
            return uploadingEntity == null
        }

        fun uploadSolitaire(context: Context, filePath: String, fileType: String, textContent: String?, time: Int?, targetId: String?): Boolean {
            val intent = Intent(context, PullService::class.java)
            intent.putExtra("entity", UploadEntity(filePath, fileType, UploadEntity.TYPE_ADD_SOLITAIRE, targetId, textContent, false, "", time))
            context.startService(intent)
            return uploadingEntity == null
        }

        fun uploadHomeVideo(context: Context, filePath: String, fileType: String, textContent: String?, targetId: String, chanelId: String, time: Int?, source: Int): Boolean {
            val intent = Intent(context, PullService::class.java)
            intent.putExtra("entity", UploadEntity(filePath, fileType, source, targetId, textContent, false, chanelId, time))
            context.startService(intent)
            return uploadingEntity == null
        }

        fun replyUpload(context: Context) {
            val intent = Intent(context, PullService::class.java)
            intent.putExtra("operation", "reply")
            context.startService(intent)
        }

        fun getEntity(): UploadEntity? {
            return uploadingEntity
        }

        fun deleteEntityFile(context: Context?) {
            if (context == null) {
                return
            }
            if (uploadingEntity != null) {
                val filePath = uploadingEntity?.filePath
                if (filePath != null) {
                    val file = File(filePath)
                    if (file.exists() && filePath.contains(context.packageName)) {
                        if (file.delete()) {
                            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))
                        }
                    }
                }
            }
            uploadingEntity = null
        }
    }

    private fun deleteEntity(context: Context = this) {
        deleteEntityFile(context)
    }

    private fun initUpload() {
        val config = Configuration.Builder()
                .chunkSize(512 * 1024)        // 分片上传时，每片的大小。 默认256K
                .putThreshhold(1024 * 1024)   // 启用分片上传阀值。默认512K
                .connectTimeout(10)           // 链接超时。默认10秒
                .useHttps(true)               // 是否使用https上传域名
                .responseTimeout(60)          // 服务器响应超时。默认60秒
//        .recorder(recorder)           // recorder分片上传时，已上传片记录器。默认null
//        .recorder(recorder, keyGen)   // keyGen 分片上传时，生成标识符，用于片记录器区分是那个文件的上传记录
                .zone(AutoZone.autoZone)       // 设置区域，指定不同区域的上传域名、备用域名、备用IP。
                .build()
        uploadManager = UploadManager(config)
    }

    private fun uploadFile() {
        uploadingEntity ?: return
        val filePath = uploadingEntity!!.filePath

        if (File(filePath).exists()) {
            val type = if (uploadingEntity!!.fileType!!.contains("video"))
                "VIDEO"
            else
                "IMAGE_UPLOAD"
            uploadService.getUploadToken(type, uploadingEntity!!.time?.toLong()).applySchedulers().subscribeApi(onNext = {
                if (uploadManager == null) initUpload()
                uploadManager?.put(filePath, it.key, it.token, { _, info, _ ->
                    if (info.isOK) {
                        sendServer(it.id)
                    } else {
                        RxBus.get().post(UploadFailedEvent(uploadingEntity?.isReplied, uploadingEntity!!.uploadType))
                        uploadingEntity?.let {
                            if (it.targetId != null) {
                                val commentType = if (it.fileType?.contains("video") == true) {
                                    "media"
                                } else {
                                    "img"
                                }
                                setCommentSpider(null, commentType)
                            }
                        }
                    }
                }, UploadOptions(null, null, false,
                        UpProgressHandler { _, percent ->
                            if (uploadingEntity!!.uploadType == UploadEntity.TYPE_ADD_SOLITAIRE
                                    || uploadingEntity!!.uploadType == UploadEntity.TYPE_ADD_HOME_VIDEO || uploadingEntity!!.uploadType == UploadEntity.TYPE_ADD_CHANNEL_VIDEO) {
                                RxBus.get().post(UploadQiniuProgressEvent((percent * 100).toInt(), uploadingEntity!!.uploadType, filePath))
                            }
                        }, null, NetReadyHandler {
                    RxBus.get().post(UploadFailedEvent(uploadingEntity?.isReplied, uploadingEntity!!.uploadType))
//          if (uploadingEntity?.isReplied != null && uploadingEntity?.isReplied!!) {
//            deleteEntity()
//          }
                }))
            }, onApiError = {
                RxBus.get().post(UploadFailedEvent(uploadingEntity?.isReplied, uploadingEntity!!.uploadType))
//        deleteEntity()
            })
        } else {
            deleteEntity()
        }
    }

    private fun sendServer(id: String) {
        if (uploadingEntity!!.uploadType == UploadEntity.TYPE_ADD_COMMENT) {
            if (uploadingEntity!!.targetId != null) {
                apiService.createComment(uploadingEntity!!.targetId!!, "FEED", uploadingEntity!!.textContent, id).applySchedulers().subscribeApi(onNext = {

                    val commentType = if (uploadingEntity!!.fileType!!.contains("video")) {
                        "media"
                    } else {
                        "img"
                    }
                    setCommentSpider(it, commentType)
                    deleteEntity()
                }, onApiError = {
                    deleteEntity()
                    Timber.d("上传自己的服务器失败")
                })
            } else {
                deleteEntity()
            }
        } else if (uploadingEntity!!.uploadType == UploadEntity.TYPE_ADD_SOLITAIRE) {
            if (uploadingEntity!!.targetId != null) {
                apiService.createSolitaire(id, uploadingEntity!!.targetId!!, uploadingEntity!!.textContent).applySchedulers().subscribeApi(
                        onNext = {
                            RxBus.get().post(UploadSuccessEvent(it, uploadingEntity!!.uploadType))
                            RxBus.get().post(UpdatePublishFeedListEvent())
                            setChainSpider(it)
                            deleteEntity()
                        },
                        onApiError = { apiError ->
                            uploadingEntity?.let {
                                RxBus.get().post(UploadFailedEvent(it.isReplied, it.uploadType))
                            }
                            showLongToast((apiError as? ApiError.HttpError)?.displayMsg)
                        })
            }
        } else if (uploadingEntity!!.uploadType == UploadEntity.TYPE_ADD_HOME_VIDEO
                || uploadingEntity!!.uploadType == UploadEntity.TYPE_ADD_CHANNEL_VIDEO) {

            apiService.createMainFeed(uploadingEntity!!.chanelId.toLong(), id.toLong(), uploadingEntity!!.textContent!!, "")
                    .applySchedulers().subscribeApi(
                            onNext = {
                                RxBus.get().post(UploadSuccessEvent(it, uploadingEntity!!.uploadType))
                                RxBus.get().post(UpdatePublishFeedListEvent())
                                deleteEntity()
                            },
                            onApiError = {
                                RxBus.get().post(UploadFailedEvent(uploadingEntity?.isReplied, uploadingEntity!!.uploadType))
                                /*if (uploadingEntity!!.isReplied) {
                                  deleteEntity()
                                }*/
                                //val error = ((it as? ApiError.HttpError)?.apiError?.getOrElse { null } as? FishApiError)
                                showLongToast((it as? ApiError.HttpError)?.displayMsg)
                            })
        } else {
            deleteEntity()
        }
    }

    //接龙成功的打点
    private fun setChainSpider(it: Feed) {
        spider.manuallyEvent(SpiderEventNames.SOLITAIRE_DETAILS)
                .put("masterFeed", it.masterFeedId ?: "")
                .put("userID", this.getUserId())
                .put("solitaireFeed", it.id)
                .track()
    }

    private fun setCommentSpider(it: ApiComment?, commentType: String) {
        spider.manuallyEvent(SpiderEventNames.COMMENT)
                .put("commentTarget", uploadingEntity!!.targetId!!)
                .put("TargetType", "FEED")
                .put("feedID", uploadingEntity!!.targetId!!)
                .put("content", uploadingEntity!!.textContent ?: "")
                .put("userID", accountManager.account()?.userId ?: "")
                .put("commentType", commentType)
                .put("isSuccess", it != null)
                .put("commentID", it?.id ?: "")
                .track()
    }

    data class UploadEntity(
            val filePath: String?,
            val fileType: String?,
            val uploadType: Int,
            val targetId: String?,
            val textContent: String?,
            var isReplied: Boolean = false,
            val chanelId: String = "",
            val time: Int? = 3000) : Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString(),
                parcel.readInt(),
                parcel.readString(),
                parcel.readString(),
                parcel.readByte() != 0.toByte(),
                parcel.readString() ?: "",
                parcel.readValue(Int::class.java.classLoader) as? Int)

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(filePath)
            parcel.writeString(fileType)
            parcel.writeInt(uploadType)
            parcel.writeString(targetId)
            parcel.writeString(textContent)
            parcel.writeByte(if (isReplied) 1 else 0)
            parcel.writeString(chanelId)
            parcel.writeValue(time)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object {
            const val TYPE_ADD_COMMENT = 1
            const val TYPE_ADD_SOLITAIRE = 2
            const val TYPE_ADD_HOME_VIDEO = 3
            const val TYPE_ADD_CHANNEL_VIDEO = 4
            const val TYPE_ADD_EDITED_VIDEO = 5
            @JvmField
            val CREATOR: Parcelable.Creator<UploadEntity> = object : Parcelable.Creator<UploadEntity> {
                override fun createFromParcel(source: Parcel): UploadEntity = UploadEntity(source)
                override fun newArray(size: Int): Array<UploadEntity?> = arrayOfNulls(size)
            }
        }
    }
}

