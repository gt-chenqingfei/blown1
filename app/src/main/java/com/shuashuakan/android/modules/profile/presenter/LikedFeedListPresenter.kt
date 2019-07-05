package com.shuashuakan.android.modules.profile.presenter

import android.content.Context
import com.shuashuakan.android.commons.di.AppContext
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.exts.mvp.ApiError.HttpError
import com.shuashuakan.android.exts.mvp.ApiPresenter
import io.reactivex.Observable
import javax.inject.Inject

class LikedFeedListPresenter @Inject constructor(
    @AppContext context: Context, private val apiService: ApiService
) : ApiPresenter<LikedFeedListApiView<List<Feed>>, List<Feed>>(context) {

  private var page = 0

  private var isMine: Boolean = false

  private var userId: String = ""

  override fun getObservable(): Observable<List<Feed>> {
    return if (!isMine) {
      apiService.otherUserFavFeeds(page, userId)
    } else {
      apiService.favFeeds(page)
    }
  }

  fun requestApi(page: Int, isMine: Boolean, userId: String) {
    this.page = page
    this.isMine = isMine
    this.userId = userId
    subscribe()
  }

  override fun onSuccess(data: List<Feed>) {
    view?.showData(data)
  }

  override fun onNetworkError(throwable: Throwable) {
    super.onNetworkError(throwable)
    view?.showError()
  }

  override fun onHttpError(httpError: HttpError) {
    super.onHttpError(httpError)
    view.showError()
  }
}

interface LikedFeedListApiView<in DATA> : com.shuashuakan.android.exts.mvp.ApiView {
  fun showData(data: DATA)
  fun showError()
}