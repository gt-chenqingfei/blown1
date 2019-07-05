package com.shuashuakan.android.spider;

import android.support.annotation.Nullable;
import android.view.View;

/**
 * Created by twocity on 12/27/16.
 */

public class TraceableHelper {
  private static final int METADATA_ID = Integer.MIN_VALUE;
  private static final int VISIBILITY_CHANGED_ID = Integer.MIN_VALUE + 1024;

  private TraceableHelper() {

  }

  public static void setViewMetadata(View view, @Nullable Object metadata) {
    if (metadata != null) view.setTag(METADATA_ID, metadata);
  }

  public static void enableVisibilityTrack(View view) {
    view.setTag(VISIBILITY_CHANGED_ID, true);
  }

  @Nullable static Object getViewMetadata(View view) {
    return view.getTag(METADATA_ID);
  }

  static boolean shouldTrackVisibility(View view) {
    Object tag = view.getTag(VISIBILITY_CHANGED_ID);
    if (tag != null && tag instanceof Boolean) {
      return (boolean) tag;
    } else {
      return false;
    }
  }
}
