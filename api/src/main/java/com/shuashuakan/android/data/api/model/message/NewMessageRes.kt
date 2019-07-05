package com.shuashuakan.android.data.api.model.message

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  lijie
 * Date:   2018/12/10
 * Email:  2607401801@qq.com
 */
@JsonSerializable
data class NewMessageRes(
        @Json(name = "previous_cursor")
        val previousCursor: PreviousCursor?,
        @Json(name = "cursor")
        val nextCursor: NextCursor?,
        @Json(name = "system_broadcast_notification")
        val sysNotificationSummaries: SysItemData?,
        @Json(name = "system_personal_notification")
        val sysNotificationPersonal: SysItemData?,
        @Json(name = "list")
        val notificationsList: List<NormalMsgItemData>?
)


@JsonSerializable
data class PreviousCursor(
        @Json(name = "since_id")
        val sinceId: Long?,
        @Json(name = "max_id")
        val maxId: Long?
)

@JsonSerializable
data class NextCursor(
        @Json(name = "max_id")
        val maxId: Long?,
        @Json(name = "since_id")
        val sinceId: Long?
)

@JsonSerializable
data class ActionUserInfoListItem(
        @Json(name = "address_count")
        val addressCount: Int,
        val avatar: String?,
        val bio: String?,
        val birthday: String?,
        @Json(name = "default_avatar")
        val defaultAvatar: String?,
        @Json(name = "fans_count")
        val fansCount: Int,
        @Json(name = "follow_count")
        val followCount: Int,
        val gender: Int?,
        @Json(name = "like_feed_count")
        val likeFeedCount: Int,
        val mobile: String?,
        @Json(name = "nick_name")
        val nickName: String,
        val point: Int,
        @Json(name = "share_card_url")
        val shareCardUrl: String?,
        @Json(name = "show_point")
        val showPoint: Boolean,
        @Json(name = "solitaire_num")
        val solitaireNum: Int?,
        @Json(name = "upload_feed_count")
        val uploadFeedCount: Int?,
        @Json(name = "user_id")
        val userId: String,
        @Json(name = "wechat_bind")
        val wechatBind: Boolean,
        val labels: List<Label>?,
        @Json(name = "is_follow")
        val isFollow: Boolean?,
        @Json(name = "is_fans")
        val isFans: Boolean?
)

@JsonSerializable
data class Label(
        val content: String?,
        val image: String?
)

@JsonSerializable
data class NotificationLink(
        @Json(name = "cover_url")
        val coverUrl: String?,
        @Json(name = "redirect_url")
        val redirectUrl: String?,
        @Json(name = "target_status")
        val targetStatus: String?
)

@JsonSerializable
data class ReferenceItemModel(
        @Json(name = "target_id")
        val targetId: Long?,
        @Json(name = "channel_icon")
        val channelIcon: String?,
        val title: String?,
        val content: String?,
        val media: List<Media>?,
        @Json(name = "media_id")
        val mediaId: Long,
        @Json(name = "title_icon")
        val titleIcon: String?,
        val url: String?,
        val type: String?
)

@JsonSerializable
data class Media(
        val id: Long?,
        @Json(name = "media_type")
        val mediaType: String?,
        @Json(name = "thumb_type")
        val thumbType: String?,
        @Json(name = "thumb_url")
        val thumbUrl: String?,
        @Json(name = "thumb_width")
        val thumbWidth: Int?,
        @Json(name = "media_info")
        val mediaInfo: List<MediaInfo>?,
        @Json(name = "media_url")
        val mediaUrl: String?
)

@JsonSerializable
data class MediaInfo(
        @Json(name = "clarity_type")
        val clarityType: String,
        val height: Int,
        val url: String,
        val width: Int
)

@JsonSerializable
data class NormalMsgItemData(
        val type: String,
        @Json(name = "action_user_count")
        val actionUserCount: Int,
        @Json(name = "create_at")
        var createAt: Long,
        val id: Long?,
        @Json(name = "notification_link")
        val notificationLink: NotificationLink?,
        @Json(name = "reference_item")
        val referenceItem: ReferenceItemModel?,
        @Json(name = "action_user_info_list")
        val actionUserInfoList: List<ActionUserInfoListItem>
)


@JsonSerializable
data class SysItemData(
        val id: Long?,
        @Json(name = "create_at")
        val createAt: Long,
        val type: String,
        @Json(name = "action_user_count")
        val actionUserCount: Int,
        @Json(name = "reference_item")
        val referenceItem: ReferenceItemModel?,
        @Json(name = "notification_link")
        val notificationLink: NotificationLink?,
        @Json(name = "action_user_info_list")
        val actionUserInfoList: List<ActionUserInfoListItem>

)
