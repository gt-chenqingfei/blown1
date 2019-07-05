package com.shuashuakan.android

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.jakewharton.rxrelay2.PublishRelay
import com.shuashuakan.android.spider.Spider
import com.umeng.analytics.MobclickAgent
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationMonitor @Inject constructor(spider: Spider) : Application.ActivityLifecycleCallbacks {

//  private val pageReporter = PageReporter(spider)

    private val stateSource: PublishRelay<ApplicationState> = PublishRelay.create()
    private var compositeDisposable = CompositeDisposable()
    private var currentState: ApplicationState? = null
    private var activityStartCount = 0

    public fun getActivityCount(): Int {
        return activityStartCount
    }

    companion object {
        private const val DEBUG = false
        private const val BACKGROUND_DELAY: Long = 1000
        var mActivityStack: Stack<Activity>? = null
        lateinit var mCurrentActivity: WeakReference<Activity?>
    }

    fun onInitialized() {
        if (DEBUG) {
            Timber.d("application initialized.")
        }
        stateSource.accept(ApplicationState.INITIALIZED)
        mActivityStack = Stack()
    }

    fun applicationState(observer: Observer<ApplicationState>) {
        return stateSource.subscribe(observer)
    }

    override fun onActivityPaused(activity: Activity?) {
        MobclickAgent.onPause(activity)

    }

    override fun onActivityResumed(activity: Activity?) {
        MobclickAgent.onResume(activity)
        mCurrentActivity = WeakReference(activity)
//    activity?.let {
//      pageReporter.onActivityResumed(it)
//    }

    }

    override fun onActivityStarted(activity: Activity?) {
        activityStartCount++
        if (activityStartCount == 1) {
            if (!compositeDisposable.isDisposed) {
                compositeDisposable.dispose()
            }

            Observable.just(ApplicationState.FOREGROUND).subscribe {
                if (currentState != it) {
                    if (DEBUG) {
                        Timber.d("state: %s", if (it === ApplicationState.BACKGROUND) "Background" else "Foreground")
                    }
                    currentState = it
                    stateSource.accept(it)
                }
            }.addTo(compositeDisposable)
        }

    }

    override fun onActivityDestroyed(activity: Activity?) {
        if (mActivityStack != null) {
            mActivityStack!!.remove(activity)
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}

    override fun onActivityStopped(activity: Activity?) {
        activityStartCount--
        if (activityStartCount == 0) {
            Observable.just(ApplicationState.BACKGROUND)
//        .delay(BACKGROUND_DELAY, TimeUnit.MILLISECONDS)
                    .subscribe {
                        if (currentState != it) {
                            if (DEBUG) {
                                Timber.d("state: %s", if (it === ApplicationState.BACKGROUND) "Background" else "Foreground")
                            }
                            currentState = it
                            stateSource.accept(it)
                        }
                    }.addTo(compositeDisposable)
        }
    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
//    activity?.let {
//      pageReporter.onActivityCreated(it)
//    }
        if (mActivityStack != null)
            mActivityStack!!.push(activity)
    }

    enum class ApplicationState {
        /**
         * 应用由后台切换到前台
         */
        FOREGROUND,
        /**
         * 应用由前台换到后台切
         */
        BACKGROUND,
        /**
         * 进程初始化
         */
        INITIALIZED
    }
}