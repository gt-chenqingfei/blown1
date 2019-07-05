package com.shuashuakan.android.data.api.model.channel

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class ChannelResp(
    @Json(name = "banner_list")
    val banners: List<Banner>,
    @Json(name = "category_list")
    val categories: List<Category>,
    @Json(name = "popular_channel")
    val populars: PopularList?
)

@JsonSerializable
data class Banner(
    val image: String,
    val url: String
)

@JsonSerializable
data class Category(
    val id: Long,
    val name: String
)

@JsonSerializable
data class PopularList(
    @Json(name = "feed_list")
    val feedList: List<Popular>,
    val title: String
)

@JsonSerializable
data class Popular(
    val cover: String,
    @Json(name = "channel_icon")
    val channelIcon: String,
    @Json(name = "channel_name")
    val channelName: String,
    @Json(name = "channel_url")
    val channelUrl: String
)