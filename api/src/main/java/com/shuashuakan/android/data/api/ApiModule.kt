package com.shuashuakan.android.data.api

import android.content.Context
import com.shuashuakan.android.commons.di.AppContext
import com.shuashuakan.android.commons.util.ACache
import com.shuashuakan.android.commons.util.ACache.KEY_SWITCH_ENVIRONMENT
import com.shuashuakan.android.data.api.ApiDefaultConfig.Companion.DEFAULT_URL
import com.shuashuakan.android.data.api.ApiDefaultConfig.Companion.TEST_API
import com.shuashuakan.android.data.api.ApiDefaultConfig.Companion.TEST_UPLOAD_URL
import com.shuashuakan.android.data.api.ApiDefaultConfig.Companion.UPLOAD_URL
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.data.api.services.UploadImageService
import com.shuashuakan.android.data.api.services.UploadService
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton


const val DOMAIN = "domain"
const val COMMON = "common"
const val IMAGE = "image"

@Module
class ApiModule {

  @Module
  companion object {
    @JvmStatic
    @Singleton
    @Provides
    @Named(COMMON)
    fun provideRetrofit(@Named(COMMON) httpClient: OkHttpClient, moshi: Moshi,
                        @Named(COMMON) baseUrl: HttpUrl): Retrofit {
      return Retrofit.Builder()
          .baseUrl(baseUrl)
          .client(httpClient)
          .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
          .addConverterFactory(MoshiConverterFactory.create(moshi))
          .build()
    }

    @JvmStatic
    @Singleton
    @Provides
    @Named(DOMAIN)
    fun provideUploadRetrofit(
        @Named(DOMAIN) httpClient: OkHttpClient, moshi: Moshi,
        @Named(DOMAIN) uploadUrl: HttpUrl): Retrofit {
      return Retrofit.Builder()
          .baseUrl(uploadUrl)
          .client(httpClient)
          .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
          .addConverterFactory(MoshiConverterFactory.create(moshi))
          .build()
    }

    @JvmStatic
    @Singleton
    @Provides
    @Named(IMAGE)
    fun provideUploadImageRetrofit(
        @Named(IMAGE) httpClient: OkHttpClient, moshi: Moshi,
        @Named(IMAGE) uploadImageUrl: HttpUrl): Retrofit {
      return Retrofit.Builder()
          .baseUrl(uploadImageUrl)
          .client(httpClient)
          .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
          .addConverterFactory(MoshiConverterFactory.create(moshi))
          .build()
    }

    @JvmStatic
    @Singleton
    @Provides
    @Named(COMMON)
    fun provideBaseUrl(@AppContext context: Context): HttpUrl =
        HttpUrl.parse(if (BuildConfig.DEBUG)
          if (ACache.get(context).getAsString(KEY_SWITCH_ENVIRONMENT) == "release")
            DEFAULT_URL else TEST_API
        else DEFAULT_URL)!!


    @JvmStatic
    @Singleton
    @Provides
    @Named(DOMAIN)
    fun provideUploadUrl(@AppContext context: Context): HttpUrl =
        HttpUrl.parse(if (BuildConfig.DEBUG)
          if (ACache.get(context).getAsString(KEY_SWITCH_ENVIRONMENT) == "release")
            DEFAULT_URL else TEST_API
        else DEFAULT_URL)!!

    @JvmStatic
    @Singleton
    @Provides
    @Named(IMAGE)
    fun provideUploadImageUrl(@AppContext context: Context): HttpUrl =
        HttpUrl.parse(if (BuildConfig.DEBUG)
          if (ACache.get(context).getAsString(KEY_SWITCH_ENVIRONMENT) == "release")
            UPLOAD_URL else TEST_UPLOAD_URL
        else UPLOAD_URL)!!

    @JvmStatic
    @Singleton
    @Provides
    fun provideApiService(@Named(COMMON) retrofit: Retrofit): ApiService = retrofit.create(
        ApiService::class.java)

    @JvmStatic
    @Singleton
    @Provides
    fun provideUploadService(@Named(DOMAIN) retrofit: Retrofit): UploadService = retrofit.create(
        UploadService::class.java)

    @JvmStatic
    @Singleton
    @Provides
    fun provideUploadImageService(@Named(IMAGE) retrofit: Retrofit): UploadImageService = retrofit.create(
        UploadImageService::class.java)
  }
}