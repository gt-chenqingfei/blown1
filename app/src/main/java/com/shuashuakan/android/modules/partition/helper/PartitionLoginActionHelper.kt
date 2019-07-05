package com.shuashuakan.android.modules.partition.helper

import android.content.Context
import com.shuashuakan.android.R
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.data.api.model.partition.PartitionChainUserItemModel
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.modules.widget.FollowTextView
import com.shuashuakan.android.modules.widget.SpiderAction
import com.shuashuakan.android.utils.FollowCacheManager
import com.shuashuakan.android.utils.getSpider
import com.shuashuakan.android.utils.showLongToast
import com.shuashuakan.android.utils.userFollowEvent
import io.reactivex.disposables.CompositeDisposable

/**
 * @author hushiguang
 * @since 2019-06-20.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
object PartitionLoginActionHelper {

    private var waitUserModel: PartitionChainUserItemModel? = null
    private var waitFollowTextView: FollowTextView? = null
    private var waitFeed: Feed? = null

    fun setLoginActionWithFollowUser(waitUserModel: PartitionChainUserItemModel?,
                                     waitFollowTextView: FollowTextView?) {
        this.waitUserModel = waitUserModel
        this.waitFollowTextView = waitFollowTextView
    }

    fun setLoginActionWithFollowFeed(waitFeed: Feed?,
                                     waitFollowTextView: FollowTextView?) {
        this.waitFeed = waitFeed
        this.waitFollowTextView = waitFollowTextView
    }

    fun loginActionToFollow(mContext: Context, compositeDisposable: CompositeDisposable,
                            apiService: ApiService) {
        loginActionWithFollowUser(mContext, compositeDisposable, apiService)
        loginActionWithFollowFeed(mContext, compositeDisposable, apiService)
    }

    private fun resetWaitLoginView() {
        waitUserModel = null
        waitFeed = null
        waitFollowTextView = null
    }


    private fun loginActionWithFollowUser(mContext: Context,
                                          compositeDisposable: CompositeDisposable,
                                          apiService: ApiService) {
        waitUserModel ?: return
        waitFollowTextView ?: return
        createFollow(compositeDisposable, apiService, mContext, waitUserModel!!.userId.toString()) { follow, userId ->
            waitUserModel ?: return@createFollow
            waitFollowTextView ?: return@createFollow
            waitFollowTextView!!.followSuccessInInvisible()
            waitUserModel!!.isFollow = follow
            mContext.getSpider().userFollowEvent(mContext,
                    userId, SpiderAction.VideoPlaySource.CATEGORY_USER_LEADER_BOARD.source, true)
        }
    }

    private fun loginActionWithFollowFeed(mContext: Context,
                                          compositeDisposable: CompositeDisposable,
                                          apiService: ApiService) {
        waitFeed ?: return
        waitFollowTextView ?: return
        createFollow(compositeDisposable, apiService, mContext, waitFeed!!.getUserId()) { follow, userId ->
            waitFeed ?: return@createFollow
            waitFollowTextView ?: return@createFollow
            waitFollowTextView!!.followSuccessInInvisible()
            waitFeed!!.hasFollowUser = follow
            mContext.getSpider().userFollowEvent(mContext,
                    userId, SpiderAction.VideoPlaySource.CATEGORY_FEED_LEADER_BOARD.source, true)
        }
    }

    private fun createFollow(compositeDisposable: CompositeDisposable,
                             apiService: ApiService,
                             mContext: Context, userId: String,
                             onFollowSuccess: (Boolean, String) -> Unit) {
        FollowHelper.createFollow(compositeDisposable,
                apiService, userId) {
            if (it) {
                FollowCacheManager.putFollowUserToCache(userId, true)
                onFollowSuccess.invoke(true, userId)
            } else {
                onFollowSuccess.invoke(false, "")
                mContext.applicationContext
                        .showLongToast(mContext.applicationContext.getString(R.string.string_follow_error))
            }
            resetWaitLoginView()
        }
    }
}