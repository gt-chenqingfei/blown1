package com.shuashuakan.android.modules.partition.adapter

import android.content.Context
import android.support.v4.view.ViewPager
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.shuashuakan.android.R
import com.shuashuakan.android.modules.widget.GradualLinePagerIndicator
import com.shuashuakan.android.modules.widget.ScaleTransitionPagerTitleView
import com.shuashuakan.android.utils.getColor1
import net.lucode.hackware.magicindicator.MagicIndicator
import net.lucode.hackware.magicindicator.buildins.UIUtil
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator


/**
 * @author hushiguang
 * @since 2019-06-17.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
class CategoryMagicIndicatorAdapter(val mContext: Context,
                                    private val mViewpager: ViewPager)
    : CommonNavigatorAdapter() {

    private val mCommonNavigator = CommonNavigator(mContext)

    fun attach(magicIndicator: MagicIndicator) {
        mCommonNavigator.adapter = this
        mCommonNavigator.scrollPivotX = 0.65f
        magicIndicator.navigator = mCommonNavigator
    }

    override fun getTitleView(context: Context?, index: Int): IPagerTitleView {
        val titleView = generateTitleView(mContext)
        titleView.text = mViewpager.adapter?.getPageTitle(index)
        titleView.setOnClickListener {
            mViewpager.currentItem = index
        }
        return titleView
    }

    override fun getCount(): Int {
        return mViewpager.adapter!!.count
    }

    override fun getIndicator(context: Context?): IPagerIndicator {
        val indicator = GradualLinePagerIndicator(context)
        indicator.mode = LinePagerIndicator.MODE_WRAP_CONTENT
        indicator.yOffset = UIUtil.dip2px(context, 4.0).toFloat()
        indicator.roundRadius = UIUtil.dip2px(context, 3.0).toFloat()
        indicator.startInterpolator = AccelerateInterpolator()
        indicator.endInterpolator = DecelerateInterpolator(2.0f)
        indicator.setColors(getColor1(R.color.color_ffef30, mContext), getColor1(R.color.color_normal_59ff5a, mContext))
        return indicator
    }

    private fun generateTitleView(context: Context): ScaleTransitionPagerTitleView {
        val titleView = ScaleTransitionPagerTitleView(context)
        titleView.textSize = 20f
        titleView.normalColor = context.getColor1(R.color.white_60)
        titleView.selectedColor = context.getColor1(R.color.white)
        return titleView
    }
}