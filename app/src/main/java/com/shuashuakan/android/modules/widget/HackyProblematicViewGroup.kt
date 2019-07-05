package com.shuashuakan.android.modules.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

class HackyProblematicViewGroup(context: Context,attributeSet: AttributeSet) : FrameLayout(context) {

  override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
    try {
      return super.onInterceptTouchEvent(ev)
    } catch (e: IllegalArgumentException) {
      //uncomment if you really want to see these errors
      //e.printStackTrace();
      return false
    }

  }
}