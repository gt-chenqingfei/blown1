package com.shuashuakan.android.data.api.model

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/09/27
 * Description:
 */
@JsonSerializable
data class NoticeListBean(
    val action: String,
    val data: Data?) {
  @JsonSerializable
  data class Data(
      val id: Long,
      val content: String?,
      @Json(name = "create_at")
      val createAt: Long,
      val author: NoticeActionUserInfo,
      val comment: CommentData?
  )

  @JsonSerializable
  data class CommentData(
      @Json(name = "comment_id")
      val commentId: Long,
      val cover: String?,
      val url: String?)

  @JsonSerializable
  data class NoticeActionUserInfo(
      @Json(name = "user_id")
      val userId: Long,
      @Json(name = "nick_name")
      val nickName: String,
      val avatar: String,
      val url: String,
      @Json(name = "is_follow")
      val isFollow:Boolean?)
}
