package com.shuashuakan.android.data.api.model.comment

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/08/10
 * Description:
 */
@JsonSerializable
data class CommentListResp(
    val result: CommentResult
) {
  @JsonSerializable
  data class CommentResult(@Json(name = "has_more")
                           var hasMore: Boolean = false,

                           @Json(name = "next_cursor")
                           var nextCursor: CommentCursor?,

                           @Json(name = "hot_comments")
                           val hotComments: List<ApiComment>?,

                           var comments: MutableList<ApiComment>,
                           var summary:List<ApiSummary>?)

  @JsonSerializable
  data class CommentCursor(
      @Json(name = "since_id")
      var sinceId: Long?,
      @Json(name = "max_id")
      var maxId: Long?)

}
