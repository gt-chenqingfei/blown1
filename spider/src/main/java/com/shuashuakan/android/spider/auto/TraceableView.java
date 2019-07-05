package com.shuashuakan.android.spider.auto;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * Created by twocity on 12/23/16.
 */

public interface TraceableView {

  void installViewProxy(@NonNull ViewProxy viewProxy);

  interface ViewProxy {
    @Nullable OnClickListener onSetViewClickListener(@NonNull View view,
                                                     @Nullable OnClickListener l);

    void onViewAttachedToWindow(@NonNull View view);

    void onViewDetachedFromWindow(@NonNull View view);

    ViewProxy NONE = new ViewProxy() {
      @Nullable @Override public OnClickListener onSetViewClickListener(@NonNull View view,
          @Nullable OnClickListener l) {
        return l;
      }

      @Override public void onViewAttachedToWindow(@NonNull View view) {

      }

      @Override public void onViewDetachedFromWindow(@NonNull View view) {

      }
    };
  }
}
