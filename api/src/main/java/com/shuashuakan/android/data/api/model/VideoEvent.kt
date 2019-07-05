package com.shuashuakan.android.data.api.model

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class VideoEvent(
  @Json(name="feed_id") val id: Long,
  @Json(name="duration") val duration: String,
  @Json(name="position") val position: Int,
  @Json(name="trace") val trace: String
)