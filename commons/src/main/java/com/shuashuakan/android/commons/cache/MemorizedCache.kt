package com.shuashuakan.android.commons.cache

import arrow.core.Option
import com.shuashuakan.android.commons.cache.DiskCache.Cache

class MemorizedCache<T>(private val memory: Cache<T>,
    private val disk: Cache<T>) : Cache<T> {

  @Synchronized
  override fun contains(key: String): Boolean {
    return memory.contains(key) || disk.contains(key)
  }

  @Synchronized
  override fun put(key: String, value: T): Boolean {
    memory.put(key, value)
    return disk.put(key, value)
  }

  @Synchronized
  override fun get(key: String): Option<T> {
    val memorized = memory.get(key)
    return if (memorized.isDefined()) {
      memorized
    } else {
      disk.get(key).map {
        memory.put(key, it)
        it
      }
    }
  }

  @Synchronized
  override fun remove(key: String): Boolean {
    memory.remove(key)
    return disk.remove(key)
  }

  companion object {
    fun <T : Any> wrap(typedCache: Cache<T>): Cache<T> {
      return MemorizedCache(MemoryCache.of(), typedCache)
    }
  }

  private class MemoryCache<T> private constructor(private var cache: T? = null) : Cache<T> {

    override fun contains(key: String): Boolean = cache != null

    override fun put(key: String, value: T): Boolean {
      this.cache = value
      return true
    }

    override fun get(key: String): Option<T> {
      return Option.fromNullable(cache)
    }

    override fun remove(key: String): Boolean {
      cache = null
      return true
    }

    companion object {
      fun <T> of(value: T? = null): MemoryCache<T> {
        return MemoryCache(value)
      }
    }
  }

}