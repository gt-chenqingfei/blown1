package com.shuashuakan.android.utils;

import android.os.Build;
import android.support.design.widget.TabLayout;
import android.view.View;
import android.widget.LinearLayout;
import java.lang.reflect.Field;

public class TabLayoutHelper {

  public static void setUpIndicatorWidth(TabLayout tabLayout, int marginStart, int marginEnd) {
    Class<?> tabLayoutClass = tabLayout.getClass();
    Field tabStrip = null;
    try {
      tabStrip = tabLayoutClass.getDeclaredField("mTabStrip");
      tabStrip.setAccessible(true);
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    }

    LinearLayout layout = null;
    try {
      if (tabStrip != null) {
        layout = (LinearLayout) tabStrip.get(tabLayout);
      }
      if (layout != null) {
        for (int i = 0; i < layout.getChildCount(); i++) {
          View child = layout.getChildAt(i);
          child.setPadding(0, 0, 0, 0);

          LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
              LinearLayout.LayoutParams.MATCH_PARENT, 1);
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            params.setMarginStart(marginStart);
            params.setMarginEnd(marginEnd);
          }
          child.setLayoutParams(params);
          child.invalidate();
        }
      }
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }
}
