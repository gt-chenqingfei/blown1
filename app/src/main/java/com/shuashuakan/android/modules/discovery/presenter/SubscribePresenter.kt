/*
package com.shuashuakan.android.ui.explore.presenters

import android.content.Context
import com.shuashuakan.android.commons.di.AppContext
import com.shuashuakan.android.data.api.model.channel.SubscribeModel
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.modules.ApiPresenter
import com.shuashuakan.android.ui.mvp.ApiView
import io.reactivex.Observable
import javax.inject.Inject

*/
/**
 * Author:  liJie
 * Date:   2019/1/14
 * Email:  2607401801@qq.com
 *//*

class SubscribePresenter @Inject constructor(
    @AppContext context: Context, private val apiService: ApiService
) : ApiPresenter<SubscribeApiView<List<SubscribeModel>>, List<SubscribeModel>>(context) {

  private var page: Int = 0
  private var tabName: String? = null
  override fun onSuccess(data: List<SubscribeModel>) {
    view.showData(data)
  }

  override fun getObservable(): Observable<List<SubscribeModel>> {
    return if (tabName == "已订阅") apiService.getSubscribedList(page) else apiService.getAllChannel(page)
  }

  fun requestApi(page: Int, tabName: String?) {
    this.page = page
    this.tabName = tabName
    subscribe()
  }
}

interface SubscribeApiView<in Data> : ApiView {
  fun showData(data: Data)
}*/
