package com.shuashuakan.android.data.api.model.home.multitypetimeline

import android.os.Parcelable
import com.shuashuakan.android.data.api.model.home.Feed
import kotlinx.android.parcel.Parcelize
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.KotshiConstructor

@Parcelize
@JsonSerializable
data class RecommendUserFeedContent @KotshiConstructor constructor(
        val data: RecommendUserFeedList?,
        val index: Int?,
        val redirect_url: String?,
        val title: String?,
        val type: String?
) : Parcelable, CardsType

@Parcelize
@JsonSerializable
data class RecommendUserFeedList @KotshiConstructor constructor(
        val list: List<RecommendUserFeedCards>?
) : Parcelable

@Parcelize
@JsonSerializable
data class RecommendUserFeedCards @KotshiConstructor constructor(
        val avatar: String?,
        val user_id: Long?,
        val nick_name: String?,
        val bio: String?,
        var is_follow: Boolean?,
        var is_fans: Boolean?,
        val redirect_url: String?,
        val properties: RecommendUserFeedCardsProperties?
) : Parcelable

@Parcelize
@JsonSerializable
data class RecommendUserFeedCardsProperties @KotshiConstructor constructor(
        val recommend_reason: String?,
        val feed_data: List<Feed>?
) : Parcelable