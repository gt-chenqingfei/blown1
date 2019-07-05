package com.shuashuakan.android.js

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ParamBuilderTest {

  @Test
  fun buildCreate() {
    val builder = ParamsBuilder()
    assertThat(builder.asParams()).isEmpty()
    builder.errorCode(400)
    assertThat(builder.asParams()).hasSize(1)
    assertThat(builder.asParams()).containsEntry("error_code", 400)
    builder.errorMessage("error")
    assertThat(builder.asParams()).containsEntry("error_message", "error")
    assertThat(builder.asParams()).hasSize(2)
  }

}