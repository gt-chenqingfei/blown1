package com.shuashuakan.android.data.api.model.partition

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  lijie
 * Date:   2018/11/30
 * Email:  2607401801@qq.com
 * 发现页面接龙达人榜model
 */
@JsonSerializable
data class PartitionUserModel(
        @Json(name = "data_list")
        val dataList: List<PartitionChainUserItemModel>,
        val desc: String?,
        val type: String?,
        val title: String?,
        val redirect_text: String?,
        val redirect_url: String?) : PartitionModel

@JsonSerializable
data class PartitionChainUserItemModel(
        val avatar: String?,
        @Json(name = "nick_name")
        val nickName: String?,
        @Json(name = "is_follow")
        var isFollow: Boolean,
        @Json(name = "fans_count")
        val fansCount: Int?,
        @Json(name = "like_feed_count")
        val likeFeedCount: Int?,
        @Json(name = "follow_count")
        val followCount: Int?,
        @Json(name = "user_id")
        val userId: Long,
        @Json(name = "up_count")
        val upCount: Int?
)