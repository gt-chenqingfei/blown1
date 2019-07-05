package com.shuashuakan.android.js

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class Request(
    @Json(name = "scope") val methodScope: String,
    @Json(name = "method") val methodName: String,
    @Json(name = "id") val methodId: String,
    @Json(name = "params") val params: Params?)
