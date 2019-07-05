package com.shuashuakan.android.spider;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.shuashuakan.android.spider.auto.TraceableView;
import com.shuashuakan.android.spider.auto.ViewMonitor;
import com.shuashuakan.android.spider.auto.ViewProxyFactory;
import com.shuashuakan.android.spider.event.BusinessEvent;
import com.shuashuakan.android.spider.event.EventEntry;
import com.shuashuakan.android.spider.event.EventType;
import com.shuashuakan.android.spider.event.NetworkEvent;
import com.shuashuakan.android.spider.event.ProgramEvent;
import com.shuashuakan.android.spider.event.SpanEvent;
import com.shuashuakan.android.spider.event.TraceEvent;

import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import timber.log.Timber;

import static com.shuashuakan.android.spider.Utils.checkNotNull;
import static java.lang.System.currentTimeMillis;

/**
 * Created by twocity on 16/8/25.
 */

public class Spider
        implements AppStateListener, ViewProxyFactory, ViewMonitor.ViewMonitorFactory, Handler.Callback {
    private static final boolean DEBUG = false;
    static final Logger LOGGER = new Logger() {
        @Override
        public void d(String tag, String message, Object... args) {
            if (DEBUG) Timber.tag(tag).d(message, args);
        }

        @Override
        public void i(String tag, String message, Object... args) {
            if (DEBUG) Timber.tag(tag).i(message, args);
        }

        @Override
        public void w(String tag, String message, Object... args) {
            if (DEBUG) Timber.tag(tag).w(message, args);
        }

        @Override
        public void e(String tag, String message, Object... args) {
            if (DEBUG) Timber.tag(tag).e(message, args);
        }

        @Override
        public void e(String tag, Throwable throwable, String message, Object... args) {
            if (DEBUG) Timber.tag(tag).e(throwable, message, args);
        }
    };
    private static final int MSG_RESET_TRACE = 42;
    private static final int MSG_REPORT_FOREGROUND = 43;
    private static final int MSG_REPORT_BACKGROUND = 44;
    private static final int MSG_REPORT_BACKTOFORE = 404;
    private static final int MSG_PAUSE_FLUSH = 45;
    private static final long FIVE_MINUTES = 5 * 60 * 1000;
    final Context context;
    final EventStorageManager eventStorageManager;
    final EventListenerManager listenerManager;
    final EventUploadManager uploadManager;
    private final IdGenerator idGenerator = IdGeneratorImpl.create();
    private final SpanEventManager spanEventManager;
    private final Dispatcher dispatcher;
    private final Handler handler;
    private final MetadataProvider metadataProvider;
    private boolean isNewSession;
    private String currentTraceId;
    private String smId;
    private final PageTracer pageTracer = new PageTracer() {
        @Override
        public void reportPageCreated(@NonNull Object page) {
            // 保证 trace id 在 span id 之前创建
            ensureTraceId();
            spanEventManager.reportPageCreated(page);
        }

        @Override
        public void reportPageShown(@NonNull Object page, @Nullable String ssr, String pageTitle) {
            spanEventManager.reportPageShown(page, ssr, pageTitle);
        }
    };

    private final DeviceIdManager.DeviceIdChangedListener idChangedListener = new DeviceIdManager.DeviceIdChangedListener() {
        @Override
        public void onDeviceIdChanged(String oldId, String newId) {
            if (!TextUtils.isEmpty(oldId) && !TextUtils.isEmpty(newId)) {
                Timber.d("Device id changed, %s ---> %s", oldId, newId);
                programEvent(SpiderEventNames.Device_ID_Changed).put("new", newId).put("old", oldId).track();
            }
        }
    };

    /**
     * debuggable 为 true 的话会使用测试环境
     **/
    public Spider(Context context, MetadataProvider metadataProvider, OkHttpClient okHttpClient,
                  boolean debuggable, DeviceIdManager deviceIdManager) {
        this.context = context;
        EventStorage eventStorage = new SqlEventStorage(context);
        this.spanEventManager = new SpanEventManager(this, idGenerator);
        this.dispatcher = new Dispatcher(this);
        DigestCreator digestCreator = new HMACDigestCreator(this::currentTraceId);
        this.eventStorageManager =
                new EventStorageManager(digestCreator, metadataProvider, eventStorage, dispatcher);
        this.metadataProvider = metadataProvider;
        this.listenerManager = new EventListenerManager();
        this.listenerManager.addEventListener(new LoggingListener());
        this.uploadManager =
                new EventUploadManager(eventStorageManager, okHttpClient, debuggable, dispatcher);
        this.handler = new Handler(Looper.getMainLooper(), this);
        deviceIdManager.setListener(idChangedListener);
    }

    public void setSmId(String smId) {
        this.smId = smId;
    }

    public String getSmId() {
        return smId;
    }

    @Nullable
    public Long getUserId() {
        return metadataProvider.getUserId();
    }

    /**
     * 开始统计业务点,返回 {@link EventCreator}, a fluent api
     *
     * @param eventName 业务点名称, 非 null
     */
    public EventCreator manuallyEvent(@NonNull String eventName) {
        return new EventCreator(this, checkNotNull(eventName, "eventName ==null"));
    }

    public AutomaticEventCreator autoEvent() {
        return new AutomaticEventCreator(this);
    }

    public ProgramEventCreator programEvent(@NonNull String eventName) {
        return new ProgramEventCreator(this, checkNotNull(eventName, "eventName ==null"));
    }

    public void submitRawTraceMeta(@Nullable String traceMeta) {
        if (!TextUtils.isEmpty(traceMeta)) {
            autoEvent().traceMeta(traceMeta).track();
        }
    }

    /**
     * 统计 Span 点
     *
     * @param span_name 当前页面的名称
     * @param spanId    当前页面的 span id
     * @param enjoyUrl  当前页面的 ENJOY url
     */
    void trackSpanEvent(@NonNull String span_name, String spanId, @Nullable String enjoyUrl) {
        SpanEvent event =
                SpanEvent.create(ensureTraceId(), spanId, enjoyUrl, span_name, currentTimeMillis());
        eventStorageManager.submit(event);
        trackTraceEventIfNeeded(enjoyUrl);
        LOGGER.d("Spider", "Track Span: %s(%s)", span_name, Utils.valueOrDefault(enjoyUrl, "N/A"));
    }

    /**
     * 如果是新的 session 开始, 则统计当前的 trace id
     */
    private void trackTraceEventIfNeeded(@SuppressWarnings("unused") String enjoyUrl) {
        if (isNewSession) {
            isNewSession = false;
            eventStorageManager.submit(TraceEvent.create(ensureTraceId()));
        }
    }

    /**
     * 统计业务点
     */
    void submitBusinessEvent(EventType eventType, String eventName, String metadata,
                             Map<String, Object> properties, String viewPath) {
        String spanId = spanEventManager.currentSpanId();
        if (Utils.isBlank(spanId)) {
            Timber.w("Drop event: %s, can't find current span id.", eventName);
        } else {
            if (eventName == null) {
                eventName = String.valueOf(System.currentTimeMillis());
            }
            BusinessEvent event = BusinessEvent.newBuilder()
                    .traceId(currentTraceId())
                    .spanId(spanId)
                    .eventId(idGenerator.generateEventId())
                    .eventName(eventName)
                    .eventType(eventType)
                    .traceMetadata(metadata)
                    .rawTraceMetadata(properties)
                    .viewPath(viewPath)
                    .timestamp(System.currentTimeMillis())
                    .build();

            eventStorageManager.submit(event);
        }
    }

    void submitProgramEvent(String eventName, @Nullable Map<String, Object> properties) {
        eventStorageManager.submit(ProgramEvent.newBuilder()
                .traceId(ensureTraceId())
                .spanId(spanEventManager.currentSpanId())
                .programId(idGenerator.generateProgramId())
                .programName(eventName)
                .content(properties)
                .build());
    }

    void trackNetworkEvent(String requestId, int code, long tookMs, Throwable e) {
        NetworkEvent event = NetworkEvent.newBuilder()
                .traceId(currentTraceId())
                .spanId(spanEventManager.currentSpanId())
                .requestId(requestId)
                .tookMS((int) tookMs)
                .statusCode(code)
                .failedReason(e == null ? null : e.getMessage())
                .build();
        eventStorageManager.submit(event);
    }

    @NonNull
    private String currentTraceId() {
        return ensureTraceId();
    }

    public PageTracer pageTracer() {
        return this.pageTracer;
    }

    @Nullable
    String currentSpanName() {
        return spanEventManager.currentSpanName();
    }

    @Override
    public void onAppStateChanged(int state) {
        if (AppStateListener.FOREGROUND == state) {
            handler.sendEmptyMessage(MSG_REPORT_FOREGROUND);
            handler.removeMessages(MSG_RESET_TRACE);
            dispatcher.resumeAutoUploader();
        } else if (AppStateListener.BACKGROUND == state) {
            // app 切到后台时自动打一个 `Background` 的点
            handler.sendEmptyMessage(MSG_REPORT_BACKGROUND);
            // 不再上传点
            // 同时 flush event storage
            handler.sendEmptyMessageDelayed(MSG_PAUSE_FLUSH, 400);
            // reset trace id
//            handler.sendEmptyMessageDelayed(MSG_RESET_TRACE, FIVE_MINUTES);
        } else if (AppStateListener.BACKTOFORE == state) {
            handler.sendEmptyMessage(MSG_REPORT_BACKTOFORE);
        } else {
            throw new IllegalStateException("UnKnown state: " + state);
        }
    }

    private synchronized String ensureTraceId() {
        if (currentTraceId == null) {
            currentTraceId = idGenerator.generateTraceId();
            LOGGER.i("Spider", "New TraceId: %s", currentTraceId);
            isNewSession = true;
        }
        return currentTraceId;
    }

    private synchronized void resetTraceId() {
        LOGGER.i("Spider", "Reset trace id.");
        currentTraceId = null;
    }

    @NonNull
    @Override
    public TraceableView.ViewProxy createViewProxy() {
        return new ViewProxyImpl(new ViewStateTracker(this));
    }

    @Override
    public ViewMonitor createViewMonitor() {
        return new ViewStateTracker(this);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_RESET_TRACE:
                resetTraceId();
                break;
            case MSG_REPORT_FOREGROUND:
                programEvent(SpiderEventNames.FOREGROUND_EVENT).track();
                break;
            case MSG_REPORT_BACKGROUND:
                programEvent(SpiderEventNames.Program.BACKGROUND_EVENT).track();
                break;
            case MSG_REPORT_BACKTOFORE:
                programEvent(SpiderEventNames.Program.APP_START).put("startupType", "resumeFromBackground").track();
                break;
            case MSG_PAUSE_FLUSH:
                dispatcher.pauseAutoUploader();
                dispatcher.dispatchFlush();
                break;
            default:
                break;
        }
        return true;
    }

    public interface MetadataProvider {
        @NonNull
        Map<String, Object> get();

        @Nullable
        Long getUserId();
    }

    public interface PageTracer {

        void reportPageCreated(@NonNull Object page);

        void reportPageShown(@NonNull Object page, @Nullable String ssr, @Nullable String pageTitle);
    }

    interface EventListener {
        void onEventSaved(EventEntry eventEntry);

        void onEventsUpload(List<EventEntry> events);

        void onEventsRemoved(List<EventEntry> events);
    }

    interface Logger {
        void d(String tag, String message, Object... args);

        void i(String tag, String message, Object... args);

        void w(String tag, String message, Object... args);

        void e(String tag, String message, Object... args);

        void e(String tag, Throwable throwable, String message, Object... args);
    }

    private static class LoggingListener implements EventListener {

        @Override
        public void onEventSaved(EventEntry eventEntry) {
            LOGGER.d("Spider", "Put %s", eventEntry.identity());
        }

        @Override
        public void onEventsUpload(List<EventEntry> events) {
            LOGGER.d("Spider", "%d events uploaded", events.size());
        }

        @Override
        public void onEventsRemoved(List<EventEntry> events) {
            LOGGER.d("Spider", "%d events removed", events.size());
        }
    }

    public interface DeviceIdManager {
        @NonNull
        String getDeviceId();

        void setListener(DeviceIdChangedListener l);

        interface DeviceIdChangedListener {
            void onDeviceIdChanged(String oldId, String newId);
        }
    }
}
