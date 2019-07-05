package com.shuashuakan.android.data.api.model.partition

import com.shuashuakan.android.data.api.runtimeAdapterFactory
import com.squareup.moshi.JsonAdapter

/**
 * @author hushiguang
 * @since 2019-06-17.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
interface PartitionModel {
    companion object {
        private const val BANNER_TYPE = "BANNER"//banner
        private const val USER_TYPE = "USER_LEADERBOARD"//达人榜
        private const val RECOMMEND_TYPE = "RECOMMEND_CHANNEL"//精彩话题
        private const val FEED_LEADERBOARD_TYPE = "FEED_LEADERBOARD"//接龙排行榜

        fun create(): JsonAdapter.Factory {
            return runtimeAdapterFactory("type", PartitionModel::class) {
                mapOf(
                        BANNER_TYPE to PartitionBannerModel::class,
                        USER_TYPE to PartitionUserModel::class,
                        RECOMMEND_TYPE to PartitionRecommendModel::class,
                        FEED_LEADERBOARD_TYPE to PartitionLeaderBoardModel::class)
            }
        }
    }

}