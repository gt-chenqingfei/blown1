/*
package com.shuashuakan.android.modules.profile.presenter

import android.content.Context
import com.shuashuakan.android.commons.di.AppContext
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.exts.mvp.ApiError.HttpError
import com.shuashuakan.android.exts.mvp.ApiPresenter
import io.reactivex.Observable
import javax.inject.Inject

class UploadFeedListPresenter @Inject constructor(
    @AppContext context: Context, private val apiService: ApiService
) : ApiPresenter<UploadFeedListApiView<List<Feed>>, List<Feed>>(context) {

  private var page = 0

  private var userId: String = ""
  private var isMine:Boolean=false

  override fun getObservable(): Observable<List<Feed>> {
    if(isMine){
      return apiService.userUploadVideo(page)
    }else {
      return apiService.otherUserUploadFeeds(page, userId)
    }
  }

  fun requestApi(page: Int, userId: String,isMine:Boolean) {
    this.page = page
    this.userId = userId
    this.isMine=isMine
    subscribe()
  }

  override fun onSuccess(data: List<Feed>) {
    view.showData(data)
  }

  override fun onNetworkError(throwable: Throwable) {
    super.onNetworkError(throwable)
    view.showError()
  }

  override fun onHttpError(httpError: HttpError) {
    super.onHttpError(httpError)
    view.showError()
  }
}

interface UploadFeedListApiView<in DATA> : com.shuashuakan.android.exts.mvp.ApiView {
  fun showData(data: DATA)
  fun showError()
}*/
