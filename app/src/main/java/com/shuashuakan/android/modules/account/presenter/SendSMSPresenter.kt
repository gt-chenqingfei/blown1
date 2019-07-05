package com.shuashuakan.android.modules.account.presenter

import android.content.Context
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.data.api.model.CommonResult
import com.shuashuakan.android.commons.di.AppContext
import com.shuashuakan.android.data.api.model.CommonResult.Result
import com.shuashuakan.android.exts.mvp.ApiPresenter
import io.reactivex.Observable
import javax.inject.Inject

class SendSMSPresenter @Inject constructor(
  @AppContext context: Context, private val apiService: ApiService
) : ApiPresenter<SendSMSApiView<Result>, CommonResult>(context) {

  private var mobilePhone: String = ""

  override fun onSuccess(data: CommonResult) {
    view.showData(data.result)
  }

  override fun getObservable(): Observable<CommonResult> =
    apiService.sendSMSCode(mobilePhone = mobilePhone)

  fun requestApi(mobilePhone: String) {
    this.mobilePhone = mobilePhone
    subscribe()
  }
}

interface SendSMSApiView<in DATA> : com.shuashuakan.android.exts.mvp.ApiView {
  fun showData(data: DATA)
}