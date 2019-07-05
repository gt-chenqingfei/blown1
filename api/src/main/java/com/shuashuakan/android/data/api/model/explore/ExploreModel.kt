package com.shuashuakan.android.data.api.model.explore

import com.shuashuakan.android.data.api.runtimeAdapterFactory
import com.squareup.moshi.JsonAdapter

/**
 * Author:  liJie
 * Date:   2019/1/15
 * Email:  2607401801@qq.com
 */
interface ExploreModel {
    companion object {
        private const val BANNER_TYPE = "BANNER"//banner
        private const val USER_TYPE = "USER"//接龙达人榜
        private const val CHANNEL_TYPE = "CHANNEL"//精彩话题
        private const val RANKING_TYPE = "FEED"//接龙排行榜
        private const val CATEGORY_TYPE = "CATEGORY_ENTRANCE"//分区入口

        fun create(): JsonAdapter.Factory {
            return runtimeAdapterFactory("type", ExploreModel::class) {
                mapOf(
                        BANNER_TYPE to ExploreBannerModel::class,
                        USER_TYPE to ExploreUserModel::class,
                        CHANNEL_TYPE to ExploreChannelModel::class,
                        RANKING_TYPE to ExploreRankingModel::class,
                        CATEGORY_TYPE to ExploreCategoryModel::class
                )
            }
        }
    }
}