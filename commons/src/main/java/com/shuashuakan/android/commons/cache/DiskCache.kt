package com.shuashuakan.android.commons.cache

import android.content.Context
import android.support.v4.util.LruCache
import arrow.core.Option
import com.jakewharton.disklrucache.DiskLruCache
import okio.BufferedSink
import okio.BufferedSource
import okio.Okio
import okio.Source
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.lang.reflect.Type

class DiskCache(context: Context, val version: Int,
    private val cacheName: String,
    private val converterFactory: ValueConverter.Factory) {

  companion object {
    private const val INDEX = 0
    private const val VALUE_COUNT = 1
    private const val SIZE = 100 * 1024 * 1024L
  }

  private val lock = java.lang.Object()
  private val converterCache: LruCache<Type, ValueConverter<*>> = LruCache(8)

  private val cache by lazy {
    DiskLruCache.open(File(context.cacheDir, cacheName), version,
        VALUE_COUNT, SIZE)
  }

  inline fun <reified T : Any> cacheOf(): Cache<T> {
    return cacheOf(T::class.java)
  }

  fun <T : Any> cacheOf(type: Type): Cache<T> {
    return TypedCache(this, getOrCreate(type))
  }

  private fun <T : Any> getOrCreate(type: Type): ValueConverter<T> {
    val cached = converterCache.get(type)
    if (cached != null) {
      @Suppress("UNCHECKED_CAST")
      return cached as ValueConverter<T>
    } else {
      val created: ValueConverter<T> = converterFactory.create(type)
      converterCache.put(type, created)
      return created
    }
  }

  internal fun containsKey(key: String): Boolean {
    return cache.get(key) != null
  }

  @Throws(IOException::class)
  internal fun put(key: String, source: Source) {
    synchronized(lock) {
      val editor = cache.edit(key)
      Okio.buffer(Okio.sink(editor.newOutputStream(INDEX))).use {
        it.writeAll(source)
        editor.commit()
      }
    }
  }

  @Throws(IOException::class)
  internal fun get(key: String, buf: BufferedSink) {
    synchronized(lock) {
      if (containsKey(key)) {
        cache.get(key).getInputStream(INDEX).use {
          buf.writeAll(Okio.source(it))
        }
      }
    }
  }

  internal fun removeByKey(key: String): Boolean = cache.remove(key)

  fun clean() {
    try {
      cache.delete()
    } catch (e: Exception) {
      Timber.e(e)
    }
  }

  interface ValueConverter<T> {
    fun to(sink: BufferedSink, value: T)

    fun from(source: BufferedSource): T?

    interface Factory {
      fun <T> create(type: Type): ValueConverter<T>
    }
  }

  interface Cache<T> {
    fun contains(key: String): Boolean
    fun put(key: String, value: T): Boolean
    fun get(key: String): Option<T>
    fun remove(key: String): Boolean
  }
}
