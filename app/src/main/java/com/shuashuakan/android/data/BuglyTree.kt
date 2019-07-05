package com.shuashuakan.android.data

import android.util.Log
import com.shuashuakan.android.BuildConfig
import com.shuashuakan.android.commons.util.valueOrDefault
import com.shuashuakan.android.utils.stackTrackAsString
import com.tencent.bugly.crashreport.BuglyLog
import com.tencent.bugly.crashreport.CrashReport
import timber.log.Timber

class BuglyTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val newTag = tag.valueOrDefault("N/A")
        val stackTrace = t?.stackTrackAsString() ?: ""
        val newMessage = message + stackTrace
        when (priority) {
            Log.INFO -> {
                BuglyLog.i(newTag, newMessage)
                if (BuildConfig.DEBUG) {
                    Log.i(newTag, newMessage)
                }
            }
            Log.WARN -> {
                BuglyLog.w(newTag, newMessage)
                if (BuildConfig.DEBUG) {
                    Log.w(newTag, newMessage)
                }
            }
            Log.ERROR -> {
                BuglyLog.e(newTag, newMessage)
                if (BuildConfig.DEBUG) {
                    Log.e(newTag, newMessage)
                }
            }
        }
        t?.let {
            CrashReport.postCatchedException(t)
        }
    }
}