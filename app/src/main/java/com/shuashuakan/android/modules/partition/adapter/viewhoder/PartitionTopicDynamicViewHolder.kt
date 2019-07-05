package com.shuashuakan.android.modules.partition.adapter.viewhoder

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
import com.shuashuakan.android.data.api.model.partition.PartitionRecommendItemModel
import com.shuashuakan.android.modules.FeedTransportManager
import com.shuashuakan.android.modules.discovery.ItemDataPair
import com.shuashuakan.android.modules.player.activity.VideoPlayActivity
import com.shuashuakan.android.utils.*
import io.reactivex.disposables.CompositeDisposable

/**
 * @author hushiguang
 * @since 2019-06-19.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
class PartitionTopicDynamicViewHolder(
        val categoryId: Int,
        val compositeDisposable: CompositeDisposable,
        val mContext: Context,
        val helper: BaseViewHolder?,
        val item: ItemDataPair?) : PartitionBaseViewHolder() {

    init {
        formatContent()
    }

    private fun formatContent() {
        helper ?: return
        item ?: return
        val topicModel = item.data as PartitionRecommendItemModel
        val nameView = helper.getView<TextView>(R.id.mTopicTopDynamicNameView)
        nameView.text = topicModel.name
        topicModel.allCount?.let {
            helper.setGone(R.id.mTopicDynamicLineView, topicModel.index != it - 1)
            helper.setGone(R.id.holderView, topicModel.index == it - 1)
        }
        val mTopicDynamicHorizontalRecyclerView = helper.getView<RecyclerView>(R.id.mTopicDynamicHorizontalRecyclerView)
        mTopicDynamicHorizontalRecyclerView.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        val mTopicHorizontalAdapter = TopicHorizontalAdapter()
        mTopicHorizontalAdapter.bindToRecyclerView(mTopicDynamicHorizontalRecyclerView)
        mTopicHorizontalAdapter.setNewData(topicModel.feed_list)

        nameView.setOnClickListener {
            topicModel.id?.let {
                mContext.getSpider().categoryTopicsTagDidSelectedEvent(it.toString())
                mContext.startActivity(topicModel.redirect_url)
            }
        }
    }

    //横向的数据列表
    internal inner class TopicHorizontalAdapter :
            BaseQuickAdapter<Feed,
                    BaseViewHolder>(R.layout.item_partition_topic_horizontal) {

        override fun convert(helper: BaseViewHolder, item: Feed) {
            val itemCoverImageView = helper.getView<SimpleDraweeView>(R.id.mItemPartitionTopicCoverView)
            val itemCoverShadowView = helper.getView<View>(R.id.mItemPartitionTopicCoverShadowView)
            val itemFeedContentView = helper.getView<TextView>(R.id.mItemPartitionTopicContentView)

            // 设置左边距的位置
            (helper.itemView.layoutParams as RecyclerView.LayoutParams).leftMargin =
                    mContext.dip(if (helper.adapterPosition == 0) {
                        15
                    } else 0)

            // 接龙数不显示的时候隐藏数字和图标
            helper.setGone(R.id.mItemPartitionTopicVideoImageView, item.solitaireNum != 0)
            helper.setGone(R.id.mItemPartitionTopicSizeView, item.solitaireNum != 0)

            // 动态设置View 的宽高
            val width = (mContext.resources.displayMetrics.widthPixels - mContext.dip(50)) / 2
            val height = width / 155.0f * 97f
            itemCoverImageView.layoutParams.width = width
            itemCoverImageView.layoutParams.height = height.toInt()
            itemCoverShadowView.layoutParams.width = width
            itemCoverShadowView.layoutParams.height = height.toInt()
            itemFeedContentView.layoutParams.width = width
            itemFeedContentView.text = StringUtils.replaceBlank(item.title)

            item.solitaireNum?.let {
                helper.setText(R.id.mItemPartitionTopicSizeView, it.toString())
            }
            itemCoverImageView.setImageUrl2Webp(item.cover!!, mContext.dip(140), mContext.dip(84))

            helper.itemView.setOnClickListener {
                mContext.getSpider().categoryTopicsFeedDidSelectedEvent(categoryId.toString(), item.id)
                val intentParam = FeedTransportManager.createCategoryRecommendTopicParam(data as List<Feed>,
                        item, item.channelId?.toLong())
                mContext.startActivity(VideoPlayActivity.create(mContext, intentParam))
            }
        }
    }


}