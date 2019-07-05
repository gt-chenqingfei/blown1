package com.shuashuakan.android.modules.widget.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * Author:  liJie
 * Date:   2019/3/1
 * Email:  2607401801@qq.com
 */
class ViewPlayBottomProgressBar : View {


    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)


    private var mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)


    private var mProgressNum:Int = 0
    fun setProgressNum(num: Int) {

    }

    fun setProgressShow(currentNum: Int) {

    }

}
