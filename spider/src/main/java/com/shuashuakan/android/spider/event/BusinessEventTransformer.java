package com.shuashuakan.android.spider.event;

import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shuashuakan.android.spider.DigestCreator;
import com.shuashuakan.android.spider.Utils;
import okio.ByteString;

import java.io.IOException;
import java.util.Map;

import static com.shuashuakan.android.spider.event.EventType.MANUALLY_USER_CUSTOM;

/**
 * Created by twocity on 1/12/17.
 */

class BusinessEventTransformer implements EventEntryTransformer<BusinessEvent> {
  private static final String TYPE_VALUE = "EVENT";
  private static final String TYPE_KEY = "type";
  private final DigestCreator digestCreator;
  private final Gson gson;
  private final TypeToken<Map<String, Object>> typeToken = new TypeToken<Map<String, Object>>() {
  };

  BusinessEventTransformer(DigestCreator digestCreator, Gson gson) {
    this.digestCreator = digestCreator;
    this.gson = gson;
  }

  @Override public EventEntry transform(BusinessEvent event) throws IOException {
    Map<String, Object> traceData = new ArrayMap<>();
    traceData.put("trace_id", event.traceId());
    traceData.put("span_id", event.spanId());
    traceData.put("event_id", event.eventId());
    traceData.put(TYPE_KEY, TYPE_VALUE);
    if (MANUALLY_USER_CUSTOM.equals(event.eventType())) {
      traceData.put("event_name", event.eventName());
    } else {
      traceData.put("event_type", event.eventType().name());
    }
    // the trace_meta
    Map<String, Object> properties = event.rawTraceMetadata();
    if (!Utils.isBlank(event.traceMetadata())) {
      traceData.put("trace_metadata", event.traceMetadata());
    } else if (properties != null && !properties.isEmpty()) {
      String json = gson.toJson(event.rawTraceMetadata(), typeToken.getType());
      traceData.put("trace_metadata", ByteString.encodeUtf8(json).base64());
    }

    // the view_path
    if (!Utils.isBlank(event.viewPath())) {
      traceData.put("view_path", event.viewPath());
    }

    traceData.put("timestamp", event.timestamp());

    traceData.put("digest", digestCreator.createDigest(traceData));
    String jsonData = gson.toJson(traceData, typeToken.getType());
    return EventEntry.create(event.eventId() + "@" + System.currentTimeMillis(), jsonData);
  }
}
