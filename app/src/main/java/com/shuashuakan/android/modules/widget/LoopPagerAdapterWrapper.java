package com.shuashuakan.android.modules.widget;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class LoopPagerAdapterWrapper extends PagerAdapter {
  private PagerAdapter adapter;

  LoopPagerAdapterWrapper(PagerAdapter adapter) {
    this.adapter = adapter;
  }

  @Override public void notifyDataSetChanged() {
    super.notifyDataSetChanged();
    adapter.notifyDataSetChanged();
  }

  public int toRealPosition(int position) {
    int realCount = getRealCount();
    if (realCount == 0) return 0;
    int realPosition = (position - 1) % realCount;
    if (realPosition < 0) realPosition += realCount;
    return realPosition;
  }

  public int toInnerPosition(int realPosition) {
    return realPosition + 1;
  }

  @Override public int getCount() {
    if (adapter.getCount() == 0) return 0;
    return adapter.getCount() == 1 ? 1 : adapter.getCount() + 2;
  }

  public int getRealCount() {
    return adapter.getCount();
  }

  public PagerAdapter getRealAdapter() {
    return adapter;
  }

  @Override public Object instantiateItem(ViewGroup container, int position) {
    int realPosition = toRealPosition(position);
    return adapter.instantiateItem(container, realPosition);
  }

  @Override public void destroyItem(ViewGroup container, int position, Object object) {
    int realPosition = toRealPosition(position);
    adapter.destroyItem(container, realPosition, object);
  }

  @Override public void finishUpdate(ViewGroup container) {
    adapter.finishUpdate(container);
  }

  @Override public boolean isViewFromObject(View view, Object object) {
    return adapter.isViewFromObject(view, object);
  }

  @Override public void restoreState(Parcelable bundle, ClassLoader classLoader) {
    adapter.restoreState(bundle, classLoader);
  }

  @Override public Parcelable saveState() {
    return adapter.saveState();
  }

  @Override public void startUpdate(ViewGroup container) {
    adapter.startUpdate(container);
  }

  @Override public void setPrimaryItem(ViewGroup container, int position, Object object) {
    adapter.setPrimaryItem(container, position, object);
  }
}
