package com.shuashuakan.android.data.api.model.channel


import com.shuashuakan.android.data.api.model.home.Feed
import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class ChannelFeed(
 val action :Action?,
 @Json(name="feed_channel")
 val feedChannel: FeedChannel?,
 @Json(name="feed_list")
 val feeds: List<Feed>
)

@JsonSerializable
data class FeedChannel(
 @Json(name="back_ground")
 val background: String?,
 @Json(name="banner_list")
 val banners: List<Banner>?,
 val name: String?,
 val description: String?,
 @Json(name="subscribed_count")
 val subscribedCount:Int,
 @Json(name="total_feed_num")
 val totalFeedNum:Int,
 @Json(name="has_subscribe")
 val hasSubscribe:Boolean
)

@JsonSerializable
data class Action(
    @Json(name ="sub_title")
    val subTitle :String,
    val type :Int,
    val url :String
)




