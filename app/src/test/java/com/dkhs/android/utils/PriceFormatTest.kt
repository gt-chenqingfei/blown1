package com.dkhs.android.utils

import com.shuashuakan.android.utils.formatPrice
import org.junit.Test
import org.junit.Assert.*

class PriceFormatTest {

  @Test
  fun testFormat() {
    assertEquals("12.34", formatPrice("12.34"))
    assertEquals("12",  formatPrice("12.00"))
    assertEquals("12.1", formatPrice("12.10"))
    assertEquals("12", formatPrice( "12.0"))
  }
}