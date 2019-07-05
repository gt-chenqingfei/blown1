package com.shuashuakan.android.commons.cache

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.content.edit
import com.shuashuakan.android.commons.cache.DiskCache.Cache
import com.shuashuakan.android.commons.di.AppContext
import com.squareup.moshi.Moshi
import timber.log.Timber
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Singleton


@Suppress("MemberVisibilityCanPrivate", "unused")
@Singleton
class Storage @Inject constructor(@AppContext context: Context) {
  private val converterFactory by lazy { MoshiValueConverter.Factory(Moshi.Builder().build()) }

  val appCache by lazy {
    DiskCache(context, DISK_CACHE_VERSION, APP_CACHE_NAME, converterFactory)
  }

  val userCache by lazy {
    DiskCache(context, DISK_CACHE_VERSION, USER_CACHE_NAME, converterFactory)
  }

  val userPreference by lazy {
    context.getSharedPreferences(context.packageName + ".user", MODE_PRIVATE)!!
  }

  val appPreference by lazy {
    context.getSharedPreferences(context.packageName + ".app", MODE_PRIVATE)!!
  }

  inline fun <reified T : Any> userCacheOf(): Cache<T> {
    return userCache.cacheOf()
  }

  inline fun <reified T : Any> appCacheOf(): Cache<T> {
    return appCache.cacheOf()
  }

  fun <T : Any> appCacheOf(type: Type): Cache<T> {
    return appCache.cacheOf(type)
  }

  fun <T : Any> userCacheOf(type: Type): Cache<T> {
    return userCache.cacheOf(type)
  }

  @Synchronized
  fun cleanUserStorage() {
    Timber.i("clean user storage")
    userCache.clean()
    userPreference.edit(commit = true) {
      clear()
    }
  }

  @Synchronized
  fun cleanAppStorage() {
    Timber.i("clean app storage")
    appCache.clean()
    appPreference.edit(commit = true) {
      clear()
    }
  }

  companion object {
    private const val APP_CACHE_NAME = "duck_disk.cache"
    private const val USER_CACHE_NAME = "user_disk.cache"
    private const val DISK_CACHE_VERSION = 1
  }
}