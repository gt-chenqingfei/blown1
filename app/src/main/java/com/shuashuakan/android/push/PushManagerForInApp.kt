package com.shuashuakan.android.push

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.shuashuakan.android.ApplicationMonitor
import com.shuashuakan.android.R
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.modules.web.H5Activity
import com.shuashuakan.android.utils.getSpider
import com.shuashuakan.android.utils.getUserId
import org.json.JSONObject


object PushManagerForInapp {
    fun show(context: Context?, data: JSONObject?, hideTime: Int) {
        if (context == null || data == null)
            return
        val modle = InappMessageModle.parsing(data)
        if (modle == null && modle?.expire_at ?: 0 < System.currentTimeMillis())
            return
        val activity = ApplicationMonitor.mCurrentActivity.get() ?: return

        activity.runOnUiThread(object : Runnable {
            var isClick = false
            override fun run() {
                val inflater = activity.layoutInflater
                val view = inflater.inflate(R.layout.toast_push_inapp, null)
                view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                val tv_content = view.findViewById<TextView>(R.id.tv_content)
                val im_icon = view.findViewById<ImageView>(R.id.im_icon)
                val im_cover = view.findViewById<ImageView>(R.id.im_cover)
                val toast = Toast(context)
                toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 100)
                toast.duration = hideTime
                view.isEnabled = true
                view.isClickable = true
                view.setOnClickListener {
                    if (!isClick) {
                        activity.startActivity(H5Activity.intent(activity, modle.redirect_url))

                        context.getSpider().programEvent(SpiderEventNames.INAPP_MESSAGE_CLICK)
                                .put("type", "RECEIVE_FRAGMENT")
                                .put("ssr", modle.redirect_url)
                                .put("userID", context.getUserId()).track()
                    }
                    isClick = true
                }

                toast.view = view
                try {
                    val mTN: Any?
                    mTN = getField(toast, "mTN")
                    if (mTN != null) {
                        val mParams = getField(mTN, "mParams")
                        if (mParams != null && mParams is WindowManager.LayoutParams) {
                            val params = mParams as WindowManager.LayoutParams?
                            params!!.windowAnimations = R.style.pushInappToast
                            params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                toast.show()
                isClick = false
                tv_content.text = modle.content
                Glide.with(context).load(modle.icon_url).into(im_icon)
                Glide.with(context).load(modle.cover_url).into(im_cover)
                context.getSpider().programEvent(SpiderEventNames.INAPP_MESSAGE_EXPOSURE)
                        .put("type", "RECEIVE_FRAGMENT")
                        .put("ssr", modle.redirect_url)
                        .put("userID", context.getUserId()).track()
            }
        })
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    private fun getField(`object`: Any, fieldName: String): Any? {
        val field = `object`.javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        return field.get(`object`)
    }

}
