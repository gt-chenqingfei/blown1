package com.shuashuakan.android.modules.home.vm

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.shuashuakan.android.ApplicationMonitor
import com.shuashuakan.android.analytics.AppForceStatusObserver
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.utils.daggerComponent

/**
 * @author hushiguang
 * @since 2019-06-05.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
class HomeViewModel(val app: Application) : AndroidViewModel(app) {
    private val apiService: ApiService = app.daggerComponent().apiService()
    private val applicationMonitor: ApplicationMonitor = app.daggerComponent().appMonitor()
    val mTimelineBadgeLiveData = MutableLiveData<Boolean>()
    val mApplicationStateLiveData = MutableLiveData<ApplicationMonitor.ApplicationState>()

    init {
        applicationMonitor.applicationState(AppForceStatusObserver {
            mApplicationStateLiveData.postValue(it)
        })
    }

    fun getTimeLineBadge() {
        apiService.getFollowUnReadPoint()
                .applySchedulers()
                .subscribeApi(onNext = {
                    mTimelineBadgeLiveData.postValue(it.result.isSuccess)
                }, onApiError = {
                    mTimelineBadgeLiveData.postValue(false)
                })
    }

}