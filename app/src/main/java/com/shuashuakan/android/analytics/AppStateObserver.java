package com.shuashuakan.android.analytics;

import android.util.Log;

import com.shuashuakan.android.ApplicationMonitor;
import com.shuashuakan.android.spider.AppStateListener;
import com.shuashuakan.android.spider.Spider;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/10/08
 * Description:
 */
public class AppStateObserver implements Observer<ApplicationMonitor.ApplicationState> {
    private final Spider spider;
    private int appState = 0;

    public static AppStateObserver create(Spider spider) {
        return new AppStateObserver(spider);
    }

    private AppStateObserver(Spider spider) {
        this.spider = spider;
    }

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onNext(ApplicationMonitor.ApplicationState applicationState) {
        switch (applicationState) {
            case BACKGROUND:
                spider.onAppStateChanged(AppStateListener.BACKGROUND);
                appState = AppStateListener.BACKGROUND;
                break;
            case FOREGROUND:
                spider.onAppStateChanged(AppStateListener.FOREGROUND);
                if (appState == AppStateListener.BACKGROUND) {
                    //从后台进入前台
                    spider.onAppStateChanged(AppStateListener.BACKTOFORE);
                }
                appState = AppStateListener.FOREGROUND;
                break;
            default:
                break;
        }
    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onComplete() {

    }
}
