package com.shuashuakan.android.data.api.model.home.multitypetimeline

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.KotshiConstructor


@Parcelize
@JsonSerializable
data class SubscribedChannelContent @KotshiConstructor constructor(
        val data: SubscribedChannelCards?,
        val index: Int?,
        val redirect_url: String?,
        val title: String?,
        val type: String?
) : Parcelable, CardsType

@Parcelize
@JsonSerializable
data class SubscribedChannelCards @KotshiConstructor constructor(
        val list: List<SubscribedChannel>?
) : Parcelable

@Parcelize
@JsonSerializable
data class SubscribedChannel @KotshiConstructor constructor(
        val back_ground: String?,
        val id: Long?,
        val cover_url: String?,
        val description: String?,
        val create_at: Long?,
        val name: String?,
        val new_feed_num: Int?,
        val total_feed_num: Int?,
        val redirect_url: String?
) : Parcelable