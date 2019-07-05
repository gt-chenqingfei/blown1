package com.shuashuakan.android.spider;

import android.content.Context;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by twocity on 1/17/17.
 */

public final class SpiderInterceptor implements Interceptor {
  private static final String TAG = SpiderInterceptor.class.getSimpleName();
  private static final int SEQUENCE_MIN = 1000;
  private static final int SEQUENCE_MAX = 9999;
  private static final AtomicInteger requestId = new AtomicInteger(SEQUENCE_MIN);
  private static final String RICEBOOK_TRACE_HEADER = "x-ricebook-trace";
  private final Context context;
  private final Spider.DeviceIdManager deviceIdManager;
  private Spider spider;

  public SpiderInterceptor(Context context, Spider.DeviceIdManager deviceIdManager) {
    this.context = context;
    this.deviceIdManager = deviceIdManager;
  }

  private static String generateNetworkTraceId(Spider.DeviceIdManager idManager) {
    try {
      String deviceId = Utils.nullToEmpty(idManager.getDeviceId());
      if (!Utils.isBlank(deviceId)) {
        long now = System.currentTimeMillis();
        int sequence = getAndIncrement();
        long timestamp = now * 10000 + sequence;
        String suffix = Long.toString(timestamp, 36);
        Spider.LOGGER.d(TAG, "Build timestamp: %d --> %d --> %s", now, timestamp, suffix);
        return deviceId + '-' + suffix;
      }
    } catch (Exception ignored) {
    }

    return null;
  }

  private static synchronized int getAndIncrement() {
    requestId.compareAndSet(SEQUENCE_MAX, SEQUENCE_MIN);
    return requestId.getAndIncrement();
  }

  /**
   * 解决循环依赖问题，spider 不再作为构造函数参数传入。
   * Spider --> MetaDataProvider  --> HybridResManager --> Retrofit --> Spider
   */
  public void installSpider(Spider spider) {
    this.spider = spider;
  }

  @Override public Response intercept(Chain chain) throws IOException {
    long now = System.currentTimeMillis();
    String networkTraceId = generateNetworkTraceId(deviceIdManager);

    Request originalRequest = chain.request();

    if (Utils.isBlank(networkTraceId)) {
      return chain.proceed(originalRequest);
    } else {
      Spider.LOGGER.d(TAG, "x-ricebook-trace: %s", networkTraceId);
      Request newRequest =
          originalRequest.newBuilder().header(RICEBOOK_TRACE_HEADER, networkTraceId).build();
      try {
        Response response = chain.proceed(newRequest);
        // 只有 response header 带 `x-ricebook-trace` 的请求才统计
        String requestId = response.header(RICEBOOK_TRACE_HEADER, null);
        if (!Utils.isBlank(requestId)) {
          long tookMS = System.currentTimeMillis() - now;
          reportRequestEvent(requestId, response.code(), tookMS);
        }
        return response;
      } catch (IOException e) {
        if (Utils.isNetworkAvailable(context)) {
          reportRequestError(networkTraceId, e);
        }
        throw e;
      }
    }
  }

  private void reportRequestEvent(String requestId, int code, long tookMs) {
    if (spider != null) {
      spider.trackNetworkEvent(requestId, code, tookMs, null);
    }
  }

  private void reportRequestError(String requestId, Throwable throwable) {
    if (spider != null) {
      spider.trackNetworkEvent(requestId, -1, 0, throwable);
    }
  }
}
