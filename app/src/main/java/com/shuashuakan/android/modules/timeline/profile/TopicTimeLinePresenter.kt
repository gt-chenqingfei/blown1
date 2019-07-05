package com.shuashuakan.android.modules.timeline.profile

import android.content.Context
import com.shuashuakan.android.commons.di.AppContext
import com.shuashuakan.android.data.api.model.home.TopicTimeLineModel
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.exts.mvp.ApiPresenter
import io.reactivex.Observable
import javax.inject.Inject

/**
 * 话题-动态 Presenter
 *
 * Author: ZhaiDongyang
 * Date: 2019/1/19
 */
class TopicTimeLinePresenter @Inject constructor(
    @AppContext context: Context, private val apiService: ApiService
) : ApiPresenter<TopicTimeLineApiView<TopicTimeLineModel>, TopicTimeLineModel>(context) {

  private var channelId: Long = 0
  private var nextId: String? = null

  override fun getObservable(): Observable<TopicTimeLineModel> {
    if (nextId!!.isEmpty()) {
      return apiService.getChannelTopicTimeLineInfo(channelId)
    } else {
      return apiService.getChannelTopicTimeLineInfo(channelId, nextId)
    }
  }

  fun requestApi(channelId: Long, nextId: String?) {
    this.channelId = channelId
    this.nextId = nextId
    subscribe()
  }

  override fun onSuccess(data: TopicTimeLineModel) {
    view.showTopicTimeLineData(data)
  }

  override fun onNetworkError(throwable: Throwable) {
    super.onNetworkError(throwable)
    view.showError()
  }

  override fun onHttpError(httpError: ApiError.HttpError) {
    super.onHttpError(httpError)
    view.showError()
  }
}

interface TopicTimeLineApiView<in Data> : com.shuashuakan.android.exts.mvp.ApiView {
  fun showTopicTimeLineData(data: Data)
  fun showError()
}