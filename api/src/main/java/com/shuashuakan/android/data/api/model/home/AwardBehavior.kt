package com.shuashuakan.android.data.api.model.home

import com.squareup.moshi.Json

data class AwardBehavior(
  val id: String,
  @Json(name="start_name")
  val startTime: Long?,
  @Json(name="end_time")
  val endTime: Long?,
  @Json(name="play_status")
  val playStatus: Int,
  val trace: String
)
