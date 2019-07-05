package com.shuashuakan.android.spider;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.GsonBuilder;
import com.shuashuakan.android.spider.event.*;

import timber.log.Timber;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * Created by twocity on 1/12/17.
 */

class EventStorageManager {
    private static final String TAG = EventStorageManager.class.getSimpleName();
    private static final String THREAD_NAME = "enjoy-event-storage-thread";
    private final EventStorage eventStorage;
    private final ExecutorService executorService;
    private final SimpleTransformerFactory factory;
    private final Dispatcher dispatcher;

    EventStorageManager(DigestCreator digestCreator, Spider.MetadataProvider metadataProvider,
                        EventStorage eventStorage, Dispatcher dispatcher) {
        this.eventStorage = eventStorage;
        this.dispatcher = dispatcher;
        this.factory = new SimpleTransformerFactory(new GsonBuilder().disableHtmlEscaping().create(),
                digestCreator, metadataProvider);
        executorService = newSingleThreadExecutor();
    }

    void submit(@NonNull TraceEvent event) {
        executorService.execute(
                new PutRunnable<>(event, factory.ofEvent(TraceEvent.class), eventStorage, dispatcher));
    }

    void submit(@NonNull SpanEvent event) {
        PutRunnable<SpanEvent> runnable =
                new PutRunnable<>(event, factory.ofEvent(SpanEvent.class), eventStorage, dispatcher);
        executorService.execute(runnable);
    }

    void submit(@NonNull BusinessEvent event) {
        PutRunnable<BusinessEvent> runnable =
                new PutRunnable<>(event, factory.ofEvent(BusinessEvent.class), eventStorage, dispatcher);
        executorService.execute(runnable);

        if (BuildConfig.DEBUG) {
            Timber.d(event.eventName() + " ->  " + event.rawTraceMetadata());
        }
    }

    void submit(@NonNull NetworkEvent event) {
        PutRunnable<NetworkEvent> runnable =
                new PutRunnable<>(event, factory.ofEvent(NetworkEvent.class), eventStorage, dispatcher);
        executorService.execute(runnable);
    }

    void submit(@NonNull ProgramEvent event) {
        PutRunnable<ProgramEvent> runnable =
                new PutRunnable<>(event, factory.ofEvent(ProgramEvent.class), eventStorage, dispatcher);
        executorService.execute(runnable);
//    if (event.programName().equals("AppEnd")){
//
//      dispatcher.dispatchEventSaved();
//    }

        if (BuildConfig.DEBUG) {
            Timber.d(event.programName() + " ->  " + event.content());
        }
    }

    List<EventEntry> readByCount(long count) throws IOException {
        Utils.checkState(count > 0, "count must > 0 ");
        long maxSize = eventStorage.size();
        return readByCount(count, maxSize);
    }

    List<EventEntry> readAll() throws IOException {
        long maxSize = eventStorage.size();
        return readByCount(maxSize, maxSize);
    }

    private List<EventEntry> readByCount(long count, long max) throws IOException {
        if (count > 0 && max > 0) {
            return eventStorage.query(Math.min(count, max));
        } else {
            dispatcher.dispatchEventsEmpty();
            return Collections.emptyList();
        }
    }

    void removeEvents(List<EventEntry> entries) {
        executorService.execute(new RemoveRunnable(eventStorage, entries, dispatcher));
    }

    private final static class PutRunnable<T> implements Runnable {
        private final EventEntryTransformer<T> eventEntryTransformer;
        private final EventStorage eventStorage;
        private final T data;
        private final Dispatcher dispatcher;

        private PutRunnable(T t, EventEntryTransformer<T> eventEntryTransformer,
                            EventStorage eventStorage, Dispatcher dispatcher) {
            this.data = t;
            this.dispatcher = dispatcher;
            this.eventEntryTransformer = eventEntryTransformer;
            this.eventStorage = eventStorage;
        }

        @Override
        public void run() {
            try {
                EventEntry eventEntry = eventEntryTransformer.transform(data);
                eventStorage.put(eventEntry);
                dispatcher.dispatchEventSaved(eventEntry);
                if (eventStorage.size() > 9) {
                    dispatcher.numMaxUploader();
                }
            } catch (Exception e) {
                Timber.tag(TAG).e(e, "Save event failed.");
            }

        }
    }

    private static class RemoveRunnable implements Runnable {
        private final EventStorage eventStorage;
        private final List<EventEntry> eventEntries;
        private final Dispatcher dispatcher;

        private RemoveRunnable(EventStorage eventStorage, List<EventEntry> eventEntries,
                               Dispatcher dispatcher) {
            this.eventStorage = eventStorage;
            this.eventEntries = eventEntries;
            this.dispatcher = dispatcher;
        }

        @Override
        public void run() {
            try {
                eventStorage.removeAll(eventEntries);
                dispatcher.dispatchEventsRemoved(eventEntries);
            } catch (Exception e) {
                Timber.tag(TAG).e(e, "Remove events failed.");
            }
        }
    }
}
