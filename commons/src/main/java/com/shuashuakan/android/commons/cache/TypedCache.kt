package com.shuashuakan.android.commons.cache

import arrow.core.Option
import arrow.data.Try
import arrow.data.getOrDefault
import arrow.data.getOrElse
import com.shuashuakan.android.commons.cache.DiskCache.Cache
import com.shuashuakan.android.commons.cache.DiskCache.ValueConverter
import com.shuashuakan.android.commons.util.md5
import okio.Buffer
import timber.log.Timber

class TypedCache<T>(private val diskCache: DiskCache,
    private val converter: ValueConverter<T>
) : Cache<T> {

  override fun contains(key: String): Boolean = Try {
    diskCache.containsKey(key.md5())
  }.getOrDefault { false }

  override fun remove(key: String): Boolean = Try {
    diskCache.removeByKey(key.md5())
  }.getOrElse { false }

  override fun put(key: String, value: T): Boolean {
    return try {
      val buf = Buffer()
      converter.to(buf, value)
      diskCache.put(key.md5(), buf)
      Timber.tag("DiskCache").d("put $key done")
      true
    } catch (e: Exception) {
      Timber.tag("DiskCache").w(e, "Can't save cache for given key: $key")
      false
    }
  }

  override fun get(key: String): Option<T> {
    return try {
      val buf = Buffer()
      diskCache.get(key.md5(), buf)
      Option.fromNullable(converter.from(buf))
    } catch (e: Exception) {
      Timber.tag("DiskCache").w(e, "Can't get cached value by key: $key")
      Option.empty()
    }
  }
}