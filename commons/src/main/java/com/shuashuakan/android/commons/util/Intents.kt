@file:JvmName("Intents")

package com.shuashuakan.android.commons.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import kotlin.reflect.KClass


inline fun <reified T : Any> Context.startActivity(klass: KClass<T>,
    block: Intent.() -> Unit = {}) {
  Intent(this, klass.java).let {
    block(it)
    startActivity(it)
  }
}

inline fun <reified T : Any> Activity.startActivityForResult(klass: KClass<T>, requestCode: Int,
    block: Intent.() -> Unit = {}) {
  Intent(this, klass.java).let {
    block(intent)
    startActivityForResult(intent, requestCode)
  }
}

fun startCallIntent(context: Context, tel: String) {
  try {
    if (!TextUtils.isEmpty(tel)) {
      val uri = Uri.parse("tel:" + tel)
      val intent = Intent("android.intent.action.DIAL", uri)
      context.startActivity(intent)
    }
  } catch (e: Exception) {

  }
}