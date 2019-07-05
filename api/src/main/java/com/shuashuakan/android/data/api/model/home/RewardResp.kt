package com.shuashuakan.android.data.api.model.home

import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class RewardResp(
  val reward: Int,
  val point: Int,
  val tips: List<String>
)


