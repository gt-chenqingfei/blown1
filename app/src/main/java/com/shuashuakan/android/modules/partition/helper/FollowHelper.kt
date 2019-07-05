package com.shuashuakan.android.modules.partition.helper

import android.content.Context
import com.shuashuakan.android.R
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.utils.extension.showCancelFollowDialog
import com.shuashuakan.android.utils.showLongToast
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo

/**
 * @author hushiguang
 * @since 2019-06-17.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
object FollowHelper {

    fun createFollow(compositeDisposable: CompositeDisposable,
                     apiService: ApiService,
                     userId: String,
                     onFollowSuccess: (Boolean) -> Unit) {

        apiService.createFollow(userId)
                .applySchedulers()
                .subscribeApi(onNext = {
                    onFollowSuccess(it.result.isSuccess)
                }, onApiError = {
                    onFollowSuccess(false)
                }).addTo(compositeDisposable)
    }

    fun cancelFollow(compositeDisposable: CompositeDisposable,
            context: Context,
            apiService: ApiService,
            userId: String,
            nickName: String,
            onCancelSuccess: (Boolean) -> Unit) {
        val cancelFollow = {
            apiService.cancelFollow(userId).applySchedulers().subscribeApi(onNext = {
                onCancelSuccess.invoke(it.result.isSuccess)
            }, onApiError = {
                onCancelSuccess.invoke(false)
                if (it is ApiError.HttpError) {
                    context.showLongToast(it.displayMsg)
                } else {
                    context.showLongToast(context.getString(R.string.string_un_follow_error))
                }
            }).addTo(compositeDisposable)
        }
        context.showCancelFollowDialog(nickName, cancelFollow)
    }

}