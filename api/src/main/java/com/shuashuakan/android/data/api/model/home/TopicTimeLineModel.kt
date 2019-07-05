package com.shuashuakan.android.data.api.model.home

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * TimeLine
 *
 * Author: ZhaiDongyang
 * Date: 2019/1/19
 */
@JsonSerializable
data class TopicTimeLineModel(
    @Json(name = "cursor")
    val cursor: TopicTimeLineCursor?,
    @Json(name = "has_more")
    val hasMore: Boolean?,
    @Json(name = "feed_list")
    val feedList: List<TopicFeedData>
)

@JsonSerializable
data class TopicTimeLineCursor(
    @Json(name = "next_id")
    val nextId: String?,
    @Json(name = "previous_id")
    val previousId: String?
)