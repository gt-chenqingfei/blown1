package com.shuashuakan.android.modules.web.method

import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.js.MethodProcessor
import com.shuashuakan.android.js.Request
import com.shuashuakan.android.js.Response
import com.shuashuakan.android.network.SigningInterceptor
import com.shuashuakan.android.modules.web.method.ViewMethodProcessor.ViewController
import timber.log.Timber
import javax.inject.Inject

class UserMethodProcessor @Inject constructor(
    private val viewController: ViewController,
    private val accountManager: AccountManager
) : MethodProcessor {

  override fun process(request: Request): Response {
    return when (request.methodName) {
      "login" -> login(request)
      "fetchToken" -> fetchToken(request)
      "fetchSecret" -> fetchSecret(request)
      "didSelectAddress" -> fetchSelectAddress(request)
      else -> Response.methodNotSupported(request, "${request.methodName} not defined")
    }
  }

  private fun fetchSelectAddress(request: Request): Response {
    return Response.success(request)
  }



  private fun login(request: Request): Response {
    Timber.d("request login")
    viewController.openPage("ssr://oauth2/login")
    return Response.success(request)
  }

  private fun fetchToken(request: Request): Response {
    val account = accountManager.account()
    return if (account != null) Response.success(request) {
      put("token", account.accessToken ?: "")
    } else Response.failed(request) {
      errorMessage("no user")
    }
  }

  private fun fetchSecret(request: Request): Response {
    return Response.success(request) {
      put("secret", SigningInterceptor.SECRET_KEY)
    }
  }

  override fun canHandleRequest(
      request: Request): Boolean = request.methodScope == MethodScope.USER.scope

}