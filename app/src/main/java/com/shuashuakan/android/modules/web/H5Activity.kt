package com.shuashuakan.android.modules.web

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.shuashuakan.android.ui.base.FishActivity
import com.sensorsdata.analytics.android.sdk.ScreenAutoTracker
import org.json.JSONObject

class H5Activity : FishActivity(), ScreenAutoTracker {

  override fun getTrackProperties(): JSONObject? {
    return null
  }

  override fun getScreenUrl(): String {
    return httpUrl ?: "http url is null"
  }

  companion object {
    private const val HTTP_URL = "extra_http_url"
    fun intent(
        context: Context,
        url: String
    ): Intent {
      return Intent(context, H5Activity::class.java).apply {
        putExtra(HTTP_URL, url)
      }
    }
  }

  private var httpUrl: String? = null
//  @Inject
//  lateinit var spider: Spider

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    spider.pageTracer().reportPageCreated(this)
    httpUrl = intent.getStringExtra(HTTP_URL)
    if (httpUrl.isNullOrBlank()) finish() else {
      supportFragmentManager.beginTransaction()
          .replace(android.R.id.content, H5Fragment.create(true, httpUrl!!)).commit()
    }
  }

  override fun onResume() {
    super.onResume()
    if (httpUrl != null)
      spider.pageTracer().reportPageShown(this, httpUrl, "")
  }

  override fun onActivityResult(
      requestCode: Int,
      resultCode: Int,
      data: Intent?
  ) {
    super.onActivityResult(requestCode, resultCode, data)
    val fragment = supportFragmentManager.findFragmentById(android.R.id.content)
    fragment.onActivityResult(requestCode, resultCode, data)
  }
}