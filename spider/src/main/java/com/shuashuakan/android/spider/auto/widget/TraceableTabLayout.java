package com.shuashuakan.android.spider.auto.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;

import com.shuashuakan.android.spider.auto.TraceableView;
import com.shuashuakan.android.spider.auto.ViewMonitor;

import static com.shuashuakan.android.spider.Utils.checkNotNull;

/**
 * Created by twocity on 22/05/2017.
 */
public class TraceableTabLayout extends TabLayout implements TabLayout.OnTabSelectedListener,TraceableView {
  private ViewMonitor viewMonitor;
  private ViewProxy viewProxy = ViewProxy.NONE;

  public TraceableTabLayout(Context context) {
    this(context, null);
  }

  public TraceableTabLayout(Context context, AttributeSet attrs) {
    this(context, attrs, -1);
  }

  public TraceableTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    addOnTabSelectedListener(this);
  }

  public void installMonitor(@Nullable ViewMonitor monitor) {
    this.viewMonitor = monitor;
  }

  @Override public void onTabSelected(Tab tab) {
    if (viewMonitor != null) {
      viewMonitor.onTabSelected(this, tab);
    }
  }

  @Override public void onTabUnselected(Tab tab) {

  }

  @Override public void onTabReselected(Tab tab) {
    onTabSelected(tab);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    getViewProxy().onViewAttachedToWindow(this);
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    getViewProxy().onViewDetachedFromWindow(this);
  }

  @Override public void installViewProxy(@NonNull ViewProxy viewProxy) {
    this.viewProxy = checkNotNull(viewProxy, "viewProxy == null");
  }

  private ViewProxy getViewProxy() {
    if (viewProxy != null) {
      return viewProxy;
    }
    return ViewProxy.NONE;
  }
}
