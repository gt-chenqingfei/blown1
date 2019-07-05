package com.shuashuakan.android.modules.widget;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public class FixedSpeedScroller extends Scroller {

  public static final int DEFAULT_DURATION = 500;

  /**
   * scroll duration when viewpager switch
   */
  private int fixedDuration = DEFAULT_DURATION;

  /**
   * set viewpager scroll fixedDuration
   *
   * @param duration millisecond
   */
  public void setDuration(int duration) {
    this.fixedDuration = duration;
  }

  public FixedSpeedScroller(Context context) {
    super(context);
  }

  public FixedSpeedScroller(Context context, Interpolator interpolator) {
    super(context, interpolator);
  }

  public FixedSpeedScroller(Context context, Interpolator interpolator, boolean flywheel) {
    super(context, interpolator, flywheel);
  }

  @Override public void startScroll(int startX, int startY, int dx, int dy, int duration) {
    // Ignore received duration, use fixed one instead
    super.startScroll(startX, startY, dx, dy, fixedDuration);
  }

  @Override public void startScroll(int startX, int startY, int dx, int dy) {
    // Ignore received duration, use fixed one instead
    super.startScroll(startX, startY, dx, dy, fixedDuration);
  }
}
