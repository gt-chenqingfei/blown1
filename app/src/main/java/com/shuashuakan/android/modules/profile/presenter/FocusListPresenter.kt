package com.shuashuakan.android.modules.profile.presenter

import android.content.Context
import com.shuashuakan.android.commons.di.AppContext
import com.shuashuakan.android.data.api.model.account.FocusModel
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.modules.profile.FocusListActivity.Companion.FANS_TYPE
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.exts.mvp.ApiPresenter
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by lijie on 2018/10/24 上午11:10
 */
class FocusListPresenter @Inject constructor(
        @AppContext context: Context, private val apiService: ApiService
) : ApiPresenter<FocusListApiView<List<FocusModel>>, List<FocusModel>>(context) {

    private var page = 0
    private var count = 20
    private var isMine: Boolean = true
    private var type: String? = null

    private var userId: String? = null

    override fun getObservable(): Observable<List<FocusModel>> {
        if (type == FANS_TYPE) {
            if (isMine)
                return apiService.fansList(userId, page, count)
            else {
                Timber.e("他的" + userId)
                return apiService.fansList(userId, page, count)
            }
        } else {
            if (isMine)
                return apiService.focusList(userId, page, count)
            else
                return apiService.focusList(userId, page, count)
        }
    }

    fun requestApi(type: String?, isMine: Boolean, page: Int, userId: String?) {
        this.type = type
        this.page = page
        this.isMine = isMine
        this.userId = userId
        subscribe()
    }

    override fun onSuccess(data: List<FocusModel>) {
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
}

interface FocusListApiView<in Data> : com.shuashuakan.android.exts.mvp.ApiView {
    fun showData(data: Data)
    fun showError()
}