package com.shuashuakan.android.data.api.model.home

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class TabBar(
  val title: String,
  @Json(name="normal_image_url")
  val normalImageUrl: String,
  @Json(name="highlighted_image_url")
  val highlightedImageUrl: String,
  val url: String,
  val index: Int
)