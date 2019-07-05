package com.shuashuakan.android.network

import android.content.Context
import com.shuashuakan.android.R
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.exts.mvp.ApiError.HttpError
import com.shuashuakan.android.exts.mvp.ApiError.NetworkError
import com.shuashuakan.android.exts.mvp.ApiError.UnExpectedError
import com.shuashuakan.android.utils.showLongToast
import com.shuashuakan.android.utils.throwOrLog

abstract class ApiErrorHandler(val context: Context) {

   fun handleApiError(apiError: ApiError
  ) {
    when (apiError) {
      is HttpError -> onHttpError(apiError)
      is NetworkError -> onNetworkError(apiError.throwable)
      is UnExpectedError -> onUnExpectedError(apiError.throwable)
    }
  }

  open fun onHttpError(httpError: HttpError) {
    context.showLongToast(httpError.displayMsg)
  }

  open fun onNetworkError(@Suppress("UNUSED_PARAMETER") throwable: Throwable) {
    context.applicationContext.showLongToast(
        context.resources.getString(R.string.network_not_available)
    )
  }

  open fun onUnExpectedError(throwable: Throwable) {
    throwable.throwOrLog()
  }

}