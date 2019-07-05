package com.shuashuakan.android.spider;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.shuashuakan.android.spider.event.EventEntry;

import java.util.List;

import timber.log.Timber;

/**
 * Created by twocity on 1/12/17.
 */

class Dispatcher implements Handler.Callback {
    private static final int SEND_DELAY = 6 * 1000;

    @SuppressWarnings("unused")
    private static final int MSG_EVENT_SUBMIT = 1;
    private static final int MSG_EVENT_SAVED = 2;
    private static final int MSG_EVENT_SENT = 3;
    private static final int MSG_EVENT_REMOVED = 4;
    private static final int MSG_SEND_EVENT_PERIOD = 5;
    private static final int MSG_FLUSH = 6;
    private static final int MSG_SEND_EVENT_NUM = 7;
    private boolean isNowSend = false;

    private final Handler handler = new Handler(Looper.getMainLooper(), this);
    private final Spider spider;

    Dispatcher(Spider spider) {
        this.spider = spider;
    }

    void pauseAutoUploader() {
        handler.removeMessages(MSG_SEND_EVENT_PERIOD);
    }

    void resumeAutoUploader() {
        handler.sendEmptyMessageDelayed(MSG_SEND_EVENT_PERIOD, SEND_DELAY);
    }

    void numMaxUploader() {
        handler.sendEmptyMessage(MSG_SEND_EVENT_NUM);
    }

    void dispatchEventSaved(EventEntry eventEntry) {
        Message message = handler.obtainMessage(MSG_EVENT_SAVED, eventEntry);
        handler.sendMessage(message);
    }

    void dispatchEventsSend(List<EventEntry> entries) {
        Message message = handler.obtainMessage(MSG_EVENT_SENT, entries);
        handler.sendMessage(message);
    }

    void dispatchEventsRemoved(List<EventEntry> entries) {
        Message message = handler.obtainMessage(MSG_EVENT_REMOVED, entries);
        handler.sendMessage(message);
    }

    //可能上传失败  上传失败后 isNowSend=false  达到十个继续触发上传  不然 永远不会触发上传事件
    void dispatchEventsError() {
        isNowSend = false;
        handler.removeMessages(MSG_SEND_EVENT_PERIOD);
        handler.sendEmptyMessageDelayed(MSG_SEND_EVENT_PERIOD, SEND_DELAY);
    }

    //当Storge存的List为空 就不向下执行 导致 isNowSen一直为true
    void dispatchEventsEmpty() {
        isNowSend = false;
        handler.removeMessages(MSG_SEND_EVENT_PERIOD);
        handler.sendEmptyMessageDelayed(MSG_SEND_EVENT_PERIOD, SEND_DELAY);
    }

    void dispatchFlush() {
        handler.sendEmptyMessage(MSG_FLUSH);
    }

    private void handleEventSaved(EventEntry eventEntry) {
        spider.listenerManager.onEventSaved(eventEntry);
    }

    private void handleEventsSent(List<EventEntry> entries) {
        spider.listenerManager.onEventsUpload(entries);
        spider.eventStorageManager.removeEvents(entries);
    }

    private void handleEventsRemoved(List<EventEntry> entries) {
        spider.listenerManager.onEventsRemoved(entries);
        isNowSend = false;
        handler.removeMessages(MSG_SEND_EVENT_PERIOD);
        handler.sendEmptyMessageDelayed(MSG_SEND_EVENT_PERIOD, SEND_DELAY);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_EVENT_SAVED:
                handleEventSaved((EventEntry) msg.obj);
                break;
            case MSG_EVENT_SENT:
                handleEventsSent((List<EventEntry>) msg.obj);
                break;
            case MSG_EVENT_REMOVED:
                handleEventsRemoved((List<EventEntry>) msg.obj);
                break;
            case MSG_FLUSH:
                spider.uploadManager.flushStorage();
                break;
            case MSG_SEND_EVENT_PERIOD:
                if (!isNowSend) {
//          handler.sendEmptyMessageDelayed(MSG_SEND_EVENT_PERIOD, SEND_DELAY);
                    isNowSend = true;
                    spider.uploadManager.uploadByCount(EventUploadManager.COUNT_PER_REQUEST);

                    //大招  规避 未知错误
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isNowSend) {
                                dispatchEventsEmpty();
                            }
                        }
                    }, 15000);
                }
                break;
            case MSG_SEND_EVENT_NUM:
                if (!isNowSend) {
                    isNowSend = true;
                    spider.uploadManager.uploadByCount(EventUploadManager.COUNT_PER_REQUEST);
                }
                break;
            default:
                return false;
        }
        return true;
    }
}
