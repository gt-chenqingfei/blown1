package com.shuashuakan.android.spider;

import android.annotation.TargetApi;

import com.shuashuakan.android.spider.event.EventEntry;

import okhttp3.OkHttpClient;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by twocity on 1/12/17.
 */

class EventUploadManager {
    static final int COUNT_PER_REQUEST = 10;
    private static final int UPLOAD_THREAD_SIZE = 2;
    private static final String THREAD_PREFIX = "spider-events-uploader";
    private static final String TAG = EventUploadManager.class.getSimpleName();
    private static final Func1<List<EventEntry>, Observable<EventEntry>> FLAT_LIST = Observable::from;
    private final EventStorageManager eventStorageManager;
    private final Dispatcher dispatcher;
    private final EventsUploader eventsUploader;
    private final Set<EventEntry> pendingEvents =
            Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Executor workingExecutor;

    EventUploadManager(EventStorageManager eventStorageManager, OkHttpClient okHttpClient,
                       boolean debuggable, Dispatcher dispatcher) {
        this.eventStorageManager = eventStorageManager;
        this.dispatcher = dispatcher;
        this.eventsUploader = new EventsUploader(okHttpClient, debuggable);
        this.workingExecutor = Executors.newFixedThreadPool(UPLOAD_THREAD_SIZE);
    }

    void uploadByCount(int count) {
        upload(() -> eventStorageManager.readByCount(count));
    }

    void flushStorage() {
        Spider.LOGGER.d(TAG, "Flushing EventStorage...");
        upload(() -> eventStorageManager.readAll());
    }

    @SuppressWarnings("RxLeakedSubscription")
    @TargetApi(19)
    private void upload(Callable<List<EventEntry>> callable) {
        uploadObservable(callable).subscribeOn(Schedulers.io())
                .subscribe(dispatcher::dispatchEventsSend,
                        throwable -> {
                            Spider.LOGGER.e(TAG, throwable, "Upload error");
                            dispatcher.dispatchEventsError();
                        }, () -> {

                        });
    }

    private Observable<List<EventEntry>> uploadObservable(Callable<List<EventEntry>> source) {
        return Observable.fromCallable(source)
                .flatMap(FLAT_LIST)
                .filter(eventEntry -> {
                    boolean keep = !pendingEvents.contains(eventEntry);
                    if (!keep) Spider.LOGGER.d(TAG, "Ignore: %s", eventEntry.identity());
                    return keep;
                })
                .buffer(COUNT_PER_REQUEST)
                .filter(eventEntries -> !eventEntries.isEmpty())
                .flatMap(new Func1<List<EventEntry>, Observable<List<EventEntry>>>() {
                    @Override
                    public Observable<List<EventEntry>> call(List<EventEntry> eventEntries) {
                        return Observable.fromCallable(
                                new UploadCallable(pendingEvents, eventsUploader, eventEntries))
                                .subscribeOn(Schedulers.from(workingExecutor));
                    }
                });
    }

    private final static class UploadCallable implements Callable<List<EventEntry>> {
        private final Set<EventEntry> pending;
        private final EventsUploader uploader;
        private final List<EventEntry> eventEntries;

        UploadCallable(Set<EventEntry> pending, EventsUploader uploader,
                       List<EventEntry> eventEntries) {
            this.pending = pending;
            this.uploader = uploader;
            this.eventEntries = eventEntries;
        }

        @Override
        public List<EventEntry> call() throws Exception {
            try {
                Spider.LOGGER.d(TAG, "Upload @%s", Thread.currentThread().getName());
                pending.addAll(eventEntries);
                uploader.upload(eventEntries);
            } finally {
                pending.removeAll(eventEntries);
            }
            return eventEntries;
        }
    }
}
