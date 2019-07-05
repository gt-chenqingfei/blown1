package com.shuashuakan.android.modules.topic

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v7.graphics.Palette
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.gyf.barlibrary.ImmersionBar
import com.sensorsdata.analytics.android.sdk.ScreenAutoTracker
import com.shuashuakan.android.R
import com.shuashuakan.android.config.AppConfig
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.channel.ChannelTopicInfo
import com.shuashuakan.android.data.api.model.explore.RankingListModel
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.event.LoginSuccessEvent
import com.shuashuakan.android.event.SubscribeEvent
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.JOIN_PAGE
import com.shuashuakan.android.modules.SSR_UP_STAR_RANK
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.account.activity.LoginActivity
import com.shuashuakan.android.modules.publisher.PermissionRequestFragment
import com.shuashuakan.android.modules.share.ShareConfig
import com.shuashuakan.android.modules.share.ShareHelper
import com.shuashuakan.android.modules.topic.adapter.TopicDetailViewPageAdapter
import com.shuashuakan.android.modules.widget.ColorFlipPagerTitleView
import com.shuashuakan.android.modules.widget.NoScrollViewPager
import com.shuashuakan.android.modules.widget.SpiderAction
import com.shuashuakan.android.service.PullService
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.ui.base.FishActivity
import com.shuashuakan.android.utils.*
import com.shuashuakan.android.utils.ViewHelper.getColorWithAlpha
import com.shuashuakan.android.utils.extension.loadBlur
import com.shuashuakan.android.utils.extension.processBitmap
import com.shuashuakan.android.utils.extension.showCancelSubscribeDialog
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_topic_detail.*
import me.twocities.linker.annotations.LINK
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
import org.json.JSONObject
import javax.inject.Inject

/**
 * 话题页-包括动态和推荐
 *
 * Author: ZhaiDongyang
 * Date: 2019/1/17
 */
@Link("ssr://channel/list")
class TopicDetailActivity : FishActivity(), ChannelTopicApiView<ChannelTopicInfo>, ScreenAutoTracker {

    companion object {
        const val REQUEST_LOGIN_FOR_TIMELINE = 0x01
        const val SOURCE_PERSONAL_PAGE = "PersonalPage"
        const val SOURCE_FOLLOW_TIMELINE = "FollowTimeline"
        const val SOURCE_EXPLORE = "Explore"
        const val SOURCE_ALL_CHANNEL = "AllChannel"
        const val SOURCE_FEED_PLAY = "FeedPlay"
        const val SOURCE_CATEGORY = "CategoryFeedLeaderboard"

        const val EXTRA_CHANNEL_ID = "id"
        const val EXTRA_SOURCE = "source"

        fun launch(context: Context, channelId: String, source: String) {
            context.startActivity(Intent(context,
                    TopicDetailActivity::class.java).putExtra(EXTRA_CHANNEL_ID, channelId).putExtra(EXTRA_SOURCE, source))
        }
    }

    override fun getTrackProperties(): JSONObject? {
        return null
    }

    override fun getScreenUrl(): String {
        return intent.getStringExtra(LINK)
    }

    @Inject
    lateinit var presenter: ChannelTopicPresenter
    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var shareHelper: ShareHelper
    @Inject
    lateinit var accountManager: AccountManager
    @Inject
    lateinit var appConfig: AppConfig

    @LinkQuery("id")
    lateinit var channelId: String

    private var channelName: String = ""

    private val channelBgView by bindView<SimpleDraweeView>(R.id.bg_view)
    private val topicCover by bindView<SimpleDraweeView>(R.id.topicCover)
    private val tabLayout by bindView<MagicIndicator>(R.id.home_indicator)
    private val viewPager by bindView<NoScrollViewPager>(R.id.view_pager)
    private val toolbar by bindView<Toolbar>(R.id.toolbar)
    private val appBarLayout by bindView<AppBarLayout>(R.id.appbar_layout)
    private val bottomShareBtn by bindView<LinearLayout>(R.id.channel_detail_share_btn)
    private val shareBtn by bindView<TextView>(R.id.shareBtn)
    private val titleTv by bindView<TextView>(R.id.channel_detail_title)
    private val toolbarTitle by bindView<TextView>(R.id.toolbar_title)
    private val topSubscribeBtn by bindView<TextView>(R.id.top_subscribe_btn)
    private val subscribeBtn by bindView<TextView>(R.id.subscribe_btn)
    private val subscribeVideoNum by bindView<TextView>(R.id.subscribe_video_num)
    private val subscribeNum by bindView<TextView>(R.id.subscribe_num)
    private val channelDetailSubTitle by bindView<TextView>(R.id.channel_detail_sub_title)
    private val topLayout by bindView<ViewGroup>(R.id.top_layout)
    private val rankLayout by bindView<ViewGroup>(R.id.rank_layout)
    private val rankTitle by bindView<TextView>(R.id.rank_title)

    private var feeds: MutableList<Feed> = mutableListOf()
    private var isSubscribe: Boolean = false
    private var shareUrl: String? = ""
    private var onePage = true
    private val rankIconList = ArrayList<SimpleDraweeView>()

    private var alreadyPostSubscribeEvent: Boolean = false

    private val compositeDisposable = CompositeDisposable()

    private var waitSubscribeChannelId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this).init()
        bindLinkParams()
        setContentView(R.layout.activity_topic_detail)
        ImmersionBar.setTitleBar(this, toolbar)
        spider.pageTracer().reportPageCreated(this)
        val source: String? = intent.getStringExtra(EXTRA_SOURCE)
        spider.manuallyEvent(SpiderEventNames.CHANNEL_DETAIL_EXPOSURE).put("source", source
                ?: "").track()
        toolbar.inflateMenu(R.menu.menu_share)
        presenter.attachView(this)
        val gridLayoutManager = GridLayoutManager(this, 3)
        gridLayoutManager.recycleChildrenOnDetach = true
        initView()
        presenter.request(channelId)

        collapsingToolbarLayout.post {
            val params = channelBgView.layoutParams
            params.width = collapsingToolbarLayout.width
            params.height = collapsingToolbarLayout.height
            channelBgView.layoutParams = params
        }


        initListener()

        RxBus.get().toFlowable().subscribe {
            when (it) {
                is LoginSuccessEvent -> {
                    waitSubscribeChannelId?.let {
                        subscribeEvent()
                    }
                }
                is SubscribeEvent -> {
                    presenter.request(channelId)
                }
            }
        }.addTo(compositeDisposable)
    }

    private fun initView() {
        rankIconList.add(findViewById(R.id.ranking_head4))
        rankIconList.add(findViewById(R.id.ranking_head3))
        rankIconList.add(findViewById(R.id.ranking_head2))
        rankIconList.add(findViewById(R.id.ranking_head1))
    }

    private fun initListener() {
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        toolbar.setOnMenuItemClickListener {
            shareChannel()
            true
        }
        rankLayout.setOnClickListener{
            spider.manuallyEvent(SpiderEventNames.CHANNEL_UP_STAR_CLICK).put("userID", getUserId())
                    .put("channelId",channelId)
                    .track()
            startActivity("$SSR_UP_STAR_RANK?channelId=$channelId")
        }
        appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange
            val bottomScrollRange = totalScrollRange * 0.1f
            val bottomBeginOffset = totalScrollRange * 0.9f
            val absVerticalOffset = Math.abs(verticalOffset).toFloat()
            val percentage = absVerticalOffset / totalScrollRange

            toolbar.background?.alpha = (percentage * 255).toInt()
            if (percentage >= 0.9f) {
                val alpha = (absVerticalOffset - bottomBeginOffset) / bottomScrollRange
                toolbarTitle.alpha = alpha
                topSubscribeBtn.alpha = alpha
                topSubscribeBtn.isClickable = true
            } else {
                toolbarTitle.alpha = 0f
                topSubscribeBtn.alpha = 0f
                topSubscribeBtn.isClickable = false
            }

        }
        subscribeBtn.setOnClickListener {
            subscribeEvent()
        }
        topSubscribeBtn.setOnClickListener {
            subscribeEvent()
        }


        bottomShareBtn.setOnClickListener {
            if (!accountManager.hasAccount()) {
                LoginActivity.launch(this)
                return@setOnClickListener
            } else {
                if (appConfig.isShowCreateFeed()) {
                    goPermissionPage()
                } else {
                    startActivity(JOIN_PAGE)
                }
            }
        }
    }

    private fun goPermissionPage() {
        if (PullService.canUpload()) {
            PermissionRequestFragment.create(PullService.UploadEntity.TYPE_ADD_CHANNEL_VIDEO, "",
                    channelId, channelName).show(supportFragmentManager, "channel_detail")
        } else {
            showShortToast(getString(R.string.string_publish_wait_edit))
        }
    }

    private fun subscribeEvent() {
        if (accountManager.hasAccount()) {
            if (isSubscribe) {
                showCancelSubscribeDialog(channelName,{cancelSub()})
            } else {
                doSubscribe()
            }
        } else {
            waitSubscribeChannelId = channelId
            LoginActivity.launchForResult(this@TopicDetailActivity, REQUEST_LOGIN_FOR_TIMELINE)
        }
    }


    private fun cancelSub() {
        apiService.cancelSubscribe(channelId.toLong())
                .applySchedulers()
                .subscribeApi(onNext = {
                    if (it.result.isSuccess) {
                        showLongToast(getString(R.string.string_un_subscription_success))
                        subscribeBtn.text = getString(R.string.string_subscription)
                        subscribeBtn.setBackgroundResource(R.drawable.bg_subscribe_btn)
                        subscribeBtn.setTextColor(getColor1(R.color.ricebook_color_1))

                        topSubscribeBtn.text = getString(R.string.string_subscription)
                        topSubscribeBtn.setBackgroundResource(R.drawable.bg_subscribe_btn)
                        topSubscribeBtn.setTextColor(getColor1(R.color.ricebook_color_1))
                        isSubscribe = false
                        RxBus.get().post(SubscribeEvent())
                        alreadyPostSubscribeEvent = true
                        waitSubscribeChannelId = null
                    }
                }, onApiError = {
                    waitSubscribeChannelId = null
                })
    }

    private fun doSubscribe() {
        apiService.subscribeMethod(channelId.toLong())
                .applySchedulers()
                .subscribeApi(onNext = {
                    if (it.result.isSuccess) {
                        getSpider().subscribeChinnalClickEvent(this, channelId,
                                SpiderAction.VideoPlaySource.CHANNEL_PAGE.source)
                        showLongToast(getString(R.string.string_subscription_success))
                        subscribeBtn.text = getString(R.string.string_has_subscription)
                        subscribeBtn.setBackgroundResource(R.drawable.bg_unsubscribe)
                        subscribeBtn.setTextColor(getColor1(R.color.white))

                        topSubscribeBtn.text = getString(R.string.string_has_subscription)
                        topSubscribeBtn.setBackgroundResource(R.drawable.bg_unsubscribe)
                        topSubscribeBtn.setTextColor(getColor1(R.color.white))
                        isSubscribe = true
                        RxBus.get().post(SubscribeEvent())
                        alreadyPostSubscribeEvent = true
                        waitSubscribeChannelId = null
                    }
                }, onApiError = {
                    waitSubscribeChannelId = null
                    //showMessage(getString(R.string.string_subscription_error))
                })
    }

    override fun onResume() {
        super.onResume()
        spider.pageTracer().reportPageShown(this, "ssr://channel/list?id=$channelId", "")
    }


    private fun shareChannel() {
        shareHelper.shareType = ShareConfig.SHARE_TYPE_CHANNEL
        shareHelper.doShare(this, null, null, false, false, null, channelId)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun changeStatusBarColorWithAlpha(alpha: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val color: Int = if (alpha <= 0) {
                Color.TRANSPARENT
            } else {
                getColorWithAlpha(alpha, resources.getColor(R.color.colorPrimaryDark))
            }
            window.statusBarColor = color
        }
    }

    override fun showData(data: ChannelTopicInfo) {
        if (alreadyPostSubscribeEvent) {
            initHeader(data)
        } else {
            channelName = data.name ?: ""
            initHeader(data)
            initMagicIndicator()
            initRank()
            if (onePage) {
                shareUrl = data.action?.url
                shareBtn.text = data.action?.subTitle
                bottomShareBtn.visibility = View.VISIBLE
                onePage = false
            }
        }
    }

    override fun showMessage(message: String) {
        showLongToast(message)
    }

    private fun initHeader(data: ChannelTopicInfo) {
        toolbarTitle.setTextColor(Color.WHITE)
        toolbarTitle.text = data.name
        titleTv.text = data.name
        topicCover.processBitmap(data.cover_url) { bitmap ->
            val setDefaultColor = {
                channelBgView.loadBlur(data.cover_url, 10, 10)
                toolbar.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.statusBarColor = resources.getColor(R.color.colorPrimaryDark)
                }
            }

            if (bitmap == null) {
                setDefaultColor()
            } else {
                Palette.from(bitmap).generate { palette:Palette? ->
                    if (palette == null){
                        setDefaultColor()
                    }else{
                        val switch = palette.darkMutedSwatch ?: palette.darkVibrantSwatch
                        ?: palette.mutedSwatch
                        ?: palette.vibrantSwatch ?: palette.lightMutedSwatch
                        ?: palette.lightVibrantSwatch
                        if (switch == null) {
                            setDefaultColor()
                        } else {
                            collapsingToolbarLayout.setBackgroundColor(switch.rgb)

                            tabLayoutContainer.setBackgroundColor(switch.rgb)

                            toolbar.setBackgroundColor(switch.rgb)

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                window.statusBarColor = switch.rgb
                            }
                        }
                    }
                }
            }

        }
        isSubscribe = data.hasSubscribe!!
        subscribeVideoNum.text = String.format(getString(com.shuashuakan.android.base.ui.R.string.string_story_format),
                numFormat(data.totalFeedNum))
        subscribeNum.text = String.format(getString(com.shuashuakan.android.base.ui.R.string.string_video_subscription_people_format),
                numFormat(data.subscribedCount))
        if (data.hasSubscribe!!) {
            subscribeBtn.text = getString(R.string.string_has_subscription)
            subscribeBtn.setBackgroundResource(R.drawable.bg_unsubscribe)
            subscribeBtn.setTextColor(getColor1(R.color.white))

            topSubscribeBtn.text = getString(R.string.string_has_subscription)
            topSubscribeBtn.setBackgroundResource(R.drawable.bg_unsubscribe)
            topSubscribeBtn.setTextColor(getColor1(R.color.white))
        } else {
            subscribeBtn.text = getString(R.string.string_subscription)
            subscribeBtn.setBackgroundResource(R.drawable.bg_subscribe_btn)
            subscribeBtn.setTextColor(getColor1(R.color.ricebook_color_1))

            topSubscribeBtn.text = getString(R.string.string_subscription)
            topSubscribeBtn.setBackgroundResource(R.drawable.bg_subscribe_btn)
            topSubscribeBtn.setTextColor(getColor1(R.color.ricebook_color_1))
        }
        channelDetailSubTitle.text = data.description
    }

    private fun initMagicIndicator() {
        apiService.channelRecommendData(channelId.toLong(), null, null).applySchedulers()
                .subscribeApi(onNext = {
                    if (it.feedList.isEmpty()){
                        initMagicIndicator(TopicDetailViewPageAdapter(this, supportFragmentManager,
                                arrayListOf(getString(R.string.string_dynamic_label)), channelId.toLong()))
                    }else{
                        initMagicIndicator(TopicDetailViewPageAdapter(this, supportFragmentManager,
                                arrayListOf(getString(R.string.string_recommend),
                                        getString(R.string.string_dynamic_label)), channelId.toLong()))
                    }

                }, onApiError = {
                    initMagicIndicator(TopicDetailViewPageAdapter(this, supportFragmentManager,
                            arrayListOf(getString(R.string.string_dynamic_label)), channelId.toLong()))
                })
    }

    private fun initRank(){
        apiService.getRankListData(type ="CATEGORY_USER_LEADER_BOARD", channelId = channelId, page = 0, categoryId = null).applySchedulers()
                .subscribeApi(onNext = {
                    initRankList(it)
                }, onApiError = {

                })
    }
    private fun initRankList(it: RankingListModel){
        rankLayout.visibility = View.VISIBLE
        rankTitle.text = it.title
        for(i in it.dataList.indices){
            rankIconList[i].setImageURI(it.dataList[i].avatar)
        }
    }
    private fun initMagicIndicator(adapter: TopicDetailViewPageAdapter) {
        viewPager.adapter = adapter
        val commonNavigator7 = CommonNavigator(this)
        commonNavigator7.scrollPivotX = 0.65f
        commonNavigator7.adapter = object : CommonNavigatorAdapter() {
            override fun getCount(): Int {
                return viewPager.adapter!!.count
            }

            override fun getTitleView(context: Context, index: Int): IPagerTitleView {
                val simplePagerTitleView = ColorFlipPagerTitleView(context)
                simplePagerTitleView.text = viewPager.adapter!!.getPageTitle(index)
                simplePagerTitleView.textSize = 14f
                simplePagerTitleView.normalColor = getColor1(R.color.color_normal_b6b6b6)
                simplePagerTitleView.selectedColor = getColor1(R.color.white)
                simplePagerTitleView.setOnClickListener {
                    spider.manuallyEvent(SpiderEventNames.CHANNEL_PAGE_TAB_CLICK)
                            .put("channelID", channelId)
                            .put("userID", getUserId())
                            .put("tabTitle", if (index == 0) "Recommend" else "Realtime")
                            .track()
                    viewPager.currentItem = index
                }
                return simplePagerTitleView
            }

            override fun getIndicator(context: Context): IPagerIndicator {
                val indicator = com.shuashuakan.android.modules.widget.GradualLinePagerIndicator(context)
                indicator.mode = LinePagerIndicator.MODE_EXACTLY
                indicator.lineHeight = UIUtil.dip2px(context, 3.0).toFloat()
                indicator.lineWidth = UIUtil.dip2px(context, 28.0).toFloat()
                indicator.roundRadius = UIUtil.dip2px(context, 3.0).toFloat()
                indicator.yOffset = 12f
                indicator.startInterpolator = AccelerateInterpolator()
                indicator.endInterpolator = DecelerateInterpolator(2.0f)
                indicator.setColors(getColor1(R.color.color_ffef30), getColor1(R.color.color_normal_59ff5a))
                return indicator
            }
        }
        tabLayout.navigator = commonNavigator7
        ViewPagerHelper.bind(tabLayout, viewPager)
    }

    override fun onDestroy() {
        super.onDestroy()
        ImmersionBar.with(this).destroy()
        compositeDisposable.clear()
    }
}