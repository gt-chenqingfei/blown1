package com.shuashuakan.android.spider.event;

import com.google.gson.Gson;
import com.shuashuakan.android.spider.DigestCreator;
import com.shuashuakan.android.spider.Spider.MetadataProvider;

/**
 * Created by twocity on 1/12/17.
 */

public class SimpleTransformerFactory {

  private final Gson gson;
  private final DigestCreator digestCreator;
  private final MetadataProvider metadataProvide;

  public SimpleTransformerFactory(Gson gson, DigestCreator digestCreator,
      MetadataProvider metadataProvider) {
    this.gson = gson;
    this.metadataProvide = metadataProvider;
    this.digestCreator = digestCreator;
  }

  @SuppressWarnings("unchecked") public <T> EventEntryTransformer<T> ofEvent(Class<T> clazz) {
    if (clazz.equals(TraceEvent.class)) {
      return (EventEntryTransformer<T>) new TraceEventTransformer(gson, metadataProvide,
          digestCreator);
    } else if (clazz.equals(SpanEvent.class)) {
      return (EventEntryTransformer<T>) new SpanEventTransformer(gson, digestCreator);
    } else if (clazz.equals(NetworkEvent.class)) {
      return (EventEntryTransformer<T>) new NetworkEventTransformer(digestCreator, gson);
    } else if (clazz.equals(BusinessEvent.class)) {
      return (EventEntryTransformer<T>) new BusinessEventTransformer(digestCreator, gson);
    } else if (clazz.equals(ProgramEvent.class)) {
      return (EventEntryTransformer<T>) new ProgramEventTransformer(digestCreator, gson);
    } else {
      throw new IllegalStateException("Unknown event: " + clazz.getCanonicalName());
    }
  }
}
