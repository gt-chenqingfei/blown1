package com.shuashuakan.android

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import com.google.gson.jpush.Gson
import com.shuashuakan.android.android.router.Router
import com.shuashuakan.android.commons.cache.Storage
import com.shuashuakan.android.config.AppConfig
import com.shuashuakan.android.data.api.model.TestCaseResp
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.HOME_PAGE
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.account.JMessageFunc
import com.shuashuakan.android.modules.account.activity.WelcomeActivity
import com.shuashuakan.android.modules.home.HomeActivity
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.ui.base.FishActivity
import com.shuashuakan.android.utils.startActivity
import javax.inject.Inject


class SplashActivity : FishActivity() {

    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var jMessageFunc: JMessageFunc
    @Inject
    lateinit var appConfig: AppConfig
    @Inject
    lateinit var accountManager: AccountManager
    @Inject
    lateinit var storage: Storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        spider.pageTracer().reportPageCreated(this)
        spider.pageTracer().reportPageShown(this, "ssr://landing", "")
        spider.programEvent(SpiderEventNames.Program.APP_START).put("startupType", "isFirstTime").track()
        testcase()
    }

    private fun testcase() {
        jMessageFunc.loginIM {}
        apiService.testcase().applySchedulers().subscribeApi(onNext = {
            appConfig.setShowNewChannel(it.test_case_channel_page != null && it.test_case_channel_page!!)
            appConfig.setCaseRoulettePage(it.test_case_roulette_page != null && it.test_case_roulette_page!!)
            appConfig.setShowFollowPage(it.test_case_follow_index != null && it.test_case_follow_index!!)
            appConfig.setShowCreateFeed(it.test_case_create_feed != null && it.test_case_create_feed!!)
            appConfig.setShowPackage(it.test_case_show_package != null && it.test_case_show_package!!)
            appConfig.setShowNewHomePage(it.test_case_new_display_page != null && it.test_case_new_display_page!!)
            appConfig.setDanmakaOpen(it.test_case_danmuku != null && it.test_case_danmuku!!)
            postSpiderEvent(it)
            goHomeOrWelcome()
        }, onApiError = {
            goHomeOrWelcome()
        })
    }

    private fun goHomeOrWelcome() {
        if (accountManager.hasAccount()) {
            goHome()
        } else {
            goWelcome()
        }

    }

    private fun goHome() {
        appConfig.setHomePageFromH5(false)
        if (intent == null || Intent.ACTION_VIEW != intent.action) {
            startActivity(HOME_PAGE)
            finish()
            return
        }

        intent.data?.let {
            it.getQueryParameter("url")?.let { url ->
                if (url.contains("ssr://home")) {
                    startActivity(url)
                } else {
                    val result = Router.get(this@SplashActivity).resolve(url)
                    if (result.success) {
                        result.intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        appConfig.setHomePageFromH5(true)
                        val backIntent = Intent(this@SplashActivity, HomeActivity::class.java)
                        backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                        val pendingIntent = PendingIntent.getActivities(this@SplashActivity, 0,
                                arrayOf(backIntent, result.intent!!), PendingIntent.FLAG_ONE_SHOT)
                        pendingIntent.send()
                    }
                }
            }

        }
        finish()
    }

    private fun goWelcome() {
        val isShown = storage.appPreference.getBoolean(BuildConfig.VERSION_CODE.toString(), false)
        if (isShown) {
            goHome()
            return
        }

        val it = Intent(this, WelcomeActivity::class.java)
        startActivity(it)
        finish()
        storage.appPreference.edit().putBoolean(BuildConfig.VERSION_CODE.toString(), true).apply()
    }

    private fun postSpiderEvent(response: TestCaseResp) {
        val toJson = Gson().toJson(response)
        val toMap = Gson().fromJson(toJson, Map::class.java)
        toMap.filter { it.value == true }
        val toJsonMap = Gson().toJson(toMap)
        spider.programEvent(SpiderEventNames.Program.AB_INTERFACE).put("info", toJsonMap).track()
    }
}
