package com.shuashuakan.android.modules.widget

import android.content.Context
import android.support.v4.widget.NestedScrollView
import android.util.AttributeSet

class ObservableScrollView : NestedScrollView {

  var callback: OnScrollChangeCallback? = null

  constructor(context: Context): super(context)

  constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)

  override fun onScrollChanged( l: Int, t: Int, oldl: Int, oldt: Int ) {
    super.onScrollChanged(l, t, oldl, oldt)
    callback?.let {
      it.onScroll(l, t)
    }
  }

  interface OnScrollChangeCallback{
    fun onScroll(l: Int, r: Int)
  }
}