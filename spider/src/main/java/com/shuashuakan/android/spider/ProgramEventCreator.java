package com.shuashuakan.android.spider;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import java.util.Map;

import timber.log.Timber;

import static com.shuashuakan.android.spider.Utils.checkNotNull;

/**
 * Created by twocity on 27/05/2017.
 */
public class ProgramEventCreator {
  private final Spider spider;
  private final String name;
  private final Map<String, Object> properties;

  ProgramEventCreator(Spider spider, String name) {
    this.spider = spider;
    this.name = name;
    this.properties = new ArrayMap<>();
  }

  public ProgramEventCreator put(@NonNull String key, @NonNull Object value) {
    properties.put(checkNotNull(key, "key == null"), checkNotNull(value, "value == null"));
    return this;
  }

  public ProgramEventCreator put(@NonNull String key, int value) {
    properties.put(checkNotNull(key, "key == null"), value);
    return this;
  }

  public ProgramEventCreator put(@NonNull String key, long value) {
    properties.put(checkNotNull(key, "key == null"), value);
    return this;
  }

  public ProgramEventCreator put(@NonNull String key, float value) {
    properties.put(checkNotNull(key, "key == null"), value);
    return this;
  }

  public ProgramEventCreator put(@NonNull String key, short value) {
    properties.put(checkNotNull(key, "key == null"), value);
    return this;
  }

  public ProgramEventCreator put(@NonNull String key, double value) {
    properties.put(checkNotNull(key, "key == null"), value);
    return this;
  }

  public ProgramEventCreator put(@NonNull String key, boolean value) {
    properties.put(checkNotNull(key, "key == null"), value);
    return this;
  }

  public ProgramEventCreator put(@NonNull Map<String, Object> map) {
    checkNotNull(map, "map == null");
    for (Map.Entry<String, Object> entrySet : map.entrySet()) {
      properties.put(checkNotNull(entrySet.getKey(), "key == null"), entrySet.getValue());
    }
    return this;
  }

  public void track() {
    try {
      properties.put("sm_id",spider.getSmId());
      spider.submitProgramEvent(name, properties);
    } catch (Exception e) {
      Timber.e(e, "unexpected");
    }
  }
}
