package com.shuashuakan.android.modules.share

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v7.app.AlertDialog
import com.shuashuakan.android.R
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.Complain
import com.shuashuakan.android.data.api.model.FeedShare
import com.shuashuakan.android.data.api.model.detail.ShareContent
import com.shuashuakan.android.data.api.model.detail.ShareResult
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.event.ShareBoardDeleteFeedEvent
import com.shuashuakan.android.event.ShareBoardUnLikeFeedEvent
import com.shuashuakan.android.event.ShareEvent
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.ACCOUNT_PAGE
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.utils.*
import com.umeng.socialize.Config
import com.umeng.socialize.ShareAction
import com.umeng.socialize.UMShareAPI
import com.umeng.socialize.UMShareListener
import com.umeng.socialize.bean.SHARE_MEDIA
import com.umeng.socialize.bean.SHARE_MEDIA.*
import com.umeng.socialize.media.UMEmoji
import com.umeng.socialize.media.UMImage
import com.umeng.socialize.media.UMMin
import com.umeng.socialize.media.UMWeb
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareHelper @Inject constructor(
        val apiService: ApiService,
        val accountManager: AccountManager
) {


    fun doShare(
            context: Activity,
            shareContent: ShareContent?,
            feedId: String?,
            showUnLike: Boolean = false,
            showDelete: Boolean,
            chainFeedSource: String?,
            channelId: String? = null,
            isWeb: Boolean? = false,
            web: String? = "",
            videoUrl: String? = "",
            videoCoverUrl: String? = "",
            channelName: String? = "",
            title: String? = "",
            tag: String? = "",
            allowDownload: Boolean? = false,
            canEdit: Boolean? = false,
            message: String? = "",
            editableCount: Int? = 0,
            isMoreClick: Boolean = false,
            targetUserId: String? = "",
            isFollowed: Boolean? = false,
            type: String? = "",
            targetUserName: String? = "",
            isFans: Boolean? = false) {

        val shareListener = object : UMShareListener {
            override fun onResult(media: SHARE_MEDIA) {
                //分享完成回调接口
                if (accountManager.hasAccount()) {
                    apiService.shareFeedCallback(feedId).applySchedulers().subscribeApi(onNext = {

                    })
                }
                val shareName = getSpiderShareName(media.name)
                if (!shareName.isEmpty()) {
                    if (channelId != null) {
                        context.getSpider().shareChannelEvent(context, channelId, true, shareName)
                    } else {
                        context.getSpider().shareDetailsEvent(context, feedId, true, shareName)
                    }
                    if (isWeb != null && isWeb) {
                        context.getSpider().shareWebEvent(context, web, true, shareName)
                    }
                }
                if (shareType == ShareConfig.SHARE_TYPE_APP) {
                    context.getSpider().shareApp(getChangeShareWay(media.name))
                }
            }

            override fun onCancel(media: SHARE_MEDIA) {
                context.showLongToast(context.getString(R.string.string_has_cancel))
            }

            override fun onError(
                    media: SHARE_MEDIA,
                    throwable: Throwable) {
                val shareName = getSpiderShareName(media.name)
                if (!shareName.isEmpty()) {
                    if (channelId != null) {
                        context.getSpider().shareChannelEvent(context, channelId, false, shareName)
                    } else {
                        context.getSpider().shareDetailsEvent(context, feedId, false, shareName)
                    }
                    if (isWeb != null && isWeb) {
                        context.getSpider().shareWebEvent(context, web, false, shareName)
                    }
                }
            }

            override fun onStart(p0: SHARE_MEDIA) {}
        }


        if (isMoreClick) {
            val dialog = MoreAlertDialog(context, apiService, accountManager,
                    shareListener, feedId, showDelete, chainFeedSource, channelId, web,
                    videoUrl!!, videoCoverUrl!!, channelName!!, title!!, tag!!, allowDownload, canEdit, message, editableCount,
                    targetUserId, isFollowed, type, targetUserName, isFans)
            dialog.show()
        } else {
            val shareDialog = NewShareDialog(context, apiService, shareContent, accountManager,
                    shareListener, feedId, showDelete, chainFeedSource, channelId, web,
                    videoUrl!!, videoCoverUrl!!, channelName!!, title!!, tag!!, allowDownload, canEdit, message, editableCount, this)
            shareDialog.setFeedShareTopActive(this.feedShare)
            shareDialog.shareType = shareType
            shareDialog.show()
        }
        Config.isJumptoAppStore = true
    }

    private fun getSpiderShareName(name: String): String {
        when (name) {
            "WEIXIN" -> return "WeChat"
            "QQ" -> return "QQ"
            "WEIXIN_CIRCLE" -> return "WeChatCircle"
            "QZONE" -> return "qqSpace"
        }
        return ""
    }

    //用于 评论 分享
    fun doShare(
            context: Activity,
            commentShare: ShareResult?,
            viewName: String,
            feedId: String,
            commentId: Long,
            mediaType: String
    ) {
        Config.isJumptoAppStore = true
        val shareListener = object : UMShareListener {
            override fun onResult(media: SHARE_MEDIA) {
                //分享完成回调接口
                if (feedId != "")
                    context.getSpider().shareCommentEvent(context, feedId, mediaType, commentId, true, media.name)
            }

            override fun onCancel(media: SHARE_MEDIA) {
                context.showLongToast(context.getString(R.string.string_has_cancel))
            }

            override fun onError(
                    media: SHARE_MEDIA,
                    throwable: Throwable
            ) {
                context.getSpider().shareCommentEvent(context, feedId, mediaType, commentId, false, media.name)
            }

            override fun onStart(p0: SHARE_MEDIA) {}
        }


        val platformSource: String = when (viewName) {
            "copy_url" -> ShareConfig.SHARE_COPY_LINK
            "open_with_browser" -> ShareConfig.SHARE_OPEN_BRWOER
            "wechat_session" -> ShareConfig.SHARE_WECHAT_MINI
            "wechat_timeline" -> ShareConfig.SHARE_WECHAT_MOMENTS
            "QQ" -> ShareConfig.SHARE_QQ
            "QZONE" -> ShareConfig.SHARE_QZONE
            else -> ShareConfig.SHARE_COPY_LINK
        }

        getShareInfo(commentId.toString(), platformSource) {
            share(context, it, viewName, shareListener)
        }

    }

    fun doShare(context: Activity, mediaPath: String, viewName: String, thumb: Bitmap?, isEmoji: Boolean, block: () -> Unit) {
        Config.isJumptoAppStore = true
        val shareListener = object : UMShareListener {
            override fun onResult(media: SHARE_MEDIA) {
                //分享完成回调接口
                block()
            }

            override fun onCancel(media: SHARE_MEDIA) {
                context.showLongToast(context.getString(R.string.string_has_cancel))
            }

            override fun onError(
                    media: SHARE_MEDIA,
                    throwable: Throwable) {
                context.showLongToast(throwable.message)
            }

            override fun onStart(p0: SHARE_MEDIA) {}
        }
        share(context, mediaPath, viewName, shareListener, thumb, isEmoji)
    }


    private fun share(
            shareContent: ShareContent,
            context: Activity,
            type: String,
            shareListener: UMShareListener?,
            feedId: String?,
            chainFeedSource: String?
    ) {
        if (type == "wechat_session" || type == "wechat_timeline" || type == "QQ" || type == "qzone") {
            context.getSpider().shareClickEvent(context, feedId)
        }
        val umWeb = prepareShareUIIWeb(shareContent, context)
        if (type == "copy_url") {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            copyClipboard(clipboard, shareContent.url, context, context.getString(R.string.string_copy_link_success))
            context.getSpider().shareDetailsEvent(context, feedId, true, "CopyFeedURL")
        } else if (type == "open_with_browser") {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(shareContent.url)))
        } else if (type == "report") {
            //举报
            if (accountManager.hasAccount()) {
                if (reportList != null) {
                    reportFeed(feedId, context)
                } else {
                    getReportList(feedId, context)
                }
            } else {
                context.startActivity(ACCOUNT_PAGE)
            }
        } else if (type == "un_like") {
            //不感兴趣
            RxBus.get().post(ShareBoardUnLikeFeedEvent(feedId!!))
        } else if (type == "delete") {
            showDeleteFeed(feedId, context, chainFeedSource)
        } else {
            val shareMedia: SHARE_MEDIA = when {
                type == "wechat_session" -> WEIXIN
                type == "wechat_timeline" -> WEIXIN_CIRCLE
                type == "QQ" -> QQ
                else -> QZONE
            }
            val umMin: UMMin? = prepareUmMin(shareMedia, shareContent, context)
            with(ShareAction(context)) {
                withText(shareContent.title)
                platform = shareMedia
                umMin?.let {
                    withMedia(it)
                } ?: withMedia(umWeb)
                setCallback(shareListener)
                share()
            }
        }
        RxBus.get().post(ShareEvent(feedId, type))
    }

    private fun share(
            context: Activity,
            commentShare: ShareContent,
            type: String,
            shareListener: UMShareListener?
    ) {
        val umWeb = prepareShareUIIWeb(commentShare, context)

        when (type) {
            "copy_url" -> {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                copyClipboard(clipboard, commentShare.url, context, context.getString(R.string.string_copy_link_success))
            }
            "open_with_browser" -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(commentShare.url)))
            else -> {
                val shareMedia: SHARE_MEDIA = when (type) {
                    "wechat_session" -> WEIXIN
                    "wechat_timeline" -> WEIXIN_CIRCLE
                    "QQ" -> QQ
                    else -> QZONE
                }

                val umMin: UMMin? = prepareUmMin(shareMedia, commentShare, context)
                with(ShareAction(context)) {
                    withText(commentShare.title)
                    platform = shareMedia
                    umMin?.let {
                        withMedia(it)
                    } ?: withMedia(umWeb)
                    setCallback(shareListener)
                    share()
                }
            }
        }
    }

    private fun share(context: Activity,
                      mediaPath: String,
                      type: String,
                      shareListener: UMShareListener?,
                      thunbBitmap: Bitmap?, isEmoji: Boolean) {
        val shareMedia: SHARE_MEDIA = when (type) {
            "wechat_session" -> WEIXIN
            "QQ" -> QQ
            else -> QZONE
        }

        with(ShareAction(context)) {
            platform = shareMedia
            val isFile = File(mediaPath).isFile
            if (shareMedia == WEIXIN) {
                if (isEmoji) {
                    val emoji = if (isFile) UMEmoji(context, File(mediaPath)) else UMEmoji(context, mediaPath)
                    if (thunbBitmap != null) {
                        emoji.setThumb(UMImage(context, thunbBitmap))
                    } else {
                        emoji.setThumb(if (isFile) UMImage(context, File(mediaPath)) else UMImage(context, mediaPath))
                    }
                    withMedia(emoji)
                } else {
                    val umImage = if (isFile) UMImage(context, File(mediaPath)) else UMImage(context, mediaPath)
                    if (thunbBitmap != null) {
                        umImage.setThumb(UMImage(context, thunbBitmap))
                    } else {
                        umImage.setThumb(if (isFile) UMImage(context, File(mediaPath)) else UMImage(context, mediaPath))
                    }
                    withMedia(umImage)
                }
            } else {
                withMedia(if (isFile) UMImage(context, File(mediaPath)) else UMImage(context, mediaPath))
            }
            setCallback(shareListener)
            share()
        }
    }

    private fun prepareUmMin(
            shareMedia: SHARE_MEDIA,
            shareContent: ShareContent,
            context: Activity
    ): UMMin? {
        var umMin: UMMin? = null
        if (shareMedia == WEIXIN && !shareContent.path.isNullOrEmpty()) {
            umMin = UMMin(shareContent.url)
            umMin.setThumb(UMImage(context, shareContent.imageUrl))
            umMin.title = shareContent.title
            umMin.description = shareContent.content
            umMin.path = shareContent.path
            umMin.userName = shareContent.userName
        }
        return umMin
    }

    private fun copyClipboard(
            clipboard: ClipboardManager,
            tbPwd: String?,
            context: Activity,
            toast: String
    ) {
        clipboard.primaryClip = ClipData.newPlainText("", tbPwd)
        context.showLongToast(toast)
    }

    private fun prepareShareUIIWeb(
            shareContent: ShareResult,
            context: Activity
    ): UMWeb {
        return UMWeb(shareContent.data.url).apply {
            title = shareContent.data.title
            description = shareContent.data.content
            setThumb(UMImage(context, shareContent.data.image))
        }
    }

    private fun prepareShareUIIWeb(
            shareContent: ShareContent,
            context: Activity
    ): UMWeb {
        return UMWeb(shareContent.url).apply {
            title = shareContent.title
            description = shareContent.content
            setThumb(UMImage(context, shareContent.image))
        }
    }

    fun handleShareActivityCallback(
            context: Activity,
            requestCode: Int,
            resultCode: Int,
            data: Intent?
    ) {
        UMShareAPI.get(context)
                .onActivityResult(requestCode, resultCode, data)
    }

    fun release(context: Activity) {
        UMShareAPI.get(context)
                .release()
    }


    private var reportList: List<Complain>? = null

    private fun getReportList(feedId: String?, context: Context) {
        apiService.getComplainList().applySchedulers().subscribeApi(onNext = {
            reportList = it
            reportFeed(feedId, context)
        })
    }

    private fun showDeleteFeed(feedId: String?, context: Activity, chainFeedSource: String?) {
        val dialog: android.app.AlertDialog = android.app.AlertDialog.Builder(context)
                .setMessage(context.getString(R.string.is_delete_feed))
                .setPositiveButton(context.getString(R.string.string_confirm)) { dialog, _ ->
                    dialog.dismiss()
                    apiService.deleteFeed(feedId).applySchedulers().subscribeApi(onNext = {
                        if (it.result.isSuccess) {
                            context.showLongToast(context.getString(R.string.string_delete_success))
                            RxBus.get().post(ShareBoardDeleteFeedEvent(feedId
                                    ?: "", chainFeedSource))
                        } else {
                            context.showLongToast(context.getString(R.string.string_delete_error))
                        }
                    }, onApiError = {
                        if (it is ApiError.HttpError) {
                            context.showLongToast(it.displayMsg)
                        } else {
                            context.showLongToast(context.getString(R.string.string_delete_error))
                        }
                    })
                }
                .setNegativeButton(context.getString(R.string.string_cancel)) { dialog, _ ->
                    dialog.dismiss()
                }.create()
        dialog.show()
        val button = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
        val button2 = dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)
        button?.setTextColor(context.resources.getColor(R.color.black20))
        button2?.setTextColor(context.resources.getColor(R.color.black20))
    }

    private fun reportFeed(feedId: String?, context: Context) {
        val adapter = ComplainArrayAdapter(context, android.R.layout.simple_list_item_1, reportList!!)
        AlertDialog.Builder(context)
                .setAdapter(adapter) { _, p1 ->
                    val complain = reportList!![p1]
                    if (complain.url != null) {
                        context.startActivity(complain.url)
                    } else {
                        apiService.createComplain(feedId, complain.type).applySchedulers().subscribeApi(onNext = {
                            if (it.result.isSuccess) {
                                context.showLongToast(context.getString(R.string.string_thanks_for_you_report))
                            } else {
                                context.showLongToast(context.getString(R.string.string_operating_error))
                            }
                        }, onApiError = {
                            if (it is ApiError.HttpError) {
                                context.showLongToast(it.displayMsg)
                            } else {
                                context.showLongToast(context.getString(R.string.string_operating_error))
                            }
                        })
                    }
                }.show()
    }


    var feedShare: FeedShare? = null
    var shareType: String? = null
    /**
     * 分享顶部的活动卡片
     */
    fun initFeedShareActive(feedShare: FeedShare?) {
        this.feedShare = feedShare
    }


    fun getShareInfo(targetId: String?, platformSource: String, onRequestSuccess: (ShareContent) -> Unit) {
        apiService.getShareInfo(targetId, platformSource, shareType
                ?: ShareConfig.SHARE_TYPE_APP)
                .applySchedulers()
                .subscribeApi(onNext = { shareContent ->
                    onRequestSuccess(shareContent.data)
                }, onApiError = {
                })
    }


}