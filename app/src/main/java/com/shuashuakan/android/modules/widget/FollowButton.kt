package com.shuashuakan.android.modules.widget

import android.content.Context
import android.util.AttributeSet
import com.shuashuakan.android.R
import com.shuashuakan.android.spider.auto.widget.TraceableTextView
import com.shuashuakan.android.utils.getColor1


/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/10/22
 * Description:
 */
class FollowButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : TraceableTextView(context, attrs, defStyleAttr) {
    var isFollow = false
    var isSubscribe = false

    private var userId: String = ""

    init {
        init()
    }

    private fun init() {
        textSize = 12f
        paint.isFakeBoldText = true
        setFollowStatus(false)
    }

    fun setUserId(userId: String) {
        this.userId = userId
    }

    fun setFollowStatus(isFollow: Boolean, isFans: Boolean? = false) {
        this.isFollow = isFollow
        if (isFollow) {
            text = context.getString(R.string.string_has_follow)
            setBackgroundResource(R.drawable.bg_followed)
            setTextColor(context.getColor1(R.color.white))
        } else {
            text = if (isFans == true) {
                context.getString(R.string.string_follow_fans)
            } else {
                context.getString(R.string.string_follow_with_add)
            }
            setBackgroundResource(R.drawable.bg_not_follow)
            setTextColor(context.getColor1(R.color.colorPrimary))
        }
    }

    fun setSubscribeStatus(isSubscribe: Boolean) {
        this.isSubscribe = isSubscribe
        if (isSubscribe) {
            text = context.getString(R.string.string_has_subscription)
            setBackgroundResource(R.drawable.bg_followed)
            setTextColor(context.getColor1(R.color.white))
        } else {
            text = context.getString(R.string.string_subscription_with_add)
            setBackgroundResource(R.drawable.bg_not_follow)
            setTextColor(context.getColor1(R.color.colorPrimary))
        }
    }
}
