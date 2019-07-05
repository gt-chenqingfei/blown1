package com.shuashuakan.android.modules.discovery

import android.content.Context
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.gyf.barlibrary.ImmersionBar
import com.shuashuakan.android.R
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.event.RefreshRankingEvent
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.ui.base.FishActivity
import com.shuashuakan.android.modules.discovery.adapter.RankingListAdapter
import com.shuashuakan.android.modules.discovery.fragment.CallBackToActivity
import com.shuashuakan.android.utils.*
import me.twocities.linker.annotations.Link
import me.twocities.linker.annotations.LinkQuery
import net.lucode.hackware.magicindicator.MagicIndicator
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.UIUtil
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView

//榜单页面
@Link("ssr://ranking/up/pop")
class RankingListActivity : FishActivity(), CallBackToActivity {

    private val tabLayout by bindView<MagicIndicator>(R.id.home_indicator)
    private val viewPager by bindView<ViewPager>(R.id.view_pager)
    private val backIv by bindView<ImageView>(R.id.back_iv)
    private val ruleTv by bindView<TextView>(R.id.rule_tv)
    private lateinit var tabList: ArrayList<String>

    // weekly daily
    @LinkQuery("scope")
    @JvmField
    var scope:String? = null

    private lateinit var vpAdapter: RankingListAdapter

    companion object {
        var clickBtn: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tabList = arrayListOf(getString(R.string.string_today_top), getString(R.string.string_week_top))
        ImmersionBar.with(this).init()
        setContentView(R.layout.activity_ranking_list)
        bindLinkParams()
        initTabBar()
        initListener()
        getSpider().manuallyEvent(SpiderEventNames.LEADER_BOARD_EXPOSURE)
                .put("boardType", "Day")
                .put("userID", getUserId())
                .track()
    }

    private fun initListener() {
        backIv.setOnClickListener { finish() }

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                RxBus.get().post(RefreshRankingEvent())
            }
        })
    }


    private fun initTabBar() {
        vpAdapter = RankingListAdapter(supportFragmentManager, tabList)
        viewPager.adapter = vpAdapter

        val commonNavigator = CommonNavigator(this)
        commonNavigator.scrollPivotX = 0.75f

        commonNavigator.adapter = object : CommonNavigatorAdapter() {
            override fun getCount(): Int {
                return tabList.size
            }

            override fun getTitleView(context: Context, index: Int): IPagerTitleView {
                //  val simplePagerTitleView = ScaleTransitionPagerTitleView(context)
                val simplePagerTitleView = ColorTransitionPagerTitleView(context)
                simplePagerTitleView.text = tabList[index]
                simplePagerTitleView.textSize = 18f
                simplePagerTitleView.normalColor = getColor1(R.color.color_normal_838791)
                simplePagerTitleView.selectedColor = getColor1(R.color.white)



                simplePagerTitleView.setOnClickListener { viewPager.currentItem = index }
                return simplePagerTitleView
            }

            override fun getIndicator(context: Context): IPagerIndicator {
                val indicator = com.shuashuakan.android.modules.widget.GradualLinePagerIndicator(context)
                indicator.mode = LinePagerIndicator.MODE_EXACTLY
                indicator.lineHeight = UIUtil.dip2px(context, 3.0).toFloat()
                indicator.lineWidth = UIUtil.dip2px(context, 28.0).toFloat()
                indicator.yOffset = UIUtil.dip2px(context, 7.5).toFloat()
                indicator.roundRadius = UIUtil.dip2px(context, 3.0).toFloat()
                indicator.startInterpolator = AccelerateInterpolator()
                indicator.endInterpolator = DecelerateInterpolator(2.0f)
                indicator.setColors(getColor1(R.color.color_ffef30), getColor1(R.color.color_normal_59ff5a))
                return indicator
            }
        }
        tabLayout.navigator = commonNavigator
        val titleContainer = commonNavigator.titleContainer // must after setNavigator
        titleContainer.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
        titleContainer.dividerPadding = UIUtil.dip2px(this, 15.0)
        titleContainer.dividerDrawable = resources.getDrawable(R.drawable.simple_splitter)
        ViewPagerHelper.bind(tabLayout, viewPager)
        // 如果参数传过来需要显示weekly
        scope?.let {
            if (it.equals("weekly")) {
                tabLayout.onPageSelected(1)
                viewPager.setCurrentItem(1, false)
            }
        }
    }

    override fun sendDataToActivity(url: String,title:String) {
        ruleTv.setOnClickListener {
            if (url.isNotEmpty())
                RankingListActivity@ this.startActivity(url)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ImmersionBar.with(this).destroy()
    }
}
