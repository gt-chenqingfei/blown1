package com.shuashuakan.android.data.api.model.detail

import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class TbPwd(val result: Result) {
  @JsonSerializable
  data class Result(val tbpwd: String)
}

