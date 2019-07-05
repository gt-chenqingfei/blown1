package com.shuashuakan.android.exts.mvp

import arrow.core.Option

sealed class ApiError {

    data class HttpError(val clientError: Boolean, val displayMsg: String,
                         val apiError: Option<Any>) : ApiError()

    data class NetworkError(val throwable: Throwable) : ApiError()

    data class UnExpectedError(val throwable: Throwable) : ApiError()

}