package com.shuashuakan.android.modules.player

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.shuashuakan.android.R
import com.shuashuakan.android.modules.widget.MarqueeFactory

class RepeatLogoMarqueeFactory(context: Context) : com.shuashuakan.android.modules.widget.MarqueeFactory<View, String>(context) {

  private val inflater: LayoutInflater = LayoutInflater.from(context)

  override fun generateMarqueeItemView(data: String): View {
    val view = inflater.inflate(R.layout.view_repeat_logo_marquee, null) as TextView
    view.text = data
    return view
  }
}