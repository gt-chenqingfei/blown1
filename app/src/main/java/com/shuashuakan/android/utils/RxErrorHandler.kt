package com.shuashuakan.android.utils

import android.annotation.SuppressLint
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.functions.Consumer
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber
import java.io.IOException
import java.net.SocketException


object RxErrorHandler : Consumer<Throwable> {

  fun setup() {
    RxJavaPlugins.setErrorHandler(RxErrorHandler)
  }

  override fun accept(throwable: Throwable) {
    when (throwable) {
      is OnErrorNotImplementedException -> processException(throwable.cause!!,
          "OnErrorNotImplementedException occurred")
      is UndeliverableException -> processException(throwable.cause!!,
          "UndeliverableException occurred, an exception was thrown in subscriber/observer")
      else -> processException(throwable, "unexpected exception")
    }
  }

  @SuppressLint("TimberExceptionLogging")
  private fun processException(e: Throwable, msg: String) {
    when (e) {
      is IOException -> Timber.w(e, msg)
      is SocketException -> Timber.w(e, msg)
      is InterruptedException -> Timber.w(e, msg)
      else -> e.throwOrLog { msg }
    }
  }

}