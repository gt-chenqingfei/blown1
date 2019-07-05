package com.shuashuakan.android.js

class ParamsBuilder {
  private val map = mutableMapOf<String, Any>()

  fun asParams(): Params = map.toMap()

  fun errorCode(code: Int): ParamsBuilder {
    return put("error_code", code)
  }

  fun errorMessage(msg: String): ParamsBuilder {
    map["error_message"] = msg
    return put("error_message", msg)
  }

  fun put(key: String, value: Any): ParamsBuilder {
    map[key] = value
    return this
  }
}