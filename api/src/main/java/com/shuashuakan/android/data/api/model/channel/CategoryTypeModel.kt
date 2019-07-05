package com.shuashuakan.android.data.api.model.channel

import com.shuashuakan.android.data.api.runtimeAdapterFactory
import com.squareup.moshi.JsonAdapter

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/10/11
 * Description:
 */
interface CategoryTypeModel {

  companion object {
    private const val CHANNEL_TYPE = "CHANNEL"
    private const val FEED_TYPE = "FEED"

    fun create(): JsonAdapter.Factory {
      return runtimeAdapterFactory("type", CategoryTypeModel::class) {
        mapOf(
            CHANNEL_TYPE to ChannelTypeModel::class,
            FEED_TYPE to FeedTypeModel::class
        )
      }
    }
  }
}