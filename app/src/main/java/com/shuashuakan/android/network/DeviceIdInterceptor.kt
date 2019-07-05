package com.shuashuakan.android.network

import android.content.Context
import android.util.Log
import com.shuashuakan.android.BuildConfig
import com.shuashuakan.android.commons.di.AppContext
import com.shuashuakan.android.utils.DeviceUtils
import com.shuashuakan.android.utils.channelName
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response
import timber.log.Timber

class DeviceIdInterceptor(@AppContext val context: Context, private val deviceUtils: DeviceUtils) :
    Interceptor {

  override fun intercept(chain: Chain): Response {
    val request = chain.request()
    val newRequest = request.newBuilder()
        .addHeader("x-ssk-version", "${BuildConfig.APP_UA}/${BuildConfig.VERSION_NAME}")
        .addHeader("x-ssk-imei", deviceUtils.getDeviceId())
        .addHeader("x-ssk-channel", context.channelName())
        .build()
    return chain.proceed(newRequest)
  }
}