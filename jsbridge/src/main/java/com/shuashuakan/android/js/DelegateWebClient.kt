package com.shuashuakan.android.js

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient


class DelegateWebClient(private val delegate: WebViewClient?) : WebViewClient() {
  override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
    delegate?.onPageStarted(view, url, favicon)
  }

  override fun onPageFinished(view: WebView?, url: String?) {
    delegate?.onPageFinished(view, url)
  }

  override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
    if (delegate != null) return delegate.shouldOverrideUrlLoading(view, url)
    return super.shouldOverrideUrlLoading(view, url)
  }
}