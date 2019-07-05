package com.shuashuakan.android.modules.timeline.multitype

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.shuashuakan.android.R
import com.shuashuakan.android.data.api.model.home.multitypetimeline.FollowUserCard
import com.shuashuakan.android.data.api.model.home.multitypetimeline.FollowUserContent
import com.shuashuakan.android.modules.profile.UserProfileActivity
import com.shuashuakan.android.modules.profile.fragment.ProfileFragment
import com.shuashuakan.android.modules.timeline.multitype.util.StartSnapHelper
import com.shuashuakan.android.modules.widget.SpiderAction
import com.shuashuakan.android.utils.dip
import com.shuashuakan.android.utils.getScreenSize
import com.shuashuakan.android.utils.noDoubleClick
import com.shuashuakan.android.utils.setImageUrl2Webp
import timber.log.Timber

/**
 * TimeLine 兴趣 Type
 *
 * Author: ZhaiDongyang
 * Date: 2019/2/26
 */
class MultiTypeTimeLineFollowUserViewHolder(
        val mContext: Context,
        mHelper: BaseViewHolder,
        val dataList: FollowUserContent) {

    init {
        val dataInterest = dataList.data!!.list!!
        val recyclerView: RecyclerView = mHelper.getView(R.id.multitype_timeline_follow_user_list)
        val linearLayoutManager = LinearLayoutManager(mContext)
        linearLayoutManager.orientation = RecyclerView.HORIZONTAL
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.onFlingListener = null
        StartSnapHelper().attachToRecyclerView(recyclerView)

        val adapter = object : BaseQuickAdapter<FollowUserCard,
                BaseViewHolder>(R.layout.fragment_multitype_timeline_follow_user_time) {
            @SuppressLint("CheckResult")
            override fun convert(helper: BaseViewHolder, item: FollowUserCard) {
                val avatar = helper.getView<SimpleDraweeView>(R.id.avatar)
                val name = helper.getView<TextView>(R.id.tv_timeline_interest_name)
                item.has_unread?.let {
                    helper.setGone(R.id.follow_badge, it)
                }
                helper.itemView.layoutParams.width = ((mContext.getScreenSize().x - mContext.dip(36f)) / 4.4).toInt()
                (helper.itemView.layoutParams as RecyclerView.LayoutParams).leftMargin = if (helper.layoutPosition == 0) mContext.dip(8) else mContext.dip(4)
                avatar.setImageUrl2Webp(item.avatar ?: "", mContext.dip(60), mContext.dip(60))
                name.text = item.nick_name

                avatar.noDoubleClick {
                    mContext.startActivity(Intent(mContext, UserProfileActivity::class.java)
                            .putExtra("id", item.user_id.toString())
                            .putExtra(ProfileFragment.EXTRA_HAS_UNREAD,item.has_unread ?: false)
                            .putExtra("source", SpiderAction.PersonSource.FOLLOW_TIMELINE_MY_FOLLOW.source))
                    item.has_unread = false
                    helper.setGone(R.id.follow_badge, item.has_unread!!)
                }
            }


        }
        recyclerView.adapter = adapter
        adapter.addData(dataInterest)
    }
}