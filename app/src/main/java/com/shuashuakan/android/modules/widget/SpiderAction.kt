package com.shuashuakan.android.modules.widget

/**
 * Author:  lijie
 * Date:   2018/12/21
 * Email:  2607401801@qq.com
 */
class SpiderAction {
    //个人页来源
    enum class PersonSource(val source: String) {
        FAN_LIST("FanList"),
        MESSAGE_CENTER("MessageCenter"),
        FEED_PLAY("FeedPlay"),
        FOLLOW_LIST("FollowList"),
        COMMENT("Comment"),
        TIMELINE("Timeline"), // 个人动态页面
        FOLLOW_TIMELINE("FollowTimeline"),// 关注页面
        RECOMMEND_LIST("RecommendList"),
        RANKING_LIST("RankingList"),
        VIP_HOME_PAGE("VipHomePage"),
        FOLLOW_TIMELINE_MY_FOLLOW("FollowTimelineMyFollow"),
        Category("Category")
    }

    /**
     * 视频页 UP、Comment、Share 来源、话题订阅来源
     */
    enum class VideoPlaySource(val source: String) {
        FOLLOW_TIMELINE("FollowTimeline"),
        CHANNEL_TIMELINE("ChannelTimeline"),
        PERSONAL_PAGE_TIMELINE("PersonalPageTimeline"),
        CHANNEL_SUBSCRIPTION_PAGE("ChannelSubscriptionPage"),
        PERSONA_PAGE("PersonaPage"),
        FEED_PLAY("FeedPlay"),
        CHANNEL_PAGE("ChannelPage"),
        FAN_LIST("FanList"), // 粉丝列表关注
        EXPLORE_PAGE("ExplorePage"),
        VIP_HOME_PAGE("VipHomePage"),
        CATEGORY_FEED_LEADER_BOARD("CategoryFeedLeaderboard"),
        CATEGORY_USER_LEADER_BOARD("CategoryUserLeaderboard")
    }


    /**
     * 全部话题页面的来源
     */
    enum class TopicCategorySource(val source: String) {
        Category("Category"),
        PersonelPage("PersonelPage"),
        FollowTimeline("FollowTimeline"),
        PostFeed("PostFeed"),
    }

}