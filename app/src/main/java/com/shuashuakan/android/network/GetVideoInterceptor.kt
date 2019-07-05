package com.shuashuakan.android.network

import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response

class GetVideoInterceptor :
    Interceptor {

  override fun intercept(chain: Chain): Response {
    val request = chain.request()
    val newRequest = request.newBuilder()
        .addHeader("Referer", "mts.shuashuakan.net")
        .build()
    return chain.proceed(newRequest)
  }
}