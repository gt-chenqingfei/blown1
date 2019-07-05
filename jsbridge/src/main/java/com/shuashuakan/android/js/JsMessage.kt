package com.shuashuakan.android.js

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class JsMessage(
    @Json(name = "method") val messageName: String,
    @Json(name = "scope") val messageScope: String,
    @Json(name = "code") val code: Int = 200,
    @Json(name = "data") val params: Params) {
  companion object {
    fun create(scope: String, name: String, init: ParamsBuilder.() -> Unit): JsMessage {
      val builder = ParamsBuilder()
      builder.init()
      return JsMessage(messageName = name, messageScope = scope, code = 200,
          params = builder.asParams())
    }
  }
}
