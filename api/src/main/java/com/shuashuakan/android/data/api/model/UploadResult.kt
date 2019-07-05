package com.shuashuakan.android.data.api.model

import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class UploadResult(val result: Result) {
  @JsonSerializable
  data class Result(val id: Long, val url: String)
}
