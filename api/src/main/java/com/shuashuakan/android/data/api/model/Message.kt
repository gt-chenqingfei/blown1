package com.shuashuakan.android.data.api.model

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/08/02
 * Description:
 */
@JsonSerializable
data class Message(
    val id: Long?,
    @Json(name = "user_id")
    val userId: Long?,
    val content: String,
    val url: String?,
    val image: String?,
    val style: Int?,
    val type:Int?
)