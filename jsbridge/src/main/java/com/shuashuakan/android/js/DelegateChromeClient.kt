package com.shuashuakan.android.js

import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.annotation.RequiresApi
import android.webkit.JsPromptResult
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast
import timber.log.Timber

class DelegateChromeClient(private val delegate: WebChromeClient?,
                           private val callback: (String) -> Boolean) : WebChromeClient() {

  override fun onJsPrompt(view: WebView?, url: String?, message: String?, defaultValue: String?,
                          result: JsPromptResult?): Boolean {
    Timber.tag("JS").d("message: $message")

    return if (message != null) {
      if (callback(message)) {
        result?.cancel()
        true
      } else {
        broadcast(view, url, message, defaultValue, result)
      }
    } else {
      broadcast(view, url, message, defaultValue, result)
    }
  }

  override fun onProgressChanged(view: WebView?, newProgress: Int) {
    if (delegate != null) {
      delegate.onProgressChanged(view, newProgress)
    } else {
      super.onProgressChanged(view, newProgress)
    }
  }

  override fun onReceivedTitle(view: WebView?, title: String?) {
    if (delegate != null) {
      delegate.onReceivedTitle(view, title)
    } else {
      super.onReceivedTitle(view, title)
    }
  }

  private fun broadcast(view: WebView?, url: String?, message: String?, defaultValue: String?,
                        result: JsPromptResult?): Boolean {
    if (delegate != null) {
      return delegate.onJsPrompt(view, url, message, defaultValue, result)
    }
    return super.onJsPrompt(view, url, message, defaultValue, result)
  }

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
  override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {
    if (delegate != null) {
      return delegate.onShowFileChooser(webView, filePathCallback, fileChooserParams)
    }
    return super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
  }
}