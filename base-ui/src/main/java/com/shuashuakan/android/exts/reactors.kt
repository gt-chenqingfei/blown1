package com.shuashuakan.android.exts

import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.exts.mvp.ApiError.*
import com.shuashuakan.android.utils.toApiError
import com.uber.autodispose.ObservableSubscribeProxy
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber


private val onCompleteStub: () -> Unit = {}
private val onErrorHandlerSub: (ApiError) -> Unit = {
    when (it) {
        is NetworkError -> Timber.w(it.throwable, "Network Error")
        is UnExpectedError -> Timber.e(it.throwable, "UnExpected Error")
        is HttpError -> Timber.e("Http Error: $it")
    }
}

fun <T> Observable<T>.subscribeApi(
        onNext: (T) -> Unit = { Timber.d(it.toString()) },
        onApiError: (ApiError) -> Unit = onErrorHandlerSub,
        onComplete: () -> Unit = onCompleteStub
): Disposable = subscribe(onNext, { e -> onApiError(e.toApiError()) }, onComplete)

fun <T> Observable<T>.applySchedulers(): Observable<T> {
    return this.compose { up ->
        up.subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread())
    }
}

fun <T> ObservableSubscribeProxy<T>.subscribeApi(
        onNext: (T) -> Unit = { Timber.d(it.toString()) },
        onApiError: (ApiError) -> Unit = onErrorHandlerSub,
        onComplete: () -> Unit = onCompleteStub
): Disposable = subscribe(onNext, { e -> onApiError(e.toApiError()) }, onComplete)


