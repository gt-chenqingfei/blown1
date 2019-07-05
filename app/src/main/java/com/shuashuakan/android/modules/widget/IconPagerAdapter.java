package com.shuashuakan.android.modules.widget;

public interface IconPagerAdapter {
  /**
   * Get icon representing the page at {@code index} in the feedAdapter.
   */
  int getIconResId(int index);

  // From PagerAdapter
  int getCount();
}
