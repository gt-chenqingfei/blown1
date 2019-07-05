package com.shuashuakan.android.modules.timeline.multitype

import android.support.v4.app.FragmentManager
import android.support.v7.widget.RecyclerView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.shuashuakan.android.R
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.data.api.model.home.multitypetimeline.*
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.modules.partition.helper.PartitionLoginActionHelper
import com.shuashuakan.android.modules.share.ShareHelper
import com.shuashuakan.android.modules.timeline.vm.MutitypeTimeLineViewModel
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.utils.createUUID
import com.shuashuakan.android.utils.followTimeLineEvent
import com.shuashuakan.android.utils.getSpider
import io.reactivex.disposables.CompositeDisposable

/**
 * 多类型 TimeLine Adapter
 *
 * Author: ZhaiDongyang
 * Date: 2019/2/25
 */
class MultiTypeTimeLineAdapter constructor(
        private val fragmentManager: FragmentManager,
        private val apiService: ApiService,
        private val shareHelper: ShareHelper,
        dataList: List<MultiItemEntity>?,
        var mutitypeTimeLineViewModel: MutitypeTimeLineViewModel) : BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(dataList) {

    private val compositeDisposable = CompositeDisposable()

    companion object {
        const val TAG = "MultiTypeTimeLineAdapter"
        const val MULTITYPE_TIMELINE_VIDEO = 0 // 关注人发的视频
        const val MULTITYPE_TIMELINE_VIDEO_TOPIC = 1 // 推荐话题视频
        const val MULTITYPE_TIMELINE_SUBSCRIBED_TOPIC = 2 // 订阅的话题
        const val MULTITYPE_TIMELINE_RECOMMEND_TOPIC = 3 // 推荐的话题
        const val MULTITYPE_TIMELINE_INTEREST = 4 // 感兴趣的人
        const val MULTITYPE_TIMELINE_FOLLOW_USER = 5 // 关注的人
        const val MULTITYPE_TIMELINE_RECOMMEN_USER_FEED = 6 // 推荐的up主
        const val MULTITYPE_TIMELINE_USER_LEADRER_BOARD = 7 // 人气榜的人
    }

    val uuid = createUUID()
    var timelineModel = MultiTypeTimeLineFragment.DATA_TYPE_NOT_LOGIN
    var listener: OnAdapterPlayerViewClickListener? = null

    interface OnAdapterPlayerViewClickListener {
        fun onAdapterPlayerViewClickListener(position: Int)
    }

    init {
        addItemType(MULTITYPE_TIMELINE_VIDEO, R.layout.multitype_timeline_video_item)
        addItemType(MULTITYPE_TIMELINE_VIDEO_TOPIC, R.layout.multitype_timeline_video_item)
        addItemType(MULTITYPE_TIMELINE_SUBSCRIBED_TOPIC, R.layout.fragment_multitype_timeline_subscribed_topic)
        addItemType(MULTITYPE_TIMELINE_RECOMMEND_TOPIC, R.layout.fragment_multitype_timeline_recommend_topic)
        addItemType(MULTITYPE_TIMELINE_INTEREST, R.layout.fragment_multitype_timeline_interest)
        addItemType(MULTITYPE_TIMELINE_FOLLOW_USER, R.layout.fragment_multitype_timeline_follow_user)
        addItemType(MULTITYPE_TIMELINE_RECOMMEN_USER_FEED, R.layout.item_timeline_recommend_user_feed)
        addItemType(MULTITYPE_TIMELINE_USER_LEADRER_BOARD, R.layout.item_muliti_chain_user_timeline)
    }


    override fun convert(helper: BaseViewHolder, item: MultiItemEntity) {
        val itemData = item as com.shuashuakan.android.modules.discovery.ItemDataPair
        val dataAny = itemData.data
        when (helper.itemViewType) {
            MULTITYPE_TIMELINE_VIDEO -> {
                MultiTypeTimeLineVideoViewHolder(mutitypeTimeLineViewModel, mContext, helper, dataAny as Feed,
                        fragmentManager, apiService, shareHelper, uuid, listener!!, MULTITYPE_TIMELINE_VIDEO, followModel = timelineModel)
                mContext.getSpider().followTimeLineEvent(mContext, SpiderEventNames.FOLLOW_TIMELINE_FEED_EXPOSURE,
                        feedID = dataAny.id, type = "fromUser")
            }
            MULTITYPE_TIMELINE_VIDEO_TOPIC -> {
                MultiTypeTimeLineVideoViewHolder(mutitypeTimeLineViewModel, mContext, helper, dataAny as Feed,
                        fragmentManager, apiService, shareHelper, uuid, listener!!, MULTITYPE_TIMELINE_VIDEO_TOPIC, followModel = timelineModel)
                mContext.getSpider().followTimeLineEvent(mContext, SpiderEventNames.FOLLOW_TIMELINE_FEED_EXPOSURE,
                        feedID = dataAny.id, type = "fromChannel")
            }
            MULTITYPE_TIMELINE_SUBSCRIBED_TOPIC -> {
                MultiTypeTimeLineSubscribedTopicViewHolder(
                        mContext, helper, dataAny as SubscribedChannelContent)
            }
            MULTITYPE_TIMELINE_RECOMMEND_TOPIC -> {
                MultiTypeTimeLineRecommendTopicViewHolder(mContext, helper, dataAny as RecommendChannelContent)
                mContext.getSpider().followTimeLineEvent(mContext,
                        SpiderEventNames.RECOMMENDED_CHANNEL_EXPOSURE, source = "FollowTimeline")
            }
            MULTITYPE_TIMELINE_INTEREST -> {
                MultiTypeTimeLineInterestViewHolder(mContext, helper, dataAny as RecommendUserContent, recyclerView, data) { item ->
                    mutitypeTimeLineViewModel.targetUserId = item.user_id.toString()
                    mutitypeTimeLineViewModel.isFollow = item.is_follow
                    mutitypeTimeLineViewModel.mRecommendUserPosition = helper.layoutPosition
                }
                mContext.getSpider().followTimeLineEvent(mContext,
                        SpiderEventNames.RECOMMENDED_USER_EXPOSURE, source = "FollowTimeline")
            }
            MULTITYPE_TIMELINE_FOLLOW_USER -> {
                MultiTypeTimeLineFollowUserViewHolder(mContext, helper, dataAny as FollowUserContent)
            }

            MULTITYPE_TIMELINE_RECOMMEN_USER_FEED -> {
                MultiTypeTimeLineRecommendUserFeedViewHolder(mutitypeTimeLineViewModel,
                        mContext, helper, dataAny as RecommendUserFeedContent)
            }
            MULTITYPE_TIMELINE_USER_LEADRER_BOARD -> {
                MuliTypeChainUserLeaderBoardViewHolder(apiService, compositeDisposable,
                        mContext, helper, dataAny as UserLeaderBoard)
            }
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        compositeDisposable.clear()
    }

    fun loginAction() {
        PartitionLoginActionHelper.loginActionToFollow(mContext, compositeDisposable, apiService)
    }

}