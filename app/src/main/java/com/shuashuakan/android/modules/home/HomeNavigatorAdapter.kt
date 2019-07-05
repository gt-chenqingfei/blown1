package com.shuashuakan.android.modules.home

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.luck.picture.lib.tools.ScreenUtils
import com.shuashuakan.android.R
import com.shuashuakan.android.modules.player.fragment.VideoListFragment
import com.shuashuakan.android.modules.timeline.multitype.MultiTypeTimeLineFragment
import com.shuashuakan.android.modules.widget.ScaleTransitionPagerTitleView
import com.shuashuakan.android.utils.getColor1
import net.lucode.hackware.magicindicator.FragmentContainerHelper
import net.lucode.hackware.magicindicator.MagicIndicator
import net.lucode.hackware.magicindicator.buildins.UIUtil
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.badge.BadgeAnchor
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.badge.BadgePagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.badge.BadgeRule


/**
 * @author qingfei.chen
 * @since 2019/5/31  下午4:08
 */
class HomeNavigatorAdapter(context: Context, val mFragmentManager: FragmentManager, isFromH5: Boolean,
                           val onHomePageListener: OnHomePageListener) : CommonNavigatorAdapter() {

    private val PAGE_TIMELINE = 1

    private val mFragmentList = mutableListOf<Fragment>(
            VideoListFragment.create(true, R.id.home_navigation_rl, isFromH5),
            MultiTypeTimeLineFragment.create(""))

    private val mNavigationItems = mutableListOf(R.string.string_recommend, R.string.string_follow)
    private val mCommonNavigator = CommonNavigator(context)
    private val mFragmentContainerHelper = FragmentContainerHelper()

    var timeLineBadge = false

    fun attach(magicIndicator: MagicIndicator, firstPage: Int) {
        magicIndicator.navigator = mCommonNavigator
        mFragmentContainerHelper.attachMagicIndicator(magicIndicator)
        mCommonNavigator.adapter = this
        selectPage(firstPage, false)
    }

    override fun getTitleView(context: Context, position: Int): IPagerTitleView {
        val badgePagerTitleView = BadgePagerTitleView(context)
        val titleView = generateTitleView(context)
        titleView.text = context.getString(mNavigationItems[position])

        val titleContainer = mCommonNavigator.titleContainer
        titleContainer.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
        titleContainer.dividerDrawable = object : ColorDrawable() {
            override fun getIntrinsicWidth(): Int {
                return ScreenUtils.dip2px(context, -15f)
            }
        }

        titleView.setOnClickListener {
            selectPage(position, true)
        }
        getTimeLineBadgerView(position, context, badgePagerTitleView)
        badgePagerTitleView.innerPagerTitleView = titleView
        return badgePagerTitleView
    }

    private fun getTimeLineBadgerView(position: Int, context: Context, badgePagerTitleView: BadgePagerTitleView) {
        if (position == PAGE_TIMELINE && timeLineBadge) {
            val badgeTextView = LayoutInflater.from(context).inflate(R.layout.widget_badge_follow, null) as TextView
            badgeTextView.text = ""
            badgePagerTitleView.badgeView = badgeTextView
            badgePagerTitleView.xBadgeRule = BadgeRule(BadgeAnchor.CONTENT_RIGHT, -UIUtil.dip2px(context, 6.0))
            badgePagerTitleView.yBadgeRule = BadgeRule(BadgeAnchor.CONTENT_TOP, UIUtil.dip2px(context, 6.0))
        }
        badgePagerTitleView.isAutoCancelBadge = true
    }

    override fun getCount(): Int {
        return mNavigationItems.size
    }

    override fun getIndicator(context: Context?): IPagerIndicator? {
        return null
    }

    private fun generateTitleView(context: Context): ScaleTransitionPagerTitleView {
        val titleView = ScaleTransitionPagerTitleView(context)
        titleView.textSize = 24f
        titleView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        titleView.normalColor = context.getColor1(R.color.white_60)
        titleView.selectedColor = context.getColor1(R.color.white)

        return titleView
    }

    fun selectPage(position: Int) {
        selectPage(position, true)
    }

    fun selectPage(position: Int, smooth: Boolean) {
        mFragmentContainerHelper.handlePageSelected(position, smooth)
        val current = mFragmentList[position]
        val fragmentTransaction = mFragmentManager.beginTransaction()
        mFragmentList.forEachIndexed { i, fragment ->
            if (position != i && fragment.isAdded) {
                fragmentTransaction.hide(fragment)
            }

        }
        if (mFragmentList[position].isAdded) {
            fragmentTransaction.show(current)
        } else {
            fragmentTransaction.add(R.id.home_container, current)
        }
        fragmentTransaction.commitAllowingStateLoss()


        for (i in mFragmentList.indices) {
            val target = mFragmentList[i]
            if (target.isAdded && target.view != null) {
                target.userVisibleHint = position == i
            }
        }

        onHomePageListener.onPageSelected(position)
    }

    interface OnHomePageListener {
        fun onPageSelected(position: Int)
    }
}