package com.shuashuakan.android.data.api.model.explore

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  liJie
 * Date:   2019/1/17
 * Email:  2607401801@qq.com
 * 榜单model
 */
@JsonSerializable
data class RankingListModel (
    val title:String,
    val type:String,
    val link:LinkModel,
    @Json(name = "data_list")
    val dataList:List<RankListModel>
)

@JsonSerializable
data class LinkModel(
    @Json(name = "redirect_url")
    val redirectUrl:String?
)

@JsonSerializable
data class RankListModel(
    val avatar:String,
    @Json(name = "fans_count")
    var fansCount:Int?,
    @Json(name = "follow_count")
    val followCount:Int?,
    @Json(name = "is_follow")
    var isFollow:Boolean,
    @Json(name = "like_feed_count")
    val likeFeedCount:Int?,
    @Json(name = "nick_name")
    val nickName:String,
    @Json(name = "redirect_url")
    val redirectUrl:String?,
    @Json(name = "up_count")
    val upCount:Int?,
    @Json(name = "user_id")
    val userId:Long,
    val tags:List<RankTag>?,
    val properties:RankPropertiesModel?,
    val is_fans:Boolean?//是否关注自己的用户

)
@JsonSerializable
data class RankPropertiesModel(
        @Json(name = "common_follow_des")
        val commonFollowDes:String?
)
@JsonSerializable
data class RankTag(
        val icon:String?
)