package com.shuashuakan.android.spider;

import android.support.annotation.NonNull;

/**
 * Created by twocity on 16/8/30.
 */

interface IdGenerator {
  @NonNull String generateTraceId();

  @NonNull String generateSpanId();

  @NonNull String generateEventId();

  @NonNull String generateProgramId();
}
