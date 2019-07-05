package com.shuashuakan.android.data.api.model.explore

import com.shuashuakan.android.data.api.model.home.Feed
import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  lijie
 * Date:   2018/11/30
 * Email:  2607401801@qq.com
 */
@JsonSerializable
data class ExploreChannel (
   val data :ExploreChannelData,
   val type:String
)

@JsonSerializable
data class ExploreChannelData(
    @Json(name = "icon_url")
    val iconUrl:String?,
    val id :String,
    val description:String?,
    val list:List<Feed>,
    val name:String?,
    @Json(name="redirect_url")
    val redirectUrl:String?
)