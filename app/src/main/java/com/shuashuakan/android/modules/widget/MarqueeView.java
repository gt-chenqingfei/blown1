package com.shuashuakan.android.modules.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ViewFlipper;
import com.shuashuakan.android.R;
import java.util.List;

public class MarqueeView extends ViewFlipper {
  private int interval = 3000;
  private int duration = 500;
  private Animation animIn, animOut;
  private int animInRes = R.anim.marquee_bottom_in;
  private int animOutRes = R.anim.marquee_top_out;

  public MarqueeView(Context context) {
    this(context, null);
  }

  public MarqueeView(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.MarqueeView, 0, 0);
    interval = array.getInt(R.styleable.MarqueeView_interval, interval);
    animInRes = array.getResourceId(R.styleable.MarqueeView_inAnimation, animInRes);
    animOutRes = array.getResourceId(R.styleable.MarqueeView_outAnimation, animOutRes);
    duration = array.getInt(R.styleable.MarqueeView_duration, duration);
    array.recycle();

    setFlipInterval(interval);
    animIn = AnimationUtils.loadAnimation(getContext(), animInRes);
    animIn.setDuration(duration);
    setInAnimation(animIn);
    animOut = AnimationUtils.loadAnimation(getContext(), animOutRes);
    animOut.setDuration(duration);
    setOutAnimation(animOut);
  }

  public void setMarqueeFactory(MarqueeFactory factory) {
    factory.setAttachedToMarqueeView(this);
    removeAllViews();
    List<View> views = factory.getMarqueeViews();
    if (views != null) {
      for (int i = 0; i < views.size(); i++) {
        addView(views.get(i));
      }
    }
  }

  public void setInterval(int interval) {
    this.interval = interval;
    setFlipInterval(interval);
  }

  public void setDuration(int duration) {
    this.duration = duration;
    animIn.setDuration(duration);
    setInAnimation(animIn);
    animOut.setDuration(duration);
    setOutAnimation(animOut);
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    stopFlipping();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override public void startFlipping() {
    if (getChildCount() > 1) {
      super.startFlipping();
    }
  }

  @Override public void stopFlipping() {
    if (getChildCount() > 1) {
      super.stopFlipping();
    }
  }
}
