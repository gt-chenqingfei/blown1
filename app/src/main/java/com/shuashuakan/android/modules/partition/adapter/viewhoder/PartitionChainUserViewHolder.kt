package com.shuashuakan.android.modules.partition.adapter.viewhoder

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.shuashuakan.android.R
import com.shuashuakan.android.data.api.model.partition.PartitionChainUserItemModel
import com.shuashuakan.android.data.api.model.partition.PartitionUserModel
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.account.activity.LoginActivity
import com.shuashuakan.android.modules.discovery.ItemDataPair
import com.shuashuakan.android.modules.partition.helper.FollowHelper
import com.shuashuakan.android.modules.partition.helper.PartitionLoginActionHelper
import com.shuashuakan.android.modules.profile.UserProfileActivity
import com.shuashuakan.android.modules.widget.BetterRecyclerView
import com.shuashuakan.android.modules.widget.FollowTextView
import com.shuashuakan.android.modules.widget.SpiderAction
import com.shuashuakan.android.utils.*
import io.reactivex.disposables.CompositeDisposable

/**
 * @author hushiguang
 * @since 2019-06-17.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
class PartitionChainUserViewHolder(
        private val categoryId: Int,
        val compositeDisposable: CompositeDisposable,
        val mContext: Context,
        val helper: BaseViewHolder,
        val item: ItemDataPair) : PartitionBaseViewHolder() {

    val accountManager: AccountManager = mContext.applicationContext.daggerComponent().accountManager()

    init {
        formatChainUserContent()
    }

    private fun formatChainUserContent() {
        val userModel = item.data as PartitionUserModel
        val chainUserRec = helper.getView<BetterRecyclerView>(R.id.recommend_rec)
        val moreTextView = helper.getView<TextView>(R.id.rank_list)
        val title = helper.getView<TextView>(R.id.title)

        val list = userModel.dataList
        title.text = userModel.title
        moreTextView.text = userModel.redirect_text

        val linearLayoutManager = LinearLayoutManager(mContext)
        linearLayoutManager.orientation = RecyclerView.HORIZONTAL
        chainUserRec.layoutManager = linearLayoutManager
        val adapter = PartitionChainUserAdapter(mContext)
        chainUserRec.adapter = adapter
        adapter.addData(list)

        moreTextView.setOnClickListener {
            mContext.getSpider().channelUpUserStarListEntranceClickEvent(categoryId)
            mContext.startActivity(userModel.redirect_url)
        }
    }

    internal inner class PartitionChainUserAdapter(val context: Context)
        : BaseQuickAdapter<PartitionChainUserItemModel, BaseViewHolder>(R.layout.item_sub_explore_chain_user) {

        private val rankIconArray = intArrayOf(R.drawable.explore_ic_gold,
                R.drawable.explore_ic_silver, R.drawable.explore_ic_bronze)

        @SuppressLint("CheckResult")
        override fun convert(helper: BaseViewHolder, item: PartitionChainUserItemModel) {

            val avatar = helper.getView<SimpleDraweeView>(R.id.chain_user_avatar)
            val rankIcon = helper.getView<ImageView>(R.id.chain_user_rank_icon)
            val follow = helper.getView<FollowTextView>(R.id.chain_user_follow)

            avatar.setImageUrl2Webp(item.avatar ?: "", context.dip(54), context.dip(54))
            helper.setText(R.id.chain_user_name, item.nickName)
                    .setText(R.id.chain_user_up_num,
                            String.format(context.getString(com.shuashuakan.android.base.ui.R.string.string_up_value_format),
                                    numFormat(item.upCount)))


            helper.setVisible(R.id.chain_user_follow, !item.isFollow)

            if (helper.adapterPosition < 3) {
                rankIcon.visibility = View.VISIBLE
                rankIcon.setImageResource(rankIconArray[helper.adapterPosition])
            } else {
                rankIcon.visibility = View.GONE
            }

            val itemWidth: Double = (context.getScreenSize().x - context.dip(36f)) / 3.4
            helper.itemView.layoutParams.width = itemWidth.toInt()

            avatar.noDoubleClick {
                context.startActivity(Intent(context, UserProfileActivity::class.java)
                        .putExtra("id", item.userId.toString())
                        .putExtra("source", SpiderAction.PersonSource.Category.source))
            }

            follow.noDoubleClick {
                if (!accountManager.hasAccount()) {
                    LoginActivity.launch(context)
                    PartitionLoginActionHelper.setLoginActionWithFollowUser(item, follow)
                    return@noDoubleClick
                }

                FollowHelper.createFollow(compositeDisposable,
                        apiService, item.userId.toString()) {
                    if (it) {
                        FollowCacheManager.putFollowUserToCache(item.userId.toString(), true)
                        follow.followSuccessInInvisible()
                        item.isFollow = true
                        mContext.getSpider().userFollowEvent(mContext,
                                item.userId.toString(), SpiderAction.VideoPlaySource.CATEGORY_USER_LEADER_BOARD.source, true)
                    } else {
                        context.showLongToast(context.getString(R.string.string_follow_error))
                    }
                }
            }
        }
    }


}