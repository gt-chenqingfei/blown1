package com.shuashuakan.android.data.api.model.account

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import se.ansman.kotshi.JsonDefaultValueString
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.KotshiConstructor

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@Parcelize
@JsonSerializable
data class UserAccount @KotshiConstructor constructor(
        @Json(name = "user_id")
        val userId: Long?,
        var avatar: String?,
        @Json(name = "default_avatar")
        val defaultAvatar: String?,
        @JsonDefaultValueString("")
        val birthday: String?,
        @Json(name = "nick_name")
        val nickName: String?,
        val point: Int?,
        @JsonDefaultValueString("")
        val bio: String?,
        @Json(name = "address_count")
        val addressCount: Int?,
        val mobile: String?,
        val gender: Int?,
        @Json(name = "wechat_bind")
        val wechatBind: Boolean?,
        @Json(name = "fans_count")
        var fansCount: Int?,
        @Json(name = "follow_count")
        val followCount: Int?,
        @Json(name = "upload_feed_count")
        val uploadFeedCount: Int?,
        @Json(name = "is_follow")
        val isFollow: Boolean?,
        val labels: List<UserTag>?,
        @Json(name = "share_card_url")
        val shareCardUrl: String?,
        @Json(name = "solitaire_num") // 接龙
        val solitaireNum: Int?,
        @Json(name = "optimal_solitaire_count") // 抢2F
        val optimalSolitaireCount: Int?,
        @Json(name = "up_count") // UP值
        val upCount: Int?,
        val tags: List<Tags>?, // 个人标识
        @Json(name = "user_interest")
        val userInterest: UserInfoInterests?, // 个人兴趣
        val subscribed_channel_count: Int?,//订阅数量
        @Json(name = "create_at") // 用户的创建时间
        val createAt: Long?,
        val categories: List<CategoriesTag>?,
        val like_feed_count: Int?,
        val is_fans: Boolean?//是否关注自己的用户
) : Parcelable

@Parcelize
@JsonSerializable
data class UserTag @KotshiConstructor constructor(val content: String?, val image: String?)
    : Parcelable


@Parcelize
@JsonSerializable
data class Tags @KotshiConstructor constructor(
        @Json(name = "tag_id")
        val tagId: Long,
        val icon: String,
        @Json(name = "redirect_url")
        val redirectUrl: String,
        val title: String
) : Parcelable

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@Parcelize
@JsonSerializable
data class UserInfoInterests @KotshiConstructor constructor(
        val interests: List<Interests>?
) : Parcelable

@Parcelize
@JsonSerializable
data class Interests @KotshiConstructor constructor(
        val id: Long?,
        val name: String?
) : Parcelable

@Parcelize
@JsonSerializable
data class CategoriesTag @KotshiConstructor constructor(
        val id: Int?,
        val name: String?,
        val status: Int?,
        val sort: Int?,
        val image_url: String?,
        val redirect_url: String?,
        var startIndex: Int?,
        var endIndex: Int?
) : Parcelable


