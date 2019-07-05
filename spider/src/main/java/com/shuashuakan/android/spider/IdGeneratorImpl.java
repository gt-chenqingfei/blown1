package com.shuashuakan.android.spider;

import android.support.annotation.NonNull;

import static com.shuashuakan.android.spider.RandomStringUtils.randomNumeric;

/**
 * Created by twocity on 16/8/30.
 */

class IdGeneratorImpl implements IdGenerator {
  // 设备标识  Android 为 1, iOS 为 2
  private static final String DEVICE_FLAG = "1";
  private static final String TRACE_ID_PREFIX = "T";
  private static final String SPAN_ID_PREFIX = "S";
  private static final String EVENT_ID_PREFIX = "E";
  private static final String PROGRAM_ID_PREFIX = "P";

  public static IdGenerator create() {
    return new IdGeneratorImpl();
  }

  private IdGeneratorImpl() {
    // private
  }

  @NonNull @Override public String generateTraceId() {
    return TRACE_ID_PREFIX + System.currentTimeMillis() + DEVICE_FLAG + randomNumeric(10);
  }

  @NonNull @Override public String generateSpanId() {
    return SPAN_ID_PREFIX + System.currentTimeMillis() + DEVICE_FLAG + randomNumeric(2);
  }

  @NonNull @Override public String generateEventId() {
    return EVENT_ID_PREFIX + System.currentTimeMillis() + DEVICE_FLAG + randomNumeric(2);
  }

  @NonNull @Override public String generateProgramId() {
    return PROGRAM_ID_PREFIX + System.currentTimeMillis() + DEVICE_FLAG + randomNumeric(2);
  }
}
