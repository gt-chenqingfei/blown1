package com.shuashuakan.android.modules.partition.vm

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.data.api.model.partition.PartitionBannerModel
import com.shuashuakan.android.data.api.model.partition.PartitionLeaderBoardModel
import com.shuashuakan.android.data.api.model.partition.PartitionRecommendModel
import com.shuashuakan.android.data.api.model.partition.PartitionUserModel
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.event.LoginSuccessEvent
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.FeedTransportManager
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.discovery.ItemDataPair
import com.shuashuakan.android.modules.partition.PartitionConstant
import com.shuashuakan.android.modules.player.activity.VideoPlayActivity
import com.shuashuakan.android.utils.categoryFeedLeaderboardDidSelectedEvent
import com.shuashuakan.android.utils.daggerComponent
import com.shuashuakan.android.utils.getSpider
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo

/**
 * @author hushiguang
 * @since 2019-06-17.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
class CategoryContentViewModel(val app: Application) : AndroidViewModel(app) {
    var apiService: ApiService
    val partitionLiveData = MutableLiveData<MutableList<MultiItemEntity>>()
    val mLeaderBoardFeedListLiveData = MutableLiveData<List<Feed>>()
//    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    init {
        apiService = app.daggerComponent().apiService()
    }

    fun getPartitionDetail(partitionId: Int) {
        apiService.getPartition(partitionId)
                .applySchedulers()
                .subscribeApi(
                        onNext = {
                            val resultPartitionData: MutableList<MultiItemEntity> = mutableListOf()
                            val mLeaderBoardFeedList = arrayListOf<Feed>()
                            for (item in it.classificationList!!) {
                                when (item) {
                                    is PartitionBannerModel -> resultPartitionData.add(ItemDataPair(item, PartitionConstant.BANNER))
                                    is PartitionUserModel -> resultPartitionData.add(ItemDataPair(item, PartitionConstant.CHAIN_USER_LIST))
                                    is PartitionRecommendModel -> {
                                        resultPartitionData.add(ItemDataPair(item, PartitionConstant.HOT_CHANNEL_TITLE))
                                        for (position in item.dataList.indices) {
                                            val partitionRecommendItemModel = item.dataList[position]
                                            partitionRecommendItemModel.index = position
                                            partitionRecommendItemModel.allCount = item.dataList.size
                                            resultPartitionData.add(ItemDataPair(partitionRecommendItemModel, PartitionConstant.HOT_CHANNEL))
                                        }
                                    }
                                    is PartitionLeaderBoardModel -> {
                                        resultPartitionData.add(ItemDataPair(item, PartitionConstant.HOT_CHAIN_TITLE))
                                        for (feed in item.dataList) {
                                            mLeaderBoardFeedList.add(feed)
                                            resultPartitionData.add(ItemDataPair(feed, PartitionConstant.HOT_CHAIN))
                                        }
                                    }
                                }
                            }
                            mLeaderBoardFeedListLiveData.value = mLeaderBoardFeedList
                            partitionLiveData.postValue(resultPartitionData)
                        }, onApiError = {
                    partitionLiveData.postValue(mutableListOf())
                })
    }


    override fun onCleared() {
        super.onCleared()
//        compositeDisposable.clear()
    }

    // 热门视频的点击事件
    fun onHotLeaderBoardClick(categoryId: String, mContext: Context, position: Int) {
        partitionLiveData.value?.let {
            val itemDataPair = it[position] as ItemDataPair
            val data = itemDataPair.data
            if (data is Feed) {
                mLeaderBoardFeedListLiveData.value?.let { feedList ->
                    mContext.getSpider().categoryFeedLeaderboardDidSelectedEvent(categoryId, data.id)
                    val intentParam = FeedTransportManager.createCategoryLeaderBoardParam(feedList,
                            data, data.channelId?.toLong())
                    mContext.startActivity(VideoPlayActivity.create(mContext, intentParam))
                }
            }
        }
    }
}