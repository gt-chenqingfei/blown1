package com.shuashuakan.android.data.api.model.partition

import com.shuashuakan.android.data.api.model.home.Feed
import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  lijie
 * Date:   2018/11/30
 * Email:  2607401801@qq.com
 */
@JsonSerializable
data class PartitionRecommendModel(
        @Json(name = "data_list")
        val dataList: List<PartitionRecommendItemModel>,
        val type: String?,
        val title: String?,
        val redirect_text: String?,
        val redirect_url: String?
) : PartitionModel

@JsonSerializable
data class PartitionRecommendItemModel(
        @Json(name = "has_subscribe")
        val hasSubscribe: Boolean?,
        val id: Int?,
        val score: Int?,
        val name: String?,
        val redirect_url: String?,
        val feed_list: List<Feed>?,
        val shareable: Boolean?,
        @Json(name = "subscribed_count")
        val subscribedCount: Int?,
        @Json(name = "total_feed_num")
        val totalFeedNum: Int?,
        @Json(name = "new_feed_num")
        val newFeedNum: Int?,
        var allCount: Int?,
        var index: Int?)