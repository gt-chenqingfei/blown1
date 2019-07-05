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
data class ChannelTypeModel(
    val data: ChannelData,
    val type: String
) : CategoryTypeModel

@JsonSerializable
data class ChannelData(
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
    @Json(name = "feed_count")
    val feedCount: Int?,
    val tag: String,
    val url: String,
    val width: Int,
    val height: Int
)
