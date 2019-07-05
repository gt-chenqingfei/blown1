package com.shuashuakan.android.data.api.model.detail

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class BroadCast(
  @Json(name="avatar_url")
  val avatarUrl: String,
  val text: String,
  @Json(name="redirect_url")
  val redirectUrl: String?
)
