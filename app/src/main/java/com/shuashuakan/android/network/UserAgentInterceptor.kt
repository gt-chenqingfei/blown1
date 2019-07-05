package com.shuashuakan.android.network

import android.os.Build
import android.os.Build.VERSION
import arrow.data.Try
import arrow.data.getOrDefault
import com.shuashuakan.android.commons.util.valueOrDefault
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response
import okio.Buffer


class UserAgentInterceptor(private val appName: String,
    private val versionName: String) : Interceptor {

  override fun intercept(chain: Chain): Response {
    val request = chain.request()
    val originAgent = request.header("User-Agent").valueOrDefault("")
    val newAgent = makeUserAgent(originAgent)
//    Timber.d("ua:$newAgent")
    val newRequest = request.newBuilder().header("User-Agent",
        toHumanReadableAscii(newAgent)).build()
    return chain.proceed(newRequest)
  }

  private fun makeUserAgent(originAgent: String): String {
    val appInfo = "$appName/$versionName"
    val vmInfo = Try { vmInfo() }.getOrDefault { "" }
    val deviceInfo = Try { deviceInfo() }.getOrDefault { "" }
    return "$appInfo $vmInfo $deviceInfo $originAgent"
  }

  private fun vmInfo(): String {
    val version = System.getProperty("java.vm.version") ?: return ""
    return if (version.startsWith("2.")) "ART/$version" else "Dalvik/$version"
  }

  private fun deviceInfo(): String {
    return buildString {
      append('(')
      append("Android ${VERSION.RELEASE.valueOrDefault("1.0")};") // system version: android 5.0
      append(' ')
      append("${Build.BRAND} ${Build.MODEL}") // brand: google pixel2
      append(')')
    }
  }

  private fun toHumanReadableAscii(s: String): String {
    var i = 0
    var c: Int
    val length = s.length
    while (i < length) {
      c = s.codePointAt(i)
      if (c <= 31 || c >= 127) {
        val buffer = Buffer()
        buffer.writeUtf8(s, 0, i)
        var j = i
        while (j < length) {
          c = s.codePointAt(j)
          buffer.writeUtf8CodePoint(if (c in 32..126) c else 63)
          j += Character.charCount(c)
        }
        return buffer.readUtf8()
      }
      i += Character.charCount(c)
    }
    return s
  }

}