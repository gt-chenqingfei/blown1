package com.shuashuakan.android.network

import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response

class AccessTokenInterceptor(private val accessTokenProvider: AccessTokenProvider) : Interceptor {

  override fun intercept(chain: Chain): Response {
    val token = accessTokenProvider.accessToken()
    val request = chain.request()
    return if (token != null) {
      val newRequest = request.newBuilder()
          .addHeader("Authorization", "OAuth2 $token")
          .build()
      chain.proceed(newRequest)
    } else {
      chain.proceed(request)
    }
  }

  interface AccessTokenProvider {
    fun accessToken(): String?
  }
}