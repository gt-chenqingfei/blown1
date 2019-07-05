package com.shuashuakan.android.modules.account.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.luck.picture.lib.tools.ScreenUtils
import com.shuashuakan.android.R
import kotlinx.android.synthetic.main.item_profile_age.view.*


/**
 * @author hushiguang
 * @since 2019-05-08.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
class PerfectAgeAdapter(var ageList: ArrayList<String>, var onItemClick: (String) -> Unit) : RecyclerView.Adapter<PerfectAgeAdapter.ProfileAgeViewHolder>() {

    var selectTabValue: String? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileAgeViewHolder {
        return ProfileAgeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_profile_age, parent, false))
    }

    override fun getItemCount(): Int {
        return ageList.size
    }

    override fun onBindViewHolder(holder: ProfileAgeViewHolder, position: Int) {
        val itemWidth = (holder.itemView.resources.displayMetrics.widthPixels - ScreenUtils.dip2px(holder.itemView.context, 160f)) / 3
        holder.ageValueText.layoutParams.width = itemWidth
        holder.ageValueText.layoutParams.height = (itemWidth.toFloat() * 0.4).toInt()
        holder.ageValueText.isSelected = selectTabValue == ageList[position]
        holder.ageValueText.text = ageList[position]
        holder.ageValueText.setOnClickListener {
            onItemClick(ageList[position])
        }
    }

    class ProfileAgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ageValueText = itemView.item_profile_age_value!!
    }
}