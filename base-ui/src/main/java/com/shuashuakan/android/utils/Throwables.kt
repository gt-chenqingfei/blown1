@file:JvmName("Throwables")

package com.shuashuakan.android.utils

import android.annotation.SuppressLint
import arrow.core.None
import arrow.core.Option
import com.shuashuakan.android.base.ui.BuildConfig
import com.shuashuakan.android.data.api.model.FishApiError
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.exts.mvp.ApiError.HttpError
import com.shuashuakan.android.exts.mvp.ApiError.NetworkError
import com.shuashuakan.android.exts.mvp.ApiError.UnExpectedError
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter

@SuppressLint("TimberExceptionLogging")
inline fun Throwable.throwOrLog(message: () -> String = { "unexpected exception:" }) {
    if (BuildConfig.DEBUG) {
        Timber.e("==================================================================================")
        Timber.e("An unexpected exception was happened(${javaClass.simpleName}): ${this.message}")
        Timber.e(
                "This exception was thrown at DEBUG mode only, and was reported to bugly in RELEASE mode.")
        Timber.e("==================================================================================")
        // propagate
        if (this is RuntimeException) throw this
        else throw RuntimeException(this)
    } else {
        Timber.e(this, message())
    }
}


/**
 * From Guava
 * Returns a string containing the result of {@link Throwable#toString() toString()}, followed by
 * the full, recursive stack trace of {@code throwable}. Note that you probably should not be
 * parsing the resulting string; if you need programmatic access to the stack frames, you can call
 * {@link Throwable#getStackTrace()}.
 */
fun Throwable.stackTrackAsString(): String {
    val stringWriter = StringWriter()
    printStackTrace(PrintWriter(stringWriter))
    return stringWriter.toString()
}

fun Throwable.toApiError(): ApiError {
    return when (this) {
        is IOException -> NetworkError(this)
        is HttpException -> toClientError(this)
        else -> UnExpectedError(this)
    }
}

private fun toClientError(e: HttpException): HttpError {
    val clientError = e.code() in 400 until 500
    return if (clientError) {
        var message: String? = null
        val apiError = toFishApiError(e.response())
        if (apiError != null) {
            message = messageLookup(apiError.errorCode) ?: apiError.errorMsg
            if (message.isNullOrBlank()) {
                message = "未知错误：" + apiError.errorCode
            }
        }
        HttpError(true, message ?: "未知错误", Option.fromNullable(apiError))
    } else {
        HttpError(false, "服务器好像有点问题", None)
    }
}


private fun toFishApiError(response: Response<*>?): FishApiError? {
    if (response != null) {
        try {
            val json = response.errorBody()?.string() ?: return null
            val jsonObj = JSONObject(json)
            val errorCode = jsonObj.getLong("error_code")
            val errorMsg = jsonObj.optString("display_msg") ?: jsonObj.optString("error_msg")
            return FishApiError(errorCode, errorMsg)
        } catch (ignore: Exception) {
        }
    }
    return null
}

private fun messageLookup(errorCode: Long): String? {
    return FishApiError.messageLookup(errorCode)
}
