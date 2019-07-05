package com.shuashuakan.android.modules.timeline.profile

import android.content.Context
import com.shuashuakan.android.commons.di.AppContext
import com.shuashuakan.android.data.api.model.home.Feed
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
class ProfileTimeLinePresenter @Inject constructor(
        @AppContext context: Context, private val apiService: ApiService
) : ApiPresenter<ProfileTimeLineApiView<List<Feed>>, List<Feed>>(context) {

    private var page: Int = 0
    private var isMine: Boolean = false
    private var userId: String = ""

    override fun getObservable(): Observable<List<Feed>> {
        return if (!isMine) {
            apiService.otherUserUploadFeeds(page, userId)
        } else {
            apiService.userUploadVideo(page)
        }
    }

    fun requestApi(page: Int, isMine: Boolean, userId: String) {
        this.page = page
        this.isMine = isMine
        this.userId = userId
        subscribe()
    }

    override fun onSuccess(data: List<Feed>) {
        view?.showProfileTimeLineData(data)
    }

    override fun onNetworkError(throwable: Throwable) {
        super.onNetworkError(throwable)
        view?.showError()
    }

    override fun onHttpError(httpError: ApiError.HttpError) {
        super.onHttpError(httpError)
        view?.showError()
    }
}

interface ProfileTimeLineApiView<in Data> : com.shuashuakan.android.exts.mvp.ApiView {
    fun showProfileTimeLineData(data: Data)
    fun showError()
}