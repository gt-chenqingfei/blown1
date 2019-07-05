package com.shuashuakan.android.modules.timeline.view

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.shuashuakan.android.R
import com.shuashuakan.android.modules.account.activity.LoginActivity
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.utils.getSpider

/**
 * @author hushiguang
 * @since 2019-05-20.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
class TimeLineLoginHeaderView : LinearLayout {
    private var view: View
    private lateinit var mHeadView: ImageView
    private lateinit var mTitleView: TextView
    private lateinit var mDescView: View
    private lateinit var mLoginBtn: View


    constructor(context: Context) : super(context) {
        view = View.inflate(context, R.layout.view_header_follow_unlogin, null)
        initView()
        addView(view)
    }

    private fun initView() {
        mHeadView = view.findViewById(R.id.empty_timeline)
        mTitleView = view.findViewById(R.id.empty_text)
        mDescView = view.findViewById<TextView>(R.id.follow_login_desc)
        mLoginBtn = view.findViewById<TextView>(R.id.follow_login)
        mLoginBtn.setOnClickListener {
            context.getSpider().manuallyEvent(SpiderEventNames.FOLLOW_TIMELINE_FEED_LOGIN_CLICK).track()
            LoginActivity.launch(context)
        }
    }

    fun updateLoginView(isLogin: Boolean) {
        mDescView.visibility = if (isLogin) View.VISIBLE else View.GONE
        mLoginBtn.visibility = if (isLogin) View.GONE else View.VISIBLE
        mTitleView.text = if (isLogin) view.resources.getString(R.string.string_login_success) else view.resources.getString(R.string.string_login_follow_up_master)
        mHeadView.setImageDrawable(view.resources.getDrawable(if (isLogin)
            R.drawable.icon_follow_login
        else
            R.drawable.icon_follow_unlogin))
    }

}