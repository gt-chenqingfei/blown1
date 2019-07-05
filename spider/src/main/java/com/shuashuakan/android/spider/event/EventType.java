package com.shuashuakan.android.spider.event;

/**
 * Created by twocity on 1/12/17.
 */

public enum EventType {
  CLICK("click"), MANUALLY_USER_CUSTOM("custom"), EXPOSURE("EXPOSURE");

  private final String name;

  EventType(String name) {
    this.name = name;
  }
}
