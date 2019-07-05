package com.shuashuakan.android.js

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import se.ansman.kotshi.KotshiJsonAdapterFactory


@KotshiJsonAdapterFactory abstract class JsJsonAdapterFactory : JsonAdapter.Factory {
  companion object {
    val INSTANCE: JsJsonAdapterFactory = KotshiJsJsonAdapterFactory()
  }
}

internal val moshi = Moshi.Builder()
    .add(JsJsonAdapterFactory.INSTANCE)
    .build()

fun createRequest(json: String): Request? {
  return try {
    val adapter = moshi.adapter(Request::class.java)
    adapter.fromJson(json)
  } catch (e: Exception) {
    null
  }
}

fun Response.toJson(): String {
  val adapter = moshi.adapter(Response::class.java)
  return adapter.toJson(this)
}

fun JsMessage.toJson(): String {
  val adapter = moshi.adapter(JsMessage::class.java)
  return adapter.toJson(this)
}

