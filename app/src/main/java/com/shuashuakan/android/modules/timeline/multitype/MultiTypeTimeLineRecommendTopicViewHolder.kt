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
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.home.multitypetimeline.RecommendChannel
import com.shuashuakan.android.data.api.model.home.multitypetimeline.RecommendChannelContent
import com.shuashuakan.android.event.SubscribeEvent
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.modules.timeline.multitype.util.StartSnapHelper
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.modules.widget.SpiderAction
import com.shuashuakan.android.utils.*
import com.shuashuakan.android.utils.extension.showCancelSubscribeDialog


/**
 * TimeLine 推荐话题 Type
 *
 * Author: ZhaiDongyang
 * Date: 2019/2/26
 */
class MultiTypeTimeLineRecommendTopicViewHolder(
    private val mContext: Context,
    private val helper: BaseViewHolder,
    private val _data: RecommendChannelContent) {

  init {
    val dataList = _data.data!!.list!!
    val recyclerView: RecyclerView = helper.getView(R.id.multitype_timeline_recommend_topic_rl)
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

    val adapter = object : BaseQuickAdapter<RecommendChannel,
        BaseViewHolder>(R.layout.fragment_multitype_timeline_recommend_topic_item) {
      @SuppressLint("CheckResult")
      override fun convert(helper: BaseViewHolder, item: RecommendChannel) {
        (helper.itemView.layoutParams as RecyclerView.LayoutParams).leftMargin = if (helper.layoutPosition == 0) mContext.dip(15) else 0
        val name = helper.getView<TextView>(R.id.topic_channel_name_tv)
        val subscribeTextView = helper.getView<TextView>(R.id.topic_channel_subscribe_tv)
        val bgCoverImageView = helper.getView<SimpleDraweeView>(R.id.topic_channel_iv_bg_cover)
        val position = helper.adapterPosition
        when (position % 3) {
          0 -> bgCoverImageView.setActualImageResource(R.drawable.bg_timeline_recommend1)
          1 -> bgCoverImageView.setActualImageResource(R.drawable.bg_timeline_recommend2)
          2 -> bgCoverImageView.setActualImageResource(R.drawable.bg_timeline_recommend3)
        }
        val iv1  = helper.getView<SimpleDraweeView>(R.id.topic_channel_sd_iv1)
        val iv2  = helper.getView<SimpleDraweeView>(R.id.topic_channel_sd_iv2)
        val iv3  = helper.getView<SimpleDraweeView>(R.id.topic_channel_sd_iv3)
        if (item.properties != null && item.properties!!.feed_data != null
            && item.properties!!.feed_data!!.isNotEmpty()) {
          item.properties!!.feed_data!!.forEachIndexed { index, feed ->
            when (index) {
              0 -> iv1.setImageUrl2Webp(feed.cover, mContext.dip(90), mContext.dip(90))
              1 -> iv2.setImageUrl2Webp(feed.cover, mContext.dip(90), mContext.dip(90))
              2 -> iv3.setImageUrl2Webp(feed.cover, mContext.dip(90), mContext.dip(90))
            }
          }
        }
        name.text = item.name
        helper.itemView.noDoubleClick {
          mContext.getSpider().followTimeLineEvent(mContext, SpiderEventNames.FOLLOW_TIMELINE_RECOMMENDED_CHANNEL_CLICK,
              channelID = item.id!!.toString())
          mContext.startActivity(item.redirect_url)
        }
        if (item.has_subscribe!!) {
          subscribed(subscribeTextView)
        } else {
          noSubscribed(subscribeTextView)
        }
        subscribeTextView.noDoubleClick {
          if (item.has_subscribe!!) {
            val cancelSubscribe = { mContext.apiService().cancelSubscribe(item.id!!).applySchedulers().subscribeApi(onNext = {
                  if (it.result.isSuccess) {
                    noSubscribed(subscribeTextView)
                    item.has_subscribe = false
                    RxBus.get().post(SubscribeEvent())
                    toastCustomText(mContext, mContext.getString(R.string.string_un_subscription_success))
                  } else {
                    mContext.showLongToast(mContext.getString(R.string.string_un_subscription_error))
                  }
                }, onApiError = {
                  if (it is ApiError.HttpError) {
                    mContext.showLongToast(it.displayMsg)
                  } else {
                    mContext.showLongToast(mContext.getString(R.string.string_un_subscription_error))
                  }
                })
            }
              mContext.showCancelSubscribeDialog(item.name,cancelSubscribe)
          } else {
            mContext.apiService().subscribeMethod(item.id!!).applySchedulers().subscribeApi(onNext = {
              if (it.result.isSuccess) {
                subscribed(subscribeTextView)
                item.has_subscribe = true
                mContext.getSpider().subscribeChinnalClickEvent(mContext, item.id!!.toString(),
                    SpiderAction.VideoPlaySource.FOLLOW_TIMELINE.source)
                RxBus.get().post(SubscribeEvent())
                toastCustomText(mContext, mContext.getString(R.string.string_subscription_success))
              } else {
                mContext.showLongToast(mContext.getString(R.string.string_subscription_error))
              }
            }, onApiError = {
              if (it is ApiError.HttpError) {
                mContext.showLongToast(it.displayMsg)
              } else {
                mContext.showLongToast(mContext.getString(R.string.string_subscription_error))
              }
            })
          }
        }
      }

      private fun noSubscribed(textView: TextView) {
        textView.background = mContext.resources.getDrawable(R.drawable.bg_multitype_timeline_un_follow)
        textView.text = mContext.getString(R.string.string_subscription)
        textView.visibility= View.VISIBLE
      }

      private fun subscribed(textView: TextView) {
        textView.background = mContext.resources.getDrawable(R.drawable.bg_multitype_timeline_follow)
        textView.text = mContext.getString(R.string.string_has_subscription)
        textView.visibility= View.GONE
      }
    }
    recyclerView.adapter = adapter
    adapter.addData(dataList)
  }
}