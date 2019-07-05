package com.shuashuakan.android.network

import com.shuashuakan.android.data.api.ApiDefaultConfig.Companion.UPLOAD_URL
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response

class HostSelectionInterceptor(private val hostProvider: HostProvider,
  private val baseUrl: HttpUrl) : Interceptor {

  override fun intercept(chain: Chain): Response {
    var host = hostProvider.get()
    if (host == null) host = baseUrl.toString()
    val request = chain.request()
    if (request.url().toString().contains(UPLOAD_URL)) {
      return chain.proceed(request)
    }
    val httpUrl = HttpUrl.parse(host) ?:throw IllegalArgumentException("http url is illegal")
    val newHttpUrl = request.url()
        .newBuilder()
        .scheme(httpUrl.scheme())
        .host(httpUrl.host())
        .build()
    val newRequest = request.newBuilder()
        .url(newHttpUrl)
        .build()
    return chain.proceed(newRequest)
  }

  interface HostProvider {
    fun get(): String?
  }
}