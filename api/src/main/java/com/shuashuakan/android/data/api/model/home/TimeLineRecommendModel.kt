package com.shuashuakan.android.data.api.model.home

import com.shuashuakan.android.data.api.runtimeAdapterFactory
import com.squareup.moshi.JsonAdapter

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/11/02
 * Description:
 */
interface TimeLineRecommendModel {
  companion object {
    private const val RECMOOEND_USER_TYPE = "RECOMMEND_USER"

    fun create(): JsonAdapter.Factory {
      return runtimeAdapterFactory("type", TimeLineRecommendModel::class) {
        mapOf(
            RECMOOEND_USER_TYPE to TimeLineRecommendUserTypeModel::class
        )
      }
    }
  }
}