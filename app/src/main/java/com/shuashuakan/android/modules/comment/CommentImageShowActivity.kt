package com.shuashuakan.android.modules.comment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.support.v4.app.ActivityOptionsCompat
import android.transition.Explode
import android.view.View
import android.view.Window
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.github.chrisbanes.photoview.PhotoView
import com.jakewharton.rxbinding2.view.RxView
import com.luck.picture.lib.previewloading.CircleProgressView
import com.shuashuakan.android.R
import com.shuashuakan.android.constant.Constants
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.ui.base.FishActivity
import com.shuashuakan.android.modules.share.ShareConfig
import com.shuashuakan.android.modules.share.ShareHelper
import com.shuashuakan.android.utils.*
import com.tbruyelle.rxpermissions2.RxPermissions
import com.umeng.socialize.UMShareAPI
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class CommentImageShowActivity : FishActivity() {

    @Inject
    lateinit var exoPlayerHelper: com.shuashuakan.android.modules.player.ExoPlayerHelper

    private val mSimpleDraweeView by bindView<PhotoView>(R.id.simple_image)
    private val mProgressView by bindView<CircleProgressView>(R.id.circleProgressView)
    private val mGifImage by bindView<ImageView>(R.id.gif_image)
    private val btnDownload by bindView<ImageView>(R.id.btn_download)
    private val btnShare by bindView<ImageView>(R.id.btn_share)

    companion object {
        fun create(context: Context, previewUrl: String?, commentImage: ImageView, mediaType: String, waterUrl: String?, targetId: String?) {
            val intent = Intent(context, CommentImageShowActivity::class.java).putExtra("preview_url", previewUrl)
                    .putExtra("media_type", mediaType).putExtra("water_url", waterUrl).putExtra("target_id", targetId)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(context as Activity, commentImage, context.getString(R.string.image_trans_name))
                context.startActivity(intent, optionsCompat.toBundle())
            } else {
                context.startActivity(intent)
            }
        }

        fun create(context: Context, previewUrl: String?, mediaType: String, waterUrl: String?, targetId: String?) {
            val intent = Intent(context, CommentImageShowActivity::class.java).putExtra("preview_url", previewUrl)
                    .putExtra("media_type", mediaType).putExtra("water_url", waterUrl).putExtra("target_id", targetId)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(context as Activity)
                context.startActivity(intent, optionsCompat.toBundle())
            } else {
                context.startActivity(intent)
            }
        }
    }

    private lateinit var previewUrl: String
    private var waterUrl: String? = null
    private var mediaType: String? = null
    private var videoUrl: String? = null
    private var previewWidth: Int? = null
    private var previewHeight: Int? = null
    private var commentId: Long? = null
    private var targetId: String? = null
    @Inject
    lateinit var shareHelper: ShareHelper
    @Inject
    lateinit var apiService: ApiService
    private val compositeDisposable = CompositeDisposable()
    private var shareDialog: com.shuashuakan.android.modules.comment.CommentShareDialog? = null
    private val mKeyBoardListener by lazy { SoftKeyBoardListener(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            window.exitTransition = Explode()
        }
        setContentView(R.layout.activity_comment_image_show)
        if (intent != null) {
            previewUrl = intent.getStringExtra("preview_url")
            waterUrl = intent.getStringExtra("water_url")
            mediaType = intent.getStringExtra("media_type")
            videoUrl = intent.getStringExtra("video_url")
            previewWidth = intent.getIntExtra("width", 0)
            previewHeight = intent.getIntExtra("height", 0)
            commentId = intent.getLongExtra("comment_id", 0)
            targetId = intent.getStringExtra("target_id")
        }
        setViews()
        setListener()
    }

    private fun setViews() {
        if (mediaType != null) {
            when (mediaType) {
                com.shuashuakan.android.modules.comment.CommentListAdapter.TYPE_IMAGE -> {
                    showImage()
                }
                com.shuashuakan.android.modules.comment.CommentListAdapter.TYPE_LONG -> {
                    showImage()
                }
                com.shuashuakan.android.modules.comment.CommentListAdapter.TYPE_GIF -> {
                    showGifImage()
                }
            }
        }
    }

    private fun showGifImage() {
        mGifImage.visibility = View.VISIBLE
        setImageForGlide(this, previewUrl, mGifImage, false)
    }

    private fun showImage() {
        mSimpleDraweeView.visibility = View.VISIBLE
        setImageLoadingForGlide(this, previewUrl, mSimpleDraweeView, mProgressView)
    }

    @SuppressLint("CheckResult")
    private fun setListener() {
        mSimpleDraweeView.setOnClickListener { onBackPressed() }
        mGifImage.setOnClickListener { onBackPressed() }
        RxView.clicks(btnDownload)
                .throttleFirst(Constants.DOUBLE_CLICK_INTERVAL, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    toDownloadImage()
                }
        RxView.clicks(btnShare)
                .throttleFirst(Constants.DOUBLE_CLICK_INTERVAL, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (videoUrl != null) {
                        showShareDialog()
                    } else {
                        val items = arrayOf(getString(R.string.string_send_face_with_wechat),
                                getString(R.string.string_share_pic_withwechat),
                                getString(R.string.string_share_pic_with_qq))
                        AlertDialog.Builder(this)
                                .setItems(items) { _, which ->
                                    when (which) {
                                        0 -> shareAsEmoji()
                                        1 -> shareAsWeChat()
                                        2 -> shareHelper.doShare(this@CommentImageShowActivity, waterUrl
                                                ?: previewUrl, "QQ", null, false) {}
                                    }
                                }
                                .show()
                    }
                }
        mKeyBoardListener.setOnSoftKeyBoardChangeListener(object : SoftKeyBoardListener.OnSoftKeyBoardChangeListener {
            override fun keyBoardShow(height: Int) {
                KeyBoardUtil.hideInputSoftFromWindowMethod(this@CommentImageShowActivity, mSimpleDraweeView)
            }

            override fun keyBoardHide(height: Int) {

            }
        })
    }

    private fun shareAsWeChat() {
        Glide.with(this).asBitmap().load(previewUrl).into(object : SimpleTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                if (resource.byteCount > 10485760) {
                    showLongToast(getString(com.shuashuakan.android.base.ui.R.string.string_pic_max_length_error))
                    return
                }
                val bitmap = ImageUtils.compressScale(resource, 120f, 120f)
                if (bitmap != null) {
                    shareHelper.doShare(this@CommentImageShowActivity, waterUrl
                            ?: previewUrl, "wechat_session", bitmap, false) {}
                }
            }
        })
    }

    private fun shareAsEmoji() {
        Glide.with(this).asBitmap().load(previewUrl).into(object : SimpleTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                if (resource.byteCount > 1048576) {
                    showLongToast(getString(R.string.string_pic_max_length_error))
                    return
                }
                val bitmap = ImageUtils.compressScale(resource, 120f, 120f)
                if (bitmap != null) {
                    shareHelper.doShare(this@CommentImageShowActivity, waterUrl
                            ?: previewUrl, "wechat_session", bitmap, true) {}
                }
            }
        })
    }

    private fun showShareDialog() {
        if (shareDialog == null) {
            shareDialog = com.shuashuakan.android.modules.comment.CommentShareDialog(this, com.shuashuakan.android.modules.comment.CommentShareDialog.ShareDialogListener {
                shareHelper.shareType = ShareConfig.SHARE_TYPE_COMMENT
                shareHelper.doShare(this, null, exchangeId(it), targetId ?: "", commentId
                        ?: 0, mediaType ?: "")
            })
        }
        if (shareDialog != null)
            shareDialog!!.show()
    }

    private fun exchangeId(viewId: Int): String {
        when (viewId) {
            R.id.share_wechat -> return "wechat_session"
            R.id.share_moments -> return "wechat_timeline"
            R.id.share_qq -> return "QQ"
            R.id.share_qzone -> return "QZONE"
            R.id.share_copy_url -> return "copy_url"
            R.id.share_open_browser -> return "open_with_browser"
        }
        return ""
    }

    @SuppressLint("CheckResult")
    private fun toDownloadImage() {
        RxPermissions(this)
                .requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe { permission ->
                    when {
                        permission.granted -> {
                            if (mediaType.equals(com.shuashuakan.android.modules.comment.CommentListAdapter.TYPE_GIF))
                                downloadImage(waterUrl ?: previewUrl, true)
                            else
                                downloadImage(waterUrl ?: previewUrl, false)
                        }
                        permission.shouldShowRequestPermissionRationale -> applicationContext.showLongToast(getString(R.string.string_can_next_with_agree_tips))
                        else -> setGoSettingDialog()
                    }
                }
    }

    @SuppressLint("CheckResult")
    private fun downloadImage(imageUrl: String, isGif: Boolean) {
        Observable.create(ObservableOnSubscribe<File> {
            it.onNext(Glide.with(this)
                    .load(waterUrl ?: previewUrl)
                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get())
            it.onComplete()
        }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .flatMap(Function<File, ObservableSource<String>> { t ->
                    Observable.create {
                        val gifName =
                                if (isGif)
                                    "刷刷看_" + TimeUtil.getCurrentTime("yyyyMMddHHmmss") + ".gif"
                                else
                                    "刷刷看_" + TimeUtil.getCurrentTime("yyyyMMddHHmmss") + ".png"
                        val pictureFolder = Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera/"
                        val destFile = File(pictureFolder, gifName)
                        it.onNext(FileUtil.copyFile(t, destFile))
                        // 最后通知图库更新
                        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(File(destFile.path))))
                        it.onComplete()
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<String> {
                    override fun onComplete() {

                    }

                    override fun onSubscribe(d: Disposable) {
//            showLongToast("保存图片成功，保存位置：${d.toString()}")
                    }

                    override fun onNext(t: String) {
                        showLongToast(String.format(getString(com.shuashuakan.android.base.ui.R.string.string_pic_save_format), t))
                    }

                    override fun onError(e: Throwable) {
                        showLongToast(getString(com.shuashuakan.android.base.ui.R.string.string_save_photo_error))
                    }
                })
    }

    private fun setGoSettingDialog() {
        val dialog: AlertDialog = AlertDialog.Builder(this)
                .setMessage(getString(R.string.string_guide_permission))
                .setPositiveButton(getString(R.string.string_setting)) { dialogInterface, _ ->
                    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData(Uri.fromParts("package", packageName, null)))
                    dialogInterface.cancel()
                }
                .create()
        dialog.show()
        val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        button?.setTextColor(resources.getColor(R.color.black20))
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
        mKeyBoardListener.setOnSoftKeyBoardChangeListener(null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data)
    }
}
