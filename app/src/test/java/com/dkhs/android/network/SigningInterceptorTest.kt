package com.dkhs.android.network

import com.shuashuakan.android.network.SigningInterceptor
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SigningInterceptorTest {
  private val map = mapOf(
      "aaa" to "123",
      "bbb" to "goodman",
      "c" to "true",
      "fields" to "cid,parent_cid,name,is_parent",
      "timestamp" to "1522044649000",
      "token" to "hello world"
  )

  @Test
  fun testSign() {
    assertThat(SigningInterceptor.sign(map, "this_is_my_secret")).isEqualTo(
        "9d98d6c2b9ecaf08b99dd340fb9af315")
  }

  @Test
  fun testBodyAsMap() {
    val body = "aaa=123&bbb=goodman&c=true&fields=cid%2Cparent_cid%2Cname%2Cis_parent&timestamp=1522044649000&token=hello+world"
    val map = SigningInterceptor.parseAsMap(body)

    assertThat(map).hasSize(6)
    assertThat(map).containsEntry("aaa", "123")
    assertThat(map).containsEntry("bbb", "goodman")
    assertThat(map).containsEntry("c", "true")
    assertThat(map).containsEntry("timestamp", "1522044649000")
    assertThat(map).containsEntry("token", "hello world")
    assertThat(map).containsEntry("fields", "cid,parent_cid,name,is_parent")
  }
}