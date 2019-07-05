package com.shuashuakan.android.modules.web.method

import com.shuashuakan.android.js.MethodProcessor
import com.shuashuakan.android.js.Request
import com.shuashuakan.android.js.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StateMethodProcessor @Inject constructor() : MethodProcessor {
  override fun canHandleRequest(request: Request): Boolean {
    return request.methodScope == MethodScope.APP_STATE.scope
  }

  override fun process(request: Request): Response {
    return when (request.methodName) {
      "fetchCurrentLocation" -> buildResponse(request)
      else -> Response.methodNotSupported(request, "${request.methodName} not defined")
    }
  }

  private fun buildResponse(request: Request): Response {
//    val location = locationController.getLocation()
//    return if (location.isValid()) {
//      Response.success(request) {
//        put("latitude", location.latitude)
//        put("longitude", location.longitude)
//      }
//    } else {
//      Response.failed(request) {
//        errorMessage("can't find location")
//      }
//    }
    return Response.success(request) {}
  }
}