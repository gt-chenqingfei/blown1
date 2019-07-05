package com.shuashuakan.android.spider.event;

import android.content.ContentValues;
import com.google.auto.value.AutoValue;

/**
 * Created by twocity on 16/8/25.
 */

@AutoValue public abstract class EventEntry {
  public static final String TABLE = "events";
  public static final String ID = "identity";
  public static final String DATA = "raw_data";

  abstract public String identity();

  abstract public String rawData();

  public static EventEntry create(String identity, String rawData) {
    return new AutoValue_EventEntry(identity, rawData);
  }

  @Override public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof EventEntry) {
      EventEntry that = (EventEntry) o;
      return (identity().equals(that.identity()));
    }
    return false;
  }

  @Override public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= this.identity().hashCode();
    return h;
  }

  public static final class Builder {
    private final ContentValues values = new ContentValues();

    public Builder id(String id) {
      values.put(ID, id);
      return this;
    }

    public Builder rawData(String data) {
      values.put(DATA, data);
      return this;
    }

    public ContentValues build() {
      return values;
    }
  }
}
