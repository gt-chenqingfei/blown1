package com.shuashuakan.android.di

import android.content.Context
import com.shuashuakan.android.BuildConfig
import com.shuashuakan.android.commons.di.AppContext
import com.shuashuakan.android.commons.di.AppInterceptor
import com.shuashuakan.android.commons.di.NetworkInterceptor
import com.shuashuakan.android.data.api.ApiModule
import com.shuashuakan.android.data.api.COMMON
import com.shuashuakan.android.data.api.DOMAIN
import com.shuashuakan.android.data.api.IMAGE
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.network.*
import com.shuashuakan.android.utils.DeviceUtils
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap
import dagger.multibindings.Multibinds
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import java.util.*
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Named
import javax.inject.Singleton

@Module(includes = [ApiModule::class])
abstract class NetworkModule {

  @Multibinds
  @NetworkInterceptor
  abstract fun bindNetworkInterceptors(): Map<Int, Interceptor>

  @Multibinds
  @AppInterceptor
  abstract fun bindAppInterceptors(): Map<Int, Interceptor>

  @Module
  companion object {
    private const val HTTP_RESPONSE_CACHE = (20 * 1024 * 1024).toLong() // 20M
    private const val HTTP_TIMEOUT_S = 60L // 60 seconds


    @Provides
    @Singleton
    @JvmStatic
    @IntoMap
    @IntKey(1) // order
    @NetworkInterceptor
    fun provideUserAgentInterceptor(): Interceptor = UserAgentInterceptor(BuildConfig.APP_UA,
        BuildConfig.VERSION_NAME)

    @Provides
    @Singleton
    @JvmStatic
    @IntoMap
    @IntKey(2) // order
    @NetworkInterceptor
    fun provideAccessTokenInterceptor(
        accountManager: AccountManager): Interceptor = AccessTokenInterceptor(accountManager)


    @Provides
    @Singleton
    @JvmStatic
    @IntoMap
    @IntKey(3) // order
    @NetworkInterceptor
    fun provideDeviceIdInterceptor(
      @AppContext context: Context, deviceUtils: DeviceUtils): Interceptor  =
      DeviceIdInterceptor(context, deviceUtils)


    @Provides
    @Singleton
    @JvmStatic
    @IntoMap
    @IntKey(4) // order
    @AppInterceptor
    fun provideSigningInterceptor(@AppContext context: Context): Interceptor = SigningInterceptor(context)

    @JvmStatic
    @Provides
    @Singleton
    @Named(COMMON)
    fun provideOkHttpClient(
        @NetworkInterceptor networkInterceptors: Map<Int, @JvmSuppressWildcards Interceptor>,
        @AppInterceptor appInterceptors: Map<Int, @JvmSuppressWildcards Interceptor>,
        @AppContext context: Context): OkHttpClient {
      val builder = OkHttpClient.Builder()
      with(builder) {
        // interceptors
        networkInterceptors().addAll(
            networkInterceptors.map { it.key to it.value }.sortedBy { it.first }.map { it.second })
        interceptors().addAll(
            appInterceptors.map { it.key to it.value }.sortedBy { it.first }.map { it.second })
        // cache
        cache(Cache(context.cacheDir, HTTP_RESPONSE_CACHE))
        retryOnConnectionFailure(true)
        // time specific
        readTimeout(HTTP_TIMEOUT_S, SECONDS)
        writeTimeout(HTTP_TIMEOUT_S, SECONDS)
        connectTimeout(HTTP_TIMEOUT_S, SECONDS)
      }
      return builder.build()
    }

    @JvmStatic
    @Provides
    @Singleton
    @Named(DOMAIN)
    fun provideDomainOkHttpClient(
        @NetworkInterceptor networkInterceptors: Map<Int, @JvmSuppressWildcards Interceptor>,
        @AppInterceptor appInterceptors: Map<Int, @JvmSuppressWildcards Interceptor>,
        @AppContext context: Context): OkHttpClient {
      val builder = OkHttpClient.Builder()
      with(builder) {
        cache(Cache(context.cacheDir, HTTP_RESPONSE_CACHE))

        networkInterceptors().addAll(
            networkInterceptors.map { it.key to it.value }.sortedBy { it.first }.map { it.second })
        interceptors().addAll(
            appInterceptors.map { it.key to it.value }.sortedBy { it.first }.map { it.second })

        protocols(Collections.singletonList(Protocol.HTTP_1_1))
        interceptors().add(GetVideoInterceptor())
        retryOnConnectionFailure(true)
        readTimeout(HTTP_TIMEOUT_S, SECONDS)
        writeTimeout(HTTP_TIMEOUT_S, SECONDS)
        connectTimeout(HTTP_TIMEOUT_S, SECONDS)
      }
      return builder.build()
    }

    @JvmStatic
    @Provides
    @Singleton
    @Named(IMAGE)
    fun provideImageOkHttpClient(
        @NetworkInterceptor networkInterceptors: Map<Int, @JvmSuppressWildcards Interceptor>,
        @AppInterceptor appInterceptors: Map<Int, @JvmSuppressWildcards Interceptor>,
        @AppContext context: Context): OkHttpClient {
      val builder = OkHttpClient.Builder()
      with(builder) {
        cache(Cache(context.cacheDir, HTTP_RESPONSE_CACHE))

        networkInterceptors().addAll(
            networkInterceptors.map { it.key to it.value }.sortedBy { it.first }.map { it.second })
        interceptors().addAll(
            appInterceptors.map { it.key to it.value }.sortedBy { it.first }.map { it.second })

        protocols(Collections.singletonList(Protocol.HTTP_1_1))
        interceptors().add(GetVideoInterceptor())
        retryOnConnectionFailure(true)
        readTimeout(HTTP_TIMEOUT_S, SECONDS)
        writeTimeout(HTTP_TIMEOUT_S, SECONDS)
        connectTimeout(HTTP_TIMEOUT_S, SECONDS)
      }
      return builder.build()
    }
  }
}