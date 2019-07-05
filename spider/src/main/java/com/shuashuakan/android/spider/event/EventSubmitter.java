package com.shuashuakan.android.spider.event;

import android.support.annotation.NonNull;

/**
 * Created by twocity on 01/03/2017.
 */

public interface EventSubmitter {

  void submit(@NonNull TraceEvent event);

  void submit(@NonNull SpanEvent event);

  void submit(@NonNull BusinessEvent event);

  void submit(@NonNull NetworkEvent event);

  void submit(@NonNull ProgramEvent event);
}
