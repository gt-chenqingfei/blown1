package com.shuashuakan.android.modules.account.presenter

import android.content.Context
import com.shuashuakan.android.commons.di.AppContext
import com.shuashuakan.android.data.api.model.home.HomeRecommendInterestTypeModelDetail
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.exts.mvp.ApiPresenter
import io.reactivex.Observable
import javax.inject.Inject

class PrefectSelectHobbyPresenter @Inject constructor(
    @AppContext context: Context, private val apiService: ApiService
) : ApiPresenter<PrefectSelectHobbyPresenter.PrefectSelectHobbyApiView<HomeRecommendInterestTypeModelDetail>, HomeRecommendInterestTypeModelDetail>(context) {


  override fun getObservable(): Observable<HomeRecommendInterestTypeModelDetail> {
    return apiService.getMyInterest()
  }

  fun requestApi() {
    subscribe()
  }

  override fun onSuccess(data: HomeRecommendInterestTypeModelDetail) {
    view.showData(data)
  }

  override fun onNetworkError(throwable: Throwable) {
    super.onNetworkError(throwable)
    view.showError()
  }

  override fun onHttpError(httpError: ApiError.HttpError) {
    super.onHttpError(httpError)
    view.showError()
  }

  interface PrefectSelectHobbyApiView<in Data> : com.shuashuakan.android.exts.mvp.ApiView {
    fun showData(data: Data)
    fun showError()
  }
}