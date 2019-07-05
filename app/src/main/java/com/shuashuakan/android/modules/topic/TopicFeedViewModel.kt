package com.shuashuakan.android.modules.topic

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
import com.shuashuakan.android.ui.base.BaseEpoxyHolder
import com.shuashuakan.android.ui.base.bindView
import com.shuashuakan.android.modules.topic.TopicFeedViewModel.ChannelViewHolder
import com.shuashuakan.android.utils.getScreenSize
import com.shuashuakan.android.utils.isWifiConnected
import com.shuashuakan.android.utils.numFormat
import com.shuashuakan.android.utils.setGifImage

@EpoxyModelClass(layout = R.layout.item_channel_list)
abstract class TopicFeedViewModel : EpoxyModelWithHolder<ChannelViewHolder>() {

  @EpoxyAttribute
  lateinit var feed: Feed
  @EpoxyAttribute
  lateinit var context: Context
  @EpoxyAttribute
  var channelId: Long = 0
  @EpoxyAttribute
  var position: Int = 0
  @EpoxyAttribute
  lateinit var onItemClickListener: OnItemClickListener

  override fun bind(holder: ChannelViewHolder) {
    with(holder) {
      itemView.layoutParams.height = itemHeight
      imageView.aspectRatio = 3 / 4f
      if (!feed.animationCover.isNullOrEmpty() && isWifiConnected(context)) {
        imageView.setGifImage(feed.animationCover)
      } else {
        imageView.setImageURI(feed.cover)
      }
      playCountView.text = if (feed.playCount != null) numFormat(feed.playCount!!) else ""
      itemView.setOnClickListener {
        onItemClickListener.onItemClick(position)
      }
      if(feed.solitaireNum==0){
        holder.numChains.text=""
      }else{
        holder.numChains.text= String.format(itemView.context
                .getString(com.shuashuakan.android.base.ui.R.string.string_video_solitaire_format),numFormat(feed.solitaireNum))
      }
    }
  }

  inner class ChannelViewHolder : BaseEpoxyHolder() {
    val imageView by bindView<SimpleDraweeView>(R.id.image_view)
    val playCountView by bindView<TextView>(R.id.liked_number_view)
    val numChains by bindView<TextView>(R.id.num_chains)
    var itemHeight: Int = 0

    override fun onBindView(view: View) {
      itemHeight = ((context.getScreenSize().x / 3f) / 0.75f).toInt()
    }
  }
}