package com.shuashuakan.android.spider.auto.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import com.shuashuakan.android.spider.auto.TraceableView;

import static com.shuashuakan.android.spider.Utils.checkNotNull;

/**
 * Created by twocity on 12/22/16.
 */

public class TraceableImageView extends AppCompatImageView implements TraceableView {
  private ViewProxy viewProxy = ViewProxy.NONE;

  public TraceableImageView(Context context) {
    super(context);
  }

  public TraceableImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public TraceableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
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
