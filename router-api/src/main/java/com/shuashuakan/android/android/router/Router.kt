@file:Suppress("unused")

package com.shuashuakan.android.android.router

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import me.twocities.linker.LinkResolver

interface Router : LinkResolver {

  fun startActivity(context: Context, link: String?, block: Intent.() -> Unit = {})

  fun startActivityForResult(context: Activity,
      link: String?, requestCode: Int, block: Intent.() -> Unit = {})

  companion object {
    const val REFERRER = "com.andorid.ssk.android.referrer"
    private const val ROUTER_SERVICE_NAME = "com.android.ssk.android.service.ROUTER"
    private var INSTANCE: Router? = null

    fun get(context: Context): Router =
        INSTANCE ?: synchronized(this) {
          INSTANCE
              ?: createRouter(
                  context
              ).also { INSTANCE = it }
        }

    @SuppressLint("WrongConstant")
    private fun createRouter(context: Context): Router {
      val resolver = context.applicationContext.getSystemService(
          ROUTER_SERVICE_NAME
      )
      return RouterImpl(resolver as LinkResolver)
    }

    fun isRouterService(name: String?): Boolean = TextUtils.equals(
        ROUTER_SERVICE_NAME, name)
  }

}