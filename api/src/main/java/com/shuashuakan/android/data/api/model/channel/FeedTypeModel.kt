package com.shuashuakan.android.data.api.model.channel

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/10/11
 * Description:
 */
@JsonSerializable
data class FeedTypeModel(
    val data: FeedData,
    val type: String
) : CategoryTypeModel

@JsonSerializable
data class FeedData(
    @Json(name = "animation_cover")
    val animationCover: String?,
    @Json(name = "channel_icon")
    val channelIcon: String,
    @Json(name = "channel_id")
    val channelId: Long,
    @Json(name = "channel_name")
    val channelName: String,
    @Json(name = "cover")
    val cover: String,
    val id: String,
    val text: String?,
    val title: String,
    val video: String,
    val url: String,
    val width: Int,
    val height: Int
)