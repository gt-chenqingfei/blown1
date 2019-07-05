package com.shuashuakan.android.modules.widget;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Interpolator;
import java.lang.reflect.Field;
import timber.log.Timber;

public class LoopViewPager extends WrapContentViewPager {

  private static final int VIEW_PAGER_LOOP_DURATION = 3000;
  private OnPageChangeListener outerPageChangeListener;
  private LoopPagerAdapterWrapper loopPagerAdapter;
  private FixedSpeedScroller fixedSpeedScroller;
  private Handler handler;
  public boolean isStop = true;
  private float previousOffset = -1;
  private float previousPosition = -1;
  private int currentPosition = 0;
  // 自动轮播
  private boolean enableLoop = true;
  private Runnable loopTask = new Runnable() {
    @Override public void run() {
      if (loopPagerAdapter != null) {
        int currentItem = getCurrentItem();
        setCurrentItem(currentItem + 1);
        currentPosition = getCurrentItem();
        if (!isStop) {
          handler.postDelayed(loopTask, VIEW_PAGER_LOOP_DURATION);
        }
      }
    }
  };
  private OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {

    @Override public void onPageSelected(int position) {
      int realPosition = loopPagerAdapter.toRealPosition(position);
      if (previousPosition != realPosition) {
        previousPosition = realPosition;
        if (outerPageChangeListener != null) {
          outerPageChangeListener.onPageSelected(realPosition);
        }
      }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
      int realPosition = position;
      if (loopPagerAdapter != null) {
        realPosition = loopPagerAdapter.toRealPosition(position);

        if (positionOffset == 0 && previousOffset == 0 && (position == 0
            || position == loopPagerAdapter.getCount() - 1)) {
          setCurrentItem(realPosition, false);
          currentPosition = realPosition;
        }
      }

      previousOffset = positionOffset;
      if (outerPageChangeListener != null && loopPagerAdapter != null) {
        if (realPosition != loopPagerAdapter.getRealCount() - 1) {
          outerPageChangeListener.onPageScrolled(realPosition, positionOffset,
              positionOffsetPixels);
        } else {
          outerPageChangeListener.onPageScrolled(positionOffset > .5 ? 0 : realPosition, 0, 0);
        }
      }
    }

    @Override public void onPageScrollStateChanged(int state) {
      if (loopPagerAdapter != null) {
        int position = LoopViewPager.super.getCurrentItem();
        int realPosition = loopPagerAdapter.toRealPosition(position);
        currentPosition = realPosition;
        if (state == ViewPager.SCROLL_STATE_IDLE && (position == 0
            || position == loopPagerAdapter.getCount() - 1)) {
          setCurrentItem(realPosition, false);
        }
      }
      if (outerPageChangeListener != null) {
        outerPageChangeListener.onPageScrollStateChanged(state);
      }
    }
  };

  public LoopViewPager(Context context) {
    super(context);
    init();
  }

  public LoopViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  @Override public PagerAdapter getAdapter() {
    return loopPagerAdapter != null ? loopPagerAdapter.getRealAdapter() : null;
  }

  @Override public void setAdapter(PagerAdapter adapter) {
    loopPagerAdapter = new LoopPagerAdapterWrapper(adapter);
    super.setAdapter(loopPagerAdapter);
  }

  @Override public int getCurrentItem() {
    return loopPagerAdapter != null ? loopPagerAdapter.toRealPosition(super.getCurrentItem()) : 0;
  }

  @Override public void setCurrentItem(int item) {
    if (getCurrentItem() != item) {
      setCurrentItem(item, true);
    }
  }

  public void startLoop() {
    notifyDataSetChanged();
    setCurrentItem(currentPosition, false);
    setOffscreenPageLimit(loopPagerAdapter.getCount());
    startLoopTask();
  }

  public int getLoopPagerCount() {
    return loopPagerAdapter == null ? getAdapter().getCount() : loopPagerAdapter.getCount();
  }

  public void notifyDataSetChanged() {
    loopPagerAdapter.notifyDataSetChanged();
  }

  /**
   * 设置过渡时间
   *
   * @param duration 建议使用 200 - 300 毫秒
   */
  public void setScrollDuration(int duration) {
    fixedSpeedScroller.setDuration(duration);
  }

  @Override public void setCurrentItem(int item, boolean smoothScroll) {
    int realItem = loopPagerAdapter.toInnerPosition(item);
    super.setCurrentItem(realItem, smoothScroll);
  }

  @Override public void setOnPageChangeListener(OnPageChangeListener listener) {
    outerPageChangeListener = listener;
  }

  private void init() {
    handler = new Handler();
    super.setOnPageChangeListener(onPageChangeListener);
    setupScroller();
  }

  private void setupScroller() {
    try {
      Field scrollerField;
      Class<?> viewpager = ViewPager.class;
      scrollerField = viewpager.getDeclaredField("mScroller");
      scrollerField.setAccessible(true);
      Field interpolator = viewpager.getDeclaredField("sInterpolator");
      interpolator.setAccessible(true);
      fixedSpeedScroller =
          new FixedSpeedScroller(getContext(), (Interpolator) interpolator.get(null));
      scrollerField.set(this, fixedSpeedScroller);
    } catch (Exception e) {
      Timber.e(e, "LoopViewPager setupScroller exception");
    }
  }

  public void enableAutoLoop(boolean enable) {
    this.enableLoop = enable;
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    return isPagingEnable() && super.onTouchEvent(event);
  }

  @Override public boolean onInterceptTouchEvent(MotionEvent event) {
    return isPagingEnable() && super.onInterceptTouchEvent(event);
  }

  private boolean isPagingEnable() {
    return loopPagerAdapter.getRealCount() > 1;
  }

  @Override public boolean dispatchTouchEvent(MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_CANCEL) {
      startLoopTask();
    } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
      stopLoopTask();
    }
    return super.dispatchTouchEvent(event);
  }

  private void startLoopTask() {
    if (enableLoop && loopPagerAdapter.getRealAdapter().getCount() > 1 && isStop) {
      isStop = false;
      handler.postDelayed(loopTask, VIEW_PAGER_LOOP_DURATION);
    }
  }

  public void stopLoopTask() {
    if (enableLoop && !isStop) {
      isStop = true;
      handler.removeCallbacks(loopTask);
    }
  }
}
