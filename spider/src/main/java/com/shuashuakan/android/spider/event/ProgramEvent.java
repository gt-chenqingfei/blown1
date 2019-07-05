package com.shuashuakan.android.spider.event;

import android.support.annotation.Nullable;
import com.google.auto.value.AutoValue;

import java.util.Map;

/**
 * Created by twocity on 01/03/2017.
 */

@AutoValue abstract public class ProgramEvent {

  public abstract String traceId();

  public abstract @Nullable String spanId();

  public abstract String programId();

  public abstract String programName();

  public abstract @Nullable Map<String, Object> content();

  public static Builder newBuilder() {
    return new AutoValue_ProgramEvent.Builder();
  }

  @AutoValue.Builder public interface Builder {

    Builder spanId(String spanId);

    Builder traceId(String traceId);

    Builder programId(String programId);

    Builder programName(String eventName);

    Builder content(Map<String, Object> objectMap);

    ProgramEvent build();
  }
}
