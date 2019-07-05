package com.shuashuakan.android.modules.profile.presenter

import android.content.Context
import com.shuashuakan.android.commons.di.AppContext
import com.shuashuakan.android.data.api.model.account.UserAccount
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.exts.mvp.ApiPresenter
import io.reactivex.Observable
import javax.inject.Inject

class ProfilePresenter @Inject constructor(
        @AppContext context: Context, private val apiService: ApiService
) : ApiPresenter<ProfileApiView<UserAccount>, UserAccount>(context) {

    private var userId: String? = null
    private var isMine: Boolean = false

    override fun getObservable(): Observable<UserAccount> {
        return if (!isMine) {
            apiService.getOtherUserInfo(userId!!)
        } else {
            apiService.getUserInfo()
        }
    }

    fun requestApi(userId: String?, isMine: Boolean) {
        this.userId = userId
        this.isMine = isMine
        subscribe()
    }

    override fun onSuccess(data: UserAccount) {
        if (view == null) {
            return
        }
        view.showData(data)
    }
}

interface ProfileApiView<in DATA> : com.shuashuakan.android.exts.mvp.ApiView {
    fun showData(data: DATA)
}