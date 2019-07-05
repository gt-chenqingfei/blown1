package com.shuashuakan.android.data.json

import com.shuashuakan.android.utils.throwOrLog
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


private class FilterNullAdapter<T>(private val delegate: JsonAdapter<T>) : JsonAdapter<List<T>>() {

  override fun fromJson(reader: JsonReader): List<T> {
    reader.beginArray()
    val list = mutableListOf<T>()
    while (reader.hasNext()) {
      try {
        delegate.fromJson(reader)?.let { list.add(it) }
      } catch (e: JsonDataException) {
        e.throwOrLog()
      }
    }
    reader.endArray()
    return list.filter { it != null }.toList()
  }

  override fun toJson(writer: JsonWriter, list: List<T>?) {
    writer.beginArray()
    list?.forEach {
      delegate.toJson(writer, it)
    }
    writer.endArray()
  }

}

object FilterNullAdapterFactory : JsonAdapter.Factory {
  override fun create(type: Type, annotations: MutableSet<out Annotation>,
      moshi: Moshi): JsonAdapter<*>? {
    if (type is ParameterizedType && type.rawType == java.util.List::class.java) {
      val elementType = type.actualTypeArguments[0]
      val elementAdapter: JsonAdapter<Any> = moshi.adapter(elementType)
      return FilterNullAdapter(elementAdapter).nullSafe()
    }
    return null
  }

}