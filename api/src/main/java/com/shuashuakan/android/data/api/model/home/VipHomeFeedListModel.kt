package com.shuashuakan.android.data.api.model.home

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  treasure_ct
 * Date:    2018/12/01
 */
@JsonSerializable
data class VipHomeFeedListModel(
        @Json(name = "feeds")
        var feedList: MutableList<Feed>?,
        @Json(name = "message")
        val message: VipHomeMessage?
)

@JsonSerializable
data class VipHomeMessage(
        @Json(name = "author")
        var author: String,
        @Json(name = "content")
        val content: String
)

