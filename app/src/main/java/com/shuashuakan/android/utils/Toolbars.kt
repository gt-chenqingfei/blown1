package com.shuashuakan.android.utils

import android.support.v7.widget.Toolbar
import android.view.View


fun Toolbar.navigationIcon(icon: Int, callback: (View) -> Unit) {
  setNavigationIcon(icon)
  setNavigationOnClickListener {
    callback(it)
  }
}