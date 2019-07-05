package com.shuashuakan.android

import android.content.Context
import android.preference.PreferenceManager
import android.support.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import com.shuashuakan.android.analytics.AppStateObserver
import com.shuashuakan.android.android.router.Router
import com.shuashuakan.android.data.BuglyTree
import com.shuashuakan.android.di.DaggerDuckComponent
import com.shuashuakan.android.di.DuckComponent
import com.shuashuakan.android.service.UploadVideoFloatManager
import com.shuashuakan.android.spider.Spider
import com.shuashuakan.android.support.RuntimeHandler
import com.shuashuakan.android.utils.SpUtil
import com.shuashuakan.android.utils.isMainProcess
import com.simple.spiderman.SpiderMan
import com.squareup.leakcanary.LeakCanary
import com.tendcloud.tenddata.TCAgent
import com.umeng.commonsdk.utils.UMUtils
import io.fabric.sdk.android.Fabric
import timber.log.Timber
import timber.log.Timber.DebugTree
import javax.inject.Inject

open class DuckApplication : MultiDexApplication() {

    @Inject
    lateinit var injectorProvider: AndroidInjectorProvider
    @Inject
    lateinit var applicationMonitor: ApplicationMonitor
    @Inject
    lateinit var spider: Spider

    companion object {
        var HAS_SM_ID = false //是否已经拥有数美ID
        var HAS_SHOW_WIFI_DIALOG = false
        var CACHE_USER_FOLLOW_STATUS = hashMapOf<String, Boolean>()

        fun from(context: Context): DuckApplication {
            return context.applicationContext as DuckApplication
        }
    }

    private val appComponent by lazy {
        val start = System.currentTimeMillis()
        val component = buildComponent()
        Timber.tag("DuckApplication").d("build component took ${System.currentTimeMillis() - start}ms")
        component
    }

    override fun onCreate() {
        super.onCreate()
        SpUtil.init(PreferenceManager.getDefaultSharedPreferences(this))
        if (isMainProcess(this)) {
            SpiderMan.init(this)
            appComponent.inject(this)
            initAtMainProcess()
            UploadVideoFloatManager(this).register()
        }
        try {
            Fabric.with(this, Crashlytics())
        } catch (e: Exception) {
        }
        // App ID: 在TalkingData Game Analytics创建应用后会得到App ID。
        // 渠道 ID: 是渠道标识符，可通过不同渠道单独追踪数据。
        val channelName = UMUtils.getChannel(applicationContext)
        TCAgent.init(this, "7D004E2AD4214DAE8C1B4339BD6E0A0F", channelName)
    }

    open fun initAtMainProcess() {
        AppInitializer.initialize(this)
        registerActivityLifecycleCallbacks(applicationMonitor)

        applicationMonitor.applicationState(AppStateObserver.create(spider))
        applicationMonitor.onInitialized()

        if (BuildConfig.DEBUG) {
            if (LeakCanary.isInAnalyzerProcess(this)) {
                return
            }
            LeakCanary.install(this);
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        RuntimeHandler.intecpt()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        } else {
            Timber.plant(BuglyTree())
        }
    }

    private fun buildComponent(): DuckComponent {
        return DaggerDuckComponent.builder()
                .application(this)
                .build()
    }

    override fun getSystemService(name: String?): Any? {
        if (name != null) {
            if (DuckComponent.matches(name)) return appComponent
            if (AndroidInjectorProvider.hasService(name)) return injectorProvider.getService(name)
            if (Router.isRouterService(name)) return appComponent.linkResolver()
        }
        return super.getSystemService(name ?: "")
    }
}