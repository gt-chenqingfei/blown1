package com.shuashuakan.android.spider.event;

import android.support.annotation.Nullable;
import com.google.auto.value.AutoValue;

/**
 * Created by twocity on 16/8/30.
 */

@AutoValue abstract public class SpanEvent {
  abstract public String traceId();

  abstract public String spanId();

  abstract @Nullable public String ssr();

  abstract public long timestamp();

  abstract public String span_name();

  public static SpanEvent create(String traceId, String spanId, String ssr, String span_name,
      long timestamp) {
    return new AutoValue_SpanEvent(traceId, spanId, ssr, timestamp, span_name);
  }

  @Override public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof SpanEvent) {
      SpanEvent that = (SpanEvent) o;
      return (spanId().equals(that.spanId()));
    }
    return false;
  }

  @Override public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= this.spanId().hashCode();
    return h;
  }
}
