package com.shuashuakan.android.data.api.model.home.multitypetimeline

import com.shuashuakan.android.data.api.runtimeAdapterFactory
import com.squareup.moshi.JsonAdapter

interface CardsType {
    companion object {
        private const val CARDS_TYPE_SUBSCRIBED_CHANNEL = "SUBSCRIBED_CHANNEL" // 订阅的话题
        private const val CARDS_TYPE_CHANNEL_RECOMMEND_FEED = "CHANNEL_RECOMMEND_FEED" // 推荐话题视频
        private const val CARDS_TYPE_RECOMMEND_USER = "RECOMMEND_USER"// 推荐的话题
        private const val CARDS_TYPE_RECOMMEND_CHANNEL = "RECOMMEND_CHANNEL"// 感兴趣的人
        private const val CARDS_TYPE_FOLLOW_USER = "FOLLOW_USER"// 关注的人
        private const val CARDS_TYPE_USER_LEADERBOARD = "USER_LEADERBOARD"// up人气榜
        private const val CARDS_TYPE_RECOMMEND_USER_AND_FEED = "RECOMMEND_USER_AND_FEED"// 推荐up主

        fun create(): JsonAdapter.Factory {
            return runtimeAdapterFactory("type", CardsType::class) {
                mapOf(
                        CARDS_TYPE_SUBSCRIBED_CHANNEL to SubscribedChannelContent::class,
                        CARDS_TYPE_CHANNEL_RECOMMEND_FEED to TimeLineContent::class,
                        CARDS_TYPE_RECOMMEND_USER to RecommendUserContent::class,
                        CARDS_TYPE_RECOMMEND_CHANNEL to RecommendChannelContent::class,
                        CARDS_TYPE_FOLLOW_USER to FollowUserContent::class,
                        CARDS_TYPE_USER_LEADERBOARD to UserLeaderBoard::class,
                        CARDS_TYPE_RECOMMEND_USER_AND_FEED to RecommendUserFeedContent::class
                )
            }
        }
    }
}