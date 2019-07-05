package com.shuashuakan.android.modules.share

import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout.HORIZONTAL
import android.widget.RelativeLayout
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.luck.picture.lib.tools.ScreenUtils
import com.shuashuakan.android.R
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.Complain
import com.shuashuakan.android.data.api.model.FeedShare
import com.shuashuakan.android.data.api.model.FeedSharePosition
import com.shuashuakan.android.data.api.model.detail.ShareContent
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.event.DownloadVideoEvent
import com.shuashuakan.android.event.ShareBoardDeleteFeedEvent
import com.shuashuakan.android.event.ShareEvent
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.ACCOUNT_PAGE
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.publisher.RecordDataModel
import com.shuashuakan.android.modules.publisher.chains.ChainsPublishActivity
import com.shuashuakan.android.modules.track.ClickAction
import com.shuashuakan.android.service.PullService
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.utils.*
import com.shuashuakan.android.utils.extension.loadWithSize
import com.umeng.socialize.ShareAction
import com.umeng.socialize.UMShareListener
import com.umeng.socialize.bean.SHARE_MEDIA
import com.umeng.socialize.media.UMImage
import com.umeng.socialize.media.UMMin
import com.umeng.socialize.media.UMWeb

/**
 * Author:  lijie
 * Date:   2018/12/25
 * Email:  2607401801@qq.com
 */
class NewShareDialog(
        private val activity: Activity,
        private val apiService: ApiService,
        private val shareContent: ShareContent?,
        private val accountManager: AccountManager,
        private val shareListener: UMShareListener,
        private var feedId: String?,
        private val showDelete: Boolean,
        private val chainFeedSource: String?,
        private val channelId: String?,
        private val web: String?,
        private val videoUrl: String,
        private val videoCoverUrl: String,
        private val channelName: String,
        private val title: String,
        private val tag: String,
        private val allowDownload: Boolean? = false,
        private val canEdit: Boolean? = false,
        private val message: String? = "",
        private val editableCount: Int? = 0,
        private val shareHelper: ShareHelper
) : Dialog(activity, R.style.showCommentShareDialog) {

    private lateinit var shareChannelRcy: RecyclerView
    private lateinit var recyclerBottom: RecyclerView
    private lateinit var cancelBtn: TextView
    private lateinit var ivShareActiveImageView: ImageView

    private var topShareChannelData = mutableListOf<ShareData>()
    private var bottomOperData = mutableListOf<ShareData>()

    private lateinit var topShareChannelAdapter: BaseQuickAdapter<ShareData, BaseViewHolder>
    private lateinit var bottomOperAdapter: BaseQuickAdapter<ShareData, BaseViewHolder>

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_new_share, null, false)
        setContentView(view)
        getView(view)
    }

    private var reportList: List<Complain>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val window = window
        window!!.setGravity(Gravity.BOTTOM)
        window.setWindowAnimations(R.style.showAvatarDialog)
        val attributes = window.attributes
        attributes.width = context.getScreenSize().x
        window.attributes = attributes
        initListener()
        formatAdapterData()
        setRecyclerView()
    }

    private fun initListener() {
        cancelBtn.setOnClickListener {
            dismiss()
        }
        // 活动卡片的点击事件
        feedShare?.let {
            ivShareActiveImageView.setOnClickListener {
                // 视频资源位点击
                context.getSpider().manuallyEvent(SpiderEventNames.FEED_SHARE_CARD_CLICK).track()
                context.startActivity(feedShare!!.redirect_url)
            }
        }
    }


    private fun setRecyclerView() {
        val managerTop = LinearLayoutManager(context)
        managerTop.orientation = HORIZONTAL
        shareChannelRcy.layoutManager = managerTop
        val managerBottom = LinearLayoutManager(context)
        managerBottom.orientation = HORIZONTAL
        recyclerBottom.layoutManager = managerBottom

        topShareChannelAdapter = object : BaseQuickAdapter<ShareData, BaseViewHolder>(R.layout.item_share_layout) {
            override fun convert(helper: BaseViewHolder, item: ShareData) {
                val itemWidth: Int = (context.getScreenSize().x) / 5
                helper.itemView.layoutParams.width = itemWidth
                helper.getView<ImageView>(R.id.item_icon).setImageResource(item.image)
                helper.getView<TextView>(R.id.item_tv).text = item.name
                helper.itemView.setOnClickListener {
                    var type = ""
                    when (item.type) {
                        ShareConfig.SHARE_WECHAT_SESSION -> {
                            goPlatform(activity, SHARE_MEDIA.WEIXIN, item.type)
                            type = "WeChat"
                        }
                        ShareConfig.SHARE_WECHAT_MOMENTS -> {
                            goPlatform(activity, SHARE_MEDIA.WEIXIN_CIRCLE, item.type)
                            type = "WeChatCircle"
                        }
                        ShareConfig.SHARE_QQ -> {
                            goPlatform(activity, SHARE_MEDIA.QQ, item.type)
                            type = "QQ"
                        }
                        ShareConfig.SHARE_QZONE -> {
                            goPlatform(activity, SHARE_MEDIA.QZONE, item.type)
                            type = "qqSpace"
                        }
                        ShareConfig.SHARE_WECHAT_MINI -> {
                            goPlatform(activity, SHARE_MEDIA.WEIXIN, item.type)
                            type = "WeChat"
                        }
                    }
                    RxBus.get().post(ShareEvent(feedId, type))
                    context.getSpider().shareClickEvent(context, feedId)
                }
            }
        }
        topShareChannelAdapter.setNewData(topShareChannelData)
        shareChannelRcy.adapter = topShareChannelAdapter

        bottomOperAdapter = object : BaseQuickAdapter<ShareData, BaseViewHolder>(R.layout.item_share_layout) {
            override fun convert(helper: BaseViewHolder, item: ShareData) {
                val itemWidth: Int = (activity.getScreenSize().x) / 5
                helper.itemView.layoutParams.width = itemWidth
                helper.getView<ImageView>(R.id.item_icon).setImageResource(item.image)
                helper.getView<TextView>(R.id.item_tv).text = item.name


                if (item.name == context.getString(R.string.string_edit_change) && !canEdit!!) {
                    helper.getView<TextView>(R.id.item_tv).setTextColor(context.getColor1(R.color.color_normal_b6b6b6))
                } else {
                    helper.getView<TextView>(R.id.item_tv).setTextColor(context.getColor1(R.color.color_normal_5d6066))
                }

                helper.itemView.setOnClickListener {
                    when (item.type) {
                        ShareConfig.SHARE_COPY_LINK -> {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            shareHelper.getShareInfo(getFeedId(), ShareConfig.SHARE_COPY_LINK) {
                                copyClipboard(clipboard, it.content + it.url, activity, context.getString(R.string.string_copy_link_success))
                            }

                            if (channelId != null) {
                                context.getSpider().shareChannelEvent(context, channelId, true, "CopyFeedURL")
                            } else {
                                context.getSpider().shareDetailsEvent(context, feedId, true, "CopyFeedURL")
                            }
                            if (web != "") {
                                context.getSpider().shareWebEvent(context, web, true, "CopyFeedURL")
                            }
                        }
                        ShareConfig.SHARE_OPEN_BRWOER -> {
                            shareHelper.getShareInfo(getFeedId(), ShareConfig.SHARE_OPEN_BRWOER) {
                                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.url)))
                            }
                        }
                        ShareConfig.SHARE_SAVE_ALUME -> {
                            saveVideo()
                        }
                        ShareConfig.SHARE_EDIT_CHANGE -> {
                            if (canEdit!!) {
                                toastCustomText(mContext, String.format(activity.getString(R.string.string_change_number_format), editableCount!!))
                                context.getSpider().editVideoEvent(context, feedId!!, true)
                                editVideo()
                            } else {
                                toastCustomText(mContext, message!!)
                                context.getSpider().editVideoEvent(context, feedId!!, false)
                            }
                        }
                        ShareConfig.SHARE_DELETE -> {
                            showDeleteFeed(feedId, activity, chainFeedSource)
                        }
                        ShareConfig.SHARE_FEED_BACK -> {
                            reportMethod()
                        }
                    }
                    dismiss()
                }
            }
        }
        bottomOperAdapter.setNewData(bottomOperData)
        recyclerBottom.adapter = bottomOperAdapter
    }

    private fun saveVideo() {
        RxBus.get().post(DownloadVideoEvent(tag, feedId!!))
    }

    private fun editVideo() {
        context.startActivity(ChainsPublishActivity.create(context, RecordDataModel(
                videoCoverUrl, null, PullService.UploadEntity.TYPE_ADD_EDITED_VIDEO,
                null, channelId, channelName), feedId!!, videoUrl, title, canEdit!!, editableCount!!))
    }

    private fun reportMethod() {
        //举报
        if (accountManager.hasAccount()) {
            if (reportList != null) {
                reportFeed(feedId, activity)
            } else {
                getReportList(feedId, activity)
            }
        } else {
            activity.startActivity(ACCOUNT_PAGE)
        }
    }

    fun goPlatform(mActivity: Activity, shareMedia: SHARE_MEDIA, platformSource: String) {
        if (web?.isNotEmpty()!!) {
            shareContent?.let { shareContent ->
                shareTo(shareContent, mActivity, shareMedia)
            }
            return
        }

        shareHelper.getShareInfo(getFeedId(), platformSource) { shareContent ->
            shareTo(shareContent, mActivity, shareMedia)
        }
    }

    private fun shareTo(shareContent: ShareContent, mActivity: Activity, shareMedia: SHARE_MEDIA) {
        val umWeb = prepareShareUIIWeb(shareContent, mActivity)
        val umMin: UMMin? = prepareUmMin(shareMedia, shareContent, mActivity)
        with(ShareAction(mActivity)) {
            withText(shareContent.title)
            platform = shareMedia
            umMin?.let {
                withMedia(it)
            } ?: withMedia(umWeb)
            setCallback(shareListener)
            share()
            dismiss()
        }
    }

    private fun getFeedId(): String? {
        feedId = when (shareType) {
            ShareConfig.SHARE_TYPE_APP -> null
            ShareConfig.SHARE_TYPE_VIDEO -> feedId
            ShareConfig.SHARE_TYPE_CHANNEL -> channelId
            else -> null
        }
        return feedId
    }

    private fun copyClipboard(clipboard: ClipboardManager, tbPwd: String?, context: Context, toast: String) {
        clipboard.primaryClip = ClipData.newPlainText("", tbPwd)
        context.showLongToast(toast)
    }

    private fun prepareShareUIIWeb(shareContent: ShareContent, context: Activity): UMWeb {
        return UMWeb(shareContent.url).apply {
            title = shareContent.title
            description = shareContent.content
            setThumb(UMImage(context, shareContent.image))
        }
    }

    private fun prepareUmMin(shareMedia: SHARE_MEDIA, shareContent: ShareContent, context: Activity): UMMin? {
        var umMin: UMMin? = null
        if (shareMedia == SHARE_MEDIA.WEIXIN && !shareContent.path.isNullOrEmpty()) {
            umMin = UMMin(shareContent.webpageUrl)
            umMin.setThumb(UMImage(context, shareContent.imageUrl))
            umMin.title = shareContent.title
            umMin.description = shareContent.content ?: ""
            umMin.path = shareContent.path
            umMin.userName = shareContent.userName
        }
        return umMin
    }


    private fun getReportList(feedId: String?, context: Context) {
        apiService.getComplainList().applySchedulers().subscribeApi(onNext = {
            reportList = it
            reportFeed(feedId, context)
        })
    }

    private fun reportFeed(feedId: String?, context: Context) {
        context.getSpider().manuallyEvent(SpiderEventNames.FEED_BACK_EXPOSURE).track()
        val adapter = ComplainArrayAdapter(context, android.R.layout.simple_list_item_1, reportList!!)
        AlertDialog.Builder(context)
                .setAdapter(adapter) { _, p1 ->
                    val complain = reportList!![p1]
                    if (complain.url != null) {
                        context.startActivity(complain.url)
                    } else {
                        apiService.createComplain(feedId, complain.type).applySchedulers().subscribeApi(onNext = {
                            if (it.result.isSuccess) {
                                context.getSpider().manuallyEvent(SpiderEventNames.FEED_BACK_RESULT).put("isSuccess", true).track()
                                context.showLongToast(context.getString(R.string.string_thanks_for_you_report))
                            } else {
                                context.showLongToast(context.getString(R.string.string_operating_error))
                                context.getSpider().manuallyEvent(SpiderEventNames.FEED_BACK_RESULT).put("isSuccess", false).track()
                            }
                        }, onApiError = {
                            context.getSpider().manuallyEvent(SpiderEventNames.FEED_BACK_RESULT).put("isSuccess", false).track()
                            if (it is ApiError.HttpError) {
                                context.showLongToast(it.displayMsg)
                            } else {
                                context.showLongToast(context.getString(R.string.string_operating_error))
                            }
                        })
                    }
                }.show()
    }

    private fun showDeleteFeed(feedId: String?, context: Context, chainFeedSource: String?) {
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
        button?.setTextColor(ContextCompat.getColor(context, R.color.black20))
        button2?.setTextColor(ContextCompat.getColor(context, R.color.black20))
    }


    var feedShare: FeedShare? = null
    var shareType: String? = null
    /**
     * 分享顶部的活动卡片
     */
    fun setFeedShareTopActive(feedShare: FeedShare?) {
        this.feedShare = feedShare
    }


    private fun getView(view: View) {
        shareChannelRcy = view.findViewById(R.id.recycler_top)
        recyclerBottom = view.findViewById(R.id.recycler_bottom)
        cancelBtn = view.findViewById(R.id.share_cancel)
        ivShareActiveImageView = view.findViewById(R.id.share_dialog_active)
    }

    private fun formatAdapterData() {
        // 微信分享好友是直接分享到小程序
        topShareChannelData.add(ShareData(R.drawable.ic_wechat_share, context.getString(R.string.string_share_wechat_friend_label), ShareConfig.SHARE_WECHAT_MINI))
        topShareChannelData.add(ShareData(R.drawable.ic_discover_share, context.getString(R.string.string_share_wechat_circle), ShareConfig.SHARE_WECHAT_MOMENTS))
        topShareChannelData.add(ShareData(R.drawable.ic_qq_share, context.getString(R.string.string_share_qq_friends), ShareConfig.SHARE_QQ))
        topShareChannelData.add(ShareData(R.drawable.ic_qqzone_share, context.getString(R.string.string_share_qq_place), ShareConfig.SHARE_QZONE))


        bottomOperData.add(ShareData(R.drawable.ic_link_share, context.getString(R.string.string_share_copy_link), ShareConfig.SHARE_COPY_LINK))
        bottomOperData.add(ShareData(R.drawable.ic_safari_share, context.getString(R.string.string_open_link_with_brower), ShareConfig.SHARE_OPEN_BRWOER))
        if (allowDownload!!) {
            bottomOperData.add(ShareData(R.drawable.ic_download_share, context.getString(R.string.string_save_to_picture), ShareConfig.SHARE_SAVE_ALUME))
        }
        if (feedId != null) {
            if (chainFeedSource != ClickAction.FeedSource.PUBLISH_LIST.source) {
                bottomOperData.add(ShareData(R.drawable.ic_advice_new, context.getString(R.string.string_feed_back_label), ShareConfig.SHARE_FEED_BACK))
            }
        }

        if (showDelete) {
            if (canEdit!!) {
                bottomOperData.add(ShareData(R.drawable.ic_video_edit, context.getString(R.string.string_edit_change), ShareConfig.SHARE_EDIT_CHANGE))
            } else {
                bottomOperData.add(ShareData(R.drawable.ic_video_edit_disabled, context.getString(R.string.string_edit_change), ShareConfig.SHARE_EDIT_CHANGE))
            }
            bottomOperData.add(ShareData(R.drawable.ic_delete_share, context.getString(R.string.string_delete), ShareConfig.SHARE_DELETE))
        }

        feedShare?.let {
            val currentTimeMillis = System.currentTimeMillis()
            if (feedShare!!.expire_at > currentTimeMillis) {
                // 视频资源位曝光
                context.getSpider().manuallyEvent(SpiderEventNames.FEED_SHARE_CARD_EXPOSURE).track()
                var layoutParams = ivShareActiveImageView.layoutParams as RelativeLayout.LayoutParams

                when (feedShare!!.position) {
                    FeedSharePosition.left.position -> {
                        layoutAndLoadPicView(layoutParams, RelativeLayout.ALIGN_PARENT_LEFT, 0, isFull = false)
                    }
                    FeedSharePosition.right.position -> {
                        layoutAndLoadPicView(layoutParams, RelativeLayout.ALIGN_PARENT_RIGHT, 0, isFull = false)
                    }
                    FeedSharePosition.center.position -> {
                        layoutAndLoadPicView(layoutParams, RelativeLayout.CENTER_HORIZONTAL, 0, isFull = false)
                    }
                    FeedSharePosition.full.position -> {
                        layoutAndLoadPicView(layoutParams, RelativeLayout.ALIGN_PARENT_LEFT, context.dip(15), isFull = true)
                    }
                }
            }
        }
    }


    /**
     * 重新布置布局参数
     */
    private fun layoutAndLoadPicView(layoutParams: RelativeLayout.LayoutParams, gravity: Int, margin: Int, isFull: Boolean) {
        layoutParams.addRule(gravity, R.id.share_dialog_root_view)
        layoutParams.leftMargin = margin
        layoutParams.rightMargin = margin
        layoutParams.bottomMargin = margin

        if (!isFull) {
            ivShareActiveImageView.loadWithSize(feedShare!!.image_url) { bitmap, width, height ->
                layoutParams.width = width
                layoutParams.height = height
                ivShareActiveImageView.layoutParams = layoutParams
                ivShareActiveImageView.setImageBitmap(bitmap)
            }
        } else {
            ivShareActiveImageView.loadWithSize(feedShare!!.image_url) { bitmap, width, height ->
                var itemWidth = ScreenUtils.getScreenWidth(activity) - ScreenUtils.dip2px(activity, 30f)
                layoutParams.width = itemWidth
                layoutParams.height = (height * (itemWidth.toFloat() / width)).toInt()
                ivShareActiveImageView.layoutParams = layoutParams
                ivShareActiveImageView.setImageBitmap(bitmap)
            }
        }
    }
}