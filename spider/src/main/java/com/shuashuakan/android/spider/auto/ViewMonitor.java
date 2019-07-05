package com.shuashuakan.android.spider.auto;

import android.support.design.widget.TabLayout;
import android.view.MenuItem;

/**
 * Created by twocity on 24/05/2017.
 */
public interface ViewMonitor {

  ViewMonitor EMPTY = new ViewMonitor() {
    @Override public void onMenuItemClicked(MenuItem item) {

    }

    @Override public void onTabSelected(TabLayout tableLayout, TabLayout.Tab tab) {

    }
  };

  void onMenuItemClicked(MenuItem item);

  void onTabSelected(TabLayout tableLayout, TabLayout.Tab tab);

  interface ViewMonitorFactory {
    ViewMonitor createViewMonitor();
  }
}
