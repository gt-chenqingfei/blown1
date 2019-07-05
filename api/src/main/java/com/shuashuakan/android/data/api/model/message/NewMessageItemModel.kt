package com.shuashuakan.android.data.api.model.message

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.KotshiConstructor

/**
 * Author:  lijie
 * Date:   2018/12/10
 * Email:  2607401801@qq.com
 */
@JsonSerializable
data class NewMessageItemModel(
        val action: String,
        val data: NewMessageSubModel
)

@JsonSerializable
data class NewMessageSubModel(
        val id: Long,
        @Json(name = "comment_id")
        val commentId: Long?,
        val content: String?,
        @Json(name = "create_at")
        val createAt: Long,
        val link: MessageLinkModel?,
        val user: MessageUserModel?,
        @Json(name = "user_list")
        val userList: List<MessageUserItemModel>?
)

@JsonSerializable
data class MessageLinkModel(
        @Json(name = "cover_url")
        val coverUrl: String?,
        @Json(name = "redirct_url")
        val redirctUrl: String?
)

@JsonSerializable
data class MessageUserModel(
        @Json(name = "address_count")
        val addressCount: Int,
        val avatar: String?,
        @Json(name = "fans_count")
        val fansCount: Int,
        @Json(name = "follow_count")
        val followCount: Int,
        @Json(name = "like_feed_count")
        val likeFeedCount: Int,
        @Json(name = "nick_name")
        val nikeName: String,
        val point: Int,
        @Json(name = "show_point")
        val showPoint: Boolean,
        @Json(name = "upload_feed_count")
        val uploadFeedCount: Int,
        @Json(name = "user_id")
        val userId: String?,
        @Json(name = "wechat_bind")
        val wechatBind: Boolean
)

@JsonSerializable
data class MessageUserItemModel @KotshiConstructor constructor(
        @Json(name = "address_count")
        val addressCount: Int,
        val avatar: String?,
        @Json(name = "fans_count")
        val fansCount: Int,
        @Json(name = "follow_count")
        val followCount: Int,
        @Json(name = "like_feed_count")
        val likeFeedCount: Int,
        @Json(name = "nick_name")
        val nikeName: String,
        val point: Int,
        @Json(name = "show_point")
        val showPoint: Boolean,
        @Json(name = "upload_feed_count")
        val uploadFeedCount: Int,
        @Json(name = "user_id")
        val userId: Long,
        @Json(name = "wechat_bind")
        val wechatBind: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString() ?: "",
            parcel.readInt(),
            parcel.readByte() != 0.toByte(),
            parcel.readInt(),
            parcel.readLong(),
            parcel.readByte() != 0.toByte())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(addressCount)
        parcel.writeString(avatar)
        parcel.writeInt(fansCount)
        parcel.writeInt(followCount)
        parcel.writeInt(likeFeedCount)
        parcel.writeString(nikeName)
        parcel.writeInt(point)
        parcel.writeByte(if (showPoint) 1 else 0)
        parcel.writeInt(uploadFeedCount)
        parcel.writeLong(userId)
        parcel.writeByte(if (wechatBind) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MessageUserItemModel> {
        override fun createFromParcel(parcel: Parcel): MessageUserItemModel {
            return MessageUserItemModel(parcel)
        }

        override fun newArray(size: Int): Array<MessageUserItemModel?> {
            return arrayOfNulls(size)
        }
    }
}
