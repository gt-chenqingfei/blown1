package com.shuashuakan.android.spider;

import com.shuashuakan.android.spider.event.EventEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by twocity on 1/12/17.
 */

class EventListenerManager implements Spider.EventListener {

  private final List<Spider.EventListener> eventListeners = new ArrayList<>();

  public void addEventListener(Spider.EventListener listener) {
    this.eventListeners.add(listener);
  }

  @Override public void onEventSaved(EventEntry eventEntry) {
    for (Spider.EventListener listener : eventListeners) {
      listener.onEventSaved(eventEntry);
    }
  }

  @Override public void onEventsUpload(List<EventEntry> events) {
    for (Spider.EventListener listener : eventListeners) {
      listener.onEventsUpload(events);
    }
  }

  @Override public void onEventsRemoved(List<EventEntry> events) {
    for (Spider.EventListener listener : eventListeners) {
      listener.onEventsRemoved(events);
    }
  }
}
