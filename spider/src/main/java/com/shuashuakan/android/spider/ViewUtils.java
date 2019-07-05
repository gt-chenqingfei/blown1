package com.shuashuakan.android.spider;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by twocity on 12/26/16.
 */

class ViewUtils {

  private ViewUtils() {
  }

  static String findViewPath(View view) {
    List<String> ids = new ArrayList<>();
    View parent = view;
    appendViewId(ids, parent);
    while (parent.getParent() != null && parent.getParent() instanceof View) {
      parent = (View) parent.getParent();
      if (parent.getId() == android.R.id.content) break;
      appendViewId(ids, parent);
    }
    Collections.reverse(ids);
    return TextUtils.join("/", ids);
  }

  private static void appendViewId(List<String> list, View view) {
    String content = "%s[%s, %d]";
    String idString = "";
    if (view.getId() != View.NO_ID) {
      idString =
          getIdStringQuietly(view.getClass().getCanonicalName(), view.getResources(), view.getId());
    }
    int index = getViewIndexInParent(view);
    list.add(String.format(Locale.US, content, view.getClass().getSimpleName(), idString, index));
  }

  @NonNull private static String getIdStringQuietly(Object idContext, @Nullable Resources r,
      int resourceId) {
    try {
      return getIdString(r, resourceId);
    } catch (Resources.NotFoundException e) {
      String idString = getFallbackIdString(resourceId);
      Timber.w("Unknown identifier encountered on %s : %s", idContext, idString);
      return idString;
    }
  }

  static String getIdString(@Nullable Resources r, int resourceId)
      throws Resources.NotFoundException {
    if (r == null) {
      return getFallbackIdString(resourceId);
    }

    String prefix;
    String prefixSeparator;
    switch (getResourcePackageId(resourceId)) {
      case 0x7f:
        prefix = "";
        prefixSeparator = "";
        break;
      default:
        prefix = r.getResourcePackageName(resourceId);
        prefixSeparator = ":";
        break;
    }

    String typeName = r.getResourceTypeName(resourceId);
    String entryName = r.getResourceEntryName(resourceId);

    StringBuilder sb = new StringBuilder(1
        + prefix.length()
        + prefixSeparator.length()
        + typeName.length()
        + 1
        + entryName.length());
    sb.append("@");
    sb.append(prefix);
    sb.append(prefixSeparator);
    sb.append(typeName);
    sb.append("/");
    sb.append(entryName);

    return sb.toString();
  }

  private static String getFallbackIdString(int resourceId) {
    return "#" + Integer.toHexString(resourceId);
  }

  private static int getResourcePackageId(int id) {
    return (id >>> 24) & 0xff;
  }

  private static int getViewIndexInParent(View view) {
    int index = 0;
    if (view.getParent() instanceof ViewGroup) {
      ViewGroup parent = (ViewGroup) view.getParent();
      if (parent instanceof RecyclerView) {
        index = ((RecyclerView) parent).getChildAdapterPosition(view);
      } else if (parent instanceof AbsListView) {
        index = ((AbsListView) parent).getPositionForView(view);
      } else if (parent instanceof ViewPager) {
        index = ((ViewPager) parent).getCurrentItem();
      } else {
        index = parent.indexOfChild(view);
      }
    }
    return index;
  }
}
