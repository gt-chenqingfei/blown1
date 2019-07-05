package com.shuashuakan.android.spider.event;

import com.google.auto.value.AutoValue;

/**
 * Created by twocity on 16/8/30.
 */

@AutoValue public abstract class TraceEvent {
  abstract public String traceId();

  public static TraceEvent create(String traceId) {
    return new AutoValue_TraceEvent(traceId);
  }

  @Override public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof TraceEvent) {
      TraceEvent that = (TraceEvent) o;
      return (traceId().equals(that.traceId()));
    }
    return false;
  }

  @Override public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= this.traceId().hashCode();
    return h;
  }
}
