package com.shuashuakan.android.js

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.accessibility.AccessibilityManager
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import timber.log.Timber
import java.util.Locale

internal const val JS_RECEIVE_METHOD = "window.SSK.receive(%s)"
internal const val JS_CALLBACK_METHOD = "window.SSK.callback(%s)"
internal const val JS_METHOD = "javascript:%s;"

@TargetApi(VERSION_CODES.KITKAT)
internal fun WebView.callJsFunction(script: String) {
//  Timber.tag(RainbowBridge.TAG).d("callJsFunction $script")
  if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
    evaluateJavascript(script, null)
  } else {
    loadUrl(String.format(Locale.US, JS_METHOD, script))
  }
}

@SuppressLint("SetJavaScriptEnabled")
fun WebView.setupWithDefaultSettings() {
  setInitialScale(1)
  val ws = settings
  ws.loadWithOverviewMode = true
  ws.useWideViewPort = true
  ws.domStorageEnabled = true
  ws.cacheMode = WebSettings.LOAD_DEFAULT
  ws.setGeolocationEnabled(true)
  ws.setSupportZoom(false)
  ws.setAppCacheEnabled(true)

  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
    ws.useWideViewPort = true
  }

  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
    ws.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
  }
  enableJavaScriptForWebView(context, ws)
}

@SuppressLint("SetJavaScriptEnabled")
private fun enableJavaScriptForWebView(context: Context?, ws: WebSettings) {
  try {
    if (context != null) disableAccessibility(context)
    ws.javaScriptEnabled = true
  } catch (e: Exception) {
  }
}

fun WebView.appendUserAgents(userAgents: List<String>) {
  settings.userAgentString = buildString {
    settings.userAgentString?.let { append(it) }
    userAgents.forEach {
      append(' ')
      append(it)
    }
  }
  Timber.i("Now WebView UA: %s", settings.userAgentString)
}

@Suppress("DEPRECATION")
fun clearWebViewCache(context: Context) {
  val webStorage = WebStorage.getInstance()
  webStorage.deleteAllData()

  val syncManager = CookieSyncManager.createInstance(context)
  val cookieManager = CookieManager.getInstance()
  cookieManager.removeAllCookie()
  cookieManager.removeSessionCookie()
  syncManager.sync()

  val webView = WebView(context)
  webView.webChromeClient = null
  webView.webViewClient = null
  webView.settings.javaScriptEnabled = false
  webView.clearCache(true)
}

/**
 * m:lorss
 * 关闭辅助功能，针对4.2.1和4.2.2 崩溃问题
 * java.lang.NullPointerException
 * at android.webkit.AccessibilityInjector$TextToSpeechWrapper$1.onInit(AccessibilityInjector.java:753)
 * ... ...
 * at android.webkit.CallbackProxy.handleMessage(CallbackProxy.java:321)
 */
private fun disableAccessibility(context: Context) {
  if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR1) {
    try {
      val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
      if (!am.isEnabled) {
        //Not need to disable accessibility
        return
      }

      val setState = am.javaClass.getDeclaredMethod("setState", Int::class.javaPrimitiveType)
      setState.isAccessible = true
      setState.invoke(am, 0)
      /**[AccessibilityManager.STATE_FLAG_ACCESSIBILITY_ENABLED] */
    } catch (ignored: Exception) {
      ignored.printStackTrace()
    }

  }
}