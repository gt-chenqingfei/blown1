package com.shuashuakan.android.data.api.model.account

import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class Good(
  val id: Long,
  val image: List<String>,
  val title: String,
  val url: List<String>
)
