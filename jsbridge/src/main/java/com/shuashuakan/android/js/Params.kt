package com.shuashuakan.android.js

import org.json.JSONObject

typealias Params = Map<String, Any>


fun Params.getString(key: String, defaultValue: String? = null): String? {
  return get(key)?.toString() ?: defaultValue
}

fun Params.getInt(key: String, defaultValue: Int): Int {
  val string = get(key)?.toString()
  return string?.toIntOrNull() ?: defaultValue
}

fun Params.asJson(): String? {
  return try {
    JSONObject(this).toString()
  } catch (e: Exception) {
    null
  }
}