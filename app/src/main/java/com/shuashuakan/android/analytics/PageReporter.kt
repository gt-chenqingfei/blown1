package com.shuashuakan.android.analytics

import android.app.Activity
import com.shuashuakan.android.spider.Spider
import com.shuashuakan.android.ui.base.FishActivity
import timber.log.Timber

class PageReporter(private val spider: Spider) {
    private fun shouldReport(activity: Activity): Boolean {
        return activity is FishActivity
    }

    fun onActivityCreated(activity: Activity) {
        if (shouldReport(activity)) {
            spider.pageTracer().reportPageCreated(activity)
        } else {
            Timber.tag("Spider").d("Ignore: %s", activity::class.java.canonicalName)
        }
    }

    fun onActivityResumed(activity: Activity) {
        if (shouldReport(activity)) {
            spider.pageTracer().reportPageShown(activity, null, javaClass.simpleName)
        }
    }
}