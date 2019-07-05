package com.shuashuakan.android.spider.event;

import android.support.annotation.WorkerThread;

import java.io.IOException;

/**
 * Created by twocity on 1/12/17.
 */

public interface EventEntryTransformer<T> {
  @WorkerThread EventEntry transform(T t) throws IOException;
}
