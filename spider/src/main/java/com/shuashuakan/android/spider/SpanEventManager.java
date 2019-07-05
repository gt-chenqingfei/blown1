package com.shuashuakan.android.spider;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.view.View;
import me.twocities.linker.annotations.LinkKt;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;


/**
 * Created by twocity on 16/8/30.
 */

class SpanEventManager implements Spider.PageTracer {
  private final Spider spider;
  private final IdGenerator idGenerator;

  private final Map<String, String> spanIds = new ArrayMap<>();
  private final Deque<String> spanIdQueue = new ArrayDeque<>();
  private final Deque<String> spanNameQueue = new ArrayDeque<>();

  SpanEventManager(Spider spider, IdGenerator idGenerator) {
    this.spider = spider;
    this.idGenerator = idGenerator;
  }

  @Override public void reportPageCreated(@NonNull Object page) {
    spanIds.put(buildPageName(page), idGenerator.generateSpanId());
  }

  @Override public void reportPageShown(@NonNull Object page, @Nullable String ssr, String pageTitle) {
    if (page instanceof Activity) {
      Activity activity = (Activity) page;
      // 如果 enjoyUrl 为 null 的话,我们从 activity 的 intent 中查找对应的 ENJOYLINK
      innerShown(activity, Utils.valueOrDefault(ssr, getEnjoyUrl(activity)),pageTitle);
    } else if (page instanceof Fragment) {
      innerShown(page, ssr,pageTitle);
    } else if (page instanceof View) {
      innerShown(page, ssr,pageTitle);
    } else {
      throw new IllegalArgumentException("page must be one of [Activity, Fragment, View]");
    }
  }

  @Nullable String currentSpanId() {
    return spanIdQueue.peekFirst();
  }

  @Nullable String currentSpanName() {
    return spanNameQueue.peekFirst();
  }

  private void innerShown(Object page, String enjoyUrl,String pageTitle) {
    String pageName = buildPageName(page);
    String spanId = spanIds.get(pageName);
    if (spanId == null) {
      // span id 不存在会有两种原因:
      //   1. 忘记在 reportPageShown 之前调用 reportPageCreated
      //   2. 当前页 (Activity,Fragment) 被系统销毁然后从之前的状态恢复, 这时候 reportPageCreated() 有可能没有触发,
      //      同时 page 在内存中的地址已经发生变化, hashCode() ( 参见 #buildPageName()) 也不再是之前的值。
      // 这时候要自动创建相应的新 span id
      reportPageCreated(page);
      Spider.LOGGER.w("Spider", "Can't find span id of page: %s, create new one", pageName);
      // get again
      spanId = spanIds.get(pageName);
    }
    spanIdQueue.addFirst(spanId);
    spanNameQueue.addFirst(page.getClass().getSimpleName());
    // 上报 span name 时不再需要
//    spider.trackSpanEvent(page.getClass().getCanonicalName(), spanId, enjoyUrl);
    spider.trackSpanEvent(pageTitle, spanId, enjoyUrl);
  }

  private String buildPageName(Object object) {
    return object.getClass().getCanonicalName() + "@" + object.hashCode();
  }

  private String getEnjoyUrl(Activity activity) {
    Intent intent = activity.getIntent();
    String result = null;
    if (intent != null) {
       // TODO 临时方案，从 intent 中获取 link 的方法应由外部传入
       result = intent.getStringExtra(LinkKt.LINK) ;
    }
    return result;
  }
}
