package com.shuashuakan.android.modules.timeline.multitype

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.shuashuakan.android.R
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.data.api.model.home.multitypetimeline.RecommendUserFeedCards
import com.shuashuakan.android.data.api.model.home.multitypetimeline.RecommendUserFeedContent
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.FeedTransportManager
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.account.activity.LoginActivity
import com.shuashuakan.android.modules.player.activity.VideoPlayActivity
import com.shuashuakan.android.modules.timeline.vm.MutitypeTimeLineViewModel
import com.shuashuakan.android.modules.widget.FollowTextView
import com.shuashuakan.android.modules.widget.SpiderAction
import com.shuashuakan.android.utils.*

/**
 * @author hushiguang
 * @since 2019-06-20.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
class MultiTypeTimeLineRecommendUserFeedViewHolder(
        private val mutitypeTimeLineViewModel: MutitypeTimeLineViewModel,
        val mContext: Context,
        val helper: BaseViewHolder,
        val dataList: RecommendUserFeedContent) {

    val accountManager: AccountManager = mContext.applicationContext.daggerComponent().accountManager()
    val apiService: ApiService = mContext.applicationContext.daggerComponent().apiService()

    init {
        formatContent()
    }


    private fun formatContent() {
        var dataModel = dataList.data
        dataModel ?: return
        var list = dataModel.list
        list ?: return
        if (list.isEmpty()) {
            return
        }

        var recommendUserFeedCards = list[0]
        val followTextView = helper.getView<FollowTextView>(R.id.mRecommendUserFollowView)
        val horizontalRecyclerView = helper.getView<RecyclerView>(R.id.mRecommendUserRecyclerView)
        val mUserHeadImageView = helper.getView<SimpleDraweeView>(R.id.mRecommendUserImageView)
        horizontalRecyclerView.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        val mHorizontalRecommendFeedAdapter = HorizontalRecommendFeedAdapter()
        mHorizontalRecommendFeedAdapter.bindToRecyclerView(horizontalRecyclerView)

        mUserHeadImageView.setImageUrl2Webp(recommendUserFeedCards.avatar
                ?: "", mContext.dip(36), mContext.dip(36))
        helper.setText(R.id.mRecommendUserNameView, recommendUserFeedCards.nick_name)
        recommendUserFeedCards.properties?.let {
            helper.setText(R.id.mRecommendUserTagView, it.recommend_reason)
            mHorizontalRecommendFeedAdapter.setNewData(it.feed_data)
        }
        followTextView.visibility = if (recommendUserFeedCards.is_follow == true) View.GONE else View.VISIBLE
        followTextView.text = if (recommendUserFeedCards.is_fans == true) {
            mContext.getString(R.string.string_follow_fans)
        } else {
            mContext.getString(R.string.string_follow)
        }

        followTextView.setOnClickListener {
            if (accountManager.hasAccount()) {
                followUser(recommendUserFeedCards, followTextView) {}
            } else {
                mutitypeTimeLineViewModel.multiTypeTimeLineRecommendUserFeedViewHolder = this
            }
        }

        helper.itemView.setOnClickListener {
            mContext.startActivity(recommendUserFeedCards.redirect_url)
        }
    }


    internal inner class HorizontalRecommendFeedAdapter :
            BaseQuickAdapter<Feed, BaseViewHolder>(R.layout.item_timeline_recommend_user_feed_content) {
        override fun convert(helper: BaseViewHolder, item: Feed) {
            val itemCoverImageView = helper.getView<SimpleDraweeView>(R.id.mItemRecommendUserCoverView)
            val itemCoverShadowView = helper.getView<View>(R.id.mItemRecommendUserCoverShadowView)
            val itemFeedContentView = helper.getView<TextView>(R.id.mItemRecommendUserContentView)

            // 设置左边距的位置
            (helper.itemView.layoutParams as RecyclerView.LayoutParams).leftMargin =
                    mContext.dip(if (helper.adapterPosition == 0) {
                        15
                    } else 0)

            // 动态设置View 的宽高
            val width = (mContext.resources.displayMetrics.widthPixels - mContext.dip(50)) / 2
            val height = width / 155.0f * 97f
            itemCoverImageView.layoutParams.width = width
            itemCoverImageView.layoutParams.height = height.toInt()
            itemCoverShadowView.layoutParams.width = width
            itemCoverShadowView.layoutParams.height = height.toInt()
            itemFeedContentView.layoutParams.width = width
            itemFeedContentView.text = StringUtils.replaceBlank(item.title)

            itemCoverImageView.setImageUrl2Webp(item.cover!!, mContext.dip(140), mContext.dip(84))

            helper.itemView.setOnClickListener {
                val intentParam = FeedTransportManager.createRecommendUserFeedParam(data as List<Feed>, item, item.channelId?.toLong())
                mContext.startActivity(VideoPlayActivity.create(mContext, intentParam))
            }
        }
    }

    private fun followUser(feedData: RecommendUserFeedCards, followText: FollowTextView,
                           followSuccess: () -> Unit) {
        if (!accountManager.hasAccount()) {
            waitFeedData = feedData
            waitFollowText = followText
            LoginActivity.launch(followText.context)
        } else {
            if (!feedData.is_follow!!) {
                apiService.createFollow(feedData.user_id.toString())
                        .applySchedulers()
                        .subscribeApi(onNext = {
                            if (it.result.isSuccess) {
                                followSuccess.invoke()
                                FollowCacheManager.putFollowUserToCache(feedData.user_id.toString(), true)
                                feedData.is_follow = true
                                followText.followSuccessGone()
                                followText.context.getSpider().userFollowEvent(followText.context, feedData.user_id.toString(),
                                        SpiderAction.VideoPlaySource.EXPLORE_PAGE.source, true)
                            }
                        }
                        )
            }
        }
    }

    var waitFeedData: RecommendUserFeedCards? = null
    var waitFollowText: FollowTextView? = null

    fun onLoginFollowStatus(onFollowCallback: () -> Unit) {
        if (accountManager.hasAccount()) {
            waitFeedData ?: return
            waitFollowText ?: return
            followUser(waitFeedData!!, waitFollowText!!) {
                onFollowCallback.invoke()
                waitFeedData = null
                waitFollowText = null
            }
        }
    }

}