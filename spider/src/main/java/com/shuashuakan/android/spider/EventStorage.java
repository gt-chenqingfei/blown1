package com.shuashuakan.android.spider;

import android.support.annotation.WorkerThread;
import com.shuashuakan.android.spider.event.EventEntry;

import java.io.IOException;
import java.util.List;

/**
 * Created by twocity on 16/8/25.
 */
interface EventStorage {
  @WorkerThread void put(EventEntry entry) throws IOException;

  @WorkerThread void remove(EventEntry entry) throws IOException;

  @WorkerThread void putAll(List<EventEntry> entries) throws IOException;

  @WorkerThread void removeAll(List<EventEntry> entries) throws IOException;

  @WorkerThread List<EventEntry> query(long size) throws IOException;

  @WorkerThread long size() throws IOException;
}
