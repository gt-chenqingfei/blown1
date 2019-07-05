package com.shuashuakan.android.spider.auto;

import android.content.Context;
import android.view.View;

/**
 * Created by twocity on 24/04/2017.
 */
class ViewClassUtil {

  private ViewClassUtil() {

  }

  /**
   * Loads class for the given class name.
   */
  private static Class<? extends View> loadViewClass(Context context, String name)
      throws ClassNotFoundException {
    return context.getClassLoader().loadClass(name).asSubclass(View.class);
  }

  /*
   * Tries to load the view proxy class generated at build time for the
   * given class name.
   */
  static Class<? extends View> findProxyViewClass(Context context, String name) {
    try {
      return loadViewClass(context, name + "$SpiderProxyView");
    } catch (ClassNotFoundException e) {
      return null;
    }
  }
}
