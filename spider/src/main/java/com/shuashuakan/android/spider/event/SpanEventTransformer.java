package com.shuashuakan.android.spider.event;

import android.support.v4.util.ArrayMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shuashuakan.android.spider.DigestCreator;
import com.shuashuakan.android.spider.Utils;

import java.io.IOException;
import java.util.Map;

/**
 * Created by twocity on 1/12/17.
 */

class SpanEventTransformer implements EventEntryTransformer<SpanEvent> {
  private static final String TYPE_VALUE = "SPAN";
  private static final String TYPE_KEY = "type";
  private final DigestCreator digestCreator;
  private final Gson gson;

  SpanEventTransformer(Gson gson, DigestCreator digestCreator) {
    this.digestCreator = digestCreator;
    this.gson = gson;
  }

  @Override public EventEntry transform(SpanEvent spanEvent) throws IOException {
    Map<String, Object> traceData = new ArrayMap<>();
    if (Utils.isBlank(spanEvent.ssr()) && Utils.isBlank(spanEvent.span_name())) {
      throw new IOException("One of enjoy_url, span_name must be set");
    }
    traceData.put("trace_id", spanEvent.traceId());
    traceData.put("span_id", spanEvent.spanId());
    traceData.put(TYPE_KEY, TYPE_VALUE);
    if (!Utils.isBlank(spanEvent.ssr())) {
      traceData.put("ssr", spanEvent.ssr());
    } else {
      traceData.put("span_name", spanEvent.span_name());
    }
    traceData.put("timestamp", spanEvent.timestamp());
    traceData.put("digest", digestCreator.createDigest(traceData));

    TypeToken<Map<String, Object>> typeToken = new TypeToken<Map<String, Object>>() {
    };
    String jsonData = gson.toJson(traceData, typeToken.getType());
    return EventEntry.create(spanEvent.spanId() + "@" + System.currentTimeMillis(), jsonData);
  }
}
