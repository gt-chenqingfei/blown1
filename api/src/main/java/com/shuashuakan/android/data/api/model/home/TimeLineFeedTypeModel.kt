package com.shuashuakan.android.data.api.model.home

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/10/25
 * Description:
 */
@JsonSerializable
data class TimeLineFeedTypeModel(
    val data: FeedData,
    val type: String
) : TimeLineModel

@JsonSerializable
data class FeedData(
    val id: Long,
    @Json(name = "user_id")
    val userId: Long,

    @Json(name = "nick_name")
    val nickName: String,

    val avatar: String,

    @Json(name = "feed_id")
    val feedId: String,

    @Json(name = "cover")
    val cover: String,

    @Json(name = "animation_cover")
    val animationCover: String?,

    @Json(name = "feed_title")
    val feedTitle: String,

    @Json(name = "channel_id")
    val channelId: Long,

    @Json(name = "channel_name")
    val channelName: String,

    val width: Int,
    val height: Int,

    @Json(name = "fav_num")
    var favNum: Int,

    @Json(name = "share_num")
    val shareNum: Int,

    @Json(name = "comment_num")
    var commentNum: Int,

    @Json(name = "feed_create_at")
    val feedCreateAt: Long,

    var like: Boolean,

    val goods: List<VideoProduct>?,

    val comment: List<TimeLineFeedComment>?,

    @Json(name = "video_details")
    val videoDetails: List<VideoDetail>
) {
  @JsonSerializable
  data class TimeLineFeedComment(
      @Json(name = "comment_id")
      val commentId: String,
      @Json(name = "comment_nick_name")
      val commentNickName: String,
      @Json(name = "comment_content")
      val commentContent: String,
      @Json(name = "user_id")
      val userId: Long,
      @Json(name = "url")
      val url: String?)
}
