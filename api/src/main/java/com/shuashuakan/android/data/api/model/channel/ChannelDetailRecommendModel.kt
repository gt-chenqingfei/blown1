package com.shuashuakan.android.data.api.model.channel

import com.shuashuakan.android.data.api.model.home.Feed
import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  liJie
 * Date:   2019/1/18
 * Email:  2607401801@qq.com
 * 话题详情页中的推荐Model
 */
@JsonSerializable
data class ChannelDetailRecommendModel (
   @Json(name = "has_more")
   val hasMore:Boolean,
   val cursor:AllCursorModel?,
   @Json(name = "feed_list")
   val feedList:List<ChannelItemModel>
)

@JsonSerializable
data class AllCursorModel(
    @Json(name = "next_id")
    val nextId:String,
    @Json(name = "previous_id")
    val previousId:String
)

@JsonSerializable
data class ChannelItemModel(
    val type:String,
    val data:ChannelItemSubModel
)
@JsonSerializable
data class ChannelItemSubModel(
    @Json(name = "solitaire_feeds")
    val solitaireFeeds: List<Feed>
)




