package com.shuashuakan.android.data.api.model.home.multitypetimeline

import android.os.Parcelable
import com.shuashuakan.android.data.api.model.home.Feed
import kotlinx.android.parcel.Parcelize
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.KotshiConstructor

@Parcelize
@JsonSerializable
data class TimeLineContent @KotshiConstructor constructor(
        val data: TimeLineContentCards?,
        val index: Int?,
        val redirect_url: String?,
        val title: String?,
        val type: String?
) : Parcelable, CardsType

@Parcelize
@JsonSerializable
data class TimeLineContentCards @KotshiConstructor constructor(
        val list: List<Feed>?
) : Parcelable