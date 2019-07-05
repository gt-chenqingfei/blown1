package com.shuashuakan.android.modules

import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.data.api.model.home.VipHomeMessage
import com.shuashuakan.android.enums.ChainFeedSource
import com.shuashuakan.android.modules.publisher.chains.ChainsListIntentParam

object FeedTransportManager {
    const val MARK_FROM_UNDEFINE = 0
    const val MARK_FROM_FOLLOW = 1
    const val MARK_FROM_TOPIC_DETAIL_RECOMMEND = 2
    const val MARK_FROM_TOPIC_DETAIL_TIMELINE = 3
    const val MARK_FROM_PERSONAL_NEWEST = 4
    const val MARK_FROM_EXPLORE_EXCELLENT_CHAINS = 5
    const val MARK_FROM_PERSONAL_UP = 6
    const val MARK_FROM_VIP_HOME = 7
    const val MARK_FROM_RECOMMEND_USER_FEED = 8
    const val MARK_FROM_CATEGORY_RECOMMEND_TOPIC = 9
    const val MARK_FROM_CATEGORY_LEADERBOARD = 10

    var intentParam: ChainsListIntentParam? = null
    var message: VipHomeMessage? = null


    /**
     * 从关注页面跳视频播放页面传递的参数
     */
    fun jumpToVideoFromFollow(currentFeed: Feed, floorFeedList: List<Feed>, nextId: String?): ChainsListIntentParam {
        return ChainsListIntentParam(
                position = null,
                feedList = floorFeedList,
                nextId = nextId,
                feedSource = ChainFeedSource.FOLLOW_TIMELINE,
                currentFloorFeed = currentFeed,
                fromMark = MARK_FROM_FOLLOW)
    }

    /**
     * 话题详情页-动态tab跳视频播放页面传递的参数
     */
    fun jumpToVideoFromTopicDetailRecommend(channelId: Long?, nextId: String?, floorFeedList: List<Feed>, chainFeedList: List<Feed>, currentFeed: Feed?, chainPosition: Int): ChainsListIntentParam {
        return ChainsListIntentParam(
                nextId = nextId,
                position = null,
                childEnterPosition = chainPosition,
                feedList = floorFeedList,
                childFeedList = chainFeedList,
                channelId = channelId,
                currentFloorFeed = currentFeed,
                feedSource = ChainFeedSource.CHANNEL_RECOMMEND,
                fromMark = MARK_FROM_TOPIC_DETAIL_RECOMMEND)
    }

    /**
     * 话题详情页-推荐tab跳视频播放页面传递的参数
     */
    fun jumpToVideoFromTopicDetailTimeline(channelId: Long?, nextId: String?, floorFeedList: List<Feed>, currentFeed: Feed): ChainsListIntentParam {
        return ChainsListIntentParam(
                nextId = nextId,
                position = null,
                currentFloorFeed = currentFeed,
                feedList = floorFeedList,
                channelId = channelId,
                feedSource = ChainFeedSource.CHANNEL_TIMELINE,
                fromMark = MARK_FROM_TOPIC_DETAIL_TIMELINE)
    }

    /**
     * 个人页动态tab-最新 跳视频播放页面传递的参数
     */
    fun jumpToVideoFromPersonalNewest(page: Int, floorFeedList: List<Feed>, currentFeed: Feed): ChainsListIntentParam {
        return ChainsListIntentParam(
                page = page,
                currentFloorFeed = currentFeed,
                position = null,
                feedList = floorFeedList,
                feedSource = ChainFeedSource.PERSONAL_PAGE,
                fromMark = MARK_FROM_PERSONAL_NEWEST)
    }

    /**
     * 发现页-优秀视频集 跳视频播放页面传递的参数
     */
    fun jumpToVideoFromExploreExcellentChains(floorFeedList: List<Feed>, currentFeed: Feed, channelId: Long?): ChainsListIntentParam {
        return ChainsListIntentParam(
                currentFloorFeed = currentFeed,
                position = null,
                channelId = channelId,
                feedList = floorFeedList,
                feedSource = ChainFeedSource.EXPLORE_RANKING_LIST,
                fromMark = MARK_FROM_EXPLORE_EXCELLENT_CHAINS)
    }

    /**
     * 个人页动态tab-Up的视频 跳视频播放页面传递的参数
     */
    fun jumpToVideoFromPersonalUp(page: Int, currentFeed: Feed, floorFeedList: List<Feed>): ChainsListIntentParam {
        return ChainsListIntentParam(
                currentFloorFeed = currentFeed,
                position = null,
                page = page,
                feedList = floorFeedList,
                feedSource = ChainFeedSource.LIKE,
                fromMark = MARK_FROM_PERSONAL_UP)
    }

    /**
     * 新人vip房间跳转（即非主视频）
     */
    fun createVipRoomOpenParam(feedList: List<Feed>): ChainsListIntentParam {
        val id = feedList[0].masterFeedId
        return ChainsListIntentParam(ChainFeedSource.VIP_HOME, id, 0, feedList, 0, null, fromMark = MARK_FROM_VIP_HOME)
    }


    /**
     * 关注页面推荐up主 跳视频播放页面传递的参数
     */
    fun createRecommendUserFeedParam(floorFeedList: List<Feed>, currentFeed: Feed, channelId: Long?): ChainsListIntentParam {
        return ChainsListIntentParam(
                currentFloorFeed = currentFeed,
                position = null,
                channelId = channelId,
                feedList = floorFeedList,
                feedSource = ChainFeedSource.FOLLOW_TIMELINE,
                fromMark = MARK_FROM_RECOMMEND_USER_FEED)
    }

    /**
     * 分区界面的推荐话题的视频跳转
     */
    fun createCategoryRecommendTopicParam(floorFeedList: List<Feed>, currentFeed: Feed, channelId: Long?): ChainsListIntentParam {
        return ChainsListIntentParam(
                currentFloorFeed = currentFeed,
                position = null,
                channelId = channelId,
                feedList = floorFeedList,
                feedSource = ChainFeedSource.CATEGORY,
                fromMark = MARK_FROM_CATEGORY_RECOMMEND_TOPIC)
    }

    /**
     * 分区界面的热门的视频跳转
     */
    fun createCategoryLeaderBoardParam(floorFeedList: List<Feed>, currentFeed: Feed, channelId: Long?): ChainsListIntentParam {
        return ChainsListIntentParam(
                currentFloorFeed = currentFeed,
                position = null,
                channelId = channelId,
                feedList = floorFeedList,
                feedSource = ChainFeedSource.CATEGORY,
                fromMark = MARK_FROM_CATEGORY_LEADERBOARD)
    }

}