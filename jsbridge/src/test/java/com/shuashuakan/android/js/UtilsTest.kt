package com.shuashuakan.android.js

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import org.junit.Test

class UtilsTest {
  private val request = Request(methodScope = "scope", methodId = "id", methodName = "method",
      params = mapOf("a" to "1", "b" to "2"))
  private val response = Response.create(request, 200) { put("a", "1").put("b", "2") }
  private val jsMessage = JsMessage.create("scope", "method") { put("a", "1").put("b", "2") }
  private val moshi = Moshi.Builder().add(JsJsonAdapterFactory.INSTANCE).build()

  private val requestJson = """
    {
    "scope":"view",
    "method":"fetch",
    "id":12345,
    "params":{
    "name":"fish"
    }
    }
    """

  @Test
  fun response2Json() {
    val json = response.toJson()
    val adapter = moshi.adapter(Response::class.java)
    assertThat(response).isEqualTo(adapter.fromJson(json))
  }

  @Test
  fun jsMessage2Json() {
    val json = jsMessage.toJson()
    val adapter = moshi.adapter(JsMessage::class.java)
    assertThat(jsMessage).isEqualTo(adapter.fromJson(json))
  }

  @Test
  fun json2Request() {
    val request = createRequest(requestJson)
    assertThat(request).isEqualTo(Request("view", "fetch", "12345", mapOf("name" to "fish")))
  }
}