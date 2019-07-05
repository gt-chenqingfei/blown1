package com.shuashuakan.android.data.api.model.account

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class Account(
    @Json(name = "user_id")
    val userId: Long?,
    @Json(name = "access_token")
    val accessToken: String?
)
