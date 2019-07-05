package com.shuashuakan.android.spider.event;

import android.support.v4.util.ArrayMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shuashuakan.android.spider.DigestCreator;
import com.shuashuakan.android.spider.Spider.MetadataProvider;
import com.shuashuakan.android.spider.Utils;

import java.io.IOException;
import java.util.Map;

/**
 * Created by twocity on 1/12/17.
 */

class TraceEventTransformer implements EventEntryTransformer<TraceEvent> {
  private static final String TYPE_VALUE = "TRACE";
  private static final String TYPE_KEY = "type";
  private final MetadataProvider metadataProvider;
  private final DigestCreator digestCreator;
  private final Gson gson;

  TraceEventTransformer(Gson gson, MetadataProvider metadataProvider, DigestCreator digestCreator) {
    this.metadataProvider = metadataProvider;
    this.digestCreator = digestCreator;
    this.gson = gson;
  }

  @Override public EventEntry transform(TraceEvent traceEvent) throws IOException {
    Map<String, Object> traceData = new ArrayMap<>();
    traceData.put("trace_id", traceEvent.traceId());
    traceData.put(TYPE_KEY, TYPE_VALUE);

    Map<String, Object> metadata = metadataProvider.get();
    for (Map.Entry<String, Object> entry : metadata.entrySet()) {
      Object value = entry.getValue();
      if (!Utils.isBlank(String.valueOf(value))) {
        traceData.put(entry.getKey(), value);
      }
    }

    traceData.put("digest", digestCreator.createDigest(traceData));

    TypeToken<Map<String, Object>> typeToken = new TypeToken<Map<String, Object>>() {
    };
    String jsonData = gson.toJson(traceData, typeToken.getType());
    return EventEntry.create(traceEvent.traceId() + "@" + System.currentTimeMillis(), jsonData);
  }
}
