package com.shuashuakan.android.network

import android.content.Context
import com.ishumei.smantifraud.SmAntiFraud
import com.shuashuakan.android.commons.di.AppContext
import com.shuashuakan.android.commons.util.md5
import com.shuashuakan.android.commons.util.urlDecode
import com.shuashuakan.android.commons.util.urlEncode
import com.shuashuakan.android.data.api.ApiDefaultConfig.Companion.CLIENT_SECRET
import com.shuashuakan.android.utils.throwOrLog
import okhttp3.*
import okhttp3.Interceptor.Chain
import okio.Buffer

class SigningInterceptor(@AppContext val context: Context) : Interceptor {
  override fun intercept(chain: Chain): Response {
    val request = chain.request()
    return when (request.method()) {
      "GET" -> chain.proceed(buildRequestSafely(request) { buildGetRequest(request) })
      "POST" -> chain.proceed(buildRequestSafely(request) { buildPostRequest(request) })
      else -> chain.proceed(request)
    }
  }

  private fun buildRequestSafely(request: Request, builder: (Request) -> Request): Request {
    try {
      return builder(request)
    } catch (e: Exception) {
      e.throwOrLog()
    }
    return request
  }

  private fun buildGetRequest(request: Request): Request {
    val append = appendParams()
    val url = request.url().newBuilder().apply {
      append.forEach { addQueryParameter(it.key, it.value) }
    }.build()
    val queryNameValues = url.queryParameterNames().fold(mutableMapOf<String, String>()) { acc, i ->
      url.queryParameter(i)?.let {
        acc.put(i, it)
      }
      acc
    }
    val sign = sign(queryNameValues)
    val newUrl = url.newBuilder().addQueryParameter(SIGN_KEY, sign).build()
    return request.newBuilder().url(newUrl).build()
  }

  private fun buildPostRequest(request: Request): Request {
    if (request.body() != null && shouldInterceptRequest(request.body()!!)) {
      //Timber.tag("HTTP").d("intercept POST")
      val buffer = Buffer()
      request.body()!!.writeTo(buffer)
      val params = parseAsMap(buffer.readUtf8())
      val nameAndValues = mutableMapOf<String, String>().apply {
        putAll(params)
        putAll(appendParams())
      }
      // put sign
      nameAndValues.put(SIGN_KEY, sign(nameAndValues))
      // rebuild the form body
      val form = FormBody.Builder().apply {
        nameAndValues.forEach { add(it.key, it.value) }
      }.build()
      return request.newBuilder().post(form).build()
    }
    return request
  }

  private fun appendParams(): Map<String, String> {
    return mapOf(
        "_time" to System.currentTimeMillis().toString(),
        "sm_id" to SmAntiFraud.getDeviceId()
    )
  }

  companion object {
    private val FORM_URL_ENCODED_TYPE = "application/x-www-form-urlencoded"
    private const val SIGN_KEY = "sign"
    const val SECRET_KEY = CLIENT_SECRET

    // parse post body as map: a=b&c=d into [(a, b), (c, d)]
    fun parseAsMap(body: String): Map<String, String> {
      return body.split('&').mapNotNull { parseNameAndValue(it) }.toMap()
    }

    fun sign(nameAndValues: Map<String, String>, secretKey: String = SECRET_KEY): String {
      val nameValueString = nameAndValues
          .filterValues { it.isNotEmpty() }
          .toSortedMap().map { it.key to it.value }.joinToString(
              separator = ",") {
            val encodedValue = it.second.urlEncode(true, true)
            "${it.first}=$encodedValue"
          }
      return "$secretKey$nameValueString$secretKey".md5()
    }

    private fun parseNameAndValue(s: String): Pair<String, String>? {
      val array = s.split('=')
      if (array.size == 2) {
        return array[0] to array[1].urlDecode()
      }
      return null
    }


    private fun shouldInterceptRequest(body: RequestBody): Boolean {
      return body.contentType()?.toString()?.contains(FORM_URL_ENCODED_TYPE) ?: false
    }
  }
}