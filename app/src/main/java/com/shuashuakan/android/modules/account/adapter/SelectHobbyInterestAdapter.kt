package com.shuashuakan.android.modules.account.adapter

import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.util.SparseBooleanArray
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.shuashuakan.android.R
import com.shuashuakan.android.data.api.model.home.Interests
import com.shuashuakan.android.utils.noDoubleClick

/**
 * 选择兴趣中的 Adapter
 *
 * Author: ZhaiDongyang
 * Date: 2019/2/11
 */
class SelectHobbyInterestAdapter(
    dataList: List<Interests>?)
  : BaseQuickAdapter<Interests, BaseViewHolder>((R.layout.prefect_select_hobby_interest_item), dataList) {

  private var selectedItems = SparseBooleanArray()
  private var guide: Int = 0

  private lateinit var itemClickListener: InterestAdapterItemClickListener

  interface InterestAdapterItemClickListener {
    fun onInterestAdapterItemClick(helper: BaseViewHolder)
  }

  fun setInterestAdapterItemClickListener(listener: InterestAdapterItemClickListener){
    this.itemClickListener = listener
  }

  override fun convert(helper: BaseViewHolder, item: Interests) {
    // 设置点击后的背景颜色
    val textView = helper.getView<TextView>(R.id.item_home_recommend_interest_item_textview)
    if (guide == 1) {
      textView.setBackgroundResource(if (isSelected(helper.adapterPosition)) R.drawable.bg_home_interest_textview_selected else R.drawable.bg_home_interest_textview_normal_white)
    } else {
      textView.setBackgroundResource(if (isSelected(helper.adapterPosition)) R.drawable.bg_home_interest_textview_selected else R.drawable.bg_home_interest_textview_normal)
    }
    textView.setTextColor(if (isSelected(helper.adapterPosition)) ContextCompat.getColor(mContext, R.color.ricebook_color_1) else Color.WHITE)
    textView.text = item.name

    helper.itemView.noDoubleClick {
      itemClickListener.onInterestAdapterItemClick(helper)
    }
  }

  fun getSelectedItems(): List<Int> {
    val items = ArrayList<Int>(selectedItems.size())
    for (i in 0 until selectedItems.size()) {
      if (selectedItems[selectedItems.keyAt(i)]) {
        items.add(selectedItems.keyAt(i))
      }
    }
    val list = listOf(1, 2, 3)
    return items
  }

  fun switchSelectedState(position: Int) {
    selectedItems.put(position, !selectedItems[position])
    notifyItemChanged(position)
  }

  private fun isSelected(position: Int): Boolean {
    return getSelectedItems().contains(position)
  }

  fun clearSelectedState() {
    selectedItems.clear()
    notifyDataSetChanged()
  }

  fun setGuidePage(guide: Int) {
    this.guide = guide
  }
}