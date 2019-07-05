package com.shuashuakan.android.data.api.model.home

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * 动态页的视频 Model
 *
 * Author: ZhaiDongyang
 * Date: 2019/1/19
 */
@JsonSerializable
data class TopicFeedData(
    val data: Feed,
    val type: String
)

//@JsonSerializable
//data class TopicFeedDataDetail(
//    // 个人页面和话题页面的 TimeLine 公共字段
//    val id: String?,
//    @Json(name = "user_id")
//    val userId: Long?,
//
//    @Json(name = "user_name")
//    val userName: String?,
//
//    @Json(name = "cover")
//    val cover: String?,
//
//    @Json(name = "animation_cover")
//    val animationCover: String?,
//
//    @Json(name = "channel_id")
//    val channelId: Long?,
//
//    @Json(name = "channel_name")
//    val channelName: String?,
//
//    @Json(name = "channel_icon")
//    val channelIcon: String?,
//
//    @Json(name = "first_frame")
//    val firstFrame: String?,
//
//    @Json(name = "create_at")
//    val createAT: Long?,
//
//    @Json(name = "video_details")
//    val videoDetails: List<VideoDetals>?,
//
//    val title: String?,
//    val text: String?,
//    val video: String?,
//    val width: Int?,
//    val height: Int?,
//
//
//    // 下面是个人页面 Timeline 需要的字段
//    @Json(name = "avatar")
//    val avatar: String?,
//    @Json(name = "share_num")
//    val shareNum: Int?,
//    @Json(name = "play_count")
//    val palyCount: Long?,
//    @Json(name = "channel_url")
//    var channelUrl: String?,
//    @Json(name = "comment_num")
//    var commentNum: Int?,
//    @Json(name = "fav")
//    var fav: Boolean?,
//    @Json(name = "fav_num")
//    var favNum: Int?,
//
//
//    // 下面是话题页面 TimeLine 需要的字段
//    @Json(name = "user_avatar")
//    val userAvatar: String?,
//    @Json(name = "hot_comment")
//    val hotComment: String?
//)
//
//@JsonSerializable
//data class VideoDetals(
//    val clarity: String?,
//    val width: Int?,
//    val height: Int?,
//    val size: Int?,
//    @Json(name = "url")
//    val url: String?)