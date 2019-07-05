package com.shuashuakan.android.modules.widget;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class StickyRefreshLayout extends SwipeRefreshLayout {
  private View scrollView;
  private int mTouchSlop;
  private float mPrevX;

  public StickyRefreshLayout(Context context) {
    super(context);
    mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
  }

  public StickyRefreshLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
  }

  public void setScrollView(View scrollView) {
    this.scrollView = scrollView;
  }

  @Override public boolean canChildScrollUp() {
    return scrollView != null && ViewCompat.canScrollVertically(scrollView, -1);
  }

  @Override public boolean onInterceptTouchEvent(MotionEvent event) {

    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        mPrevX = event.getX();
        break;

      case MotionEvent.ACTION_MOVE:
        final float eventX = event.getX();
        float xDiff = Math.abs(eventX - mPrevX);

        if (xDiff > mTouchSlop) {
          return false;
        }
        break;
      default:
        break;
    }

    return super.onInterceptTouchEvent(event);
  }
}
