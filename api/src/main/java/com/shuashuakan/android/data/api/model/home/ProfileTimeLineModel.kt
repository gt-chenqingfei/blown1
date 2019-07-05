package com.shuashuakan.android.data.api.model.home

import com.shuashuakan.android.data.api.runtimeAdapterFactory
import com.squareup.moshi.JsonAdapter

/**
 * TimeLine
 *
 * Author: ZhaiDongyang
 * Date: 2019/1/18
 */
interface ProfileTimeLineModel {
  companion object {
    private const val FEED_TYPE = "FEED"

    fun create(): JsonAdapter.Factory {
      return runtimeAdapterFactory("type", ProfileTimeLineModel::class) {
        mapOf(
            FEED_TYPE to ProfileTimeLineFeedTypeModel::class
        )
      }
    }
  }
}