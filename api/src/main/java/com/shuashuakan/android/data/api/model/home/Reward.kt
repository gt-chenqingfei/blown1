package com.shuashuakan.android.data.api.model.home

import com.squareup.moshi.Json


data class Reward(
  @Json(name="reward_id")
  val rewardId: Long,
  val history: List<AwardBehavior>
)
