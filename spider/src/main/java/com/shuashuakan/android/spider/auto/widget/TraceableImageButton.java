package com.shuashuakan.android.spider.auto.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import com.shuashuakan.android.spider.auto.TraceableView;

import static com.shuashuakan.android.spider.Utils.checkNotNull;

/**
 * Created by twocity on 12/21/16.
 */

public class TraceableImageButton extends AppCompatImageButton implements TraceableView {
  private ViewProxy viewProxy = ViewProxy.NONE;

  public TraceableImageButton(Context context) {
    super(context);
  }

  public TraceableImageButton(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public TraceableImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
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
