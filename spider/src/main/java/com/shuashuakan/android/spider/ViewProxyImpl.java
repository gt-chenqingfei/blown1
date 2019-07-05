package com.shuashuakan.android.spider;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewTreeObserver;
import com.shuashuakan.android.spider.auto.TraceableView;

/**
 * Created by twocity on 12/26/16.
 */

class ViewProxyImpl implements TraceableView.ViewProxy, ViewTreeObserver.OnScrollChangedListener {
  private static final int STATE_VISIBLE = 0x001;
  private static final int STATE_INVISIBLE = 0x002;
  private static final Rect VISIBILITY_RECT = new Rect();
  private int visibility = STATE_INVISIBLE;
  private View delegatedView;

  private final ViewStateListener viewStateListener;

  ViewProxyImpl(ViewStateListener viewStateListener) {
    this.viewStateListener = viewStateListener;
  }

  @Nullable @Override public View.OnClickListener onSetViewClickListener(@NonNull View view,
      @Nullable View.OnClickListener l) {
    if (l == null) {
      return null;
    } else {
      return viewStateListener.delegatedOnClickListener(l);
    }
  }

  private int getViewVisibility(View view) {
    return view.getGlobalVisibleRect(VISIBILITY_RECT) ? STATE_VISIBLE : STATE_INVISIBLE;
  }

  @Override public void onViewAttachedToWindow(@NonNull View view) {
    this.delegatedView = view;
    view.getViewTreeObserver().addOnScrollChangedListener(this);
  }

  @Override public void onViewDetachedFromWindow(@NonNull View view) {
    this.delegatedView = null;
    view.getViewTreeObserver().removeOnScrollChangedListener(this);
  }

  @Override public void onScrollChanged() {
    if (delegatedView != null) {
      int currentVisibility = getViewVisibility(delegatedView);
      if (currentVisibility != visibility) {
        this.visibility = currentVisibility;
        viewStateListener.onViewVisibilityChanged(delegatedView,
            currentVisibility == STATE_VISIBLE);
      }
    }
  }

  interface ViewStateListener {
    @Nullable View.OnClickListener delegatedOnClickListener(@NonNull View.OnClickListener listener);

    void onViewVisibilityChanged(@NonNull View view, boolean isVisible);
  }
}
