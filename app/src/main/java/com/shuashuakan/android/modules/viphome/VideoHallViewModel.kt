package com.shuashuakan.android.modules.viphome

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.luck.picture.lib.tools.ToastManage
import com.shuashuakan.android.R
import com.shuashuakan.android.data.api.model.channel.ChannelTopicInfo
import com.shuashuakan.android.data.api.model.home.VipHomeMessage
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.FeedTransportManager
import com.shuashuakan.android.modules.publisher.chains.ChainsListIntentParam
import com.shuashuakan.android.modules.widget.SpiderAction
import com.shuashuakan.android.utils.daggerComponent
import com.shuashuakan.android.utils.getSpider
import com.shuashuakan.android.utils.subscribeChinnalClickEvent
import com.shuashuakan.android.utils.throwOrLog

/**
 * @author:qingfei.chen
 * @date:2019/5/8  上午11:49
 */
class VideoHallViewModel(val app: Application) : AndroidViewModel(app) {

    private var mApiService: ApiService = app.applicationContext.daggerComponent().apiService()

    val mChainsListParamLiveData: MutableLiveData<ChainsListIntentParam> = MutableLiveData()
    val mChannelTopicInfoLiveData: MutableLiveData<ChannelTopicInfo> = MutableLiveData()
    val mChannelSubscribeLiveData: MutableLiveData<Boolean> = MutableLiveData()
    var mMessage: VipHomeMessage? = null
    fun getRookieVideoList(key: String) {
        mApiService.getFeedListBySecretKey(0, 20, key).applySchedulers().subscribeApi(onNext = {
            mMessage = it.message
            var chainsListIntentParam: ChainsListIntentParam? = null
            it.feedList?.let { feedList ->
                chainsListIntentParam = FeedTransportManager.createVipRoomOpenParam(feedList.toList())
            }
            mChainsListParamLiveData.postValue(chainsListIntentParam)

        }, onApiError = {
            handleApiError(it)
            mChainsListParamLiveData.postValue(null)
        })
    }


    fun getChannelTopicInfo(channelId: String?) {
        mApiService.getChannelInfo(channelId).applySchedulers().subscribeApi(onNext = {
            mChannelTopicInfoLiveData.postValue(it)
        }, onApiError = {
            mChannelTopicInfoLiveData.postValue(null)
        })
    }

    fun subscribe(channelId: Int) {
        mApiService.subscribeMethod(channelId.toLong()).applySchedulers().subscribeApi(onNext = {
            mChannelSubscribeLiveData.postValue(true)
            app.getSpider().subscribeChinnalClickEvent(app, channelId.toString(),
                    SpiderAction.VideoPlaySource.VIP_HOME_PAGE.source)
        }, onApiError = {
            mChannelSubscribeLiveData.postValue(false)
        })
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