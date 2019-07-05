package com.shuashuakan.android.modules.profile.presenter

import android.content.Context
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.commons.di.AppContext
import com.shuashuakan.android.data.api.model.account.Good
import com.shuashuakan.android.exts.mvp.ApiError.HttpError
import com.shuashuakan.android.exts.mvp.ApiPresenter
import io.reactivex.Observable
import javax.inject.Inject

class LikedGoodListPresenter @Inject constructor(
  @AppContext context: Context, private val apiService: ApiService
) : ApiPresenter<LikedGoodListApiView<List<Good>>, List<Good>>(context) {
  private var page: Int = 0

  override fun getObservable(): Observable<List<Good>> = apiService.favGoods(page )

  fun requestApi(page: Int) {
    this.page = page
    subscribe()
  }

  override fun onSuccess(list: List<Good>) {
    view.showData(list)
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

interface LikedGoodListApiView<in DATA> : com.shuashuakan.android.exts.mvp.ApiView {
  fun showData(data: DATA)
  fun showError()
}