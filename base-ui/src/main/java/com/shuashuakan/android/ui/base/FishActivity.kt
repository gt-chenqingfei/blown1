package com.shuashuakan.android.ui.base

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.LayoutInflaterCompat
import android.support.v7.app.AppCompatActivity
import android.transition.Slide
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.shuashuakan.android.FishInjection
import com.shuashuakan.android.spider.BuildConfig
import com.shuashuakan.android.spider.Spider
import com.shuashuakan.android.spider.auto.TraceableLayoutFactory
import com.shuashuakan.android.spider.auto.TraceableView
import com.shuashuakan.android.spider.auto.ViewMonitor
import com.shuashuakan.android.spider.auto.ViewProxyFactory
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

abstract class FishActivity : AppCompatActivity(), HasSupportFragmentInjector/*, ViewProxyFactory, ViewMonitor.ViewMonitorFactory*/ {

//    @Suppress("unused")
//    protected open val scopeProvider: AndroidLifecycleScopeProvider by lazy {
//        AndroidLifecycleScopeProvider.from(this)
//    }

    @Suppress("MemberVisibilityCanPrivate")
    @Inject
    internal lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>
    @Inject
    lateinit var spider: Spider

    override fun onCreate(savedInstanceState: Bundle?) {
        FishInjection.inject(this)
//        LayoutInflaterCompat.setFactory2(layoutInflater, TraceableLayoutFactory.create(delegate, this, this))
        super.onCreate(savedInstanceState)

    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = fragmentInjector

    fun initSwitchActivityAnimation() {
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val slide = Slide()
            slide.duration = 200
            window.enterTransition = slide
            window.exitTransition = slide
            window.reenterTransition = slide
        }
    }

    fun goActivityWithAnim(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        } else {
            startActivity(intent)
        }
    }

//    override fun createViewProxy(): TraceableView.ViewProxy {
//        if (spider == null) {
//            if (BuildConfig.DEBUG) {
//                throw IllegalStateException("spider == null")
//            }
//            return TraceableView.ViewProxy.NONE
//        }
//        return spider.createViewProxy()
//    }
//
//    override fun createViewMonitor(): ViewMonitor {
//        if (spider == null) {
//            if (BuildConfig.DEBUG) {
//                throw IllegalStateException("spider == null")
//            }
//            return ViewMonitor.EMPTY
//        }
//        return spider.createViewMonitor()
//    }

    protected fun changeTranslucentStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.statusBarColor = Color.TRANSPARENT
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }
}