package com.shuashuakan.android.spider.auto;

import android.support.annotation.NonNull;

/**
 * Created by twocity on 12/26/16.
 */

public interface ViewProxyFactory {
  @NonNull TraceableView.ViewProxy createViewProxy();
}
