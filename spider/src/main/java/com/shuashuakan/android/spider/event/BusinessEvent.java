package com.shuashuakan.android.spider.event;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import java.util.Map;

/**
 * Created by twocity on 16/8/30.
 */

@AutoValue
abstract public class BusinessEvent {
    public abstract String eventName();

    public abstract EventType eventType();

    public abstract String spanId();

    public abstract String eventId();

    public abstract String traceId();

    public abstract @Nullable
    String traceMetadata();

    public abstract @Nullable
    String viewPath();

    public abstract @Nullable
    Map<String, Object> rawTraceMetadata();

    public abstract long timestamp();

    public static BusinessEvent.Builder newBuilder() {
        return new AutoValue_BusinessEvent.Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof BusinessEvent) {
            BusinessEvent that = (BusinessEvent) o;
            return (eventId().equals(that.eventId()));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int h = 1;
        h *= 1000003;
        h ^= this.spanId().hashCode();
        return h;
    }

    @AutoValue.Builder
    public interface Builder {

        Builder eventName(String eventName);

        Builder spanId(String spanId);

        Builder eventId(String eventId);

        Builder traceId(String traceId);


        Builder eventType(EventType eventType);

        Builder viewPath(String viewPath);

        Builder traceMetadata(String traceMetadata);

        Builder rawTraceMetadata(Map<String, Object> objectMap);

        Builder timestamp(long timestamp);

        BusinessEvent build();
    }
}
