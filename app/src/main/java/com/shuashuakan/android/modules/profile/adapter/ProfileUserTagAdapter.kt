package com.shuashuakan.android.modules.profile.adapter

import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.shuashuakan.android.R
import com.shuashuakan.android.data.api.model.account.Tags
import com.shuashuakan.android.utils.dip
import com.shuashuakan.android.utils.noDoubleClick
import com.shuashuakan.android.utils.setImageForGlide

class ProfileUserTagAdapter constructor(
        val dataList: List<Tags>
) : BaseQuickAdapter<Tags, BaseViewHolder>(R.layout.fragment_profile_2f_tag_item, dataList) {

    private lateinit var userTagOnClickListener: UserTagOnClickListener

    fun setUserTagOnClickListener(userTagOnClickListener: UserTagOnClickListener) {
        this.userTagOnClickListener = userTagOnClickListener
    }

    interface UserTagOnClickListener {
        fun userTagOnClickListener(position: Int, url: String)
    }

    override fun convert(helper: BaseViewHolder, item: Tags) {
        val iconUrl = dataList[helper.adapterPosition].icon
        val imageView = helper.getView<ImageView>(R.id.profile_ll_2f_iv_id)
        val textView = helper.getView<TextView>(R.id.profile_ll_2f_tv_id)
        val redirectUrl = dataList[helper.adapterPosition].redirectUrl
        setImageForGlide(mContext, iconUrl, imageView, true)

        textView.text = dataList[helper.adapterPosition].title

        if (helper.adapterPosition > 0)
            helper.itemView.setPadding(mContext.dip(12), 0, 0, 0)

        helper.itemView.noDoubleClick {
            userTagOnClickListener.userTagOnClickListener(helper.adapterPosition, redirectUrl)
        }
    }

}