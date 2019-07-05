package com.shuashuakan.android.spider;

import com.shuashuakan.android.spider.event.EventType;

import timber.log.Timber;

import static com.shuashuakan.android.spider.event.EventType.CLICK;
import static com.shuashuakan.android.spider.event.EventType.EXPOSURE;

/**
 * Created by twocity on 27/05/2017.
 */
public class AutomaticEventCreator {
  private final Spider spider;
  private String traceMeta;
  private String viewPath;
  private EventType eventType = CLICK;

  AutomaticEventCreator(Spider spider) {
    this.spider = spider;
  }

  public AutomaticEventCreator traceMeta(String traceMeta) {
    this.traceMeta = traceMeta;
    return this;
  }

  public AutomaticEventCreator viewPath(String viewPath) {
    this.viewPath = viewPath;
    return this;
  }

  /**
   * 点击点
   */
  public AutomaticEventCreator clickedEvent() {
    eventType = CLICK;
    return this;
  }

  /**
   * 曝光点
   */
  public AutomaticEventCreator exposedEvent() {
    eventType = EXPOSURE;
    return this;
  }

  public void track() {
    try {
      if (Utils.isBlank(traceMeta) && Utils.isBlank(viewPath)) {
        throw new IllegalArgumentException("traceMeta and viewPath are both empty!");
      }
      spider.submitBusinessEvent(eventType, null, traceMeta, null, viewPath);
    } catch (Exception e) {
      Timber.e(e, "unexpected");
    }
  }
}
