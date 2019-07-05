package com.shuashuakan.android.js

import java.util.Locale


internal class MethodNotSupportedProcessor(
    private val clientVersionName: String) : MethodProcessor {

  override fun process(request: Request): Response {
    val errorMessage = String.format(Locale.US, ERROR_MESSAGE, request.methodScope,
        request.methodName,
        clientVersionName)

    return Response.methodNotSupported(request, errorMessage)
  }

  override fun canHandleRequest(request: Request): Boolean = true

  companion object {
    private val ERROR_MESSAGE = "%s#%s is not supported(%s, Android) "
  }
}