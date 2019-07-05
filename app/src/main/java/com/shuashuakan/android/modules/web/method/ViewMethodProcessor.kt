package com.shuashuakan.android.modules.web.method

import android.content.ClipboardManager
import android.content.Context
import com.shuashuakan.android.commons.di.AppContext
import com.shuashuakan.android.data.api.model.detail.ShareContent
import com.shuashuakan.android.js.*
import com.shuashuakan.android.utils.throwOrLog
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import me.twocities.linker.LinkResolver
import javax.inject.Inject


class ViewMethodProcessor @Inject constructor(
    @AppContext private val context: Context,
    private val moshi: Moshi,
    private val linkResolver: LinkResolver,
    private val viewController: ViewController
) : MethodProcessor {
  override fun canHandleRequest(
      request: Request): Boolean = request.methodScope == MethodScope.VIEW.scope


  override fun process(request: Request): Response {
    return when (request.methodName) {
      "openPage" -> {
        val url = request.params?.getString("url")
        url?.let { viewController.openPage(url) }
        successResponse(request)
      }
      "closePage" ->{
        viewController.closePage()
        successResponse(request)
      }
      "showToast" -> {
        val toast = request.params?.getString("text")
        toast?.let { viewController.showToast(it) }
        successResponse(request)
      }
      "showLoadingBar" -> {
        viewController.showLoadingBar()
        successResponse(request)
      }
      "dismissLoadingBar" -> {
        viewController.dismissLoadingBar()
        successResponse(request)
      }
      "dismissKeyboard" -> {
        viewController.dismissKeyboard()
        successResponse(request)
      }
      "setPageTitle" -> {
        val title = request.params?.getString("title")
        title?.let { viewController.setToolbarTitle(it) }
        successResponse(request)
      }
      "makeCall" -> {
        makeCall(request)
        successResponse(request)
      }
      "share" -> {
        share(request)
        successResponse(request)
      }
      "copy" -> {
        val clip = request.params?.getString("text")
        clip?.let { viewController.setClipBoard(it) }
        successResponse(request)
      }
      "paste" -> {
        val clipboardManager=
          context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboardManager.primaryClip
        val clipDataItem = clipData.getItemAt(0)
        val text = clipDataItem.text.toString()
        Response.success(request) {
          put("text", text)
        }
      }
      "inviteBySms" -> {
        viewController.inviteBySms()
        successResponse(request)
      }
      else -> Response.methodNotSupported(request, "${request.methodName} not defined")
    }
  }

  private fun makeCall(request: Request) {
    val numbers = request.params?.getString("number")
    numbers?.let {
      try {
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter: JsonAdapter<List<String>> = moshi.adapter(type)
        viewController.makeCall(adapter.fromJson(it) ?: listOf())
      } catch (e: Exception) {
        e.throwOrLog()
      }
    }
  }

  private fun share(request: Request) {
    try {
      val adapter = moshi.adapter(ShareContent::class.java)
      val json = request.params?.asJson()
      val content = json?.let { adapter.fromJson(it) }
      content?.let { viewController.share(it) }
    } catch (e: Exception) {
      e.throwOrLog()
    }
  }

  private fun successResponse(request: Request): Response = Response.success(request)


  interface ViewController {
    fun openPage(url: String)

    fun closePage()

    fun setToolbarTitle(title: String)

    fun showLoadingBar()

    fun dismissLoadingBar()

    fun makeCall(tels: List<String>)

    fun dismissKeyboard()

    fun showToast(message: String)

    fun share(shareContent: ShareContent)

    fun setClipBoard(text: String)

    fun vibrate()

    fun inviteBySms()

  }

}