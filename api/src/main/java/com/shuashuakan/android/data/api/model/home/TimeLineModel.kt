package com.shuashuakan.android.data.api.model.home

import com.shuashuakan.android.data.api.runtimeAdapterFactory
import com.squareup.moshi.JsonAdapter

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/10/25
 * Description:
 */
interface TimeLineModel {
  companion object {
    private const val FEED_TYPE = "FEED"

    fun create(): JsonAdapter.Factory {
      return runtimeAdapterFactory("type", TimeLineModel::class) {
        mapOf(
            FEED_TYPE to TimeLineFeedTypeModel::class
        )
      }
    }
  }
}