package com.shuashuakan.android.modules.timeline.vm

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.event.FeedFollowChangeEvent
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.timeline.multitype.MultiTypeTimeLineRecommendUserFeedViewHolder
import com.shuashuakan.android.modules.timeline.multitype.MultiTypeTimeLineVideoViewHolder
import com.shuashuakan.android.utils.FollowCacheManager

/**
 * @author hushiguang
 * @since 2019-05-21.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
class MutitypeTimeLineViewModel : ViewModel() {
    var followUserLiveData = MutableLiveData<FeedFollowChangeEvent>()
    var mFollowUserWithLoginCache: Feed? = null

    var targetUserId: String? = null
    var isFollow: Boolean? = null

    var mRecommendUserPosition: Int = -1
    var isNeedRefreshData = false

    var multiTypeTimeLineVideoViewHolder: MultiTypeTimeLineVideoViewHolder? = null
    var multiTypeTimeLineRecommendUserFeedViewHolder: MultiTypeTimeLineRecommendUserFeedViewHolder? = null

    fun followOrUnFollowUser(apiService: ApiService, userId: String, hasFollowUser: Boolean, success: () -> Unit) {
        if (!hasFollowUser) {
            apiService.createFollow(userId)
                    .applySchedulers()
                    .subscribeApi(
                            onNext = {
                                if (it.result.isSuccess) {
                                    success.invoke()
                                    FollowCacheManager.putFollowUserToCache(userId, true)
                                    followUserLiveData.postValue(FeedFollowChangeEvent(userId, true))
                                    resetData()
                                }
                            }, onApiError = {}
                    )
        } else {
            apiService.cancelFollow(userId)
                    .applySchedulers()
                    .subscribeApi(onNext = {
                        if (it.result.isSuccess) {
                            FollowCacheManager.putFollowUserToCache(userId, false)
                            followUserLiveData.postValue(FeedFollowChangeEvent(userId, false))
                            resetData()
                        }
                    }, onApiError = {}
                    )
        }
    }

    private fun resetData() {
        mFollowUserWithLoginCache = null
        targetUserId = null
        isFollow = null
    }

    fun loginEventFollow(apiService: ApiService) {


        mFollowUserWithLoginCache?.let {
            followOrUnFollowUser(apiService, it.userId.toString(), it.hasFollowUser!!, {})
        }

        targetUserId?.let {
            followOrUnFollowUser(apiService, it, false, {})
        }

        multiTypeTimeLineVideoViewHolder?.let {
            multiTypeTimeLineVideoViewHolder?.onLoginUpStatus {
                multiTypeTimeLineVideoViewHolder = null
            }
        }

        multiTypeTimeLineRecommendUserFeedViewHolder?.let {
            multiTypeTimeLineRecommendUserFeedViewHolder?.onLoginFollowStatus {
                multiTypeTimeLineRecommendUserFeedViewHolder = null
            }
        }
    }
}
