package com.shuashuakan.android.modules.setting

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.SwitchCompat
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import cn.jpush.android.api.JPushInterface
import com.gyf.barlibrary.ImmersionBar
import com.sensorsdata.analytics.android.sdk.ScreenAutoTracker
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI
import com.shuashuakan.android.BuildConfig
import com.shuashuakan.android.R
import com.shuashuakan.android.commons.cache.Storage
import com.shuashuakan.android.config.AppConfig
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.ApiDefaultConfig
import com.shuashuakan.android.data.api.OpenPlatformType
import com.shuashuakan.android.data.api.model.account.UserAccount
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.event.LoginOutEvent
import com.shuashuakan.android.event.ModifyPhoneSuccessEvent
import com.shuashuakan.android.event.RefreshSettingInterestEvent
import com.shuashuakan.android.event.WeChatBindEvent
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.mvp.ApiError.HttpError
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.SETTING_PAGE
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.account.JMessageFunc
import com.shuashuakan.android.modules.account.activity.MobileModifyActivity
import com.shuashuakan.android.modules.account.activity.ModifyPhoneActivity
import com.shuashuakan.android.modules.account.activity.PerfectSelectHobbyActivity
import com.shuashuakan.android.modules.home.HomeActivity
import com.shuashuakan.android.modules.message.badage.BadgeManager
import com.shuashuakan.android.modules.share.ShareConfig
import com.shuashuakan.android.modules.share.ShareHelper
import com.shuashuakan.android.modules.track.ClickAction
import com.shuashuakan.android.modules.track.trackProfileSet
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.ui.ProgressDialog
import com.shuashuakan.android.ui.base.FishActivity
import com.shuashuakan.android.utils.*
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.include_activity_setting.*
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class SettingsActivity : FishActivity(), ScreenAutoTracker {
    override fun getTrackProperties(): JSONObject? {
        return null
    }

    override fun getScreenUrl(): String {
        return SETTING_PAGE
    }

    private val toolbar by bindView<Toolbar>(R.id.toolbar)
    private val bindPhoneContainer by bindView<View>(R.id.bind_phone_container)
    private val bindPhoneView by bindView<TextView>(R.id.bind_phone_view)
    private val wechatContainer by bindView<View>(R.id.wechat_container)
    private val wechatView by bindView<TextView>(R.id.wechat_view)
    private val pushSwitch by bindView<SwitchCompat>(R.id.push_switch)
    private val flowPlaySwitch by bindView<SwitchCompat>(R.id.flow_play_switch)
    private val clearCacheContainer by bindView<View>(R.id.clear_cache_container)
    private val showCacheView by bindView<TextView>(R.id.show_cache_view)
    private val userServicesView by bindView<View>(R.id.user_services_view)
    private val shareToFriendView by bindView<View>(R.id.share_to_friend_view)
    private val versionCode by bindView<TextView>(R.id.version_code)
    private val logoutView by bindView<View>(R.id.logout_view)

    private val interestConstrainLayouot by bindView<LinearLayout>(R.id.setting_cl_insterests_all)
    private val interestLinearLayout by bindView<LinearLayout>(R.id.setting_ll_insterests)

    @Inject
    lateinit var accountManager: AccountManager
    @Inject
    lateinit var logout: Logout
    @Inject
    lateinit var appConfig: AppConfig
    @Inject
    lateinit var shareHelper: ShareHelper
    @Inject
    lateinit var apiService: ApiService
    //  @Inject
//  lateinit var spider: Spider
    @Inject
    lateinit var storage: Storage
    @Inject
    lateinit var badgeManager: BadgeManager
    @Inject
    lateinit var jMessageFunc: JMessageFunc


    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    private lateinit var wxApi: IWXAPI

    private val bindWxDialog by lazy {
        return@lazy ProgressDialog.progressDialog(this, getString(R.string.string_bind_wechat_ing))
    }

    private val unBindWxDialog by lazy {
        return@lazy ProgressDialog.progressDialog(this, getString(R.string.string_unbind_wechat))
    }
    private val shareContentDialog by lazy {
        return@lazy ProgressDialog.progressDialog(this, getString(R.string.string_get_bind_info))
    }

    companion object {
        private const val EXTRA_USER_INFO = "extra_user_info"
        fun createIntent(context: Context, userAccount: UserAccount?): Intent {
            return Intent(context, SettingsActivity::class.java).putExtra(EXTRA_USER_INFO, userAccount)
        }
    }

    private var userAccount: UserAccount? = null

    private var isWxBind = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSwitchActivityAnimation()
        setContentView(R.layout.activity_settings)
        ImmersionBar.with(this).navigationBarColor(R.color.black).titleBar(toolbar).init()
//        ImmersionBar.setTitleBar(this, toolbar)

        spider.pageTracer().reportPageCreated(this)
        userAccount = intent.getParcelableExtra(EXTRA_USER_INFO)
        wxApi = WXAPIFactory.createWXAPI(this, resources.getString(R.string.WX_ID), false)
        wxApi.registerApp(resources.getString(R.string.WX_ID))
        if (userAccount == null) {
            return
        }
        isWxBind = userAccount?.wechatBind ?: false
        setupView()
        RxBus.get().toFlowable()
                .subscribe { rxBus ->
                    if (rxBus is WeChatBindEvent) {
                        getWxToken(rxBus.code)
                        spider.manuallyEvent(SpiderEventNames.BINDING_USER)
                                .put("phone", rxBus.code)
                                .put("userID", this.getUserId())
                                .put("Type", "WeChat")
                                .track()
                    } else if (rxBus is ModifyPhoneSuccessEvent) {
                        finish()
                    } else if (rxBus is RefreshSettingInterestEvent) {
                        val listNames = ArrayList<String>()
                        rxBus.dataInterest.split(",").forEach {
                            if (it.isNotEmpty())
                                listNames.add(it)
                        }
                        interestLinearLayout.removeAllViews()
                        if (listNames.size < 3) {
                            for (i in 0 until listNames.size)
                                interestLinearLayout.addView(setInterestTextView(i, listNames[i]))
                        } else {
                            for (i in 0..2)
                                interestLinearLayout.addView(setInterestTextView(i, listNames[i]))
                        }


                    }
                }.addTo(compositeDisposable)
        Timber.d(JPushInterface.getRegistrationID(this@SettingsActivity))

        showInterests(userAccount)
    }

    private fun setupView() {
        toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.title = getString(R.string.string_setting)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        flowPlaySwitch.isChecked = appConfig.isFlowPlaySwitch()

        bindPhoneView.text = userAccount?.mobile

        getCacheSise()

        updateWxStatus()

        logoutView.setOnClickListener {
            AlertDialog.Builder(this)
                    .setTitle(getString(R.string.string_commit_logout))
                    .setPositiveButton(getString(R.string.string_confirm)) { _, _ ->


                        spider.programEvent(SpiderEventNames.Program.LOGIN_STATUS_CHANGED).put("userID", userAccount?.userId
                                ?: 0).put("type", storage.appPreference.getString(ClickAction.LOGIN_TYPE, "")).put("method", "logout").track()
                        RxBus.get().post(LoginOutEvent())
                        //登出埋点
                        SensorsDataAPI.sharedInstance().logout()
                        SensorsDataAPI.sharedInstance()
                                .profileSet(JSONObject()
                                        .put("last_logout_time", formatTime(System.currentTimeMillis())))

                        badgeManager.logout()
                        jMessageFunc.logout()

                        logout.logout()
                        FollowCacheManager.clearCache()
                        if (!appConfig.isShowNewHomePage()) {
                            val intent = Intent(applicationContext, HomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)
                        } else {
                            val intent = Intent(applicationContext, HomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)
                        }
                        finish()
                    }.setNegativeButton(getString(R.string.string_cancel), null).show()
        }
        bindPhoneContainer.setOnClickListener {
            if (userAccount?.mobile != null) {
                getSpider().manuallyEvent(SpiderEventNames.BIND_PHONE)
                        .put("userID", getUserId())
                        .track()
                startActivity(ModifyPhoneActivity.intent(this@SettingsActivity, userAccount?.mobile!!))
            } else {
                MobileModifyActivity.launchWithModify(this, "")
//                startActivity(LoginActivity.modifyPhone(this, ""))
            }

        }

        wechatContainer.setOnClickListener {
            if (isWxBind) {
                unBindWx()
            } else {
                getSpider().manuallyEvent(SpiderEventNames.BIND_WECHAT)
                        .put("userID", getUserId())
                        .track()
                bindWx()
            }
        }
        pushSwitch.setOnClickListener {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", this.packageName, null)
            intent.data = uri
            startActivity(intent)
        }

        flowPlaySwitch.setOnCheckedChangeListener { _, isChecked ->
            appConfig.setFlowPlayBoolean(isChecked)
        }
        clearCacheContainer.setOnClickListener {
            clearCache()
        }
        userServicesView.setOnClickListener {
            startActivity("https://topic.shuashuakan.net/user-agreement.html")
        }
        communityPolicy.setOnClickListener {
            startActivity("https://topic.shuashuakan.net/community/convention.html")
        }
        versionCode.text = BuildConfig.VERSION_NAME

        shareToFriendView.setOnClickListener {
            shareApp()
        }
    }

    override fun onResume() {
        super.onResume()
        spider.pageTracer().reportPageShown(this, "ssr://app/setting", "")
        val manager = NotificationManagerCompat.from(this)
        val isOpened = manager.areNotificationsEnabled()
        pushSwitch.isChecked = isOpened
    }

    private fun updateWxStatus() {
        if (isWxBind) {
            wechatView.text = getString(R.string.string_has_bind)
        } else {
            wechatView.text = getString(R.string.string_not_bind)
        }
    }

    private fun bindWx() {
        if (wxApi.isWXAppInstalled) {
            val req = SendAuth.Req()
            req.scope = "snsapi_userinfo"
            req.state = "getWxInfo"
            wxApi.sendReq(req)
        } else {
            showLongToast(getString(R.string.string_not_install_client))
        }
    }

    private fun unBindWx() {
        unBindWxDialog.show()
        apiService.revokeWeChat(OpenPlatformType.duck.value).applySchedulers().subscribeApi(onNext = {
            if (it.result.isSuccess) {
                unBindWxDialog.dismiss()
                showLongToast(getString(R.string.string_unbind_wehcat_success))
                isWxBind = false
                updateWxStatus()
            } else {
                showLongToast(getString(R.string.string_unbind_wehcat_error))
                unBindWxDialog.dismiss()
            }
        }, onApiError = {
            showLongToast(getString(R.string.string_unbind_wehcat_error))
            unBindWxDialog.dismiss()
        })
    }

    private fun clearCache() {
        showLongToast(FileSizeUtil.ClearFiles(getVideoCacheDir(this), this))
        getCacheSise()
    }

    private fun getCacheSise() {
        showCacheView.text = FileSizeUtil.getAutoFileOrFilesSize(getVideoCacheDir(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
        ImmersionBar.with(this).destroy()
    }

    private fun getWxToken(code: String) {
        apiService.getWxAccessToken(resources.getString(R.string.WX_ID),
                resources.getString(R.string.WX_KEY),
                code, "authorization_code").applySchedulers().subscribeApi(onNext = {
            bindWxToServer(it.openid, it.accessToken)
        }, onApiError = {
            bindWxDialog.dismiss()
            showLongToast(getString(R.string.string_get_share_info_error))
        })
    }

    private fun bindWxToServer(openId: String?, accessToken: String?) {
        bindWxDialog.show()
        apiService.authorizeWeChat(OpenPlatformType.duck.value, openId,
                accessToken, ApiDefaultConfig.CLIENT_KEY).applySchedulers().subscribeApi(onNext = {
            if (it.result.isSuccess) {
                //首次绑定微信
                SensorsDataAPI.sharedInstance()
                        .profileSetOnce(ClickAction.INIT_HAS_BIND_WX, formatTime(System.currentTimeMillis()))
                showLongToast(getString(R.string.string_bind_wehcat_success))
                trackProfileSet(
                        arrayListOf(
                                ClickAction.HAS_BIND_WX to formatTime(System.currentTimeMillis())
                        )
                )
                isWxBind = true
                updateWxStatus()
                bindWxDialog.dismiss()
            } else {
                bindWxDialog.dismiss()
                showLongToast(getString(R.string.string_bind_wehcat_error))
            }
        }, onApiError = {
            bindWxDialog.dismiss()
            if (it is HttpError) {
                showLongToast(it.displayMsg)
            }
        })
    }


    private fun shareApp() {
        shareHelper.shareType = ShareConfig.SHARE_TYPE_APP
        shareHelper.doShare(this, null, null, false, false, null)
    }


    private fun showInterests(userAccount: UserAccount?) {
        userAccount ?: return
        interestLinearLayout.removeAllViews()
        if (userAccount.userInterest == null || userAccount.userInterest!!.interests!!.isEmpty()) {
            val interestTextView = TextView(this)
            interestTextView.maxLines = 1
            interestTextView.setTextColor(getColor1(R.color.color_normal_838791))
            interestTextView.text = getString(R.string.string_choose_like_with_content)
            interestLinearLayout.addView(interestTextView)
        } else {
            if (userAccount.userInterest!!.interests!!.size < 3) {
                for (i in 0 until userAccount.userInterest!!.interests!!.size)
                    interestLinearLayout.addView(setInterestTextView(i, userAccount.userInterest!!.interests!![i].name))
            } else {
                for (i in 0..2)
                    interestLinearLayout.addView(setInterestTextView(i, userAccount.userInterest!!.interests!![i].name))
            }
        }

        interestConstrainLayouot.noDoubleClick {
            startActivity(Intent(this, PerfectSelectHobbyActivity::class.java))
        }
    }

    private fun setInterestTextView(count: Int, name: String?): TextView {
        val interestTextView = TextView(this)
        interestTextView.maxLines = 1
        interestTextView.setTextColor(getColor1(R.color.color_normal_a3a5a9))
        interestTextView.text = name
        interestTextView.setBackgroundResource(R.drawable.bg_setting_background)
        val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        if (count != 0) lp.setMargins(dip(10), 0, 0, 0)
        interestTextView.setPadding(dip(10), dip(5), dip(10), dip(5))
        interestTextView.layoutParams = lp
        return interestTextView
    }

}