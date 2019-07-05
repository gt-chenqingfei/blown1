package com.shuashuakan.android.modules.partition.vm

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.shuashuakan.android.data.api.model.partition.PartitionData
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.utils.daggerComponent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo

/**
 * @author hushiguang
 * @since 2019-06-17.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
class CategoryIndexViewModel(app: Application) : AndroidViewModel(app) {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val apiService = app.daggerComponent().apiService()
    val partitionListLiveData = MutableLiveData<List<PartitionData>>()


    fun getPartitionTab() {
        apiService.getPartitionTab()
                .applySchedulers()
                .subscribeApi(
                        onNext = {
                            partitionListLiveData.postValue(it)
                        }, onApiError = {
                    partitionListLiveData.postValue(arrayListOf())
                }
                )
                .addTo(compositeDisposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

}