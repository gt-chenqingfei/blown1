package com.shuashuakan.android.data.api.model.home

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class RedirectUrl(
  val result: Result
) {
  @JsonSerializable
  data class Result(
    val url: String,
    val pid: String?,
    @Json(name="trace_id")
    val traceId: String
  )
}

