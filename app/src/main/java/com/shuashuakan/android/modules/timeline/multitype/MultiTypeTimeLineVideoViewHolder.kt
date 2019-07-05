package com.shuashuakan.android.modules.timeline.multitype

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.support.v4.app.FragmentManager
import android.support.v7.widget.RecyclerView
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.luck.picture.lib.tools.ScreenUtils
import com.shuashuakan.android.R
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.account.activity.LoginActivity
import com.shuashuakan.android.modules.comment.VideoCommentDialogFragment
import com.shuashuakan.android.modules.profile.UserProfileActivity
import com.shuashuakan.android.modules.share.ShareConfig
import com.shuashuakan.android.modules.share.ShareHelper
import com.shuashuakan.android.modules.timeline.AdapterToPlayerListener
import com.shuashuakan.android.modules.timeline.vm.MutitypeTimeLineViewModel
import com.shuashuakan.android.modules.topic.TopicDetailActivity
import com.shuashuakan.android.modules.widget.MultiTypeTimeLinePlayerView
import com.shuashuakan.android.modules.widget.SpiderAction
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.utils.*
import com.shuashuakan.android.utils.download.DownloadManager
import java.util.*

@Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER", "DEPRECATION")
/**
 * TimeLine 视频 Type（话题视频和关注视频）
 *
 * Author: ZhaiDongyang
 * Date: 2019/2/26
 */
class MultiTypeTimeLineVideoViewHolder(
        private val mutitypeTimeLineViewModel: MutitypeTimeLineViewModel,
        private val mContext: Context,
        private val helper: BaseViewHolder,
        _data: Parcelable,
        private val fragmentManager: FragmentManager,
        private val apiService: ApiService,
        private val shareHelper: ShareHelper,
        private val uuid: String,
        private val listener: MultiTypeTimeLineAdapter.OnAdapterPlayerViewClickListener,
        type: Int,
        val followModel: String) {

    var accountManager: AccountManager? = null

    init {
        accountManager = mContext.applicationContext.daggerComponent().accountManager()
        val data = _data as Feed
        val avatar: SimpleDraweeView = helper.getView(R.id.avatar)
        val userName: TextView = helper.getView(R.id.user_name)
        val time: TextView = helper.getView(R.id.time)
        val followText: com.shuashuakan.android.modules.widget.FollowTextView = helper.getView(R.id.topic_channel_subscribe_tv)
        val textContent: TextView = helper.getView(R.id.text_content)
        val lottieView = helper.getView<LottieAnimationView>(R.id.up_view)
        val praiseText: TextView = helper.getView(R.id.praise_text)
        val commentText: TextView = helper.getView(R.id.comment_text)
        val avatarIv: ImageView = helper.getView(R.id.avatar_iv)
        val shareText: TextView = helper.getView(R.id.share_text)
        val praiseLayout: LinearLayout = helper.getView(R.id.praise_layout)
        val commentLayout: LinearLayout = helper.getView(R.id.comment_layout)
        val shareLayout: LinearLayout = helper.getView(R.id.share_layout)
        val shareLayoutMore: LinearLayout = helper.getView(R.id.share_layout_more)
        val playerView =
                helper.getView<MultiTypeTimeLinePlayerView>(R.id.player_view)
        val simpleDraweeView = helper.getView<SimpleDraweeView>(R.id.blur_view)


        avatar.setImageUrl2Webp(data.avatar ?: "", mContext.dip(36), mContext.dip(36))

        if (followModel != MultiTypeTimeLineFragment.DATA_TYPE_FOLLOW) {
            if (data.hasFollowUser!!) {
                followText.visibility = View.INVISIBLE
            } else {
                followText.visibility = View.VISIBLE
                followText.setBackgroundResource(R.drawable.bg_multitype_timeline_un_follow)
                followText.text = mContext.resources.getString(R.string.string_follow)
                followText.setTextColor(mContext.resources.getColor(R.color.white))
                followText.tag = data
            }
            shareLayoutMore.visibility = View.INVISIBLE
        } else {
            followText.visibility = View.INVISIBLE
            shareLayoutMore.visibility = View.VISIBLE
        }
        helper.addOnClickListener(R.id.topic_channel_subscribe_tv)

        if (type == MultiTypeTimeLineAdapter.MULTITYPE_TIMELINE_VIDEO_TOPIC) {
            userName.text = data.channelName
            avatarIv.visibility = View.VISIBLE
            avatarIv.setBackgroundResource(R.drawable.ic_timeline_topic_avawar)
            avatar.noDoubleClick {
                skipTopicPage(data)
            }
            userName.noDoubleClick {
                skipTopicPage(data)
            }
            followText.visibility = View.GONE
            time.visibility = View.VISIBLE
            shareLayoutMore.visibility = View.GONE
        } else {
            userName.text = data.userName
            avatarIv.visibility = View.GONE
            avatar.noDoubleClick {
                skipProfilePage(data)
            }
            userName.noDoubleClick {
                skipProfilePage(data)
            }
        }


        updateLikeButton(praiseText, data.fav, data.favNum, lottieView)
        val coverUrl = setImageExpectMeasure(playerView, simpleDraweeView,
                data.cover!!, data.width, data.height, ScreenUtils.getScreenWidth(mContext))

        data.videoDetails?.filter { it.clarity == "ORIGINAL" }?.forEach {
            playerView.bind(uuid, data.id, it.url, coverUrl, data.width, data.height,
                    helper.adapterPosition - getHeadCount(), object : AdapterToPlayerListener {
                override fun showLabel() {
                }

                override fun hiddenLabel() {
                }
            })
        }

        playerView.listener = object : MultiTypeTimeLinePlayerView.OnPlayerViewClickListener {
            override fun onPlayerViewClickListener() {
                listener.onAdapterPlayerViewClickListener(helper.adapterPosition - getHeadCount())
            }
        }

        if (data.createAt != null) {
            time.text = TimeUtil.getTimeFormatText(Date(data.createAt!!))
        }

        if (data.favNum == 0) {
            praiseText.text = ""
        } else {
            praiseText.text = numFormat(data.favNum)
        }
        shareText.text = mContext.getString(R.string.string_share_label)
        if (data.commentNum != 0)
            commentText.text = numFormat(data.commentNum)
        else
            commentText.text = mContext.getString(R.string.string_comment_label)

        praiseLayout.noDoubleClick {
            if (accountManager?.hasAccount()!!) {
                favMethod(data, lottieView, praiseText) {}
            } else {
                waitFeedData = data
                waitLottieView = lottieView
                waitRankUpNum = praiseText
                mutitypeTimeLineViewModel.multiTypeTimeLineVideoViewHolder = this
                LoginActivity.launch(mContext)
            }
        }

        textContent.text = data.title
        textContent.movementMethod = LinkMovementMethod.getInstance()

        commentLayout.noDoubleClick {
            //      VideoPlayerManager.instance().suspendVideoPlayer(uuid)
            val dialog = VideoCommentDialogFragment.create(
                    data.id, data.commentNum, data.userId.toString(), helper.adapterPosition - getHeadCount())
            dialog.setCountListener(object : VideoCommentDialogFragment.OnCommentCountListener {
                override fun showCount(count: Int) {
                    data.commentNum = count
                    commentText.text = numFormat(data.commentNum)
                }
            })
            dialog.show(fragmentManager, MultiTypeTimeLineAdapter.TAG)

            mContext.getSpider().viewCommentsEvent(mContext, data.id,
                    SpiderAction.VideoPlaySource.FOLLOW_TIMELINE.source)
        }

        shareLayout.noDoubleClick {
            mContext.getSpider().shareClickEvent(mContext, data.id,
                    SpiderAction.VideoPlaySource.FOLLOW_TIMELINE.source)
            shareHelper.shareType = ShareConfig.SHARE_TYPE_VIDEO
            if (mContext.getUserId() == data.userId) {

                shareHelper.doShare(mContext as Activity, null, data.id,
                        false, true, null,
                        videoUrl = data.videoDetails?.let { it[0].url }
                                ?: "", // data.videoDetails!![0].url,
                        videoCoverUrl = data.cover,
                        channelName = data.channelName,
                        title = data.title,
                        tag = DownloadManager.DOWNLOAD_TAG_FRAGMENT_MULTITYPE,
                        allowDownload = data.properties?.allow_download ?: false,
                        canEdit = data.properties?.editInfo?.canEdit ?: false,
                        message = data.properties?.editInfo?.message ?: "",
                        editableCount = data.properties?.editInfo?.editableCount
                                ?: 0)
            } else {
                shareHelper.doShare(mContext as Activity, null, data.id,
                        false, false, null,
                        tag = DownloadManager.DOWNLOAD_TAG_FRAGMENT_MULTITYPE,
                        allowDownload = data.properties?.allow_download ?: false)
            }
        }

        shareLayoutMore.noDoubleClick {
            mContext.getSpider().moreModeEvent(mContext, data.id,
                    SpiderAction.VideoPlaySource.FOLLOW_TIMELINE.source)
            shareHelper.shareType = ShareConfig.SHARE_TYPE_VIDEO
            if (mContext.getUserId() == data.userId) {
                shareHelper.doShare(mContext as Activity, null, data.id,
                        false, true, null,
                        videoUrl = data.videoDetails?.let { detail -> detail[0].url }
                                ?: "", // data.videoDetails!![0].url,
                        videoCoverUrl = data.cover,
                        channelName = data.channelName,
                        title = data.title,
                        tag = DownloadManager.DOWNLOAD_TAG_FRAGMENT_MULTITYPE,
                        allowDownload = data.properties?.allow_download ?: false,
                        canEdit = data.properties?.editInfo?.canEdit ?: false,
                        message = data.properties?.editInfo?.message ?: "",
                        editableCount = data.properties?.editInfo?.editableCount
                                ?: 0,
                        isMoreClick = true,
                        targetUserId = data.userId.toString(),
                        isFollowed = data.hasFollowUser,
                        targetUserName = data.userName,
                        type = SpiderAction.VideoPlaySource.FOLLOW_TIMELINE.source,
                        isFans = data?.author?.is_fans)
            } else {
                shareHelper.doShare(mContext as Activity, null, data.id,
                        false, false, null,
                        tag = DownloadManager.DOWNLOAD_TAG_FRAGMENT_MULTITYPE,
                        allowDownload = data.properties?.allow_download ?: false,
                        isMoreClick = true,
                        targetUserId = data.userId.toString(),
                        targetUserName = data.userName,
                        isFollowed = data.hasFollowUser,
                        type = SpiderAction.VideoPlaySource.FOLLOW_TIMELINE.source,
                        isFans = data?.author?.is_fans)
            }

        }

    }

    private fun skipTopicPage(data: Feed) {
        mContext.getSpider().followTimeLineEvent(mContext,
                SpiderEventNames.FOLLOW_TIMELINE_CHANNEL_NAME_CLICK,
                feedID = data.id,
                channelID = data.channelId.toString())
        TopicDetailActivity.launch(mContext, data.channelId.toString(), TopicDetailActivity.SOURCE_FOLLOW_TIMELINE)
    }

    private fun skipProfilePage(data: Feed) {
        if (mContext.getUserId() != data.userId.toString()) {
            mContext.startActivity(Intent(mContext, UserProfileActivity::class.java)
                    .putExtra("id", data.userId.toString())
                    .putExtra("source", SpiderAction.VideoPlaySource.FOLLOW_TIMELINE.source))
        }
    }

    private fun favMethod(feedData: Feed, lottieView: LottieAnimationView, rankUpNum: TextView, likeSuccess: () -> Unit) {
        if (!feedData.fav) {
            upMethod(feedData, rankUpNum, likeSuccess)
            setUpAnimation(lottieView)
        } else {
            cancelMethod(feedData, rankUpNum)
            lottieView.setAnimation("timeline/timeline_normal.json")
        }
    }

    private fun upMethod(item: Feed, rankUpNum: TextView, likeSuccess: () -> Unit) {
        apiService.likeFeed(item.id)
                .applySchedulers()
                .subscribeApi(onNext = {
                    likeSuccess.invoke()
                    if (it.result.isSuccess) {
                        rankUpNum.visibility = View.VISIBLE
                        item.fav = true
                        item.favNum = item.favNum + 1
                        rankUpNum.text = numFormat(item.favNum)
                        mContext.getSpider().likeEvent(mContext, item.id, "like",
                                SpiderAction.VideoPlaySource.FOLLOW_TIMELINE.source)
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
                        mContext.getSpider().likeEvent(mContext, item.id, "unlike",
                                SpiderAction.VideoPlaySource.FOLLOW_TIMELINE.source)
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

    private fun updateLikeButton(favNumberView: TextView, like: Boolean,
                                 favNum: Int, upView: LottieAnimationView) {
        favNumberView.text = numFormat(favNum)
        if (like) {
            upView.setAnimation("timeline/timeline_up_finish.json")
        } else {
            upView.setAnimation("timeline/timeline_normal.json")
        }
    }

    /**
     * 计算动态页面中视频的宽高
     */
    private fun setImageExpectMeasure(
            playerView: MultiTypeTimeLinePlayerView, simpleDraweeView: SimpleDraweeView,
            coverUrl: String, videoWidth: Int, videoHeight: Int, maxLengthOfSide: Int): String {
        val width: Float
        val height: Float
        var simpleDraweeViewWidth = 0
        var simpleDraweeViewHeight = 0
        val thumbWidth = videoWidth.toFloat()
        val thumbHeight = videoHeight.toFloat()
        when {
            thumbWidth / thumbHeight > 16 / 9 -> {
                width = maxLengthOfSide.toFloat()
                height = width * 9 / 16
            }
            thumbWidth / thumbHeight < 9 / 16 -> {
                height = maxLengthOfSide.toFloat()
                width = height * 9 / 16
                simpleDraweeViewWidth = maxLengthOfSide
                simpleDraweeViewHeight = height.toInt()
            }
            Math.abs((thumbWidth / thumbHeight) - (16 / 9)) < 0.00001 -> {
                width = maxLengthOfSide.toFloat()
                height = maxLengthOfSide.toFloat() / thumbWidth * thumbHeight
            }
            else -> {
                height = maxLengthOfSide.toFloat()
                width = maxLengthOfSide.toFloat() / thumbHeight * thumbWidth
                simpleDraweeViewWidth = maxLengthOfSide
                simpleDraweeViewHeight = maxLengthOfSide
            }
        }
        var coverImageViewWidth = 0
        var coverImageViewHeight = 0
        if (simpleDraweeViewWidth != 0 && simpleDraweeViewHeight != 0) {
            coverImageViewWidth = simpleDraweeViewWidth
            coverImageViewHeight = simpleDraweeViewHeight
        } else {
            coverImageViewWidth = width.toInt()
            coverImageViewHeight = height.toInt()
        }
        val url = imageUrl2WebP2(coverUrl, coverImageViewWidth, coverImageViewHeight)

        val params = playerView.layoutParams
        params.width = width.toInt()
        params.height = height.toInt()
        playerView.layoutParams = params

        if (simpleDraweeViewWidth != 0 && simpleDraweeViewHeight != 0) {
            simpleDraweeView.visibility = View.VISIBLE
            val paramSimpleDraweeView = simpleDraweeView.layoutParams
            paramSimpleDraweeView.width = simpleDraweeViewWidth
            paramSimpleDraweeView.height = simpleDraweeViewHeight
            simpleDraweeView.layoutParams = paramSimpleDraweeView
            showUrlBlur(simpleDraweeView, url, 20, 6)
        } else {
            simpleDraweeView.visibility = View.GONE
        }
        return url
    }

    private fun getHeadCount(): Int {
        return ((helper.itemView?.parent as? RecyclerView)?.adapter as? BaseQuickAdapter<*, *>)?.headerLayoutCount
                ?: 0
    }


    var waitFeedData: Feed? = null
    var waitLottieView: LottieAnimationView? = null
    var waitRankUpNum: TextView? = null

    fun onLoginUpStatus(loginSuccessEvent: () -> Unit) {
        if (accountManager?.hasAccount()!!) {
            waitFeedData ?: return
            waitLottieView ?: return
            waitRankUpNum ?: return
            favMethod(waitFeedData!!, waitLottieView!!, waitRankUpNum!!) {
                loginSuccessEvent.invoke()
                waitFeedData = null
                waitLottieView = null
                waitRankUpNum = null
            }

        }
    }


}