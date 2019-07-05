package com.shuashuakan.android.modules.timeline.profile

import android.animation.Animator
import android.app.Activity
import android.content.Intent
import android.support.v4.app.FragmentManager
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.*
import com.airbnb.lottie.LottieAnimationView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.facebook.drawee.view.SimpleDraweeView
import com.shuashuakan.android.R
import com.shuashuakan.android.constant.Constants
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.ACCOUNT_PAGE
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.comment.CommentImageShowActivity
import com.shuashuakan.android.modules.comment.VideoCommentDialogFragment
import com.shuashuakan.android.modules.player.VideoPlayerManager
import com.shuashuakan.android.modules.profile.UserProfileActivity
import com.shuashuakan.android.modules.share.ShareConfig
import com.shuashuakan.android.modules.share.ShareHelper
import com.shuashuakan.android.modules.timeline.AdapterToPlayerListener
import com.shuashuakan.android.modules.topic.TopicDetailActivity
import com.shuashuakan.android.modules.widget.SpiderAction
import com.shuashuakan.android.modules.widget.TimeLinePlayerView
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.ui.ProgressDialog
import com.shuashuakan.android.utils.*
import com.shuashuakan.android.utils.download.DownloadManager
import com.shuashuakan.android.utils.extension.showLongToast
import java.util.*

/**
 * 个人页面的 TimeLine
 *
 * Author: ZhaiDongyang
 * Date: 2019/1/16
 */
class ProfileTimelineAdapter constructor(
        private val fragmentManager: FragmentManager,
        private val accountManager: AccountManager,
        private val apiService: ApiService,
        private val shareHelper: ShareHelper,
        private val mChannelId: Long,
        private val mUserId: String,
        dataList: List<MultiItemEntity>?) : BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(dataList) {

    companion object {
        private const val TYPE_LONG = "LONG_IMAGE"
        private const val TYPE_GIF = "ANIMATION"
        private const val TYPE_IMAGE = "IMAGE"

        private const val TAG = "ProfileTimelineAdapter"
        const val TIMELINE = 0
        const val TIMELINE_GRID = 1
    }

    val uuid = createUUID()

    var listener: OnAdapterPlayerViewClickListener? = null

    interface OnAdapterPlayerViewClickListener {
        fun onAdapterPlayerViewClickListener(positon: Int)
    }

    init {
        addItemType(TIMELINE, R.layout.item_timeline_profile)
        addItemType(TIMELINE_GRID, R.layout.liked_list_video)
    }

    override fun convert(helper: BaseViewHolder, item: MultiItemEntity) {
        val itemData = item as com.shuashuakan.android.modules.discovery.ItemDataPair
        val timeLineFeedTypeModel = itemData.data as Feed
        when (helper.itemViewType) {
            TIMELINE -> setTimeLineData(helper, timeLineFeedTypeModel)
            TIMELINE_GRID -> setTimeLineGrideData(helper, timeLineFeedTypeModel)
        }
    }

    fun setTimeLineData(helper: BaseViewHolder, data: Feed) {
        val loadingShareDialog by lazy {
            return@lazy ProgressDialog.progressDialog(mContext, mContext.getString(R.string.string_get_share_info))
        }
        val avatar: SimpleDraweeView = helper.getView(R.id.avatar)
        val userName: TextView = helper.getView(R.id.user_name)
        val time: TextView = helper.getView(R.id.time)
        val textContent: TextView = helper.getView(R.id.text_content)
        val productTv: TextView = helper.getView(R.id.product_tv)
        val productPrice: TextView = helper.getView(R.id.product_price)
        val lottieView = helper.getView<LottieAnimationView>(R.id.up_view)
        val praiseText: TextView = helper.getView(R.id.praise_text)
        val commentText: TextView = helper.getView(R.id.comment_text)
        val shareText: TextView = helper.getView(R.id.share_text)
        val newsCommentRL: RelativeLayout = helper.getView(R.id.item_timeline_profile_rl_comment)
        val newsCommentLikeTV: TextView = helper.getView(R.id.item_timeline_profile_rl_comment_like_count)
        val newsCommentHotPicIV: ImageView = helper.getView(R.id.item_timeline_profile_rl_comment_hot_pic)
        val newsCommentHotLikeIV: ImageView = helper.getView(R.id.item_timeline_profile_rl_comment_like_iv)
        val newsCommentHotLikeLL: LinearLayout = helper.getView(R.id.item_timeline_profile_rl_comment_like_ll)
        val newsCommentHotPicTextView: TextView = helper.getView(R.id.comment_image_tag)
        val newsCommentTv: TextView = helper.getView(R.id.news_comment_tv)

        val praiseLayout: LinearLayout = helper.getView(R.id.praise_layout)
        val commentLayout: LinearLayout = helper.getView(R.id.comment_layout)
        val shareLayout: LinearLayout = helper.getView(R.id.share_layout)
        val shareLayoutMore: LinearLayout = helper.getView(R.id.share_layout_more)
        val playerView = helper.getView<TimeLinePlayerView>(R.id.player_view)

        val width: Float
        val height: Float
        val contentExpectWidth: Float = (mContext.getScreenSize().x - mContext.dip(73f)).toFloat()

        if (data.width >= data.height) {
            width = contentExpectWidth
            height = contentExpectWidth / data.width * data.height
        } else {
            height = contentExpectWidth
            width = contentExpectWidth / data.height * data.width
        }

        if (data.has_audit == true) {
            praiseLayout.alpha = 0.5f
            commentLayout.alpha = 0.5f
            shareLayout.alpha = 0.5f
            shareLayoutMore.alpha = 0.5f
        } else {
            praiseLayout.alpha = 1f
            commentLayout.alpha = 1f
            shareLayout.alpha = 1f
            shareLayoutMore.alpha = 1f
        }

        playerView.layoutParams.width = width.toInt()
        playerView.layoutParams.height = height.toInt()

        val url = imageUrl2WebP2(data.cover!!, data.width, data.height)

        data.videoDetails?.filter { it.clarity == "ORIGINAL" }?.forEach {
            playerView.bind(uuid, data.id, it.url, url,
                    data.width, data.height, helper.adapterPosition, object : AdapterToPlayerListener {
                override fun showLabel() {
                }

                override fun hiddenLabel() {
                }
            })
        }
        playerView.listener = object : TimeLinePlayerView.OnPlayerViewClickListener {
            override fun onPlayerViewClickListener() {
                listener?.onAdapterPlayerViewClickListener(helper.adapterPosition)
            }
        }

        avatar.setImageUrl2Webp(data.avatar ?: "", mContext.dip(33), mContext.dip(33))

        avatar.noDoubleClick {
            if (mUserId != data.userId.toString()) {
                mContext.startActivity(Intent(mContext, UserProfileActivity::class.java)
                        .putExtra("id", data.userId.toString())
                        .putExtra("source", SpiderAction.PersonSource.TIMELINE.source))
            }
        }

        userName.text = data.userName
        if (data.createAt != null) {
            time.text = TimeUtil.getTimeFormatText(Date(data.createAt!!))
        }

        praiseText.text = numFormat(data.favNum)
        shareText.text = mContext.getString(R.string.string_share_label)
        if (data.commentNum != 0)
            commentText.text = numFormat(data.commentNum)
        else
            commentText.text = mContext.getString(R.string.string_comment_label)

        updateLikeButton(praiseText, data.fav, data.favNum, lottieView)
        praiseLayout.noDoubleClick {
            if (data.has_audit == true) {
                mContext.showLongToast(R.string.string_video_in_audit)
            } else {

                if (accountManager.hasAccount()) {
                    if (!data.fav) {
                        upMethod(data, praiseText) {}
                        setUpAnimation(lottieView)
                    } else {
                        cancelMethod(data, praiseText)
                        lottieView.setAnimation("timeline/timeline_normal.json")
                    }
                } else {
                    waitFeedData = data
                    waitLottieView = lottieView
                    waitRankUpNum = praiseText
                    mContext.startActivityForResultByLink(ACCOUNT_PAGE, Constants.REQUEST_LOGIN_CODE)
                }
            }
        }

        textContent.text = data.title
        textContent.movementMethod = LinkMovementMethod.getInstance()

        // 个人页加话题
        if (mChannelId == 0L) {
            val channel = SpannableString(" #" + data.channelName)
            channel.setSpan(object : ClickableSpan() {
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = textContent.context.getColor1(R.color.color_normal_b6b6b6)
                    ds.flags = 0
                }

                override fun onClick(view: View) {
                    if (mChannelId.toInt() != data.channelId) {
                        view.context.getSpider().manuallyEvent(SpiderEventNames.CHANNEL_LABEL_SELECTED).track()
                        TopicDetailActivity.launch(mContext, data.channelId.toString(), TopicDetailActivity.SOURCE_PERSONAL_PAGE)
                    }
                }
            }, 0, channel.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            textContent.append(channel)
        }

        if (data.hotComment != null) {// 热评

            // 是否评论了热评
            isPraise(data.hotComment!!.has_liked, newsCommentHotLikeIV)
            newsCommentLikeTV.text = "" + data.hotComment!!.like_count

            // 点赞
            newsCommentHotLikeLL.noDoubleClick {
                if (data.hotComment!!.has_liked) {
                    apiService.cancelPraise(data.hotComment!!.id.toString(), "COMMENT").applySchedulers().subscribeApi(onNext = {
                        if (it.result.isSuccess) {
                            data.hotComment!!.has_liked = false
                            data.hotComment!!.like_count = data.hotComment!!.like_count - 1
                            isPraise(data.hotComment!!.has_liked, newsCommentHotLikeIV)
                            newsCommentLikeTV.text = "" + data.hotComment!!.like_count
                            mContext.getSpider().commentLikeEvent(mContext, data.id, data.hotComment!!.id.toString(), "unlike")
                        }
                    })
                } else {
                    apiService.praise(data.hotComment!!.id.toString(), "COMMENT").applySchedulers().subscribeApi(onNext = {
                        if (it.result.isSuccess) {
                            data.hotComment!!.has_liked = true
                            data.hotComment!!.like_count = data.hotComment!!.like_count + 1
                            isPraise(data.hotComment!!.has_liked, newsCommentHotLikeIV)
                            newsCommentLikeTV.text = "" + data.hotComment!!.like_count
                            mContext.getSpider().commentLikeEvent(mContext, data.id, data.hotComment!!.id.toString(), "like")
                        }
                    })
                }
            }

            // 热评图片
            newsCommentRL.visibility = View.VISIBLE
            newsCommentTv.text = data.hotComment?.content ?: "" // 热评内容


            data.hotComment?.media?.let {
                val thumbUrl = data.hotComment!!.media!![0].thumbUrl
                var width = 0
                var height = 0
                val media = it.firstOrNull()
                when (media?.mediaType) {
                    TYPE_IMAGE -> {
                        newsCommentHotPicTextView.visibility = View.GONE
                        setImageExpectMeasure(newsCommentHotPicIV, media.thumbWidth, media.thumbHeight, mContext.dip(120))
                        width = media.thumbWidth
                        height = media.thumbHeight
                        setImageForGlide(mContext, imageUrl2WebP(thumbUrl, width, height), newsCommentHotPicIV, true)
                    }
                    TYPE_LONG -> {
                        newsCommentHotPicTextView.visibility = View.VISIBLE
                        newsCommentHotPicTextView.text = mContext.getString(R.string.string_long_picture)
                        val layoutParams = newsCommentHotPicIV.layoutParams as FrameLayout.LayoutParams
                        width = mContext.dip(79)
                        height = mContext.dip(140)
                        layoutParams.width = width
                        layoutParams.height = height
                        newsCommentHotPicIV.layoutParams = layoutParams
                        setImageForGlide(mContext, imageUrl2WebP(thumbUrl, width, height), newsCommentHotPicIV, true)
                    }
                    TYPE_GIF -> {
                        newsCommentHotPicTextView.visibility = View.VISIBLE
                        newsCommentHotPicTextView.text = "GIF"
                        setImageExpectMeasure(newsCommentHotPicIV, media.thumbWidth, media.thumbHeight, mContext.dip(120))
                        width = media.thumbWidth
                        height = media.thumbHeight
                        setImageForGlide(mContext, thumbUrl, newsCommentHotPicIV, false)
                    }
                }

                var originalUrl: String = ""
                newsCommentHotPicIV.noDoubleClick {
                    media?.mediaInfo?.forEach {
                        if (it.clarityType.equals("ORIGINAL")) {
                            originalUrl = it.url!!
                        }
                    }
                    CommentImageShowActivity.create(mContext, thumbUrl, media?.mediaType ?: "",
                            originalUrl, data.hotComment!!.target_id)
                }
            }

            if (data.hotComment == null || data.hotComment?.media == null) {
                newsCommentHotPicIV.visibility = View.GONE
            }


            val commentUser = SpannableString(data.hotComment?.author?.nick_name)
            commentUser.setSpan(object : ClickableSpan() {
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = newsCommentTv.context.getColor1(R.color.color_normal_b6b6b6)
                    ds.flags = 0
                }

                override fun onClick(view: View) {
                    val userId = data.hotComment?.author?.user_id.toString()
                    if (userId.isNotEmpty()) {
                        mContext.startActivity(Intent(mContext, UserProfileActivity::class.java)
                                .putExtra("id", userId)
                                .putExtra("source", SpiderAction.PersonSource.COMMENT.source))
                    }
                }
            }, 0, commentUser.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            newsCommentTv.append(commentUser)
            newsCommentTv.append("：")
            newsCommentTv.append(data.hotComment!!.content)
            newsCommentTv.movementMethod = LinkMovementMethod.getInstance()
        } else {
            newsCommentRL.visibility = View.GONE
        }

        commentLayout.noDoubleClick {
            if (data.has_audit == true) {
                mContext.showLongToast(R.string.string_video_in_audit)
            } else {
                VideoPlayerManager.instance().suspendVideoPlayer(uuid)
                val dialog = VideoCommentDialogFragment.create(data.id, data.commentNum, data.userId.toString(), helper.adapterPosition)
                dialog.setCountListener(object : VideoCommentDialogFragment.OnCommentCountListener {
                    override fun showCount(count: Int) {
                        data.commentNum = count
                        commentText.text = numFormat(data.commentNum)
                    }
                })
                dialog.show(fragmentManager, TAG)
            }

            if (mChannelId == 0L) { // 个人页面
                mContext.getSpider().viewCommentsEvent(mContext, data.id, SpiderAction.VideoPlaySource.PERSONAL_PAGE_TIMELINE.source)
            } else {
                mContext.getSpider().viewCommentsEvent(mContext, data.id, SpiderAction.VideoPlaySource.CHANNEL_TIMELINE.source)
            }
        }

        shareLayout.noDoubleClick {
            if (mChannelId == 0L) { // 个人页面
                mContext.getSpider().shareClickEvent(mContext, data.id, SpiderAction.VideoPlaySource.PERSONAL_PAGE_TIMELINE.source)
            } else {
                mContext.getSpider().shareClickEvent(mContext, data.id, SpiderAction.VideoPlaySource.CHANNEL_TIMELINE.source)
            }
            if (data.has_audit == true) {
                mContext.showLongToast(R.string.string_video_in_audit)
            } else {
                VideoPlayerManager.instance().suspendVideoPlayer(uuid)
                shareHelper.shareType = ShareConfig.SHARE_TYPE_VIDEO
                if (mContext.getUserId() == data.userId) {
                    shareHelper.doShare(mContext as Activity, null, data.id,
                            false, true, null,
                            videoUrl = data.videoDetails!![0].url,
                            videoCoverUrl = data.cover,
                            channelName = data.channelName,
                            title = data.title,
                            tag = DownloadManager.DOWNLOAD_TAG_FRAGMENT_PROFILETIMELINE,
                            allowDownload = data.properties!!.allow_download,
                            canEdit = data.properties!!.editInfo!!.canEdit,
                            message = data.properties!!.editInfo!!.message,
                            editableCount = data.properties!!.editInfo!!.editableCount)
                } else {
                    shareHelper.doShare(mContext as Activity, null, data.id,
                            false, false, null,
                            tag = DownloadManager.DOWNLOAD_TAG_FRAGMENT_PROFILETIMELINE,
                            allowDownload = data.properties!!.allow_download)
                }
            }
        }

        shareLayoutMore.noDoubleClick {
            var type = ""
            if (mChannelId == 0L) { // 个人页面
                type = SpiderAction.VideoPlaySource.PERSONAL_PAGE_TIMELINE.source
                mContext.getSpider().moreModeEvent(mContext, data.id, type)
            } else {
                type = SpiderAction.VideoPlaySource.CHANNEL_TIMELINE.source
                mContext.getSpider().moreModeEvent(mContext, data.id, type)
            }
            if (data.has_audit == true) {
                mContext.showLongToast(R.string.string_video_in_audit)
            } else {
                VideoPlayerManager.instance().suspendVideoPlayer(uuid)
                shareHelper.shareType = ShareConfig.SHARE_TYPE_VIDEO
                if (mContext.getUserId() == data.userId) {
                    shareHelper.doShare(mContext as Activity, null, data.id,
                            false, true, null,
                            videoUrl = data.videoDetails!![0].url,
                            videoCoverUrl = data.cover,
                            channelName = data.channelName,
                            title = data.title,
                            tag = DownloadManager.DOWNLOAD_TAG_FRAGMENT_PROFILETIMELINE,
                            allowDownload = data.properties!!.allow_download,
                            canEdit = data.properties!!.editInfo!!.canEdit,
                            message = data.properties!!.editInfo!!.message,
                            editableCount = data.properties!!.editInfo!!.editableCount,
                            isMoreClick = true, targetUserId = data.userId.toString(), isFollowed = data.hasFollowUser,
                            targetUserName = data.userName,
                            isFans = data.author?.is_fans,
                            type = type)
                } else {
                    shareHelper.doShare(mContext as Activity, null, data.id,
                            false, false, null,
                            tag = DownloadManager.DOWNLOAD_TAG_FRAGMENT_PROFILETIMELINE,
                            allowDownload = data.properties!!.allow_download,
                            isMoreClick = true, targetUserId = data.userId.toString(), isFollowed = data.hasFollowUser,
                            targetUserName = data.userName,
                            isFans = data.author?.is_fans,
                            type = type)
                }
            }

        }
    }

    private fun setTimeLineGrideData(helper: BaseViewHolder, data: Feed) {
        val imageView = helper.getView<SimpleDraweeView>(R.id.image_view)
        var itemHeight: Int = 0
        val width = mContext.getScreenSize().x / 3f - mContext.dip(10f)
        val height = width / 0.75
        itemHeight = height.toInt()
        helper.itemView.layoutParams.height = itemHeight
        imageView.aspectRatio = 3 / 4f
        if (!data.animationCover.isNullOrEmpty() && isWifiConnected(mContext)) {
            imageView.setGifImage(data.animationCover)
        } else {
            imageView.setImageURI(data.cover)
        }

        if (mChannelId == 0L) { // 个人页面
            helper.getView<TextView>(R.id.liked_number_view).text = numFormat4Profile(data.favNum)
        } else {
            helper.getView<TextView>(R.id.liked_number_view).text = numFormat(data.favNum)
        }

        helper.itemView.setOnClickListener {
            listener?.onAdapterPlayerViewClickListener(helper.adapterPosition)
        }
//        val chainIcon = helper.getView<ImageView>(R.id.chains_icon)
//        if (data.masterFeedId == data.id) {
//            chainIcon.visibility = View.GONE
//        } else {
//            chainIcon.visibility = View.VISIBLE
//        }
    }

    private fun updateLikeButton(favNumberView: TextView, like: Boolean,
                                 favNum: Int, upView: LottieAnimationView) {
        favNumberView.text = numFormat(favNum)
        if (like) {
            upView.setAnimation("timeline/timeline_up_finish.json")
        } else {
            upView.setAnimation("timeline/timeline_normal.json")
        }
    }

    private fun favMethod(feedData: Feed, lottieView: LottieAnimationView, rankUpNum: TextView, likeSuccess: () -> Unit) {
        if (accountManager.hasAccount()) {
            if (!feedData.fav) {
                upMethod(feedData, rankUpNum, likeSuccess)
                setUpAnimation(lottieView)
            }
        } else {
            waitFeedData = feedData
            waitLottieView = lottieView
            waitRankUpNum = rankUpNum
            mContext.startActivityForResultByLink(ACCOUNT_PAGE, Constants.REQUEST_LOGIN_CODE)
        }
    }

    private fun upMethod(item: Feed, rankUpNum: TextView, likeSuccess: () -> Unit) {
        apiService.likeFeed(item.id)
                .applySchedulers()
                .subscribeApi(onNext = {
                    likeSuccess.invoke()
                    if (it.result.isSuccess) {
                        item.fav = true
                        item.favNum = item.favNum + 1
                        rankUpNum.text = numFormat(item.favNum)
                        if (mChannelId == 0L) { // 个人页面
                            mContext.getSpider().likeEvent(mContext, item.id, "like",
                                    SpiderAction.VideoPlaySource.PERSONAL_PAGE_TIMELINE.source)
                        } else {
                            mContext.getSpider().likeEvent(mContext, item.id, "like",
                                    SpiderAction.VideoPlaySource.CHANNEL_TIMELINE.source)
                        }
                    } else {
                        item.fav = false
                        rankUpNum.text = numFormat(item.favNum)
                    }
                }, onApiError = {
                    likeSuccess.invoke()
                    item.fav = false
                    rankUpNum.text = numFormat(item.favNum)
                })
    }


    private fun cancelMethod(item: Feed, rankUpNum: TextView) {
        apiService.unLikeFeed(item.id)
                .applySchedulers()
                .subscribeApi(onNext = {
                    if (it.result.isSuccess) {
                        item.fav = false
                        item.favNum = item.favNum - 1
                        if (item.favNum <= 0) {
                            item.favNum = 0
                            rankUpNum.visibility = View.INVISIBLE
                        }
                        rankUpNum.text = numFormat(item.favNum)
                        if (mChannelId == 0L) { // 个人页面
                            mContext.getSpider().likeEvent(mContext, item.id, "unlike",
                                    SpiderAction.VideoPlaySource.PERSONAL_PAGE_TIMELINE.source)
                        } else {
                            mContext.getSpider().likeEvent(mContext, item.id, "unlike",
                                    SpiderAction.VideoPlaySource.CHANNEL_TIMELINE.source)
                        }
                    } else {
                        item.fav = true
                        rankUpNum.text = numFormat(item.favNum)
                    }
                }, onApiError = {
                    item.fav = true
                    rankUpNum.text = numFormat(item.favNum)
                })
    }

    private fun setUpAnimation(upView: LottieAnimationView?) {
        if (upView != null) {
            upView.cancelAnimation()
            upView.setAnimation("timeline/timeline_up.json")
            upView.repeatCount = 0
            upView.playAnimation()
            upView.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    upView.setAnimation("timeline/timeline_up_finish.json")
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                }

            })
        }
    }

    private fun isPraise(b: Boolean, praiseIv: ImageView) {
        praiseIv.setImageResource(if (b) R.drawable.ic_comment_liked else R.drawable.ic_comment_like)
    }

    var waitFeedData: Feed? = null
    var waitLottieView: LottieAnimationView? = null
    var waitRankUpNum: TextView? = null

    fun onLoginUpStatus() {
        if (accountManager.hasAccount()) {
            waitFeedData ?: return
            waitLottieView ?: return
            waitRankUpNum ?: return
            favMethod(waitFeedData!!, waitLottieView!!, waitRankUpNum!!) {
                waitFeedData = null
                waitLottieView = null
                waitRankUpNum = null
            }

        }
    }

}

/**
 * 计算动态页面中评论图片的大小，与 iOS 端一致，CommentListAdapter 评论中也用
 */
fun setImageExpectMeasure(imageView: ImageView, thumbWidth: Int, thumbHeight: Int, maxHeight: Int) {
    //视频或图片最大显示的高度
    val contentExpectMaxHeight = maxHeight.toFloat()
    val contentExpectMaxWidth = contentExpectMaxHeight * 16 / 9.toFloat()

    var width: Float = 0f
    var height: Float = 0f
    var thumbWidth = thumbWidth.toFloat()
    var thumbHeight = thumbHeight.toFloat()
    if (thumbWidth / thumbHeight > 16 / 9) {
        width = contentExpectMaxWidth
        height = width * 9 / 16
    } else if (thumbWidth / thumbHeight < 9 / 16) {
        height = contentExpectMaxHeight
        width = height * 9 / 16
    } else {
        height = contentExpectMaxHeight
        width = contentExpectMaxHeight / thumbHeight * thumbWidth
    }

    val params = imageView.layoutParams
    params.width = width.toInt()
    params.height = height.toInt()
    imageView.layoutParams = params
}



