package com.shuashuakan.android.data.api.model.home.multitypetimeline

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.KotshiConstructor

@Parcelize
@JsonSerializable
data class RecommendChannelContent @KotshiConstructor constructor(
        val data: RecommendChannelCards?,
        val index: Int?,
        val redirect_url: String?,
        val title: String?,
        val type: String?
) : Parcelable, CardsType

@Parcelize
@JsonSerializable
data class RecommendChannelCards @KotshiConstructor constructor(
        val list: List<RecommendChannel>?
) : Parcelable

@Parcelize
@JsonSerializable
data class RecommendChannel @KotshiConstructor constructor(
        val back_ground: String?,
        val description: String?,
        val avatar: String?,
        val cover_url: String?,
        val id: Long?,
        val name: String?,
        var has_subscribe: Boolean?,
        val redirect_url: String?,
        val properties: RecommendChannelProperties?
) : Parcelable

@Parcelize
@JsonSerializable
data class RecommendChannelProperties @KotshiConstructor constructor(
        val feed_data: List<RecommendChannelPropertiesFeedData>?
) : Parcelable


@Parcelize
@JsonSerializable
data class RecommendChannelPropertiesFeedData @KotshiConstructor constructor(
        val cover: String,
        val animation_cover: String
) : Parcelable