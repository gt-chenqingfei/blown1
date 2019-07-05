package com.shuashuakan.android.js

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@Suppress("MemberVisibilityCanPrivate")
@JsonSerializable
data class Response(
    @Json(name = "scope") val methodScope: String,
    @Json(name = "method") val methodName: String,
    @Json(name = "id") val methodId: String,
    @Json(name = "code") val code: Int,
    @Json(name = "data") val responseParams: Params) {

  companion object {
    val SUCCESS_CODE = 200
    val METHOD_NOT_SUPPORTED_CODE = 404
    val BUSINESS_ERROR_CODE = 500
    val PARAMS_ERROR_CODE = 500001

    fun create(request: Request, code: Int, init: ParamsBuilder.() -> Unit): Response {
      val builder = ParamsBuilder()
      builder.init()
      return Response(methodId = request.methodId,
          methodName = request.methodName,
          methodScope = request.methodScope,
          code = code,
          responseParams = builder.asParams())
    }

    fun success(request: Request, init: ParamsBuilder.() -> Unit = {}): Response = create(request,
        SUCCESS_CODE, init)

    fun failed(request: Request, code: Int = BUSINESS_ERROR_CODE,
        init: ParamsBuilder.() -> Unit): Response = create(request,
        code, init)

    fun methodNotSupported(request: Request, message: String): Response {
      return failed(request, METHOD_NOT_SUPPORTED_CODE) {
        errorMessage(message)
      }
    }

    fun paramsError(request: Request, message: String, init: ParamsBuilder.() -> Unit = {}): Response = failed(request) {
      errorCode(PARAMS_ERROR_CODE)
      errorMessage(message)
      init()
    }
  }

}