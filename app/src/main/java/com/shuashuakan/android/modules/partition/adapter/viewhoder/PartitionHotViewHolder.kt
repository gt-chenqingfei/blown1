package com.shuashuakan.android.modules.partition.adapter.viewhoder

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import com.chad.library.adapter.base.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.shuashuakan.android.R
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.home.Author
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.event.RefreshProfileEvent
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.account.activity.LoginActivity
import com.shuashuakan.android.modules.discovery.ItemDataPair
import com.shuashuakan.android.modules.partition.helper.FollowHelper
import com.shuashuakan.android.modules.partition.helper.PartitionLoginActionHelper
import com.shuashuakan.android.modules.topic.TopicDetailActivity
import com.shuashuakan.android.modules.widget.FollowTextView
import com.shuashuakan.android.modules.widget.SpiderAction
import com.shuashuakan.android.utils.*
import com.umeng.socialize.utils.DeviceConfig.context
import io.reactivex.disposables.CompositeDisposable

/**
 * @author hushiguang
 * @since 2019-06-20.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
class PartitionHotViewHolder(
        private val categoryId: Int,
        val compositeDisposable: CompositeDisposable,
        val mContext: Context,
        val helper: BaseViewHolder?,
        val item: ItemDataPair?) : PartitionBaseViewHolder() {

    val accountManager: AccountManager = mContext.applicationContext.daggerComponent().accountManager()

    init {
        formatContent()
    }

    private fun formatContent() {
        helper ?: return
        item ?: return
        convert(helper, item.data as Feed)
    }


    fun convert(helper: BaseViewHolder, item: Feed) {

        val itemCoverImageView = helper.getView<SimpleDraweeView>(R.id.mItemPartitionHotCoverView)
        val itemUserIdentificationView = helper.getView<SimpleDraweeView>(R.id.mItemPartitionHotUserIdentificationView)
        val itemFeedHeadView = helper.getView<SimpleDraweeView>(R.id.mItemPartitionHotHeadView)
        val itemCoverShadowView = helper.getView<View>(R.id.mItemPartitionHotCoverShadowView)
        val itemFeedContentView = helper.getView<TextView>(R.id.mItemPartitionHotContentView)
        val itemFeedFollowView = helper.getView<FollowTextView>(R.id.mItemPartitionHotFollowView)
        val itemTitleView = helper.getView<TextView>(R.id.mItemPartitionHotContentView)

        // 接龙数不显示的时候隐藏数字和图标
        helper.setGone(R.id.mItemPartitionHotVideoImageView, item.solitaireNum != 0)
        helper.setGone(R.id.mItemPartitionHotSizeView, item.solitaireNum != 0)
        helper.setGone(R.id.mItemPartitionHotFollowView, !item.author?.isFollow!!)
        helper.setGone(R.id.mItemPartitionHotFollowedLayout, item.author?.isFollow!!)

        helper.setText(R.id.mItemPartitionHotUserNameView, item.author?.nick_name)

        // 设置item的点击事件
        helper.addOnClickListener(R.id.mItemPartitionHotItemView)
        put(item.id, categoryId, helper.itemView)

        // 动态设置View 的宽高
        val width = (mContext.resources.displayMetrics.widthPixels - mContext.dip(32))
        val height = width / 330.0f * 186f
        itemCoverImageView.layoutParams.width = width
        itemCoverImageView.layoutParams.height = height.toInt()
        itemCoverShadowView.layoutParams.width = width
        itemCoverShadowView.layoutParams.height = height.toInt()
        itemFeedContentView.layoutParams.width = width
        formatContentView(item, itemTitleView, helper.adapterPosition)

        item.author?.tags?.let {
            itemUserIdentificationView.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
            if (!it.isEmpty()) {
                itemUserIdentificationView.setImageURI(it[0].icon)
            }
        }

        itemFeedFollowView.text = if (item.author?.is_fans == true) {
            mContext.getString(R.string.string_follow_fans)
        } else {
            mContext.getString(R.string.string_follow)
        }

        helper.setText(R.id.mItemPartitionHotRulesView, userShownLabel(item.author))
        item.solitaireNum?.let {
            helper.setText(R.id.mItemPartitionHotSizeView, it.toString())
        }


        itemCoverImageView.setImageUrl2Webp(item.cover!!, mContext.dip(140), mContext.dip(84))
        itemFeedHeadView.setImageUrl2Webp(item.avatar!!, mContext.dip(21), mContext.dip(21))

        itemFeedFollowView.setOnClickListener {
            if (!accountManager.hasAccount()) {
                PartitionLoginActionHelper.setLoginActionWithFollowFeed(item, itemFeedFollowView)
                LoginActivity.launch(mContext)
                return@setOnClickListener
            }

            FollowHelper.createFollow(compositeDisposable,
                    apiService, item.userId.toString()) {
                if (it) {
                    FollowCacheManager.putFollowUserToCache(item.userId.toString(), true)
                    itemFeedFollowView.followSuccessGone()
                    item.hasFollowUser = true
                    RxBus.get().post(RefreshProfileEvent())
                    mContext.getSpider().userFollowEvent(mContext,
                            item.userId.toString(), SpiderAction.VideoPlaySource.CATEGORY_FEED_LEADER_BOARD.source, true)
                } else {
                    context.showLongToast(context.getString(R.string.string_follow_error))
                }
            }
        }
    }

    private fun formatContentView(item: Feed, itemTitleView: TextView, position: Int) {
        val channelTitle = item.channelName ?: ""
        val realContent = StringUtils.replaceBlank(item.title)
        val contentText = SpannableString(StringUtils.replaceBlank(item.title) + " #" + channelTitle)
        contentText.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                item.channelUrl?.let {
                    //打点之频道页面跳转
                    val channel = it.split("id=")
                    val channelId = channel[channel.size - 1]
                    TopicDetailActivity.launch(mContext, channelId, TopicDetailActivity.SOURCE_CATEGORY)
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = mContext.resources.getColor(R.color.color_normal_b6b6b6)
            }
        }, realContent.length, contentText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        itemTitleView.tag = position
        itemTitleView.text = contentText
        itemTitleView.movementMethod = LinkMovementMethod.getInstance()
    }


    private fun userShownLabel(author: Author?): String? {
        if (author?.fans_count ?: 0 >= 5000) {
            return "粉丝 " + (numFormat(author?.fans_count ?: 0))
        }
        return "UP值 " + (numFormat(author?.upCount ?: 0))
    }


    companion object {
        var array: ArrayList<String> = ArrayList()
        fun put(id: String, categoryId: Int, view: View) {
            if (array.contains(id)) {
                return
            }
            if (!array.isEmpty()) {
                array.clear()
            }
            array.add(id)
            view.context.getSpider().categoryFeedLeaderBoardExposureEvent(categoryId.toString(), id)
        }
    }

}