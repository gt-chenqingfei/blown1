package com.shuashuakan.android.spider.event;

import android.support.v4.util.ArrayMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shuashuakan.android.spider.DigestCreator;
import com.shuashuakan.android.spider.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by twocity on 01/03/2017.
 */

class ProgramEventTransformer implements EventEntryTransformer<ProgramEvent> {
  private static final String TYPE_VALUE = "PROGRAM";
  private static final String TYPE_KEY = "type";
  private final DigestCreator digestCreator;
  private final Gson gson;
  private final TypeToken<Map<String, Object>> typeToken = new TypeToken<Map<String, Object>>() {
  };

  ProgramEventTransformer(DigestCreator digestCreator, Gson gson) {
    this.digestCreator = digestCreator;
    this.gson = gson;
  }

  @Override public EventEntry transform(ProgramEvent event) throws IOException {
    Map<String, Object> traceData = new ArrayMap<>();
    traceData.put(TYPE_KEY, TYPE_VALUE);
    traceData.put("name", event.programName());
    traceData.put("trace_id", event.traceId());
    traceData.put("program_id", event.programId());
//    if (!Utils.isBlank(event.spanId())) {
//      traceData.put("span_id", event.spanId());
//    }

    Map<String, Object> properties = event.content();
    if (properties != null && !properties.isEmpty()) {
      String json = gson.toJson(properties, typeToken.getType());
      traceData.put("content", encodeUrl(json));
    }

    traceData.put("digest", digestCreator.createDigest(traceData));
    String jsonData = gson.toJson(traceData, typeToken.getType());
    return EventEntry.create(event.programId() + "@" + System.currentTimeMillis(), jsonData);
  }

  private static String encodeUrl(String value) throws UnsupportedEncodingException {
    String encoded = URLEncoder.encode(String.valueOf(value), "UTF-8");
    if (value.indexOf(' ') != -1) {
      return encoded.replace("+", "%20");
    }
    return encoded;
  }
}
