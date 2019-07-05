package com.shuashuakan.android.modules.comment

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter
import com.shuashuakan.android.R

/**
 * Author:  treasure_ct
 * Date:    2018/11/28
 */
class CommentListHeader(context: Context) : RecyclerArrayAdapter.ItemView {
  private var view: View = View.inflate(context, R.layout.item_comment_header, null)
  val title: TextView
  val commentIv: ImageView
  val commentTv: TextView
  val likeIv: ImageView
  val likeTv: TextView

  init {
    title = view.findViewById(R.id.tv_comment_title)
    likeIv = view.findViewById(R.id.iv_like_count)
    likeTv = view.findViewById(R.id.tv_like_count)
    commentIv = view.findViewById(R.id.iv_comment_count)
    commentTv = view.findViewById(R.id.tv_comment_count)
  }

  override fun onCreateView(parent: ViewGroup): View {
    return view
  }

  override fun onBindView(headerView: View) {
    val viewParent = view.parent
    if (viewParent is ViewGroup) {
      viewParent.removeView(view)
    }
  }
}
