package com.shuashuakan.android.modules.activity

import android.app.Activity
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import com.shuashuakan.android.R
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.Card
import com.shuashuakan.android.event.PopWindowEvent
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.utils.extension.load
import com.shuashuakan.android.utils.extension.screenHeight
import com.shuashuakan.android.utils.extension.screenWidth
import com.shuashuakan.android.utils.getSpider
import com.shuashuakan.android.utils.getUserId
import com.shuashuakan.android.utils.parseColor
import com.shuashuakan.android.utils.startActivity


/**
 * 活动卡片弹窗
 */
class ActivityCardPopupWindow : PopupWindow, PopupWindow.OnDismissListener {
    private val pic = "PICTURE_CARD"//单独一个图片
    private val media = "MESSAGE_CARD" // 图文
    private var mActivity: Activity? = null

    constructor(activity: Activity, card: Card) : super(activity) {
        mActivity = activity
        val imageWidth = (activity.screenWidth() * 0.5f).toInt()
        val imageHeight = (activity.screenHeight() * 0.5f).toInt()

        when (card.card_style) {
            pic -> {
                val content = LayoutInflater.from(activity).inflate(R.layout.popup_window_activity_pic_card, null, false)

                content.findViewById<ImageView>(R.id.picture_card)
                val picture_card = content.findViewById<ImageView>(R.id.picture_card)


                picture_card.load(card.image_url, imageWidth, imageHeight, useDP = false)

                this.contentView = content
            }
            media -> {
                val content = LayoutInflater.from(activity).inflate(R.layout.popup_window_activity_card, null, false)

                val picture_card = content.findViewById<ImageView>(R.id.picture_card)
                val message_title = content.findViewById<TextView>(R.id.message_title)
                val message_desc = content.findViewById<TextView>(R.id.message_desc)

                picture_card.load(card.image_url, imageWidth, imageHeight, useDP = false)

                message_title.text = card.title

                message_desc.text = card.description

                this.contentView = content
            }
        }

        if (contentView == null) {
            dismiss()
            return
        }

        width = (activity.screenWidth() * 0.78f).toInt()
        height = (activity.screenHeight() * 0.66f).toInt()
        setBackgroundDrawable(null)
        isFocusable = true


        val ok_btn = contentView.findViewById<TextView>(R.id.ok_btn)
        val ic_close = contentView.findViewById<ImageView>(R.id.ic_close)

        val button_background_color = parseColor(card.button_background_color.trim())
        if (button_background_color != 0) {
            var background = ok_btn.background
            if (background is GradientDrawable) {
                background.setColor(button_background_color)
            } else if (background is ShapeDrawable) {
                background.paint.color = button_background_color
            }
        }

        ok_btn.setTextColor(parseColor(card.button_text_color.trim()))
        ok_btn.text = card.button_text

        ok_btn.setOnClickListener {
            //活动卡片点击
            activity.getSpider().manuallyEvent(SpiderEventNames.GAME_CARD_CLICK)
                    .put("userID", activity.getUserId())
                    .put("ssr", card.redirect_url)
                    .put("target", "NORMAL")
                    .track()

            activity.startActivity(card.redirect_url)
            dismiss()
        }

        ic_close.setOnClickListener {
            dismiss()
        }

        setOnDismissListener(this)
    }


    override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
        super.showAtLocation(parent, gravity, x, y)
        val window = mActivity?.window
        window?.let {
            val attributes = it.attributes
            attributes?.alpha = 0.45f
            it.attributes = attributes
        }
        RxBus.get().post(PopWindowEvent())

    }

    override fun onDismiss() {
        val window = mActivity?.window
        window?.let {
            val attributes = it.attributes
            attributes?.alpha = 1f
            it.attributes = attributes
        }
        RxBus.get().post(PopWindowEvent(false))
    }

}