package com.dkhs.android.data.json

import com.shuashuakan.android.data.json.FilterNullAdapterFactory
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.junit.Test

class FilterNullAdapterTest {

  @Test
  fun filterNullElement() {
    val json = """["a", "b", null]"""
    val moshi = Moshi.Builder().add(FilterNullAdapterFactory).build()
    val adapter: JsonAdapter<List<String>> = moshi.adapter(
        Types.newParameterizedType(List::class.java, String::class.java))

    val list = adapter.fromJson(json)
    assertThat(list).isNotNull()
    assertThat(list!!.size).isEqualTo(2)
    assertThat(list).contains("a")
    assertThat(list).contains("b")
  }
}