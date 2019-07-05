package com.shuashuakan.android.modules.account.adapter

import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.luck.picture.lib.tools.ScreenUtils
import com.shuashuakan.android.R
import com.shuashuakan.android.data.api.model.account.ChannelModel
import com.shuashuakan.android.utils.setImagePathWithRoundBorder

/**
 * @author hushiguang
 * @since 2019-05-10.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
class PerfectTopicSubAdapter(var channelListData: List<ChannelModel>,
                             var onSizeChangeListener: (Int) -> Unit) :
        RecyclerView.Adapter<PerfectTopicSubAdapter.ProfileTopicSubViewHolder>() {

    var selectIds = ArrayList<String>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileTopicSubViewHolder {
        return ProfileTopicSubViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_profile_topic_sub, parent, false))
    }

    override fun getItemCount(): Int {
        return channelListData.size
    }

    override fun onBindViewHolder(holder: ProfileTopicSubViewHolder, position: Int) {
        val channelModel = channelListData[position]
        val itemWidth = (holder.itemView.context.resources.displayMetrics.widthPixels -
                ScreenUtils.dip2px(holder.itemView.context, 135f)) / 3
        holder.itemCover.layoutParams.width = itemWidth
        holder.itemCover.layoutParams.height = itemWidth
        holder.itemDescription.layoutParams.width = (itemWidth.toFloat() * 0.75).toInt()
        holder.itemCover.setImagePathWithRoundBorder(holder.itemView.context, channelModel.cover_url,
                4, ContextCompat.getColor(holder.itemCover.context,R.color.black), 2)
        holder.itemDescription.text = channelModel.name
        holder.itemChooseImage.isSelected = selectIds.contains(channelModel.id.toString())
        holder.itemChooseImageBg.visibility = if (selectIds.contains(channelModel.id.toString())) View.VISIBLE else View.GONE
        holder.itemView.setOnClickListener {
            if (selectIds.contains(channelModel.id.toString())) {
                selectIds.remove(channelModel.id.toString())
            } else {
                selectIds.add(channelModel.id.toString())
            }
            onSizeChangeListener(selectIds.size)
            notifyDataSetChanged()
        }
    }

    class ProfileTopicSubViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemCover = itemView.findViewById<SimpleDraweeView>(R.id.item_profile_cover)!!
        var itemChooseImage = itemView.findViewById<AppCompatImageView>(R.id.item_profile_choose)!!
        var itemChooseImageBg = itemView.findViewById<View>(R.id.item_profile_cover_bg)!!
        var itemDescription = itemView.findViewById<TextView>(R.id.item_profile_description)!!
    }

}