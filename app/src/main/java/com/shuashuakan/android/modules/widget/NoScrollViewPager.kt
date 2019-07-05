package com.shuashuakan.android.modules.widget

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

class NoScrollViewPager constructor(context: Context, attrs: AttributeSet? = null) : ViewPager(context, attrs) {

  private var isScroll: Boolean = false // 默认禁止滑动
  private var isHasScrollAnim = true // 进制显示滚动动画

  override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
    return if (isScroll)
      super.onInterceptTouchEvent(ev)
    else
      false
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(ev: MotionEvent?): Boolean {
    return if (isScroll)
      super.onTouchEvent(ev)
    else
      true // 消费，拦截事件
  }

  fun setScroll(scroll: Boolean) {
    isScroll = scroll
  }

  fun setHasScrollAnim(isHasScrollAnim: Boolean) {
    this.isHasScrollAnim = isHasScrollAnim
  }

  override fun setCurrentItem(item: Int, smoothScroll: Boolean) {
    super.setCurrentItem(item, smoothScroll)
  }

  /**
   * 设置其是否去求切换时的滚动动画
   * isHasScrollAnim为false时，会去除滚动效果
   */
  override fun setCurrentItem(item: Int) {
    super.setCurrentItem(item, isHasScrollAnim)
  }

}