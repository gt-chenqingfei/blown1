package com.shuashuakan.android.data.api.model

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class AccessToken(
  @Json(name="access_token")
  val accessToken: String
)
