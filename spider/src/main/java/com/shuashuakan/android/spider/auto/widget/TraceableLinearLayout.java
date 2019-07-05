package com.shuashuakan.android.spider.auto.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.shuashuakan.android.spider.auto.TraceableView;

import static com.shuashuakan.android.spider.Utils.checkNotNull;

/**
 * Created by twocity on 12/22/16.
 */

public class TraceableLinearLayout extends LinearLayout implements TraceableView {
  private ViewProxy viewProxy = ViewProxy.NONE;

  public TraceableLinearLayout(Context context) {
    super(context);
  }

  public TraceableLinearLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public TraceableLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public TraceableLinearLayout(Context context, AttributeSet attrs, int defStyleAttr,
      int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override public void setOnClickListener(OnClickListener l) {
    super.setOnClickListener(getViewProxy().onSetViewClickListener(this, l));
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
