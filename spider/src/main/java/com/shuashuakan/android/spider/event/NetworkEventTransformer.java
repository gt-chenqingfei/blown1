package com.shuashuakan.android.spider.event;

import android.support.v4.util.ArrayMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shuashuakan.android.spider.DigestCreator;
import com.shuashuakan.android.spider.Utils;

import java.io.IOException;
import java.util.Map;

/**
 * Created by twocity on 1/17/17.
 */

class NetworkEventTransformer implements EventEntryTransformer<NetworkEvent> {
  private static final String TYPE_VALUE = "NETWORK";
  private static final String TYPE_KEY = "type";
  private final DigestCreator digestCreator;
  private final Gson gson;
  private final TypeToken<Map<String, Object>> typeToken = new TypeToken<Map<String, Object>>() {
  };

  NetworkEventTransformer(DigestCreator digestCreator, Gson gson) {
    this.digestCreator = digestCreator;
    this.gson = gson;
  }

  @Override public EventEntry transform(NetworkEvent event) throws IOException {
    Map<String, Object> traceData = new ArrayMap<>();
    traceData.put("trace_id", event.traceId());
    traceData.put(TYPE_KEY, TYPE_VALUE);
    if (!Utils.isBlank(event.spanId())) {
      traceData.put("span_id", event.spanId());
    }
    traceData.put("request_id", event.requestId());
    if (event.failedReason() == null) {
      traceData.put("status_code", event.statusCode());
      traceData.put("took_time", event.tookMS());
    } else {
      traceData.put("failed_reason", event.failedReason());
    }

    traceData.put("digest", digestCreator.createDigest(traceData));
    String jsonData = gson.toJson(traceData, typeToken.getType());
    return EventEntry.create(event.requestId() + "@" + System.currentTimeMillis(), jsonData);
  }
}
