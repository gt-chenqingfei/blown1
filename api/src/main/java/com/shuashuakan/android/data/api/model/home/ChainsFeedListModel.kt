package com.shuashuakan.android.data.api.model.home

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  treasure_ct
 * Date:    2018/12/01
 */
@JsonSerializable
data class ChainsFeedListModel(
    @Json(name = "has_more")
    val hasMore: Boolean?,
    @Json(name = "feed_list")
    var feedList: MutableList<Feed>?,
    @Json(name = "snap_id")
    val snapId:Long?
)
