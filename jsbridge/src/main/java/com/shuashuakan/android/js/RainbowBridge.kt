package com.shuashuakan.android.js

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import timber.log.Timber
import java.util.Locale


class RainbowBridge private constructor(private val webView: WebView,
    private val processors: List<MethodProcessor>,
    private val webViewClient: WebViewClient?,
    private val chromeClient: WebChromeClient?) {
  private var debuggable = false
  private val mainHandler = Handler(Looper.getMainLooper())

  fun setup() {
    webView.webViewClient = DelegateWebClient(webViewClient)
    webView.webChromeClient = DelegateChromeClient(chromeClient) { handleRawMessageFromJS(it) }
    webView.setupWithDefaultSettings()
    Timber.i("RainbowBridge start working...")
  }

  fun enableDebug(enable: Boolean) {
    debuggable = enable
    updateDebugSettings()
  }

  private fun updateDebugSettings() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      WebView.setWebContentsDebuggingEnabled(debuggable)
    }
  }

  /**
   * 处理 JS 通过 `jsPrompt` 发送过来的数据
   */
  private fun handleRawMessageFromJS(message: String): Boolean {
    if (message.startsWith("{") &&
        message.endsWith("}") &&
        message.contains("scope") &&
        message.contains("method")) {
      try {
        val request = createRequest(message)
        if (request != null) onJsCalled(request)
        return true
      } catch (e: Exception) {
        Timber.tag(TAG).e(e)
      }
    }
    return false
  }

  /**
   * 向 JS 端发送消息
   */
  fun postMessageToJs(message: JsMessage) {
    mainHandler.post {
      try {
        val javaScript = String.format(Locale.US, JS_RECEIVE_METHOD, message.toJson())
        webView.callJsFunction(javaScript)
      } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Cant't post message to JS")
      }
    }
  }

  private fun onJsCalled(request: Request) {
//    Timber.tag(TAG).d("receive request: $request")
    mainHandler.post {
      try {
        processJsRequest(request)
      } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Can't process js request")
      }
    }
  }

  private fun processJsRequest(request: Request) {
    val processor = processors.find { it.canHandleRequest(request) }
    // since we have `MethodNotSupportedProcessor` installed, processor can't be null
    requireNotNull(processor, { "Unexpected state: processor == null" })
    // before process
    processor!!.beforeResponseProceed(request)
    // request processing
    val response = processor.process(request)
    // send response
    val arguments = response.toJson()
    webView.callJsFunction(String.format(Locale.US, JS_CALLBACK_METHOD, arguments))
    // after process
    processor.afterResponseProceed(request, response)
  }

  companion object {
    internal const val TAG = "RainbowBridge"
    fun create(webView: WebView, init: Builder.() -> Unit): RainbowBridge {

      val builder = Builder(webView)
      builder.init()
      require(builder.version != null) { "Missing: version" }
      val processors = builder.methodProcessors
          .toMutableList()
          .apply {
            add(MethodNotSupportedProcessor(builder.version!!))
          }.toList()
      return RainbowBridge(builder.webView, processors.toList(), builder.webViewClient,
          builder.chromeClient)
    }
  }

  class Builder(val webView: WebView) {
    var version: String? = null
    var webViewClient: WebViewClient? = null
    var chromeClient: WebChromeClient? = null
    var methodProcessors: List<MethodProcessor> = listOf()
  }

}