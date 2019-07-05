package com.shuashuakan.android.modules.timeline.view

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import com.shuashuakan.android.R

/**
 * @author hushiguang
 * @since 2019-05-21.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
class TimeLineEmptyHeaderView : LinearLayout {

    private val view: View

    constructor(context: Context) : super(context) {
        view = View.inflate(context, R.layout.view_timeline_empty_header, null)
        addView(view)
    }
}