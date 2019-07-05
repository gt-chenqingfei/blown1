package com.shuashuakan.android.modules.partition.adapter

import android.content.Context
import com.chad.library.adapter.base.BaseViewHolder
import com.shuashuakan.android.R
import com.shuashuakan.android.data.api.model.partition.PartitionLeaderBoardModel
import com.shuashuakan.android.modules.discovery.ItemDataPair

/**
 * @author hushiguang
 * @since 2019-06-17.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
class PartitionHotTitleViewHolder(val mContext: Context,
                                  val helper: BaseViewHolder,
                                  val item: ItemDataPair) {

    init {
        formatContent()
    }

    private fun formatContent() {
        val leaderBoardModel = item.data as PartitionLeaderBoardModel
        helper.setText(R.id.mLeaderBoardTitleView, leaderBoardModel.title)
    }

}
