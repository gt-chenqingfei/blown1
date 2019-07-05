package com.shuashuakan.android.data.api.model.home.multitypetimeline

import android.os.Parcelable
import com.shuashuakan.android.data.api.model.home.Feed
import kotlinx.android.parcel.Parcelize
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.KotshiConstructor

/**
 * 多类型关注 TimeLine 数据 Model
 */
@Parcelize
@JsonSerializable
data class MultiTypeTimeLineModel @KotshiConstructor constructor(
    val timeline: List<Feed>?,
    val source: String?,
    val cursor: TimeLineCursor?
) : Parcelable

@Parcelize
@JsonSerializable
data class TimeLineCursor @KotshiConstructor constructor(
    val next_id: String?,
    val previous_id: String?
) : Parcelable