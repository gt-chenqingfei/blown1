package com.shuashuakan.android.utils.extension

import android.support.v4.app.Fragment
import android.widget.Toast

fun Fragment.showLongToast(msg: String) {
    this.context?.let {
        Toast.makeText(it, msg, Toast.LENGTH_LONG).show()
    }
}

fun Fragment.showShortToast(msg: String) {
    this.context?.let {
        Toast.makeText(it, msg, Toast.LENGTH_SHORT).show()
    }
}