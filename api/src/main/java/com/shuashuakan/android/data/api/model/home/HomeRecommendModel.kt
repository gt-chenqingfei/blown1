package com.shuashuakan.android.data.api.model.home

import com.shuashuakan.android.data.api.runtimeAdapterFactory
import com.squareup.moshi.JsonAdapter

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/11/29
 * Description:
 */
interface HomeRecommendModel {
  companion object {
    private const val FEED_TYPE = "FEED"
    private const val INTREEST_TYPE = "INTEREST_SELECTION"

    fun create(): JsonAdapter.Factory {
      return runtimeAdapterFactory("type", HomeRecommendModel::class) {
        mapOf(
            FEED_TYPE to HomeFeedTypeModel::class,
            INTREEST_TYPE to HomeRecommendInterestTypeModel::class
        )
      }
    }
  }
}