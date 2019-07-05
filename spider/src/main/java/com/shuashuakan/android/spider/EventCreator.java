package com.shuashuakan.android.spider;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import java.util.Map;

import timber.log.Timber;

import static com.shuashuakan.android.spider.Utils.checkNotNull;
import static com.shuashuakan.android.spider.event.EventType.MANUALLY_USER_CUSTOM;

/**
 * Created by twocity on 9/19/16.
 */

public class EventCreator {
    private final Spider spider;
    private final String eventName;
    private final Map<String, Object> properties;

    EventCreator(Spider spider, String eventName) {
        this.spider = spider;
        this.eventName = eventName;
        this.properties = new ArrayMap<>();
    }

    public EventCreator put(@NonNull String key, @NonNull Object value) {
        properties.put(checkNotNull(key, "key == null"), checkNotNull(value, "value == null"));
        return this;
    }

    public EventCreator put(@NonNull String key, int value) {
        properties.put(checkNotNull(key, "key == null"), value);
        return this;
    }

    public EventCreator put(@NonNull String key, long value) {
        properties.put(checkNotNull(key, "key == null"), value);
        return this;
    }

    public EventCreator put(@NonNull String key, float value) {
        properties.put(checkNotNull(key, "key == null"), value);
        return this;
    }

    public EventCreator put(@NonNull String key, short value) {
        properties.put(checkNotNull(key, "key == null"), value);
        return this;
    }

    public EventCreator put(@NonNull String key, double value) {
        properties.put(checkNotNull(key, "key == null"), value);
        return this;
    }

    public EventCreator put(@NonNull String key, boolean value) {
        properties.put(checkNotNull(key, "key == null"), value);
        return this;
    }

    public EventCreator put(@NonNull Map<String, Object> map) {
        checkNotNull(map, "map == null");
        for (Map.Entry<String, Object> entrySet : map.entrySet()) {
            properties.put(checkNotNull(entrySet.getKey(), "key == null"), entrySet.getValue());
        }
        return this;
    }

    public void track() {
        try {
            properties.put("sm_id", spider.getSmId());
            Long userId = spider.getUserId();
            if (userId != null) {
                properties.put("userID", userId);
            }
            spider.submitBusinessEvent(MANUALLY_USER_CUSTOM, eventName, null, properties, null);
        } catch (Exception e) {
            Timber.e(e, "unexpected");
        }
    }
}
