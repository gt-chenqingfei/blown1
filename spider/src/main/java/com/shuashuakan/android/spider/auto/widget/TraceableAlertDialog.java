package com.shuashuakan.android.spider.auto.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;

import com.shuashuakan.android.spider.auto.TraceableView;
import com.shuashuakan.android.spider.auto.ViewMonitor;

import static com.shuashuakan.android.spider.Utils.checkNotNull;

/**
 * Created by twocity on 22/05/2017.
 */
public class TraceableAlertDialog extends AlertDialog.Builder implements TraceableView {
  private ViewMonitor viewMonitor;
  private ViewProxy viewProxy = ViewProxy.NONE;

  public TraceableAlertDialog(Context context) {
    super(context);
  }

  public TraceableAlertDialog(Context context, int themeResId) {
    super(context, themeResId);
  }

  @Override
  public AlertDialog.Builder setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
    if (listener == null) {
      super.setOnItemSelectedListener(null);
    } else {
      super.setOnItemSelectedListener(
          new DelegateItemClickListener(listener, TraceableAlertDialog.this::onAlertItemClick));
    }
    return super.setOnItemSelectedListener(listener);
  }

  public void installMonitor(@Nullable ViewMonitor monitor) {
    this.viewMonitor = monitor;
  }

  @Override
  public void installViewProxy(@NonNull ViewProxy viewProxy) {
    this.viewProxy = checkNotNull(viewProxy, "viewProxy == null");
  }

  private ViewProxy getViewProxy() {
    if (viewProxy != null) {
      return viewProxy;
    }
    return ViewProxy.NONE;
  }

  private void onAlertItemClick(int position) {
    if (viewMonitor != null) {
//      viewMonitor.onAlertItemClicked(position);
    }
  }

  interface OnAlertItemClickHook {
    void onAlertItemClick(int position);
  }

  private static class DelegateItemClickListener implements AdapterView.OnItemSelectedListener {
    private final AdapterView.OnItemSelectedListener delegate;
    private final OnAlertItemClickHook hook;

    public DelegateItemClickListener(AdapterView.OnItemSelectedListener delegate, OnAlertItemClickHook hook) {
      this.delegate = delegate;
      this.hook = hook;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
if (hook != null)
  hook.onAlertItemClick(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
  }
}
