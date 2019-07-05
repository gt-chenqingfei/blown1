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
data class ApiComment(
    val id: Long?,

    val author: ApiUserInfo,

    val content: String,

    @Json(name = "create_at")
    val createAt: Long,

    @Json(name = "like_count")
    var likeCount: Int,

    @Json(name = "has_liked")
    var liked: Boolean,

    val state:String?,

    @Json(name = "reply_count")
    val replyCount: Int?,

    @Json(name = "comment_count")
    var commentCount: Int?,

    @Json(name = "target_id")
    val targetId: String,

    @Json(name = "target_type")
    val targetType: String?,

    @Json(name = "newest_comments")
    var newestComments: CommentListResp.CommentResult?,

    @Json(name = "reply_to")
    val replyTo: ApiUserInfo?,

    @Json(name = "media")
    var media: List<ApiMedia>?
)