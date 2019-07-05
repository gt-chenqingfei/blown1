package com.shuashuakan.android.js

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class JsMessageTest {

  @Test
  fun buildCreate() {
    val msg = JsMessage.create("view", "get") {
      put("a", 1)
      put("b", 2)
    }
    assertThat(msg.code).isEqualTo(200)
    assertThat(msg.messageScope).isEqualTo("view")
    assertThat(msg.messageName).isEqualTo("get")
    assertThat(msg.params).hasSize(2)
    assertThat(msg.params).containsEntry("a", 1)
    assertThat(msg.params).containsEntry("b", 2)
  }
}