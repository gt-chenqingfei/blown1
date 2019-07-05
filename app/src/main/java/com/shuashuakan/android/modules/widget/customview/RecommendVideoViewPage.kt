package com.shuashuakan.android.modules.widget.customview

import android.content.Context
import android.support.v4.view.MotionEventCompat
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.MotionEvent

class RecommendVideoViewPage : RecyclerView {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attr: AttributeSet?) : this(context, attr, 0)
    constructor(context: Context, attr: AttributeSet?, def: Int) : super(context, attr)

    val INVALID_POINTER = -1
    var scrollPointerId = INVALID_POINTER
    var initialTouchX = 0
    var initialTouchY = 0
    var touchSlop = 0

    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
        if(e == null) {
            return false
        }
        val action = MotionEventCompat.getActionMasked(e)
        val actionIndex = MotionEventCompat.getActionIndex(e)
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                scrollPointerId = MotionEventCompat.getPointerId(e, 0)
                initialTouchX = Math.round(e.x + 0.5f)
                initialTouchY = Math.round(e.y + 0.5f)
                return super.onInterceptTouchEvent(e)
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                scrollPointerId = MotionEventCompat.getPointerId(e, actionIndex)
                initialTouchX = Math.round(MotionEventCompat.getX(e, actionIndex) + 0.5f)
                initialTouchY = Math.round(MotionEventCompat.getY(e, actionIndex) + 0.5f)
                return super.onInterceptTouchEvent(e)
            }

            MotionEvent.ACTION_MOVE -> {
                val index = MotionEventCompat.findPointerIndex(e, scrollPointerId)
                if(index < 0) {
                    return false
                }

                val x = Math.round(MotionEventCompat.getX(e, index) + 0.5f)
                val y = Math.round(MotionEventCompat.getY(e, index) + 0.5f)
                if(scrollState != SCROLL_STATE_DRAGGING) {
                    val dx = x - initialTouchX
                    val dy = y - initialTouchY
                    var startScroll = false
                    if(layoutManager.canScrollHorizontally() && Math.abs(dx) > touchSlop && (layoutManager.canScrollVertically() || Math.abs(dx) > Math.abs(dy))) {
                        startScroll = true
                    }
                    if(layoutManager.canScrollVertically() && Math.abs(dy) > touchSlop && (layoutManager.canScrollHorizontally() || Math.abs(dy) > Math.abs(dx))) {
                        startScroll = true
                    }
                    return startScroll && super.onInterceptTouchEvent(e)
                }

                return super.onInterceptTouchEvent(e)
            }

            else -> {
                return super.onInterceptTouchEvent(e)
            }
        }
    }

}