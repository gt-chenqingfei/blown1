package com.shuashuakan.android.data.api.model

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class CommonResult(val result: Result) {
  @JsonSerializable
  data class Result (@Json(name="is_success") val isSuccess: Boolean)
}


