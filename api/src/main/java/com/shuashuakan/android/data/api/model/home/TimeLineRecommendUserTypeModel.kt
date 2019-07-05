package com.shuashuakan.android.data.api.model.home

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/11/02
 * Description:
 */

@JsonSerializable
data class TimeLineRecommendUserTypeModel(val data: RecommendUserData,
                                          val type: String) : TimeLineRecommendModel

@JsonSerializable
data class RecommendUserData(
    val avatar: String,
    val recommend_reason: String?,
    val feed: List<Feed>,
    @Json(name = "nick_name")
    val nickName: String,
    @Json(name = "user_id")
    val userId: Long,
    @Json(name = "user_url")
    val userUrl: String
)