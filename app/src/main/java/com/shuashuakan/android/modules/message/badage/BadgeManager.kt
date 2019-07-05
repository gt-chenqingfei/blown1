package com.shuashuakan.android.modules.message.badage

import android.util.Log
import com.shuashuakan.android.BuildConfig
import com.shuashuakan.android.config.AppConfig
import com.shuashuakan.android.config.AppConfig.Companion.HAVE_SYS_MSG
import com.shuashuakan.android.config.AppConfig.Companion.HAVE_SYS_PERSONAL_MSG
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.utils.SpUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author hushiguang
 * @since 2019-05-21.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
@Singleton
class BadgeManager @Inject constructor(private val accountManager: AccountManager) {

    private var MAX_TIME = 5
    private var disposable: Disposable? = null
    var isOpenProfile = true

    fun startMainBadgeBubbleInterval() {
        if (disposable != null || !accountManager.hasAccount()) {
            return
        }
        isOpenProfile = false
        RxBus.get().post(BadgeEvent(getBadgeCount(), true))
        disposable = Observable.interval(1, 1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io()).observeOn(
                        AndroidSchedulers.mainThread())
                .subscribe {
                    if (it >= MAX_TIME) {
                        shutdownTimer()
                        isOpenProfile = true
                        RxBus.get().post(BadgeEvent(getBadgeCount(), false))
                    }
                }
    }

    fun updateSystemBadge(showBadge: Boolean) {
        SpUtil.saveOrUpdateBoolean(HAVE_SYS_MSG, showBadge)
    }

    fun updateSystemPersonalBadge(showBadge: Boolean) {
        SpUtil.saveOrUpdateBoolean(HAVE_SYS_PERSONAL_MSG, showBadge)
    }

    fun updateAppBadgeCount() {
        if (!accountManager.hasAccount()) {
            return
        }
        var badgeCount = SpUtil.getInt(AppConfig.KEY_UN_READ_MESSAGE, 0) + 1
        SpUtil.putInt(AppConfig.KEY_UN_READ_MESSAGE, badgeCount)
        if (BuildConfig.DEBUG)
            Log.d("BadgeManager", "UN_READ_COUNT $badgeCount")
    }

    fun isShowSystemBadge(): Boolean {
        if (!accountManager.hasAccount()) {
            return false
        }
        return SpUtil.findBoolean(HAVE_SYS_MSG)
    }

    fun isShowPersonalBadge(): Boolean {
        if (!accountManager.hasAccount()) {
            return false
        }
        return SpUtil.findBoolean(HAVE_SYS_PERSONAL_MSG)
    }


    fun isShowBadge(): Boolean {
        return getBadgeCount() > 0 ||
                isShowPersonalBadge() ||
                isShowSystemBadge()
    }

    fun clearNonSystemBadge() {
        shutdownTimer()
        isOpenProfile = true
        SpUtil.putInt(AppConfig.KEY_UN_READ_MESSAGE, 0)
        RxBus.get().post(BadgeClearNonSystemEvent(true))
    }

    fun logout() {
        updateSystemBadge(false)
        updateSystemPersonalBadge(false)
        clearNonSystemBadge()
    }

    private fun getBadgeCount(): Int {
        return SpUtil.getInt(AppConfig.KEY_UN_READ_MESSAGE, 0)
    }

    private fun shutdownTimer() {
        disposable?.let {
            if (!it.isDisposed) {
                it.dispose()
                disposable = null
            }
        }
    }
}