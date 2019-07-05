package com.shuashuakan.android.data.api.model.channel


import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * 话题页面-频道详细信息
 * 
 * Author: ZhaiDongyang 
 * Date: 2019/1/17
 */
@JsonSerializable
data class ChannelTopicInfo(
        val id: Long?,
        val action: ActionInfo?,
        @Json(name = "back_ground")
        val background: String?,
        @Json(name = "channel_icon")
        val channelIcon: String?,
        @Json(name = "create_at")
        val creatAt: Long?,
        val description: String?,
        @Json(name = "subscribed_count")
        val subscribedCount: Int?,
        @Json(name = "has_subscribe")
        val hasSubscribe: Boolean?,
        val name: String?,
        @Json(name = "new_feed_num")
        val newFeedNum: Int?,
        val score: Int?,
        val status: Integer?,
        val shareable: Boolean?,
        @Json(name = "total_feed_num")
        val totalFeedNum: Int?,
        val cover_url:String?

)

@JsonSerializable
data class ActionInfo(
        @Json(name = "sub_title")
        val subTitle: String?,
        val type: Int?,
        val url: String?
)




