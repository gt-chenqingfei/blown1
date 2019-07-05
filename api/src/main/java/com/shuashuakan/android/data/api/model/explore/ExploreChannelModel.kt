package com.shuashuakan.android.data.api.model.explore

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  lijie
 * Date:   2018/11/30
 * Email:  2607401801@qq.com
 * 发现页精彩话题model
 */
@JsonSerializable
data class ExploreChannelModel(
    val type:String?,
    val desc:String?,
    val title: String?,
    @Json(name = "data_list")
    val dataList:List<ExploreChannelItemModel>
):ExploreModel

@JsonSerializable
data class ExploreChannelItemModel(
    @Json(name = "cover_url")
    val coverUrl:String?,
    val description:String?,
    @Json(name = "has_subscribe")
    var hasSubscribe:Boolean,
    val name:String?,
    @Json(name="subscribed_count")
    var subscribedCount:Int?,
    @Json(name="redirect_url")
    val redirectUrl:String?,
    @Json(name="channel_icon")
    val channelIcon:String?,
    val id :Long
)