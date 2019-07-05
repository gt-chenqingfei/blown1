package com.shuashuakan.android.network

import arrow.core.Option
import com.shuashuakan.android.commons.cache.DiskCache.Cache
import com.shuashuakan.android.commons.cache.MemorizedCache
import com.shuashuakan.android.commons.cache.Storage
import com.shuashuakan.android.network.HostSelectionInterceptor.HostProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HostProviderManager @Inject constructor(val storage: Storage) : HostProvider {

  companion object {
    const val CACHE_HOST = "cache_host"
  }

  private val hostCache: Cache<Map<String, Boolean>> by lazy {
    MemorizedCache.wrap(storage.appCache.cacheOf<Map<String, Boolean>>())
  }

  fun cacheHost(hosts: List<String>?) {
    val map = mutableMapOf<String, Boolean>()
    hosts?.forEach {
      map[it] = true
    }
    hostCache.put(CACHE_HOST, map)
  }

  private fun getCache(): Option<Map<String, Boolean>> {
    return hostCache.get(CACHE_HOST)
  }

  override fun get(): String? {
    return hostCache.get(CACHE_HOST)
        .orNull()
        ?.filterValues { it }
        ?.keys?.toList()
        ?.getOrNull(0)?: return null
  }

  fun updateHostState() {
    val currentHostName = get() ?: return
    getCache().orNull()
        ?.let {
          val newMap = it.toMutableMap()
          newMap[currentHostName] = false
          hostCache.put(CACHE_HOST, newMap)
        }
  }
}