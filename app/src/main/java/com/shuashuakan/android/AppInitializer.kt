package com.shuashuakan.android

import android.app.Application
import android.content.Context
import cn.jpush.im.android.api.JMessageClient
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory
import com.facebook.imagepipeline.decoder.ProgressiveJpegConfig
import com.facebook.imagepipeline.image.ImmutableQualityInfo
import com.facebook.imagepipeline.image.QualityInfo
import com.ishumei.smantifraud.SmAntiFraud
import com.ishumei.smantifraud.SmAntiFraud.IServerSmidCallback
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI
import com.shuashuakan.android.DuckApplication.Companion.HAS_SM_ID
import com.shuashuakan.android.config.AppConfig
import com.shuashuakan.android.data.api.ApiDefaultConfig.Companion.SERVER_URL
import com.shuashuakan.android.data.api.ApiDefaultConfig.Companion.TEST_SERVER_URL
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.push.IMEventListener
import com.shuashuakan.android.push.PushManager
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.utils.*
import com.tencent.bugly.beta.Beta
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.bugly.crashreport.CrashReport.UserStrategy
import com.uber.autodispose.AutoDisposePlugins
import com.umeng.commonsdk.UMConfigure
import com.umeng.socialize.PlatformConfig
import okhttp3.OkHttpClient
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.SECONDS

object AppInitializer {

    fun initialize(app: Application) {
        Timber.d("Init main process...")
        val channelName = app.channelName()
        Thread {
            initSmAntiFraud(app, channelName)
            initCrashReport(app, channelName)
        }.start()

        initJPush(app)

        val config = OkHttpImagePipelineConfigFactory.newBuilder(app, OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .connectTimeout(60, SECONDS).build())
                .setProgressiveJpegConfig(object : ProgressiveJpegConfig {
                    override fun getNextScanNumberToDecode(scanNumber: Int): Int {
                        return scanNumber + 2
                    }

                    override fun getQualityInfo(scanNumber: Int): QualityInfo {
                        val isGoodEnough = scanNumber >= 5
                        return ImmutableQualityInfo.of(scanNumber, isGoodEnough, true)
                    }

                })
                .build()
        Fresco.initialize(app, config)
        // rx
        AutoDisposePlugins.setOutsideLifecycleHandler { it.throwOrLog() }
        RxErrorHandler.setup()
        initUmeng(app)
        initSensor(app)
    }


    private fun initSensor(app: Application) {
        Thread {
            SensorsDataAPI.sharedInstance(app, if (BuildConfig.DEBUG) TEST_SERVER_URL else SERVER_URL,
                    if (BuildConfig.DEBUG) SensorsDataAPI.DebugMode.DEBUG_OFF
                    else SensorsDataAPI.DebugMode.DEBUG_OFF
            )

            //开启调试
//      SensorsDataAPI.sharedInstance().enableLog(BuildConfig.DEBUG)

            try {
                val properties = JSONObject()
                properties.put("platform_type", "Android")
                SensorsDataAPI.sharedInstance().registerSuperProperties(properties)
            } catch (ignore: JSONException) {
            }
            SensorsDataAPI.sharedInstance().identify(SmAntiFraud.getDeviceId())
            val accountManager = app.daggerComponent().accountManager()
            if (accountManager.hasAccount()) {
                SensorsDataAPI.sharedInstance().login(accountManager.account()!!.userId.toString())
            }
            SensorsDataAPI.sharedInstance().enableHeatMap()
            SensorsDataAPI.sharedInstance().trackFragmentAppViewScreen()
            SensorsDataAPI.sharedInstance().trackInstallation(
                    "AppInstall", JSONObject().put("download_channel", app.latestChannelName()))

            SensorsDataAPI.sharedInstance()
                    .profileSet(JSONObject().put("last_install_channel", app.latestChannelName()))

            val eventTypeList = arrayListOf(
                    SensorsDataAPI.AutoTrackEventType.APP_START,
                    SensorsDataAPI.AutoTrackEventType.APP_END,
                    SensorsDataAPI.AutoTrackEventType.APP_VIEW_SCREEN,
                    SensorsDataAPI.AutoTrackEventType.APP_CLICK
            )
            SensorsDataAPI.sharedInstance().ignoreAutoTrackActivities(arrayListOf(
//          HomeActivity::class.java,
                    /*CropImageActivity::class.java,*/
                    SplashActivity::class.java).toList())
            SensorsDataAPI.sharedInstance().enableAutoTrack(eventTypeList)

            //缓存数美ID
            val realSmid = SmAntiFraud.getDeviceId()
            if (SpUtil.find(AppConfig.CACHE_SMID) == null || SpUtil.find(AppConfig.CACHE_SMID) != realSmid) {
                SpUtil.saveOrUpdate(AppConfig.CACHE_SMID, realSmid)
            }

        }.start()
    }

    private fun initUmeng(app: Application) {
        UMConfigure.setEncryptEnabled(true)
        UMConfigure.setLogEnabled(false)
        UMConfigure.init(
                app, app.resources.getString(R.string.UMENG_APPKEY), app.channelName(),
                UMConfigure.DEVICE_TYPE_PHONE, null
        )

        PlatformConfig.setQQZone(app.getString(R.string.QQ_ID), app.getString(R.string.QQ_KEY))
        PlatformConfig.setWeixin(app.getString(R.string.WX_ID), app.getString(R.string.WX_KEY))
    }

    private fun initJPush(app: Application) {
        PushManager.initPush(app)
        JMessageClient.init(app)
        JMessageClient.registerEventReceiver(IMEventListener(app))
    }

    private fun initSmAntiFraud(
            context: Context,
            channelName: String
    ) {
        val option = SmAntiFraud.SmOption()
        option.channel = channelName
        // set organization code
        option.organization = "WLQVVtANCxcBPKzxeonM"

        SmAntiFraud.registerServerIdCallback(object : IServerSmidCallback {
            override fun onError(error: Int) {
                HAS_SM_ID = false
                Timber.tag("SmAntiFraud").d("onError: errorCode: $error")
            }

            override fun onSuccess(success: String?) {
                HAS_SM_ID = true
                context.getSpider().smId = success
                val newSmid = SmAntiFraud.getDeviceId()
                val oldSmid = SpUtil.find(AppConfig.CACHE_SMID)
                if (oldSmid != newSmid) {
                    context.getSpider().programEvent(SpiderEventNames.Program.SM_ID_CHANGE)
                            .put("new", newSmid)
                            .put("old", oldSmid)
                            .put("sm_id_type", SmAntiFraud.checkDeviceIdType(newSmid))
                            .track()
                }
                SpUtil.saveOrUpdate(AppConfig.CACHE_SMID, newSmid)

                Timber.tag("SmAntiFraud")
                        .d("onSuccess: $success")
            }
        })
        SmAntiFraud.create(context, option)
    }

    private fun initCrashReport(context: Context, channelName: String) {
        try {
            val strategy = UserStrategy(context)
            strategy.appChannel = channelName
            strategy.appVersion = BuildConfig.VERSION_NAME
            if (BuildConfig.DEBUG) {
                CrashReport.initCrashReport(context, "e9b2a53cac", true, strategy)
            } else {
                CrashReport.initCrashReport(context, "666ad72d36", false, strategy)
            }
            Beta.init(context, BuildConfig.DEBUG)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
