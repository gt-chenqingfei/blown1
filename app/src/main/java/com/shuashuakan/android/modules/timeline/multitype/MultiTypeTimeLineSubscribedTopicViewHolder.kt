package com.shuashuakan.android.modules.timeline.multitype

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.shuashuakan.android.R
import com.shuashuakan.android.data.api.model.home.multitypetimeline.SubscribedChannel
import com.shuashuakan.android.data.api.model.home.multitypetimeline.SubscribedChannelContent
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.modules.timeline.multitype.util.StartSnapHelper
import com.shuashuakan.android.utils.*

/**
 * TimeLine 订阅话题 Type
 *
 * Author: ZhaiDongyang
 * Date: 2019/2/26
 */
class MultiTypeTimeLineSubscribedTopicViewHolder(
        private val mContext: Context,
        private val helper: BaseViewHolder,
        private val _data: SubscribedChannelContent) {

    init {
        val dataList = _data.data!!.list!!
        val recyclerView: RecyclerView = helper.getView(R.id.multitype_timeline_subscribed_topic_rl)
        val titleMoreLL: LinearLayout = helper.getView(R.id.ll_title_more)
        titleMoreLL.noDoubleClick {
            if (_data.redirect_url != null && _data.redirect_url!!.isNotEmpty()) {
                mContext.startActivity(_data.redirect_url)
            }
        }

        val linearLayoutManager = LinearLayoutManager(mContext)
        linearLayoutManager.orientation = RecyclerView.HORIZONTAL
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.onFlingListener = null
        StartSnapHelper().attachToRecyclerView(recyclerView)

        val adapter = object : BaseQuickAdapter<SubscribedChannel,
                BaseViewHolder>(R.layout.fragment_multitype_timeline_subscribed_topic_item) {
            @SuppressLint("CheckResult")
            override fun convert(helper: BaseViewHolder, item: SubscribedChannel) {
                val itemWidth: Int = mContext.dip(100f)
                val itemHeight: Int = itemWidth
                helper.itemView.layoutParams.height = itemHeight
                helper.itemView.layoutParams.width = itemWidth

                val content = helper.getView<TextView>(R.id.timeline_suscribe__name_tv)
                val backImage = helper.getView<SimpleDraweeView>(R.id.timeline_suscribe_iv)
                val num = helper.getView<TextView>(R.id.timeline_suscribe__num_tv)
                if (item.new_feed_num != null && item.new_feed_num != 0) {
                    num.text = String.format(mContext.getString(R.string.string_update_number_format), numFormat(item.new_feed_num))
                } else {
                    num.visibility = View.GONE
                }
                content.text = item.name
                if (item.cover_url != null) {
                    backImage.setImageUrl2Webp(item.cover_url ?: "", itemWidth, itemHeight) // 背景头像
                }
                helper.itemView.noDoubleClick {
                    if (item.redirect_url != null) {
                        mContext.getSpider().followTimeLineEvent(mContext,
                                SpiderEventNames.FOLLOW_TIMELINE_SUBSCRIBED_CHANNEL_CLICK, channelID = item.id!!.toString())
                        mContext.startActivity(item.redirect_url)
                    }
                }
            }
        }
        recyclerView.adapter = adapter
        adapter.addData(dataList)
    }
}