package com.shuashuakan.android.exts.mvp

import android.content.Context
import com.shuashuakan.android.exts.mvp.ApiError.*
import com.shuashuakan.android.exts.mvp.ApiPresenter.LifecycleEvent.ATTACHED
import com.shuashuakan.android.exts.mvp.ApiPresenter.LifecycleEvent.DETACHED
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.utils.throwOrLog
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

abstract class ApiPresenter<V : ApiView, DATA : Any>(
        private val context: Context
) : com.shuashuakan.android.exts.mvp.MvpBasePresenter<V>() {
    @JvmField
    val NETWORK_UNAVAILABLE = "网络连接不可用"
    private val lifecycle: BehaviorSubject<LifecycleEvent> = BehaviorSubject.create()

    private fun handleApiError(apiError: ApiError) {
        when (apiError) {
            is HttpError -> onHttpError(apiError)
            is NetworkError -> onNetworkError(apiError.throwable)
            is UnExpectedError -> onUnExpectedError(apiError.throwable)
        }
    }

    protected open fun onHttpError(httpError: HttpError) {
        view?.showMessage(httpError.displayMsg)
    }

    abstract fun onSuccess(data: DATA)

    protected open fun onNetworkError(@Suppress("UNUSED_PARAMETER") throwable: Throwable) {
        view?.showMessage(NETWORK_UNAVAILABLE)
    }

    protected open fun onUnExpectedError(throwable: Throwable) {
        throwable.throwOrLog()
    }

    override fun attachView(view: V) {
        lifecycle.onNext(ATTACHED)
        super.attachView(view)
    }

    override fun detachView(retainInstance: Boolean) {
        lifecycle.onNext(DETACHED)
        super.detachView(retainInstance)
    }

    abstract fun getObservable(): Observable<DATA>

    protected fun subscribe() {
        getObservable().unSubscribeWhenDetach().applySchedulers().subscribeApi(onNext = {
            onSuccess(it)
        }, onApiError = {
            handleApiError(it)
        })
    }

    private fun Observable<DATA>.unSubscribeWhenDetach(): Observable<DATA> {
        return this.compose { up ->
            up.takeUntil(lifecycle.filter { event -> event == DETACHED })
        }
    }

    protected enum class LifecycleEvent {
        ATTACHED,
        DETACHED
    }
}