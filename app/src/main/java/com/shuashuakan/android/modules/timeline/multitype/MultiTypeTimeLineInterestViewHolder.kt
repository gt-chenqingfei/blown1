package com.shuashuakan.android.modules.timeline.multitype

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.facebook.drawee.view.SimpleDraweeView
import com.shuashuakan.android.DuckApplication
import com.shuashuakan.android.R
import com.shuashuakan.android.data.api.model.home.multitypetimeline.RecommendUser
import com.shuashuakan.android.data.api.model.home.multitypetimeline.RecommendUserContent
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.account.activity.LoginActivity
import com.shuashuakan.android.modules.profile.UserProfileActivity
import com.shuashuakan.android.modules.timeline.multitype.util.StartSnapHelper
import com.shuashuakan.android.modules.widget.SpiderAction
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.utils.*
import com.shuashuakan.android.utils.extension.showCancelFollowDialog

/**
 * TimeLine 兴趣 Type
 *
 * Author: ZhaiDongyang
 * Date: 2019/2/26
 */
class MultiTypeTimeLineInterestViewHolder(
        val mContext: Context,
        val mHelper: BaseViewHolder,
        val dataList: RecommendUserContent,
        val mRecyclerView: RecyclerView,
        val dataTotal: MutableList<MultiItemEntity>,
        val clickUser: (RecommendUser) -> Unit) {

    init {
        val dataInterest = dataList.data!!.list!!
        val layout: ConstraintLayout = mHelper.getView(R.id.multitype_timeline_interest_layout)
        val mInterestTitle: TextView = mHelper.getView(R.id.multitype_timeline_interest_title_tv)
        val recyclerView: RecyclerView = mHelper.getView(R.id.multitype_timeline_interest_rl)
        mInterestTitle.text = dataList.title

        val linearLayoutManager = LinearLayoutManager(mContext)
        linearLayoutManager.orientation = RecyclerView.HORIZONTAL
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.onFlingListener = null
        StartSnapHelper().attachToRecyclerView(recyclerView)
        var accountManager: AccountManager = mContext.applicationContext.daggerComponent().accountManager()

        val adapter = object : BaseQuickAdapter<RecommendUser,
                BaseViewHolder>(R.layout.fragment_multitype_timeline_interest_item) {
            @SuppressLint("CheckResult")
            override fun convert(helper: BaseViewHolder, item: RecommendUser) {
                val avatar = helper.getView<SimpleDraweeView>(R.id.avatar)
                val name = helper.getView<TextView>(R.id.tv_timeline_interest_name)
                val subscribeLL = helper.getView<LinearLayout>(R.id.topic_channel_subscribe_ll)
                val subscribe = helper.getView<TextView>(R.id.topic_channel_subscribe_tv)
                val describe = helper.getView<TextView>(R.id.tv_timeline_interest_name_describe)
                val closeImageView = helper.getView<ImageView>(R.id.iv_timeline_interest_close)
                (helper.itemView.layoutParams as RecyclerView.LayoutParams).leftMargin = if (helper.layoutPosition == 0) mContext.dip(15) else 0
                avatar.setImageUrl2Webp(item.avatar ?: "", mContext.dip(60), mContext.dip(60))
                name.text = item.nick_name
                if (item.bio != null && item.bio!!.isNotEmpty()) {
                    describe.text = item.bio
                } else {
                    describe.text = item.properties!!.recommend_reason
                }

                if (item.is_fans == true) {
                    subscribe.setText(R.string.string_follow_fans)
                } else {
                    subscribe.setText(R.string.string_follow)
                }

                closeImageView.noDoubleClick {
                    mContext.getSpider().followTimeLineEvent(mContext,
                            SpiderEventNames.FOLLOW_TIMELINE_RECOMMENDED_USER_CLOSE_CLICK, targetUserID = item.user_id.toString())
                    val position = helper.adapterPosition
                    data.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, data.size)
                    if (data.size == 0) {
                        removeLayoutAndNotify()
                    }
                }


                if (item.is_follow!!) {
                    followed(subscribeLL, subscribe)
                } else {
                    noFollowed(subscribeLL, subscribe)
                }
                subscribeLL.tag = item
                subscribeLL.noDoubleClick {
                    if (!accountManager.hasAccount()) {
                        clickUser(item)
                        LoginActivity.launch(mContext)
                        return@noDoubleClick
                    }

                    if (item.is_follow!!) {
                        val cancelFollow = {
                            mContext.apiService().cancelFollow(item.user_id!!.toString()).applySchedulers().subscribeApi(onNext = {
                                if (it.result.isSuccess) {
                                    FollowCacheManager.putFollowUserToCache(item.user_id!!.toString(), false)
                                    noFollowed(subscribeLL, subscribe)
                                    item.is_follow = false
                                    mContext.getSpider().userFollowEvent(mContext, item.user_id!!.toString(),
                                            SpiderAction.VideoPlaySource.FOLLOW_TIMELINE.source, false)
                                } else {
                                    mContext.showLongToast(mContext.getString(R.string.string_un_follow_error))
                                }
                            }, onApiError = {
                                if (it is ApiError.HttpError) {
                                    mContext.showLongToast(it.displayMsg)
                                } else {
                                    mContext.showLongToast(mContext.getString(R.string.string_un_follow_error))
                                }
                            })
                        }
                        mContext.showCancelFollowDialog(item.nick_name, cancelFollow)
                    } else {
                        mContext.apiService().createFollow(item.user_id!!.toString()).applySchedulers().subscribeApi(onNext = {
                            if (it.result.isSuccess) {
                                FollowCacheManager.putFollowUserToCache(item.user_id!!.toString(), true)
                                followed(subscribeLL, subscribe)
                                item.is_follow = true
                                mContext.getSpider().userFollowEvent(mContext, item.user_id!!.toString(),
                                        SpiderAction.VideoPlaySource.FOLLOW_TIMELINE.source, true)
                                val position = helper.adapterPosition
                                data.removeAt(position)
                                notifyItemRemoved(position)
                                notifyItemRangeChanged(position, data.size)
                                if (data.size == 0) {
                                    removeLayoutAndNotify()
                                }
                            } else {
                                mContext.showLongToast(mContext.getString(R.string.string_attention_error))
                            }
                        }, onApiError = {
                            if (it is ApiError.HttpError) {
                                mContext.showLongToast(it.displayMsg)
                            } else {
                                mContext.showLongToast(mContext.getString(R.string.string_attention_error))
                            }
                        })
                    }
                }
                avatar.noDoubleClick {
                    skipProfilePage(item)
                }
                name.noDoubleClick {
                    skipProfilePage(item)
                }
            }

            private fun skipProfilePage(data: RecommendUser) {
                mContext.getSpider().followTimeLineEvent(mContext,
                        SpiderEventNames.FOLLOW_TIME_LINE_RECOMMENDED_USER_CLICK, targetUserID = data.user_id.toString())
                mContext.startActivity(Intent(mContext, UserProfileActivity::class.java)
                        .putExtra("id", data.user_id.toString())
                        .putExtra("source", SpiderAction.PersonSource.FOLLOW_TIMELINE.source))
            }

            private fun noFollowed(ll: LinearLayout, textView: TextView) {
                ll.background = mContext.resources.getDrawable(R.drawable.bg_not_follow)
                textView.text = mContext.getString(R.string.string_follow)
                textView.setTextColor(textView.resources.getColor(R.color.color_normal_1a1917))
            }

            private fun followed(ll: LinearLayout, textView: TextView) {
                ll.background = mContext.resources.getDrawable(R.drawable.bg_multitype_timeline_follow)
                textView.text = mContext.getString(R.string.string_has_follow)
                textView.setTextColor(textView.resources.getColor(R.color.white))
            }
        }
        recyclerView.adapter = adapter
        adapter.addData(dataInterest)
    }

    private fun removeLayoutAndNotify() {
        val totalPosition = mHelper.adapterPosition
        dataTotal.removeAt(totalPosition)
        mRecyclerView.adapter.notifyItemRemoved(totalPosition)
        mRecyclerView.adapter.notifyItemRangeChanged(totalPosition, dataTotal.size)
    }
}