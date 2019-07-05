package com.shuashuakan.android.modules.topic

import android.content.Context
import com.shuashuakan.android.commons.di.AppContext
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.data.api.model.channel.ChannelFeed
import com.shuashuakan.android.exts.mvp.ApiPresenter
import io.reactivex.Observable
import javax.inject.Inject

class ChannelFeedPresenter @Inject constructor(
  @AppContext val context: Context, val apiService: ApiService
) : ApiPresenter<ChannelFeedApiView<ChannelFeed>, ChannelFeed>(context) {

  private var channelId = ""
  private var feedId: String? = null

  override fun onSuccess(data: ChannelFeed) {
    view.showData(data)
  }

  override fun getObservable(): Observable<ChannelFeed> {
    return apiService.getChannelFeeds(feedId, channelId)
  }

  fun request(feedId: String?, channelId: String) {
    this.channelId = channelId
    this.feedId = feedId
    subscribe()
  }
}

interface ChannelFeedApiView<in DATA> : com.shuashuakan.android.exts.mvp.ApiView {
  fun showData(m: DATA)
}