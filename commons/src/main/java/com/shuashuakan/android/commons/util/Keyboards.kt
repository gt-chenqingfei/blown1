package com.shuashuakan.android.commons.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.RESULT_UNCHANGED_SHOWN
import android.view.inputmethod.InputMethodManager.SHOW_FORCED

fun View.hideSoftInput() {
  val manager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
  manager.hideSoftInputFromWindow(windowToken, 0)
}

fun View.showSoftInput() {
  val manager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
  manager.showSoftInput(this, SHOW_FORCED)

}

fun View.showSoftInputResultUnchanged() {
  val manager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
  manager.showSoftInput(this, RESULT_UNCHANGED_SHOWN)
}