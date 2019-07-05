package com.shuashuakan.android.data.api.model.account

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Created by lijie on ${Date} 上午11:45
 */
@JsonSerializable
data class FocusModel (
    val avatar :String,
    val bio :String?,
    @Json(name = "nick_name")
    val nickName:String,
    val url:String?,
    @Json(name = "user_id")
    val userId : String,
    var follow : Boolean?,
    val is_fans:Boolean?//是否关注自己的用户
)