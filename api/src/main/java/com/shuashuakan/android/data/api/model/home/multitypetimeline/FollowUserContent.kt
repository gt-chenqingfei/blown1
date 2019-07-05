package com.shuashuakan.android.data.api.model.home.multitypetimeline

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.KotshiConstructor

/**
 * @author hushiguang
 * @since 2019-06-06.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
@Parcelize
@JsonSerializable
data class FollowUserContent @KotshiConstructor constructor(
        val data: FollowUserCards?,
        val index: Int?,
        val redirect_url: String?,
        val title: String?,
        val type: String?
) : Parcelable, CardsType


@Parcelize
@JsonSerializable
data class FollowUserCards @KotshiConstructor constructor(
        val list: List<FollowUserCard>?
) : Parcelable

@Parcelize
@JsonSerializable
data class FollowUserCard @KotshiConstructor constructor(
        val address_count: Int?,
        val fans_count: Int?,
        val follow_count: Int?,
        val gender: Int?,
        val avatar: String?,
        val bio: String?,
        val default_avatar: String?,
        var has_unread: Boolean?,
        val is_follow: Boolean?,
        val nick_name: String?,
        val redirect_url: String?,
        val show_point: Boolean?,
        val user_id: Long?
) : Parcelable