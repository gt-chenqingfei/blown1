package com.shuashuakan.android.exts.mvp

import timber.log.Timber

open class BasePresenter<V : com.shuashuakan.android.exts.mvp.ApiView> : com.shuashuakan.android.exts.mvp.MvpBasePresenter<V>() {
    protected fun defaultErrorProcessor(error: ApiError): Boolean {
        when (error) {
            is ApiError.NetworkError -> Timber.w(error.throwable, "Network Error")
            is ApiError.UnExpectedError -> Timber.e(error.throwable, "UnExpected Error")
            is ApiError.HttpError -> Timber.e("Http Error: $error")
        }
        return false
    }
}