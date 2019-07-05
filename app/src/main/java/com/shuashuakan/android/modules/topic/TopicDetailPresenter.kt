package com.shuashuakan.android.modules.topic

import android.content.Context
import com.shuashuakan.android.commons.di.AppContext
import com.shuashuakan.android.data.api.model.channel.ChannelTopicInfo
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.exts.mvp.ApiPresenter
import io.reactivex.Observable
import javax.inject.Inject

class ChannelTopicPresenter @Inject constructor(
        @AppContext val context: Context, val apiService: ApiService
) : ApiPresenter<ChannelTopicApiView<ChannelTopicInfo>, ChannelTopicInfo>(context) {

    private var channelId:String ?= null

    override fun onSuccess(data: ChannelTopicInfo) {
        view.showData(data)
    }

    override fun getObservable(): Observable<ChannelTopicInfo> {
        return apiService.getChannelInfo(channelId)
    }

    fun request(channelId: String?) {
        this.channelId = channelId
        subscribe()
    }
}

interface ChannelTopicApiView<in DATA> : com.shuashuakan.android.exts.mvp.ApiView {
    fun showData(m: DATA)
}