package com.shuashuakan.android.spider;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.shuashuakan.android.spider.auto.ViewMonitor;
import timber.log.Timber;
import static com.shuashuakan.android.spider.ViewUtils.getIdString;

/**
 * Created by twocity on 12/26/16.
 */

class ViewStateTracker
    implements ViewProxyImpl.ViewStateListener, View.OnClickListener, ViewMonitor {
  private static final String TAG = "ViewStateTracker";
  private final Spider spider;

  ViewStateTracker(Spider spider) {
    this.spider = spider;
  }

  private static @Nullable String buildViewPath(Spider spider, View view) {
    try {
      String path = ViewUtils.findViewPath(view);
      CharSequence props = null;
      if (view instanceof TextView) {
        props = ((TextView) view).getText();
      }
      String contextName = buildContextName(spider, view.getContext());
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(contextName).append('/').append(path);
      if (!Utils.isBlank(props)) {
        stringBuilder.append('(').append(props).append(')');
      }
      return stringBuilder.toString();
    } catch (Exception e) {
      Timber.e(e, "unexpected");
    }
    return null;
  }

  private static String buildContextName(Spider spider, Context context) {
    String contextName;
    if (context instanceof Activity) {
      contextName = context.getClass().getSimpleName();
    } else {
      contextName = spider.currentSpanName();
    }
    return Utils.valueOrDefault(contextName, context.getClass().getSimpleName());
  }

  @Nullable @Override
  public View.OnClickListener delegatedOnClickListener(@NonNull View.OnClickListener listener) {
    return v -> {
      listener.onClick(v);
      try {
        ViewStateTracker.this.onClick(v);
      } catch (Exception e) {
        Timber.e(e, "unexpected");
      }
    };
  }

  @Override public void onViewVisibilityChanged(@NonNull View view, boolean isVisible) {
    if (isVisible && TraceableHelper.shouldTrackVisibility(view)) {
      Object metadata = TraceableHelper.getViewMetadata(view);
      String metadataOfString = metadata == null ? null : String.valueOf(metadata);
      if (metadataOfString != null) {
        //Spider.LOGGER.d(TAG, "track visible event: %s",
        //    URLDecoder.decode(ByteString.decodeBase64(metadataOfString).utf8()), "UTF-8");
        spider.autoEvent().traceMeta(metadataOfString).exposedEvent().track();
      }
    }
  }

  @Override public void onClick(View view) {
    try {
      Object metadata = TraceableHelper.getViewMetadata(view);
      String metadataOfString = metadata == null ? null : String.valueOf(metadata);
      String viewPath = buildViewPath(spider, view);
      if (metadataOfString != null) {
        Spider.LOGGER.d(TAG, "metadata: %s", metadataOfString);
      }
      Spider.LOGGER.d(TAG, "view path: %s", viewPath);
      spider.autoEvent().traceMeta(metadataOfString).viewPath(viewPath).track();
    } catch (Exception e) {
      Timber.e(e, "unexpected");
    }
  }

  @Override public void onMenuItemClicked(MenuItem item) {
    try {
      String idString = getIdString(spider.context.getResources(), item.getItemId());
      String viewPath = spider.currentSpanName() + "/MenuItem" + '[' + idString + ']';
      Spider.LOGGER.d(TAG, "ViewPath: %s", viewPath);
      spider.autoEvent().viewPath(viewPath).track();
    } catch (Exception e) {
      Timber.e(e, "unexpected");
    }
  }

  @Override public void onTabSelected(TabLayout tabLayout, TabLayout.Tab tab) {
    try {
      // build the view path
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(buildContextName(spider, tabLayout.getContext()));
      stringBuilder.append('/');
      stringBuilder.append(ViewUtils.findViewPath(tabLayout));

      for (int i = 0; i < tabLayout.getTabCount(); i++) {
        if (tab.equals(tabLayout.getTabAt(i))) {
          stringBuilder.append('/').append("Tab").append('[').append(i).append(']');
        }
      }
      if (!Utils.isBlank(tab.getText())) {
        stringBuilder.append('(').append(tab.getText()).append(')');
      }
      // get the trace meta
      Object metadata = tab.getTag();
      String metadataOfString = metadata == null ? null : String.valueOf(metadata);
      if (metadataOfString != null) {
        Spider.LOGGER.d(TAG, "metadata: %s", metadataOfString);
      }
      String viewPath = stringBuilder.toString();
      Spider.LOGGER.d(TAG, "view path: %s", viewPath);
      spider.autoEvent().viewPath(viewPath).traceMeta(metadataOfString).track();
    } catch (Exception e) {
      Timber.e(e, "unexpected");
    }
  }

}
