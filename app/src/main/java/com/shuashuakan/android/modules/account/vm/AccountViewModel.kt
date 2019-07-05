package com.shuashuakan.android.modules.account.vm

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import androidx.content.edit
import arrow.core.getOrElse
import com.google.gson.jpush.Gson
import com.luck.picture.lib.tools.ToastManage
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI
import com.shuashuakan.android.R
import com.shuashuakan.android.commons.cache.DiskCache
import com.shuashuakan.android.commons.cache.Storage
import com.shuashuakan.android.config.AppConfig
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.ApiDefaultConfig
import com.shuashuakan.android.data.api.OpenPlatformType
import com.shuashuakan.android.data.api.model.CommonResult
import com.shuashuakan.android.data.api.model.FishApiError
import com.shuashuakan.android.data.api.model.TestCaseResp
import com.shuashuakan.android.data.api.model.account.Account
import com.shuashuakan.android.data.api.model.account.GuideModel
import com.shuashuakan.android.data.api.model.account.UserAccount
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.enums.LoginSource
import com.shuashuakan.android.event.LoginSuccessEvent
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.account.JMessageFunc
import com.shuashuakan.android.modules.profile.fragment.ProfileFragment
import com.shuashuakan.android.modules.track.ClickAction
import com.shuashuakan.android.modules.track.trackClick
import com.shuashuakan.android.spider.Spider
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.utils.*
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory

/**
 * @author:qingfei.chen
 * @date:2019/5/8  上午11:49
 */
class AccountViewModel(val app: Application) : AndroidViewModel(app) {
    companion object {
        const val LOGIN_TYPE_MOBILE = "Phone"
        const val LOGIN_TYPE_WECHAT = "WeChat"
    }

    var loginType: String = LOGIN_TYPE_MOBILE
    var accountManager: AccountManager = app.applicationContext.daggerComponent().accountManager()
    private var apiService: ApiService = app.applicationContext.daggerComponent().apiService()
    private var appConfig: AppConfig = app.applicationContext.daggerComponent().appConfig()
    private var accountCache: DiskCache.Cache<UserAccount>
    private var storage: Storage = accountManager.storage
    private var spider: Spider = app.applicationContext.daggerComponent().spider()
    private var jMessageFunc: JMessageFunc = app.applicationContext.daggerComponent().jMessageFunc()
    private var loginSource: Int = 0
    private var wxApi: IWXAPI

    init {
        accountCache = storage.userCache.cacheOf()
        wxApi = WXAPIFactory.createWXAPI(app, app.getString(R.string.WX_ID), false)
        wxApi.registerApp(app.resources.getString(R.string.WX_ID))
    }

    val mValidCodeLiveData: MutableLiveData<CommonResult.Result> = MutableLiveData()
    val mExcuteBtnStateLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val mLoginStateLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val mBindMobileLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val mUserGuideLiveData: MutableLiveData<GuideModel?> = MutableLiveData()
    var mPhoneEditorLiveData: MutableLiveData<Boolean> = MutableLiveData()
    var mIsValidCodeEnable: Boolean = false

    fun updatePhoneNumberState(isPhoneNumberEnable: Boolean) {
        mPhoneEditorLiveData.postValue(isPhoneNumberEnable)
        mExcuteBtnStateLiveData.postValue(mIsValidCodeEnable && isPhoneNumberEnable)
    }

    fun updateValidCodeState(isValidCodeEnable: Boolean) {
        mIsValidCodeEnable = isValidCodeEnable
        mExcuteBtnStateLiveData.postValue(mIsValidCodeEnable && mPhoneEditorLiveData.value ?: false)
    }

    fun getValidCode(mobilePhone: String) {
        apiService.sendSMSCode(mobilePhone = mobilePhone)
                .applySchedulers()
                .subscribeApi(onNext = {
                    mValidCodeLiveData.postValue(it.result)
                }, onApiError = {
                    handleApiError(it)
                })
    }

    fun loginWithMobile(phoneNumber: String, validCode: String) {
        loginSource = LoginSource.MOBILE.source
        loginType = LOGIN_TYPE_MOBILE
        val queryMap = mapOf(
                "client_id" to ApiDefaultConfig.CLIENT_KEY,
                "client_secret" to ApiDefaultConfig.CLIENT_SECRET,
                "user_source" to ApiDefaultConfig.USER_SOURCE,
                "grant_type" to "totp",
                "open_platform_type" to OpenPlatformType.Fish.value,
                "account" to phoneNumber,
                "totp_code" to validCode)

        apiService.login(queryMap).applySchedulers()
                .subscribeApi(onNext = {
                    performLoginSuccess(it) {
                        userGuide()
                    }
                }, onApiError = {
                    performLoginError(it)
                })
    }

    fun bindMobile(oldPhoneNumber: String?, phoneNumber: String, validCode: String) {
        apiService.changeMobilePhone(oldPhoneNumber, phoneNumber, validCode).applySchedulers()
                .subscribeApi(onNext = {
                    app.showLongToast(app.getString(R.string.string_bind_phone_success))
                    spider.manuallyEvent(SpiderEventNames.BIND_PHONE)
                            .put("userID", app.getUserId())
                            .track()
                    spider.manuallyEvent(SpiderEventNames.BINDING_USER)
                            .put("phone", phoneNumber)
                            .put("userID", app.getUserId())
                            .put("Type", "Phone")
                            .track()
                    userGuide()
                }, onApiError = {
                    performBindMobileError(it)
                })
    }

    fun requestWeChatAuth(): Boolean {
        if (!wxApi.isWXAppInstalled) {
            app.showLongToast(app.getString(R.string.string_not_install_client))
            return false
        }
        val req = SendAuth.Req()
        req.scope = "snsapi_userinfo"
        req.state = "login"
        wxApi.sendReq(req)
        return true
    }

    fun loginWithWeChat(code: String, state: String) {
        loginSource = LoginSource.WECHAT.source
        loginType = LOGIN_TYPE_WECHAT
        apiService.getAccessToken(code, state, ApiDefaultConfig.CLIENT_KEY).applySchedulers()
                .subscribeApi(onNext = {
                    performLoginSuccess(it) {}
                }, onApiError = {
                    ToastManage.s(app, app.getString(R.string.string_login_error))
                    mLoginStateLiveData.postValue(false)
                })
    }

    fun userGuide() {
        apiService.userGuide(loginSource).applySchedulers()
                .subscribeApi(onNext = {
                    mUserGuideLiveData.postValue(it)
                }, onApiError = {
                    mUserGuideLiveData.postValue(null)
                })
    }

    fun needBindMobile(): Boolean {
        if (accountManager.hasAccount()) {
            val account = accountCache.get(ProfileFragment.ACCOUNT_CACHE_KEY).orNull()

            return account?.mobile.isNullOrEmpty()
        }
        return true
    }

    private fun performLoginSuccess(account: Account?, callback: () -> Unit?) {
        account?.let { accountManager.updateAccount(it) }
        apiService.getUserInfo()
                .applySchedulers()
                .subscribeApi(onNext = {
                    SpUtil.saveOrUpdate(AppConfig.PHONE_NUM, it.mobile ?: "")
                    accountCache.put(ProfileFragment.ACCOUNT_CACHE_KEY, it)
                    jMessageFunc.loginIM {
                        loginChatRoom()
                    }
                    SensorsDataAPI.sharedInstance().login(account?.userId.toString())
                    mLoginStateLiveData.postValue(true)

                    RxBus.get().post(LoginSuccessEvent(""))
                    if (accountManager.hasAccount()) {
                        getABTestCase()
                        spider.programEvent(SpiderEventNames.Program.LOGIN_STATUS_CHANGED)
                                .put("userID", account?.userId ?: "")
                                .put("type", loginType)
                                .put("method", "login").track()
                    }

                    if (loginType.isNotEmpty()) {
                        trackClick(ClickAction.LOGIN_SUCCESS, arrayListOf(
                                ClickAction.LOGIN_TYPE to loginType))
                        storage.appPreference.edit(commit = true) {
                            putString(ClickAction.LOGIN_TYPE, loginType)
                        }
                        SpUtil.saveOrUpdate(AppConfig.LOGIN_TYPE, loginType)
                    }
                    ToastManage.showCenterToast(app, app.getString(R.string.string_login_success))
                    callback()
                }, onApiError = {
                    accountManager.logout()
                    storage.userCache.cacheOf<UserAccount>().remove(ProfileFragment.ACCOUNT_CACHE_KEY)
                    mLoginStateLiveData.postValue(false)
                })
    }

    private fun loginChatRoom() {
        apiService.getChatRoomList()
                .applySchedulers()
                .subscribeApi(onNext = {
                    jMessageFunc.enterChatRoom(it)
                }, onApiError = {

                })
    }

    private fun performLoginError(apiError: ApiError) {
        if (apiError is ApiError.HttpError) {
            val httpError = apiError.apiError.getOrElse { null } as? FishApiError
            when (FishApiError.VERIFY_CODE_INVALID_CODE) {
                httpError?.errorCode -> {
                    ToastManage.showCenterToast(app, app.getString(R.string.string_valid_code_error))
                }
                else -> {
                }
            }
        }
        mLoginStateLiveData.postValue(false)
    }

    private fun performBindMobileError(apiError: ApiError) {

        val msg = (apiError as ApiError.HttpError).displayMsg
        if (msg.isNotEmpty()) {
            app.showLongToast(msg)
        } else {
            app.showLongToast(app.getString(R.string.string_bind_phone_error))
        }

        mBindMobileLiveData.postValue(false)
    }

    private fun getABTestCase() {
        apiService.testcase().applySchedulers().subscribeApi(onNext = {
            appConfig.setShowNewChannel(it.test_case_channel_page != null && it.test_case_channel_page!!)
            appConfig.setCaseRoulettePage(it.test_case_roulette_page != null && it.test_case_roulette_page!!)
            appConfig.setShowFollowPage(it.test_case_follow_index != null && it.test_case_follow_index!!)
            appConfig.setShowCreateFeed(it.test_case_create_feed != null && it.test_case_create_feed!!)
            appConfig.setShowPackage(it.test_case_show_package != null && it.test_case_show_package!!)
            appConfig.setShowNewHomePage(it.test_case_new_display_page != null && it.test_case_new_display_page!!)
            appConfig.setDanmakaOpen(it.test_case_danmuku != null && it.test_case_danmuku!!)
            postSpiderEvent(it)
        }, onApiError = {

        })
    }

    private fun postSpiderEvent(response: TestCaseResp) {
        val toJson = Gson().toJson(response)
        val toMap = Gson().fromJson(toJson, Map::class.java)
        toMap.filter { it.value == true }
        val toJsonMap = Gson().toJson(toMap)
        spider.programEvent(SpiderEventNames.Program.AB_INTERFACE).put("info", toJsonMap).track()
    }

    private fun handleApiError(apiError: ApiError) {
        when (apiError) {
            is ApiError.HttpError -> onHttpError(apiError)
            is ApiError.NetworkError -> onNetworkError(apiError.throwable)
            is ApiError.UnExpectedError -> onUnExpectedError(apiError.throwable)
        }
    }

    private fun onHttpError(httpError: ApiError.HttpError) {
        ToastManage.s(app, httpError.displayMsg)
    }

    private fun onNetworkError(@Suppress("UNUSED_PARAMETER") throwable: Throwable) {
        ToastManage.s(app, app.getString(R.string.string_net_unavailable))
    }

    private fun onUnExpectedError(throwable: Throwable) {
        throwable.throwOrLog()
    }

}