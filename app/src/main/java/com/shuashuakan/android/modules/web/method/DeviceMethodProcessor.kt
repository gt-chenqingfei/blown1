package com.shuashuakan.android.modules.web.method

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.TYPE_WIFI
import android.os.Build
import com.ishumei.smantifraud.SmAntiFraud
import com.shuashuakan.android.BuildConfig
import com.shuashuakan.android.commons.di.AppContext
import com.shuashuakan.android.js.MethodProcessor
import com.shuashuakan.android.js.Request
import com.shuashuakan.android.js.Response
import com.shuashuakan.android.modules.web.method.ViewMethodProcessor.ViewController
import javax.inject.Inject

class DeviceMethodProcessor @Inject constructor(@AppContext private val context: Context,
  private val viewController: ViewController) : MethodProcessor {

  override fun process(request: Request): Response {
    return when (request.methodName) {
      "fetchNetworkStatus" -> networkResponse(request)
      "fetchDeviceInfo" -> deviceInfoResponse(request)
      "playVibrate" -> vibrateResponse(request)
      else -> Response.methodNotSupported(request, "${request.methodName} not defined")
    }
  }

  private fun deviceInfoResponse(request: Request): Response {
    return Response.success(request) {
      put("platform", "Android")
      put("os_version", Build.VERSION.RELEASE)
      put("client_version", BuildConfig.VERSION_NAME)
      put("uuid", SmAntiFraud.getDeviceId())
      put("fetchSmId",SmAntiFraud.getDeviceId())
    }
  }

  private fun vibrateResponse(request: Request): Response {
    viewController.vibrate()
    return Response.success(request)
  }

  private fun networkResponse(request: Request): Response {
    val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkAvailable = manager.activeNetworkInfo?.isConnectedOrConnecting ?: false
    val wifi = (manager.activeNetworkInfo?.type ?: -1) == TYPE_WIFI
    val status = if (networkAvailable) if (wifi) "wifi" else "noWifi" else "disconnect"
    return Response.success(request) {
      put("status", status)
    }
  }

  override fun canHandleRequest(
      request: Request): Boolean = request.methodScope == MethodScope.DEVICE.scope

}