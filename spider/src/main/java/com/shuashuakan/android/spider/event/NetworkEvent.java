package com.shuashuakan.android.spider.event;

import android.support.annotation.Nullable;
import com.google.auto.value.AutoValue;

/**
 * Created by twocity on 1/17/17.
 */

@AutoValue public abstract class NetworkEvent {

  public abstract String traceId();

  public abstract @Nullable String spanId();

  public abstract String requestId();

  public abstract @Nullable Integer statusCode();

  public abstract @Nullable Integer tookMS();

  public abstract @Nullable String failedReason();

  public static Builder newBuilder() {
    return new AutoValue_NetworkEvent.Builder();
  }

  @AutoValue.Builder public interface Builder {
    Builder traceId(String traceId);

    Builder spanId(String spanId);

    Builder requestId(String requestId);

    Builder statusCode(Integer statusCode);

    Builder tookMS(Integer tookMS);

    Builder failedReason(String failedReason);

    NetworkEvent build();
  }
}
