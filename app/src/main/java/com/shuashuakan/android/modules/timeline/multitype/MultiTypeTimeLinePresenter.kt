package com.shuashuakan.android.modules.timeline.multitype

import android.content.Context
import com.shuashuakan.android.commons.di.AppContext
import com.shuashuakan.android.data.api.model.home.multitypetimeline.MultiTypeTimeLineModel
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.exts.mvp.ApiPresenter
import io.reactivex.Observable
import javax.inject.Inject

/**
 * 个人页-动态 Presenter
 *
 * Author: ZhaiDongyang
 * Date: 2019/1/19
 */
class MultiTypeTimeLinePresenter @Inject constructor(
    @AppContext context: Context, private val apiService: ApiService
) : ApiPresenter<MultiTypeTimeLineApiView<MultiTypeTimeLineModel>, MultiTypeTimeLineModel>(context) {

  private var mCount: Int = 0
  private var mNextId: String? = null

  override fun getObservable(): Observable<MultiTypeTimeLineModel> {
    return apiService.getMultiTypeTimeLineData(next_id = mNextId, count = mCount)
  }

  fun requestApi(nextId: String? = null, count: Int) {
    mCount = count
    mNextId = nextId
    subscribe()
  }

  override fun onSuccess(data: MultiTypeTimeLineModel) {
    view.showMultiTypeTimeLineData(data)
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

interface MultiTypeTimeLineApiView<in Data> : com.shuashuakan.android.exts.mvp.ApiView {
  fun showMultiTypeTimeLineData(data: Data)
  fun showError()
}