package com.shuashuakan.android.modules.profile

import android.content.Context
import android.view.View
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.shuashuakan.android.R
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.modules.widget.OnItemClickListener
import com.shuashuakan.android.modules.profile.ProfileLikedViewModel.ProfileLikedViewHolder
import com.shuashuakan.android.ui.base.BaseEpoxyHolder
import com.shuashuakan.android.ui.base.bindView
import com.shuashuakan.android.utils.*

@EpoxyModelClass(layout = R.layout.liked_list_video)
abstract class ProfileLikedViewModel : EpoxyModelWithHolder<ProfileLikedViewHolder>() {

  @EpoxyAttribute
  lateinit var feed: Feed
  @EpoxyAttribute
  lateinit var context: Context
  @EpoxyAttribute
  var position: Int = 0
  @EpoxyAttribute
  lateinit var onItemClickListener: OnItemClickListener

  override fun bind(holder: ProfileLikedViewHolder) {
    with(holder) {
      itemView.layoutParams.height = itemHeight
      imageView.aspectRatio = 3 / 4f
      if (!feed.animationCover.isNullOrEmpty() && isWifiConnected(context)) {
        imageView.setGifImage(feed.animationCover)
      } else {
        imageView.setImageURI(feed.cover)
      }
      likedNumberView.text = numFormat(feed.favNum)
      itemView.setOnClickListener {
        onItemClickListener.onItemClick(position)
      }
    }
  }

  inner class ProfileLikedViewHolder : BaseEpoxyHolder() {
    val imageView by bindView<SimpleDraweeView>(R.id.image_view)
    val likedNumberView by bindView<TextView>(R.id.liked_number_view)
    var itemHeight: Int = 0

    override fun onBindView(view: View) {
      val width = context.getScreenSize().x / 3f - context.dip(10f)
      val height = width / 0.75
      itemHeight = height.toInt()
    }
  }
}