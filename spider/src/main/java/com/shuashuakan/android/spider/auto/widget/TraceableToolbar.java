package com.shuashuakan.android.spider.auto.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MenuItem;
import com.shuashuakan.android.spider.auto.ViewMonitor;

/**
 * Created by twocity on 24/05/2017.
 */
public class TraceableToolbar extends Toolbar {
  private final ViewMonitor viewMonitor;

  public TraceableToolbar(Context context, @Nullable AttributeSet attrs, ViewMonitor viewMonitor) {
    this(context, attrs, android.support.v7.appcompat.R.attr.toolbarStyle, viewMonitor);
  }

  public TraceableToolbar(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, android.support.v7.appcompat.R.attr.toolbarStyle, null);
  }

  public TraceableToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, null);
  }

  private TraceableToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr,
      @Nullable ViewMonitor viewMonitor) {
    super(context, attrs, defStyleAttr);
    this.viewMonitor = viewMonitor;
  }

  @Override public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
    if (listener == null) {
      super.setOnMenuItemClickListener(null);
    } else {
      super.setOnMenuItemClickListener(
          new DelegateMenuItemClickListener(listener, TraceableToolbar.this::onMenuItemClick));
    }
  }

  private void onMenuItemClick(MenuItem item) {
    if (viewMonitor != null) {
      viewMonitor.onMenuItemClicked(item);
    }
  }

  interface OnMenuItemClickHook {
    void onMenuItemClick(MenuItem item);
  }

  private static class DelegateMenuItemClickListener implements OnMenuItemClickListener {
    private final OnMenuItemClickListener delegate;
    private final OnMenuItemClickHook hook;

    private DelegateMenuItemClickListener(OnMenuItemClickListener delegate,
                                          OnMenuItemClickHook hook) {
      this.delegate = delegate;
      this.hook = hook;
    }

    @Override public boolean onMenuItemClick(MenuItem item) {
      hook.onMenuItemClick(item);
      return delegate.onMenuItemClick(item);
    }
  }
}
