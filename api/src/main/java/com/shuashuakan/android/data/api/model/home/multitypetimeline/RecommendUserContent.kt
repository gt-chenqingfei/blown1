package com.shuashuakan.android.data.api.model.home.multitypetimeline

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.KotshiConstructor

@Parcelize
@JsonSerializable
data class RecommendUserContent @KotshiConstructor constructor(
        val data: RecommendUserCards?,
        val index: Int?,
        val redirect_url: String?,
        val title: String?,
        val type: String?
) : Parcelable, CardsType

@Parcelize
@JsonSerializable
data class RecommendUserCards @KotshiConstructor constructor(
        val list: List<RecommendUser>?
) : Parcelable

@Parcelize
@JsonSerializable
data class RecommendUser @KotshiConstructor constructor(
        val avatar: String?,
        val user_id: Long?,
        val nick_name: String?,
        val bio: String?,
        var is_follow: Boolean?,
        val redirect_url: String?,
        val properties: RecommendUserProperties?,
        val is_fans:Boolean?//是否关注自己的用户
) : Parcelable

@Parcelize
@JsonSerializable
data class RecommendUserProperties @KotshiConstructor constructor(
        val recommend_reason: String?
) : Parcelable