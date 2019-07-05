package com.shuashuakan.android.spider;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.shuashuakan.android.spider.event.EventEntry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;
import okio.GzipSink;
import okio.Okio;
import timber.log.Timber;

import static com.shuashuakan.android.spider.Utils.isEmpty;

/**
 * Created by twocity on 1/12/17.
 */

class EventsUploader {
    private static final String TEST_BASE_URL = "http://a1test.shuashuakan.net/1/pingback";
    private static final String BASE_URL = "https://a1.shuashuakan.net/1/pingback";
    private final OkHttpClient okHttpClient;
    private final JsonParser jsonParser;
    private final String requestUrl;

    EventsUploader(OkHttpClient okHttpClient, boolean debuggable) {
        this.okHttpClient = okHttpClient;
        this.jsonParser = new JsonParser();
        if (!debuggable) {
            this.requestUrl = BASE_URL;
        } else {
            this.requestUrl = TEST_BASE_URL;
        }
    }

    List<EventEntry> upload(List<EventEntry> eventEntries) throws Exception {
        if (!isEmpty(eventEntries)) {
            RequestBody requestBody = createRequestBody(eventEntries);
            Request request = new Request.Builder().url(requestUrl).post(requestBody).build();
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                response.close();
                Timber.d(response.message());
                return eventEntries;
            } else {
                String errorBody = response.body().string();
                response.body().close();
                throw new IOException("Upload failed: " + errorBody);
            }
        } else {
            return new ArrayList<>();
        }
    }

    private RequestBody createRequestBody(List<EventEntry> eventEntries) throws IOException {
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < eventEntries.size(); i++) {
            EventEntry event = eventEntries.get(i);
            Timber.d(event.rawData());
            jsonArray.add(jsonParser.parse(event.rawData()));
        }
        String json = jsonArray.toString();
        Spider.LOGGER.d(EventsUploader.class.getSimpleName(), "Prepare upload:\n%s", json);
        byte[] bytes = gzip(json.getBytes(StandardCharsets.UTF_8));
        String base64Message = ByteString.of(bytes).base64();
        return RequestBody.create(MediaType.parse("text/plain"), base64Message);
    }

    private static byte[] gzip(byte[] input) throws IOException {
        BufferedSink sink = null;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BufferedSource source = Okio.buffer(Okio.source(new ByteArrayInputStream(input)));
            sink = Okio.buffer(new GzipSink(Okio.sink(outputStream)));
            sink.writeAll(source);
            sink.close();
            return outputStream.toByteArray();
        } finally {
            Utils.closeQuietly(sink);
        }
    }
}
