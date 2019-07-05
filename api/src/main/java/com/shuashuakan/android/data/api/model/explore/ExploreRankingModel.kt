package com.shuashuakan.android.data.api.model.explore

import com.shuashuakan.android.data.api.model.home.Feed
import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  liJie
 * Date:   2019/1/16
 * Email:  2607401801@qq.com
 * 发现页接龙排行榜
 */
@JsonSerializable
data class ExploreRankingModel(
  val type:String?,
  val desc:String?,
  val title:String?,
  @Json(name = "data_list")
  val dataList:List<Feed>
):ExploreModel

@JsonSerializable
data class ExploreRankItemModel(
    val avatar:String?,
    @Json(name = "channel_name")
    val channelName:String?,
    @Json(name = "channel_url")
    val channelUrl:String?,
    val cover:String?,
    var fav:Boolean,
    @Json(name ="fav_num")
    var favNum:Int,
    @Json(name = "channel_icon")
    val channelIcon:String?,
    @Json(name = "solitaire_num")
    val solitaireNum:Int?,
    val video:String?,
    @Json(name = "user_name")
    val userName:String?,
    val title:String?,
    @Json(name = "master_feed_id")
    val masterFeedId:String?,
    @Json(name = "user_id")
    val userId:Long,
    val id:String,
    @Json(name = "channel_id")
    val channelId:String?,
    val url:String,
    @Json(name = "redirect_url")
    val redirectUrl:String,
    val properties:ExploreRankProperties
)
@JsonSerializable
data class ExploreRankProperties(
    val ranking:Int?
)