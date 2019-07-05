package com.shuashuakan.android.commons.cache

import com.shuashuakan.android.commons.cache.DiskCache.ValueConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okio.BufferedSink
import okio.BufferedSource
import java.lang.reflect.Type

class MoshiValueConverter<T>(private val adapter: JsonAdapter<T>) : ValueConverter<T> {

  override fun to(sink: BufferedSink, value: T) {
    adapter.toJson(sink, value)
  }

  override fun from(
      source: BufferedSource): T? = if (source.exhausted()) null else adapter.fromJson(source)

  class Factory(private val moshi: Moshi) : ValueConverter.Factory {

    override fun <T> create(type: Type): ValueConverter<T> = MoshiValueConverter(
        moshi.adapter(type))
  }

}
