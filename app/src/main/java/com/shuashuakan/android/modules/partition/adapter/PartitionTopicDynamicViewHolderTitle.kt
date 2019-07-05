package com.shuashuakan.android.modules.partition.adapter

import android.content.Context
import android.view.View
import com.chad.library.adapter.base.BaseViewHolder
import com.shuashuakan.android.R
import com.shuashuakan.android.data.api.model.partition.PartitionRecommendModel
import com.shuashuakan.android.modules.discovery.ItemDataPair
import com.shuashuakan.android.modules.widget.SpiderAction
import com.shuashuakan.android.utils.allTopicsExposureEvent
import com.shuashuakan.android.utils.getSpider
import com.shuashuakan.android.utils.startActivity

/**
 * @author hushiguang
 * @since 2019-06-17.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
class PartitionTopicDynamicViewHolderTitle(val mContext: Context,
                                           val helper: BaseViewHolder,
                                           val item: ItemDataPair) {
    init {
        formatContent()
    }

    private fun formatContent() {
        val topicModel = item.data as PartitionRecommendModel
        helper.setText(R.id.mTopicDynamicTitleView, topicModel.title)
        helper.setText(R.id.mTopicDynamicMoreView, topicModel.redirect_text)
        helper.getView<View>(R.id.mTopicDynamicMoreView).setOnClickListener {
            mContext.getSpider().allTopicsExposureEvent(SpiderAction.TopicCategorySource.Category.source)
            mContext.startActivity(topicModel.redirect_url)
        }
    }

}
