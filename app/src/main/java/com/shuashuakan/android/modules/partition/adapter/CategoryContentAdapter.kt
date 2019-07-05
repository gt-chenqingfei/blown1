package com.shuashuakan.android.modules.partition.adapter

import android.support.v7.widget.RecyclerView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.shuashuakan.android.R
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.modules.discovery.ItemDataPair
import com.shuashuakan.android.modules.partition.PartitionConstant
import com.shuashuakan.android.modules.partition.adapter.viewhoder.PartitionBannerViewHolder
import com.shuashuakan.android.modules.partition.adapter.viewhoder.PartitionChainUserViewHolder
import com.shuashuakan.android.modules.partition.adapter.viewhoder.PartitionHotViewHolder
import com.shuashuakan.android.modules.partition.adapter.viewhoder.PartitionTopicDynamicViewHolder
import com.shuashuakan.android.modules.partition.helper.PartitionLoginActionHelper
import io.reactivex.disposables.CompositeDisposable

/**
 * @author hushiguang
 * @since 2019-06-17.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
class CategoryContentAdapter(
        var categoryId: Int,
        private val apiService: ApiService,
        dataList: List<MultiItemEntity>?) :
        BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(dataList) {


    private val compositeDisposable = CompositeDisposable()

    init {
        addItemType(PartitionConstant.BANNER, R.layout.item_multi_banner_partition)
        addItemType(PartitionConstant.CHAIN_USER_LIST, R.layout.item_muliti_chain_user_partition)
        addItemType(PartitionConstant.HOT_CHANNEL_TITLE, R.layout.item_muliti_partition_topic_title)
        addItemType(PartitionConstant.HOT_CHANNEL, R.layout.item_topic_recommand_sub_layout)
        addItemType(PartitionConstant.HOT_CHAIN_TITLE, R.layout.item_muliti_partition_hot_title)
        addItemType(PartitionConstant.HOT_CHAIN, R.layout.item_muliti_partition_hot_content)
    }

    override fun convert(helper: BaseViewHolder?, item: MultiItemEntity?) {
        val itemData = item as ItemDataPair
        when (helper?.itemViewType) {
            PartitionConstant.BANNER -> {
                PartitionBannerViewHolder(categoryId, mContext, helper, itemData)
            }
            PartitionConstant.CHAIN_USER_LIST -> {
                PartitionChainUserViewHolder(categoryId, compositeDisposable, mContext, helper, itemData)
                        .apiService = apiService
            }
            PartitionConstant.HOT_CHANNEL_TITLE -> {
                PartitionTopicDynamicViewHolderTitle(mContext, helper, itemData)
            }
            PartitionConstant.HOT_CHANNEL -> {
                PartitionTopicDynamicViewHolder(categoryId, compositeDisposable, mContext, helper, itemData)
                        .apiService = apiService
            }
            PartitionConstant.HOT_CHAIN_TITLE -> {
                PartitionHotTitleViewHolder(mContext, helper, itemData)
            }
            PartitionConstant.HOT_CHAIN -> {
                PartitionHotViewHolder(categoryId, compositeDisposable, mContext, helper, itemData)
                        .apiService = apiService
            }
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        compositeDisposable.clear()
    }

    fun loginAction() {
        mContext?.let {
            PartitionLoginActionHelper.loginActionToFollow(it, compositeDisposable, apiService)
        }
    }
}