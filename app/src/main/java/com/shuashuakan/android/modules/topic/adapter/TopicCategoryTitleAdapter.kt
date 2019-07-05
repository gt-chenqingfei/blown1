package com.shuashuakan.android.modules.topic.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.shuashuakan.android.DuckApplication
import com.shuashuakan.android.R
import com.shuashuakan.android.data.api.model.TopicCategory

/**
 *@author: zhaoningqiang
 *@time: 2019/5/23
 *@Description:话题分类左侧标题Adapter
 */
class TopicCategoryTitleAdapter(val context: Context) : BaseQuickAdapter<TopicCategory, BaseViewHolder>(R.layout.item_topic_category_title) {
    private var mSelectedItem: TopicCategory? = null

    private val itemSelectedBGColor = Color.parseColor("#111217")

    private val titleDefaultColor = Color.parseColor("#92969c")

    private val selectTitleTypeface: Typeface by lazy {
        Typeface.createFromAsset(context.assets, "fonts/NotoSans-CondensedBold.ttf")
    }

    fun setSelectedItem(item: TopicCategory) {
        if (mSelectedItem != item) {
            mSelectedItem = item
            notifyDataSetChanged()
        }
    }


    override fun convert(helper: BaseViewHolder, item: TopicCategory) {
        helper.itemView.tag = item
        val topicTitle = helper.getView<TextView>(R.id.topicTitle)
        val selectedMark = helper.getView<ImageView>(R.id.selectedMark)
        topicTitle.text = item.name
        if (mSelectedItem == item) {
            topicTitle.textSize = 16f
            topicTitle.typeface = selectTitleTypeface
            topicTitle.setTextColor(Color.WHITE)
            helper.itemView.setBackgroundColor(itemSelectedBGColor)
            selectedMark.visibility = View.VISIBLE
        } else {
            topicTitle.textSize = 14f
            topicTitle.typeface = Typeface.DEFAULT
            topicTitle.setTextColor(titleDefaultColor)
            helper.itemView.background = null
            selectedMark.visibility = View.GONE
        }
    }


}