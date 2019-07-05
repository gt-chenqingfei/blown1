package com.dkhs.android.utils

import com.shuashuakan.android.commons.util.urlEncode
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UtilsTest {
  @Test
  fun urlEncode() {
    val s = "hello world+123"
    assertThat(s.urlEncode(spaceCharToPercentEncode = false, keepPlusChar = false)).isEqualTo(
        "hello+world%2B123")
    assertThat(s.urlEncode(spaceCharToPercentEncode = false, keepPlusChar = true)).isEqualTo(
        "hello+world+123")
    assertThat(s.urlEncode(spaceCharToPercentEncode = true, keepPlusChar = false)).isEqualTo(
        "hello%20world%2B123")
    assertThat(s.urlEncode(spaceCharToPercentEncode = true, keepPlusChar = true)).isEqualTo(
        "hello%20world+123")
  }
}