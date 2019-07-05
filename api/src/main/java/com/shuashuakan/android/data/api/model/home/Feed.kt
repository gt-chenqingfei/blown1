package com.shuashuakan.android.data.api.model.home

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import com.shuashuakan.android.data.api.model.comment.ApiMedia
import com.shuashuakan.android.data.api.model.home.multitypetimeline.CardsType
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.KotshiConstructor

@JsonSerializable
data class Feed @KotshiConstructor constructor(
        val id: String,
        var avatar: String?,
        var cover: String?,
        @Json(name = "user_id")
        val userId: Long,
        @Json(name = "animation_cover")
        val animationCover: String?,
        val height: Int,
        val width: Int,
        var title: String,
        val text: String?,
        @Json(name = "user_name")
        val userName: String,
        @Json(name = "fav_num")
        var favNum: Int,
        @Json(name = "share_num")
        var shareNum: Int?,
        var fav: Boolean,
        @Json(name = "mount_list")
        val videoProducts: List<VideoProduct>?,
        @Json(name = "channel_name")
        val channelName: String?,
        @Json(name = "channel_icon")
        val channelIcon: String?,
        @Json(name = "channel_url")
        val channelUrl: String?,
        @Json(name = "play_count")
        var playCount: Int?,
        @Json(name = "comment_num")
        var commentNum: Int,
        @Json(name = "force_feed_sign")
        var forceFeedSign: ForceFeedSign?,
        @Json(name = "has_follow_user")
        var hasFollowUser: Boolean?,
        @Json(name = "video_details")
        val videoDetails: List<VideoDetail>?,
        @Json(name = "solitaire_num")
        var solitaireNum: Int?,
        @Json(name = "channel_id")
        val channelId: Int?,
        @Json(name = "master_feed_id")
        val masterFeedId: String?,
        @Json(name = "type")
        val type: String?,
        @Json(name = "hot_comment")
        val hotComment: HotComment?,
        var properties: PropertiesModel?,
        @Json(name = "recommend_reason")
        val repeatLogo: String?,
        @Json(name = "recommend_icon")
        val excellenceIcon: String?,
        @Json(name = "create_at")
        val createAt: Long?,
        @Json(name = "redirect_url")
        val redirectUrl: String?,
        @Json(name = "activity_label")
        val activityLabel: LabelModel?,
        @Json(name = "first_frame")
        val firstFrame: String?,
        @Json(name = "author")
        val author: Author?,
        val has_audit:Boolean?//true表示在审核，false表示审核通过


) : Parcelable, CardsType {
    fun getRealFeedId(): String {
        val idArray = id.split("l")
        return if (!idArray.isEmpty()) {
            idArray[0]
        } else {
            id
        }
    }

    fun getUserId(): String {
        return userId.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Feed) {
            return false
        }

        return this.getRealFeedId() == other.getRealFeedId()
    }

    constructor(source: Parcel) : this(
            source.readString() ?: "",
            source.readString(),
            source.readString(),
            source.readLong(),
            source.readString(),
            source.readInt(),
            source.readInt(),
            source.readString() ?: "",
            source.readString(),
            source.readString() ?: "",
            source.readInt(),
            source.readValue(Int::class.java.classLoader) as Int?,
            1 == source.readInt(),
            source.createTypedArrayList(VideoProduct.CREATOR),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readInt(),
            source.readParcelable<ForceFeedSign>(ForceFeedSign::class.java.classLoader),
            source.readValue(Boolean::class.java.classLoader) as Boolean?,
            source.createTypedArrayList(VideoDetail.CREATOR),
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readString(),
            source.readString(),
            source.readParcelable<HotComment>(HotComment::class.java.classLoader),
            source.readParcelable<PropertiesModel>(PropertiesModel::class.java.classLoader),
            source.readString(),
            source.readString(),
            source.readValue(Long::class.java.classLoader) as Long?,
            source.readString(),
            source.readParcelable<LabelModel>(LabelModel::class.java.classLoader),
            source.readString(),
            source.readParcelable<Author>(Author::class.java.classLoader),
            1 == source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(avatar)
        writeString(cover)
        writeLong(userId)
        writeString(animationCover)
        writeInt(height)
        writeInt(width)
        writeString(title)
        writeString(text)
        writeString(userName)
        writeInt(favNum)
        writeValue(shareNum)
        writeInt((if (fav) 1 else 0))
        writeTypedList(videoProducts)
        writeString(channelName)
        writeString(channelIcon)
        writeString(channelUrl)
        writeValue(playCount)
        writeInt(commentNum)
        writeParcelable(forceFeedSign, 0)
        writeValue(hasFollowUser)
        writeTypedList(videoDetails)
        writeValue(solitaireNum)
        writeValue(channelId)
        writeString(masterFeedId)
        writeString(type)
        writeParcelable(hotComment, 0)
        writeParcelable(properties, 0)
        writeString(repeatLogo)
        writeString(excellenceIcon)
        writeValue(createAt)
        writeString(redirectUrl)
        writeParcelable(activityLabel, 0)
        writeString(firstFrame)
        writeParcelable(author, 0)
        writeInt((if (has_audit == true) 1 else 0))
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Feed> = object : Parcelable.Creator<Feed> {
            override fun createFromParcel(source: Parcel): Feed = Feed(source)
            override fun newArray(size: Int): Array<Feed?> = arrayOfNulls(size)
        }
    }
}

@Parcelize
@JsonSerializable
data class PropertiesModel @KotshiConstructor constructor(
        val floor: Int?,
        @Json(name = "allow_edit")
        val allowEdit: Boolean?,
        @Json(name = "allow_solitaire")
        val allowSolitaire: Boolean?,
        val ranking: Int?,
        val allow_download: Boolean?,
        @Json(name = "edit_info")
        var editInfo: EditInfo?
) : Parcelable

@JsonSerializable
data class EditInfo @KotshiConstructor constructor(
        @Json(name = "can_edit")
        var canEdit: Boolean?,
        @Json(name = "editable_count")
        var editableCount: Int?,
        @Json(name = "editable_total_count")
        val editableTotalCount: Int?,
        var message: String?
) : Parcelable {
    constructor(source: Parcel) : this(
            source.readValue(Boolean::class.java.classLoader) as Boolean?,
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeValue(canEdit)
        writeValue(editableCount)
        writeValue(editableTotalCount)
        writeString(message)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<EditInfo> = object : Parcelable.Creator<EditInfo> {
            override fun createFromParcel(source: Parcel): EditInfo = EditInfo(source)
            override fun newArray(size: Int): Array<EditInfo?> = arrayOfNulls(size)
        }
    }
}

@JsonSerializable
data class VideoProduct @KotshiConstructor constructor(
        @Json(name = "mount_id")
        val id: Long,
        val title: String,
        val url: String?,
        val image: String,
        val description: String?,
        val price: String?,
        @Json(name = "price_info")
        val priceInfo: String?,
        @Json(name = "fav_mount")
        var fav: Boolean,
        @Json(name = "tip")
        val tip: String?,
        val action: List<Action>?
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString() ?: "",
            parcel.readString(),
            parcel.readString() ?: "",
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readString(),
            parcel.createTypedArrayList(Action)
    )

    override fun writeToParcel(
            parcel: Parcel,
            flags: Int
    ) {
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeString(url)
        parcel.writeString(image)
        parcel.writeString(description)
        parcel.writeString(price)
        parcel.writeString(priceInfo)
        parcel.writeByte(if (fav) 1 else 0)
        parcel.writeString(tip)
        parcel.writeTypedList(action)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<VideoProduct> {
        override fun createFromParcel(parcel: Parcel): VideoProduct {
            return VideoProduct(parcel)
        }

        override fun newArray(size: Int): Array<VideoProduct?> {
            return arrayOfNulls(size)
        }
    }
}

@JsonSerializable
data class ForceFeedSign @KotshiConstructor constructor(
        val icon: String,
        val title: String?,
        val type: Int,
        val url: String
) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString() ?: "",
            source.readString(),
            source.readInt(),
            source.readString() ?: ""
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(icon)
        writeString(title)
        writeInt(type)
        writeString(url)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<ForceFeedSign> = object : Parcelable.Creator<ForceFeedSign> {
            override fun createFromParcel(source: Parcel): ForceFeedSign = ForceFeedSign(source)
            override fun newArray(size: Int): Array<ForceFeedSign?> = arrayOfNulls(size)
        }
    }
}

@JsonSerializable
data class VideoDetail @KotshiConstructor constructor(
        val clarity: String,
        val id: String,
        val url: String) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString() ?: "",
            source.readString() ?: "",
            source.readString() ?: ""
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(clarity)
        writeString(id)
        writeString(url)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<VideoDetail> = object : Parcelable.Creator<VideoDetail> {
            override fun createFromParcel(source: Parcel): VideoDetail = VideoDetail(source)
            override fun newArray(size: Int): Array<VideoDetail?> = arrayOfNulls(size)
        }
    }
}

@JsonSerializable
data class HotComment @KotshiConstructor constructor(
        val author: Author?,
        val comment_count: Int,
        val content: String,
        var has_liked: Boolean,
        val id: Long,
        var like_count: Int,
        val state: String,
        val target_id: String,
        val target_type: String,
        var media: List<ApiMedia>?
) : Parcelable {
    constructor(source: Parcel) : this(
            source.readParcelable<Author>(Author::class.java.classLoader),
            source.readInt(),
            source.readString() ?: "",
            1 == source.readInt(),
            source.readLong(),
            source.readInt(),
            source.readString() ?: "",
            source.readString() ?: "",
            source.readString() ?: "",
            source.createTypedArrayList(ApiMedia.CREATOR)
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeParcelable(author, 0)
        writeInt(comment_count)
        writeString(content)
        writeByte(if (has_liked) 1 else 0)
        writeLong(id)
        writeInt(like_count)
        writeString(state)
        writeString(target_id)
        writeString(target_type)
        writeTypedList(media)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<HotComment> = object : Parcelable.Creator<HotComment> {
            override fun createFromParcel(source: Parcel): HotComment = HotComment(source)
            override fun newArray(size: Int): Array<HotComment?> = arrayOfNulls(size)
        }
    }
}

@Parcelize
@JsonSerializable
data class Author @KotshiConstructor constructor(
        val address_count: Int,
        val avatar: String,
        val bio: String,
        val default_avatar: String,
        val fans_count: Int,
        val follow_count: Int,
        @Json(name = "up_count")
        val upCount: Int?,
        val like_feed_count: Int,
        val nick_name: String,
        val point: Int,
        val show_point: Boolean,
        val upload_feed_count: Int,
        val user_id: Long,
        val wechat_bind: Boolean,
        val tags: List<AuthorTagModel>?,
        @Json(name = "is_follow")
        var isFollow: Boolean?,
        val is_fans:Boolean?//是否关注自己的用户

) : Parcelable


@Parcelize
@JsonSerializable
data class LabelModel @KotshiConstructor constructor(
        @Json(name = "label_enum")
        val labelEnum: String?,
        @Json(name = "redirect_url")
        val redirectUrl: String?,
        val text: String?,
        @Json(name = "label_icon_url")
        val labelIconUrl: String?
) : Parcelable


@Parcelize
@JsonSerializable
data class AuthorTagModel @KotshiConstructor constructor(
        val icon: String?,
        @Json(name = "redirect_url")
        val redirectUrl: String?,
        @Json(name = "tag_id")
        val tagId: String?,
        val title: String?
) : Parcelable
