package com.shuashuakan.android.data.api.model.detail

import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class ProductImage(
  val width: Int,
  val height: Int,
  val url: String
)
