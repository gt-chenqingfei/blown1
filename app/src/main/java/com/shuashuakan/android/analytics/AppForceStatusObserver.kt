package com.shuashuakan.android.analytics

import com.shuashuakan.android.ApplicationMonitor
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 * @author hushiguang
 * @since 2019-06-10.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
class AppForceStatusObserver(
        private val onChangeStatus: (ApplicationMonitor.ApplicationState) -> Unit)
    : Observer<ApplicationMonitor.ApplicationState> {
    override fun onComplete() {

    }

    override fun onSubscribe(d: Disposable) {
    }

    override fun onNext(t: ApplicationMonitor.ApplicationState) {
        onChangeStatus.invoke(t)
    }

    override fun onError(e: Throwable) {

    }
}