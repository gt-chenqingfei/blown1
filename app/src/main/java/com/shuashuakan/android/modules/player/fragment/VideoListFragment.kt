package com.shuashuakan.android.modules.player.fragment


import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.ishumei.smantifraud.SmAntiFraud
import com.pili.pldroid.player.PLOnInfoListener
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI
import com.shuashuakan.android.ApplicationMonitor
import com.shuashuakan.android.BuildConfig
import com.shuashuakan.android.R
import com.shuashuakan.android.config.AppConfig
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.AcceptGift
import com.shuashuakan.android.data.api.model.ChestInfo
import com.shuashuakan.android.data.api.model.PublishActivity
import com.shuashuakan.android.data.api.model.account.Account
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.enums.ChainFeedSource
import com.shuashuakan.android.event.*
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.ACCOUNT_PAGE
import com.shuashuakan.android.modules.FeedTransportManager
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.account.activity.LoginActivity
import com.shuashuakan.android.modules.activity.ActivityCardHelper
import com.shuashuakan.android.modules.activity.ChestHelper
import com.shuashuakan.android.modules.activity.ChestPopupWindow
import com.shuashuakan.android.modules.player.presenter.RecommendVideoView
import com.shuashuakan.android.modules.player.presenter.VideoListPresenter
import com.shuashuakan.android.modules.publisher.chains.ChainsListIntentParam
import com.shuashuakan.android.modules.publisher.isStoragePermission
import com.shuashuakan.android.modules.share.ShareHelper
import com.shuashuakan.android.modules.track.ClickAction
import com.shuashuakan.android.modules.track.trackClick
import com.shuashuakan.android.modules.track.trackPageEnd
import com.shuashuakan.android.modules.viphome.Constants
import com.shuashuakan.android.modules.viphome.VideoHallExploreDialog
import com.shuashuakan.android.modules.viphome.VideoHallPlayEndDialog
import com.shuashuakan.android.modules.widget.DanmakuContainer
import com.shuashuakan.android.modules.widget.dialogs.DownloadProgressDialog
import com.shuashuakan.android.player.SSKOnScrollListener
import com.shuashuakan.android.player.SSKVideoPlayListener
import com.shuashuakan.android.player.SSKVideoTextureView
import com.shuashuakan.android.player.SSKViewPagerLayoutManager
import com.shuashuakan.android.service.PullService
import com.shuashuakan.android.spider.EventCreator
import com.shuashuakan.android.spider.Spider
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.ui.base.FishFragment
import com.shuashuakan.android.ui.player.adapter.VideoChainAdapter
import com.shuashuakan.android.ui.player.adapter.VideoListAdapter
import com.shuashuakan.android.utils.*
import com.shuashuakan.android.utils.download.DownloadManager
import com.shuashuakan.android.utils.extension.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_video_list.*
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * 横向滑动播放器结构Fragment
 */
class VideoListFragment : FishFragment(), RecommendVideoView, SSKOnScrollListener, AccountManager.AccountChangedListener, SoftKeyBoardHelper.OnSoftKeyBoardChangeListener {
    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var mVideoListPresenter: VideoListPresenter

    private val videoPlayContainer by bindView<FrameLayout>(R.id.videoPlayerContainer)

    private val back by bindView<ImageView>(R.id.back)

    private lateinit var specialFeedListAdapter: VideoListAdapter

    @Inject
    lateinit var accountManager: AccountManager
    @Inject
    lateinit var shareHelper: ShareHelper

    @Inject
    lateinit var exoPlayerHelper: com.shuashuakan.android.modules.player.ExoPlayerHelper
    @Inject
    lateinit var appConfig: AppConfig
    @Inject
    lateinit var spider: Spider

    private lateinit var videoTexture: SSKVideoTextureView
    private lateinit var emptyView: View
    private lateinit var alertIcon: ImageView
    private lateinit var alertTitle: TextView
    private lateinit var alertMsg: TextView


    private val redirect = "REDIRECT"

    private val chest = "CHEST"

    private var isLoadingView = false

    private val compositeDisposable = CompositeDisposable()

    private var mPublishActivity: PublishActivity? = null
    private var mActivityCardHelper: ActivityCardHelper? = null
    private var mChestHelper: ChestHelper? = null

    private val once = "ONCE"
    private val always = "ALWAYS"
    private val activitySP = "activity_sp"
    private var mChestPopupWindow: ChestPopupWindow? = null
    private var mFrameChestFloat: FrameLayout? = null

    private var chainFeedSource: String? = null
    private var feedId: String? = null
    private var floorFeedId: String? = null
    private var intentParam: ChainsListIntentParam? = null

    companion object {
        const val FEED_ID = "id"
        const val FEED_SOURCE = "feedSource"
        const val INTENT_PARAM = "intentParam"
        const val IS_MINE = "isMine"
        const val FLOOR_FEED_ID = "floorFeedId"
        const val iS_HOME = "isHome"
        const val VIEW_ID = "idView"
        const val isfromh5 = "isfromh5"
        var showContent: Boolean = false
        const val REQUEST_CODE_FOLLOW_LOGIN = 2

        private fun getFeedSourceIfNull(feedSource: String?, intentParam: ChainsListIntentParam?): String {
            var retSource = ""
            if (ApplicationMonitor.mActivityStack != null && ApplicationMonitor.mActivityStack!!.size >= 2) {
                val activity = ApplicationMonitor.mActivityStack?.get(ApplicationMonitor.mActivityStack?.size!! - 2)
                if (feedSource == null) {
                    retSource = if (activity.toString().contains("H5Activity")) {
                        "h5"
                    } else {
                        intentParam?.feedSource?.source ?: ""
                    }
                }
            }
            return retSource
        }

        fun create(isMine: Boolean, feedId: String?, feedSource: String?, intentParam: ChainsListIntentParam?,
                   floorFeedId: String?): VideoListFragment {
            val feedSource1 = getFeedSourceIfNull(feedSource, intentParam)
            val fragment = VideoListFragment()
            val bundle = Bundle()
            bundle.putString(FEED_SOURCE, feedSource1)
            bundle.putString(FEED_ID, feedId)
            bundle.putParcelable(INTENT_PARAM, intentParam)
            bundle.putBoolean(IS_MINE, isMine)
            bundle.putString(FLOOR_FEED_ID, floorFeedId)
            fragment.arguments = bundle
            return fragment
        }

        //home数据专属create
        fun create(isHome: Boolean, title_rl: Int, fromh5: Boolean): VideoListFragment {
            val fragment = VideoListFragment()
            val bundle = Bundle()
            bundle.putBoolean(iS_HOME, isHome)
            bundle.putInt(VIEW_ID, title_rl)
            bundle.putBoolean(isfromh5, fromh5)
            fragment.arguments = bundle
            return fragment
        }
    }

    private var mSoftKeyBoardHelper: SoftKeyBoardHelper? = null
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        spider.pageTracer().reportPageCreated(this)
        spider.pageTracer().reportPageShown(this, "ssr://home?index=homepage", "")
        accountManager.addAccountChangedListener(this)
        if (appConfig.isDanmakaOpen()) {
            mSoftKeyBoardHelper = SoftKeyBoardHelper(activity, this)
        }
    }

    private val barrageEditBackgroundColor = Color.parseColor("#ffffff")
    private val barrageEditInputTextColor = Color.parseColor("#ff92969c")

    override fun onKeyBoardShow(height: Int) {
        videoTexture.pause()
        val lm = specialFeedListAdapter.findChainCenterView()?.layoutManager as? SSKViewPagerLayoutManager
                ?: return
        val centerChainView = lm.findCenterView() ?: return
        val tagFeed = centerChainView.tag as? Feed
        if (tagFeed == videoTexture.playFeed) {
            val barrageContainer = centerChainView.findViewById<View>(R.id.barrageContainer)
            val barrageToggle = barrageContainer.findViewById<ImageView>(R.id.barrageToggle)
            barrageContainer.setBackgroundColor(barrageContainer.context.getColor1(R.color.color_normal_dd111217))
            barrageContainer.translationY -= height.toFloat()

            barrageContainer.findViewById<View>(R.id.divider).visibility = View.GONE
            val inputBarrage = barrageContainer.findViewById<EditText>(R.id.inputBarrage)

            barrageContainer.translationY = -height.toFloat()
            barrageContainer.setBackgroundColor(barrageEditBackgroundColor)

            barrageToggle.setImageResource(R.drawable.ic_barrage_toggle_keyboard_show)

            inputBarrage?.setTextColor(barrageEditInputTextColor)
            inputBarrage?.setBackgroundResource(R.drawable.bg_input_barrage_keyboard_show)
            inputBarrage.setSelection(inputBarrage.text.length)
        }
    }


    override fun onKeyBoardHide() {
        videoTexture.start()
        val lm = specialFeedListAdapter.findChainCenterView()?.layoutManager as? SSKViewPagerLayoutManager
                ?: return
        val centerChainView = lm.findCenterView() ?: return
        val tagFeed = centerChainView.tag as? Feed
        if (tagFeed == videoTexture.playFeed) {
            val barrageContainer = centerChainView.findViewById<View>(R.id.barrageContainer)
            val barrageToggle = barrageContainer.findViewById<ImageView>(R.id.barrageToggle)
            barrageContainer.findViewById<View>(R.id.divider).visibility = View.VISIBLE
            val inputBarrage = barrageContainer.findViewById<EditText>(R.id.inputBarrage)

            barrageContainer.translationY = 0f
            barrageContainer.setBackgroundColor(Color.TRANSPARENT)

            barrageToggle.setImageResource(R.drawable.ic_barrage_toggle_keyboard_hide)

            inputBarrage?.setTextColor(Color.WHITE)
            inputBarrage?.setBackgroundResource(R.drawable.bg_input_barrage_keyboard_hide)
            inputBarrage.setSelection(0)
            inputBarrage.clearFocus()
        }

    }

    override fun onFloorDataLoadSuccess(homeFeedData: List<Feed>) {
        if (homeFeedData.isEmpty()) {
            specialFeedListAdapter.loadMoreEnd()
        } else {
            specialFeedListAdapter.addData(homeFeedData)
            specialFeedListAdapter.loadMoreComplete()
        }
        if (specialFeedListAdapter.data.isEmpty()) {
            setEmptyViewState(iconDrawable = R.drawable.ic_worry_warning_comment, title = getString(R.string.no_data_hint))
        } else {
            emptyView.visibility = View.GONE
        }
    }

    override fun onFloorDataLoadError(error: ApiError) {
        if (specialFeedListAdapter.data.isEmpty()) {
            setEmptyViewState(iconDrawable = R.drawable.ic_network_error, title = getString(R.string.string_load_error), msg = getString(R.string.string_check_net_with_click))
        } else {
            emptyView.visibility = View.GONE
            if (error is ApiError.HttpError) {
                showLongToast(error.displayMsg)
            }
        }
        specialFeedListAdapter.loadMoreComplete()
    }


    override fun onPublishActivityLoadSuccess(publishActivity: PublishActivity) {
        this.mPublishActivity = publishActivity
        val ctx = context
        val cardList = publishActivity.homepage_card?.card_list
        if (ctx != null && cardList?.isNotEmpty() == true) {
            mActivityCardHelper = ActivityCardHelper()
            mActivityCardHelper?.initActivityCard(ctx, cardList)
        }


        // 分享的活动卡片
        if (ctx != null && publishActivity.feed_share != null) {
            shareHelper.initFeedShareActive(publishActivity.feed_share)
        }

        when (publishActivity.homepage_icon?.business_type) {
            chest -> {
                val chestFloatView = chestFloatViewStub.inflate()
                mFrameChestFloat = chestFloatView.findViewById(R.id.frameChestFloat)
                mFrameChestFloat?.setSafeClickListener {
                    if (accountManager.hasAccount()) {
                        //处理新人宝箱
                        mFrameChestFloat?.visibility = View.GONE
                        mChestHelper?.showOpenChestPopupWindow(accountManager, activity, activity?.window?.decorView
                                ?: view, mAcceptGiftListener)
                    } else {
                        activity?.startActivity(ACCOUNT_PAGE)
                    }

                    ctx?.let {
                        it.getSpider().manuallyEvent(SpiderEventNames.ACTIVE_CHEST_FLOAT_CLICK).track()
                    }
                }
                val chestCountTimer = chestFloatView.findViewById<com.shuashuakan.android.modules.widget.CountDownTextView>(R.id.chestCountTimer)
                publishActivity.homepage_icon?.expire_at?.let {
                    chestCountTimer.setEndTime(it)
                    chestCountTimer.setClockListener(object : com.shuashuakan.android.modules.widget.CountDownTextView.ClockListener {
                        override fun timeEnd() {
                            mFrameChestFloat?.visibility = View.GONE
                        }

                        override fun remainFiveMinutes() {
                        }

                    })
                }
                mVideoListPresenter.fetchChestInfo()
            }
            redirect -> {
                val homePageIcon = publishActivity.homepage_icon ?: return
                val cxt = context ?: return
                val currentTimeMillis = System.currentTimeMillis()
                if (homePageIcon.expire_at > currentTimeMillis) {
                    when (homePageIcon.frequency) {
                        once -> {
                            val isShowActivity = cxt.sharedPreferences(activitySP).getBoolean(homePageIcon.id.toString(), false)
                            if (!isShowActivity) {
                                val activityEvent = ActivityEvent(true, homePageIcon)
                                RxBus.get().post(activityEvent)
                                cxt.sharedPreferences(activitySP).edit().putBoolean(homePageIcon.id.toString(), true).apply()
                            }
                        }
                        always -> {
                            val activityEvent = ActivityEvent(true, homePageIcon)
                            RxBus.get().post(activityEvent)
                        }
                    }
                } else {
                    cxt.sharedPreferences(activitySP).edit().remove(homePageIcon.id.toString()).apply()
                }
            }
        }
    }

    override fun onPublishActivityLoadError(error: ApiError) {
    }


    override fun onChestLoadSuccess(result: Array<ChestInfo>) {
        val ctx = context ?: return
        if (!result[0].isEmpty() && !result[1].isEmpty()) {
            mChestHelper = ChestHelper()
            mChestHelper?.initChestInfoView(ctx, result)
            mFrameChestFloat?.visibility = View.GONE

        } else if (result[0].isEmpty() && !result[1].isEmpty()) {
            mChestHelper = ChestHelper()
            mChestHelper?.initOpenChestView(ctx, result)
            mFrameChestFloat?.visibility = View.VISIBLE
            mChestHelper?.logWithChestExposure(accountManager, activity!!, true)
        }

    }

    override fun onChestLoadError(error: ApiError) {
        Timber.e("VideoListFragment onChestLoadError $error")
    }

    /**
     * 收入囊中点击回调
     */
    private val mAcceptGiftListener = object : ChestPopupWindow.AcceptGiftListener {
        override fun onAcceptGift(v: View, popupWindow: ChestPopupWindow) {
            mVideoListPresenter.acceptGift()
            mChestPopupWindow = popupWindow
        }
    }

    /**
     * 收入囊中成功
     */
    override fun onAcceptGiftSuccess(acceptGift: AcceptGift) {
        if (acceptGift.result?.is_success == true) {
            activity?.let {
                val url = acceptGift.result?.redirect_url
                if (url != null) {
                    it.startActivity(url)
                    mChestPopupWindow?.dismiss()
                    mFrameChestFloat?.visibility = View.GONE


                    //领取新人宝箱
                    val ctx = context ?: return
                    if (accountManager.hasAccount()) {
                        val userId = accountManager.account()?.userId ?: ""
                        ctx.getSpider().manuallyEvent(SpiderEventNames.ACTIVE_CHEST_GET_RESULT)
                                .put("userID", userId)
                                .put("ssr", url)
                                .put("isSuccess", true)
                                .track()

                    }
                }
            }
        }
    }

    /**
     * 收入囊中失败
     */
    override fun onAcceptGiftError(error: ApiError) {
        if (error is ApiError.HttpError) {
            showMessage(error.displayMsg)
            mChestPopupWindow?.dismiss()
            mFrameChestFloat?.visibility = View.GONE
            val ctx = context ?: return
            ctx.getSpider().manuallyEvent(SpiderEventNames.ACTIVE_CHEST_GET_RESULT)
                    .put("isSuccess", false)
                    .track()
        }
    }


    override fun onInitComplete(layoutManager: SSKViewPagerLayoutManager, recyclerView: RecyclerView) {
    }


    override fun onScrollStateChanged(layoutManager: SSKViewPagerLayoutManager, recyclerView: RecyclerView, newState: Int) {

        when (newState) {
            RecyclerView.SCROLL_STATE_IDLE -> {
                val centerFloorView = layoutManager.findCenterView()
                if (centerFloorView != null) {
                    val position = layoutManager.getPosition(centerFloorView)

                    val publishActivity = mPublishActivity
                    if (publishActivity != null) {
                        //处理活动卡片
                        mActivityCardHelper?.showActivityCard(activity, activity?.window?.decorView
                                ?: view, position)


                        //处理新人宝箱
                        mChestHelper?.showChestPopupWindow(accountManager, activity, activity?.window?.decorView
                                ?: view, position, mAcceptGiftListener)
                    }
                    //VIP HOME
                    showPlayEndDialogIfNeeded(position)
                }
            }
        }
    }


    override fun onScrolled(layoutManager: SSKViewPagerLayoutManager, recyclerView: RecyclerView, dx: Int, dy: Int) {
    }


    override fun showMessage(message: String) {
        showShortToast(message)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_video_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)

        setListener()

        registerEvent()

        initData()

        if (specialFeedListAdapter.data.isEmpty()) {
            setEmptyViewState(iconPath = "file:///android_asset/loading.gif", title = "视频加载中...", asGif = true)
        } else {
            emptyView.visibility = View.GONE
        }
    }

    private lateinit var mRecyclerView: RecyclerView

    private fun initView(view: View) {
        //设置videoTexture
        val loadingView = view.findViewById<View>(R.id.infiniteLineLoadingView)

        videoTexture = view.findViewById(R.id.video_texture)
        videoTexture.addVideoPlayListener(mVideoPlayListener)
        videoTexture.setBufferingIndicator(loadingView)


        val danmakuView = view.findViewById<View>(R.id.danmakuView)

        //设置recyclerView
        mRecyclerView = view.findViewById<com.shuashuakan.android.modules.widget.BetterRecyclerView>(R.id.recycler_view)
        mRecyclerView.setItemViewCacheSize(0)

        val layoutManager = SSKViewPagerLayoutManager(mRecyclerView, requireContext(), RecyclerView.VERTICAL)

        layoutManager.addSSKOnScrollListener(this)
        mRecyclerView.layoutManager = layoutManager
//        val intentParam: ChainsListIntentParam? = arguments?.getParcelable(INTENT_PARAM)
        val intentParam: ChainsListIntentParam? = FeedTransportManager.intentParam
        FeedTransportManager.intentParam = null
        specialFeedListAdapter = VideoListAdapter(
                videoPlayContainer,
                apiService,
                appConfig,
                accountManager,
                shareHelper,
                intentParam?.fromMark,
                intentParam?.feedSource?.source,
                this@VideoListFragment)


        specialFeedListAdapter.setEnableLoadMore(true)
        specialFeedListAdapter.setPreLoadNumber(6)
        specialFeedListAdapter.setOnLoadMoreListener({
            mVideoListPresenter.fetchFloorData()
        }, mRecyclerView)

        //specialFeedListAdapter.disableLoadMoreIfNotFullPage()

        mRecyclerView.adapter = specialFeedListAdapter

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val statusBarHeight = activity?.getStatusBarHeight() ?: 0

            val param = back.layoutParams as FrameLayout.LayoutParams
            param.setMargins(0, statusBarHeight, 0, 0)

            val danmakuViewParam = danmakuView.layoutParams as FrameLayout.LayoutParams
            danmakuViewParam.setMargins(0, danmakuViewParam.topMargin + statusBarHeight, 0, 0)
        }

        emptyView = view.findViewById(R.id.empty_view)
        alertIcon = emptyView.findViewById(R.id.alertIcon)
        alertTitle = emptyView.findViewById(R.id.alertTitle)
        alertMsg = emptyView.findViewById(R.id.alertMsg)

        mRecyclerView.addOnItemTouchListener(mItemTouchListener)

    }

    private val mItemTouchListener = object : RecyclerView.SimpleOnItemTouchListener() {

        override fun onTouchEvent(rv: RecyclerView, ev: MotionEvent) {
            val x = ev.x
            val y = ev.y
            val child = rv.findChildViewUnder(x, y)
            val childRv = child?.findViewById<RecyclerView>(R.id.special_view_page_root)
            val childRVItemView = childRv?.findChildViewUnder(x, y)
            val danmakuContainer = childRVItemView?.findViewById<DanmakuContainer>(R.id.barrageContainer)

            if (danmakuContainer != null && danmakuContainer.translationY != 0f) {
                val translationX = danmakuContainer.translationX
                val translationY = danmakuContainer.translationY
                if (x >= danmakuContainer.left + translationX
                        && x <= danmakuContainer.right + translationX
                        && y >= danmakuContainer.top + translationY
                        && y <= danmakuContainer.bottom + translationY) {

                    ev.setLocation(x, danmakuContainer.height * 0.5f)
                    danmakuContainer.dispatchTouchEvent(ev)
                } else {
                    activity?.hideKeyboard()
                }
            }

        }


        override fun onInterceptTouchEvent(rv: RecyclerView, ev: MotionEvent): Boolean {
            val x = ev.x
            val y = ev.y
            val child = rv.findChildViewUnder(x, y)
            val childRv = child?.findViewById<RecyclerView>(R.id.special_view_page_root)
            val childRVItemView = childRv?.findChildViewUnder(x, y)
            val danmakuContainer = childRVItemView?.findViewById<DanmakuContainer>(R.id.barrageContainer)

            if (danmakuContainer != null && danmakuContainer.translationY != 0f) {
                val translationX = danmakuContainer.translationX
                val translationY = danmakuContainer.translationY
                if (x >= danmakuContainer.left + translationX
                        && x <= danmakuContainer.right + translationX
                        && y >= danmakuContainer.top + translationY
                        && y <= danmakuContainer.bottom + translationY) {

                    val transformedEvent = MotionEvent.obtain(ev)
                    transformedEvent.setLocation(x, danmakuContainer.height * 0.5f)
                    danmakuContainer.dispatchTouchEvent(transformedEvent)
                    transformedEvent.recycle()
                } else {
                    activity?.hideKeyboard()
                }
                return true
            }
            return false
        }
    }

    private fun setListener() {
        emptyView.setSafeClickListener {
            mVideoListPresenter.fetchFloorData()
        }
        back.setSafeClickListener {
            activity?.finish()
        }


    }


    private fun setEmptyViewState(iconPath: String? = null, iconDrawable: Int = 0, title: String?, msg: String? = null, asGif: Boolean = false) {
        emptyView.visibility = View.VISIBLE
        if (asGif && iconPath?.isNotEmpty() == true) {
            alertIcon.loadAsGif(iconPath, 100, 100)
        } else {
            alertIcon.load(iconDrawable, 100, 100)
        }

        title?.let {
            alertTitle.text = title
        }

        msg?.let {
            alertMsg.text = msg
        }
    }


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            videoTexture.hostResume()
        } else {
            videoTexture.hostPause()
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        mVideoListPresenter.attachView(this)
    }


    override fun onDetach() {
        super.onDetach()
        showChestHandler.removeCallbacksAndMessages(null)
        mVideoListPresenter.detachView(false)
    }


    override fun onResume() {
        super.onResume()
        if (userVisibleHint) {
            videoTexture.hostResume()
        }

        onShowChestWindow()
        showVipHomeExploreDialogInNeeded()
    }


    override fun onPause() {
        super.onPause()
        videoTexture.hostPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
        videoTexture.removeVideoPlayListener(mVideoPlayListener)
        videoTexture.stopPlayback()
        accountManager.removeAccountChangedListener(this)
        mRecyclerView.removeOnItemTouchListener(mItemTouchListener)
    }


    private fun initData() {
        val args = arguments
        val isHome = args?.getBoolean(iS_HOME) ?: false
        if (!isHome) {
            chainFeedSource = args?.getString(FEED_SOURCE)
            feedId = args?.getString(FEED_ID)
            floorFeedId = args?.getString(FLOOR_FEED_ID)
            intentParam = args?.getParcelable(INTENT_PARAM)

            val isMine = args?.getBoolean(IS_MINE) ?: false
            mVideoListPresenter.initIntentParam(intentParam, isMine)
        }

        if (isHome) {
            back.visibility = View.GONE
            mVideoListPresenter.fetchFloorData()
        } else {
            back.visibility = View.VISIBLE
            val feedList = intentParam?.feedList

            when (intentParam?.fromMark) {
                FeedTransportManager.MARK_FROM_FOLLOW,
                FeedTransportManager.MARK_FROM_TOPIC_DETAIL_RECOMMEND,
                FeedTransportManager.MARK_FROM_TOPIC_DETAIL_TIMELINE,
                FeedTransportManager.MARK_FROM_PERSONAL_NEWEST,
                FeedTransportManager.MARK_FROM_EXPLORE_EXCELLENT_CHAINS,
                FeedTransportManager.MARK_FROM_RECOMMEND_USER_FEED,
                FeedTransportManager.MARK_FROM_CATEGORY_RECOMMEND_TOPIC,
                FeedTransportManager.MARK_FROM_CATEGORY_LEADERBOARD,
                FeedTransportManager.MARK_FROM_PERSONAL_UP -> {
                    if (feedList != null) {
                        var position = intentParam?.position

                        if (position == null) {
                            val currentFloorFeed = intentParam?.currentFloorFeed
                            if (currentFloorFeed != null) {
                                position = feedList.indexOf(currentFloorFeed)
                            } else {
                                position = 0
                            }
                        }

                        specialFeedListAdapter.setNewData(feedList)

                        if (position != 0) {
                            mRecyclerView.scrollToPosition(position)
                        }

                        if (intentParam?.fromMark == FeedTransportManager.MARK_FROM_EXPLORE_EXCELLENT_CHAINS) {
                            specialFeedListAdapter.loadMoreEnd()
                        }
                    }
                }
                FeedTransportManager.MARK_FROM_VIP_HOME -> {
                    Constants.IS_OPEN_VIP_ROOM = true
                    iv_close.visibility = View.VISIBLE
                    back.visibility = View.GONE
                    iv_close.setOnClickListener {
                        activity?.finish()
                        activity?.getSpider()?.manuallyEvent(SpiderEventNames.VipRoom.ROOM_CLOSE_CLICK)?.track()
                    }
                    specialFeedListAdapter.setNewData(feedList)
                    specialFeedListAdapter.loadMoreEnd()
                }
            }

            intentParam?.childFeedList?.let {
                specialFeedListAdapter.moveToChainIndex(it as? MutableList<Feed>, intentParam?.childEnterPosition
                        ?: 0)
            }

            if (feedId.isNullOrEmpty()) {
                mVideoListPresenter.fetchFloorData()
            } else {
                specialFeedListAdapter.loadChain(feedId, floorFeedId ?: intentParam?.floorFeedId)
            }

        }

        if (isHome) {
            mVideoListPresenter.fetchActivityPublish()
        }
    }


    private fun registerEvent() {
        RxBus.get().toFlowable().subscribe { event ->
            when (event) {
                is ShareEvent -> {
                    val feed = videoTexture.playFeed
                    feed?.let {
                        if (it.id.getRealId() == event.feedId.getRealId()) {
                            trackClick(
                                    ClickAction.ACTION_SHARE, arrayListOf(
                                    ClickAction.FEED_ID to it.id,
                                    ClickAction.SHARE_TYPE to event.shareChannel
                            ))
                        }
                    }

                }
                is ShareBoardUnLikeFeedEvent -> {
                    apiService.createBored(event.feedId, SmAntiFraud.getDeviceId()).applySchedulers().subscribeApi(onNext = { commonResult ->
                        if (commonResult.result.isSuccess) {
                            requireContext().showLongToast(requireContext().getString(R.string.string_minus_video_with_user))
                            val chainAdapter = specialFeedListAdapter.findChainCenterView()?.adapter as? VideoChainAdapter
                            val eventFeed = chainAdapter?.data?.find { it.id.getRealId() == event.feedId }
                            val position = chainAdapter?.data?.indexOf(eventFeed)
                                    ?: RecyclerView.NO_POSITION
                            if (position > RecyclerView.NO_POSITION) {
                                chainAdapter?.remove(position)
                            }
                        } else {
                            requireContext().showLongToast(getString(R.string.string_operating_error))
                        }
                    }, onApiError = {
                        requireContext().showLongToast(getString(R.string.string_operating_error))
                    })
                }

                is FeedPresentVCEvent -> {
                    trackPageEnd(ClickAction.FEED_PRESENT_VC_TRACK, arrayListOf(
                            "feed_id" to event.feedId,
                            "product_ids" to event.productIds,
                            "page_view_time" to System.currentTimeMillis() - event.pageStartTime,
                            "video_play_duration" to videoTexture.currentPosition,
                            "feed_page_type" to ClickAction.FeedSource.CHAINS_FEED_LIST.source))
                }
                is SaveVideoEvent -> {
                    context?.let {
                        if (event.isSuccess) {
                            spider.shareDetailsEvent(it, event.feedId, true, "SaveIntoAlbum")
                        } else {
                            spider.shareDetailsEvent(it, event.feedId, false, "SaveIntoAlbum")
                        }
                    }
                }
                is DownloadVideoEvent -> {
                    if (event.tag == DownloadManager.DOWNLOAD_TAG_FRAGMENT_SPECIAL_VIDEO) {
                        downloadVideo(event.feedId)
                    }
                }

                is FeedRefreshCountEvent -> {
                    val currentChainView = specialFeedListAdapter.findChainCenterView()
                    val currentChainAdapter = currentChainView?.adapter as? VideoChainAdapter

                    val item = currentChainAdapter?.getItem(event.position)
                    item?.let { feed ->
                        if (feed.id.getRealId() == event.feedId.getRealId()) {
                            feed.commentNum = event.commentCount
                            val upTextView = currentChainAdapter.getViewByPosition(event.position,
                                    R.id.tv_comment_count) as? TextView
                            upTextView?.text = event.commentCount.toString()
                            currentChainAdapter.notifyItemChanged(event.position)
                        }
                    }
                }

                is LoginSuccessEvent -> {
                    checkHasWaitActionToFinish()
                }

                is UploadSuccessEvent -> {
                    if (event.source == PullService.UploadEntity.TYPE_ADD_SOLITAIRE) {
                        // showChainSuccessDialog(it.feed)
                        toastCustomText(requireContext(), getString(R.string.string_solitaire_video_publish_success))
                    }

                    if (event.source == PullService.UploadEntity.TYPE_ADD_HOME_VIDEO || event.source == PullService.UploadEntity.TYPE_ADD_CHANNEL_VIDEO) {
                        //用户发布主视频成功打点
                        spider.manuallyEvent(SpiderEventNames.MASTER_FEED_RELEASE)
                                .put("feedID", event.feed.id)
                                .put("title", event.feed.title)
                                .put("channelID", event.feed.channelId.toString())
                                .put("userID", requireContext().getUserId())
                                .track()
                    }
                }
                is PopWindowEvent -> {
                    if (event.isPopUpWindowShow) {
                        videoTexture.hostPause()
                    } else {
                        videoTexture.hostResume()
                    }
                    if (event.isneedShowChestFloat) {
                        mFrameChestFloat?.visibility = View.VISIBLE
                        mChestHelper?.logWithChestExposure(accountManager, activity!!, true)
                    }
                }
                is VideoListFollowUserLogin -> {
                    context?.let {
                        //                        startActivityForResult(Intent(it, LoginActivity::class.java), REQUEST_CODE_FOLLOW_LOGIN)
                        LoginActivity.launchForResult(it, REQUEST_CODE_FOLLOW_LOGIN)
                    }
                }
            }

        }.addTo(compositeDisposable)
    }


    private fun downloadVideo(feedId: String) {
        if (isLoadingView) {
            return
        }
        isLoadingView = true


        val activity = activity
        if (activity?.isFinishing == false) {
            if (isStoragePermission()) {
                isLoadingView = false
                toastCustomText(activity, getString(R.string.string_open_file_write))
                return
            }
            val path = Environment.getExternalStorageDirectory().path + "/DCIM/Camera/$feedId.mp4"
            val file = File(path)
            val downloadProgressDialog: DownloadProgressDialog =
                    DownloadProgressDialog.progressDialog(activity, false)
            if (!file.exists()) {
                downloadProgressDialog.show()
                downloadProgressDialog.setProgressBarVisibility(getString(R.string.string_loading))
                apiService.getWatermarkUrl(feedId)
                        .applySchedulers()
                        .subscribeApi(onNext = {
                            downloadProgressDialog.setCircleProgressBarPercentVisibility(getString(R.string.string_downloading))
                            val downloadManager = DownloadManager(activity, path, it.downloadUrl, downloadProgressDialog, feedId)
                            downloadManager.startTask()
                        }, onApiError = {
                            RxBus.get().post(SaveVideoEvent(false, feedId))
                            toastCustomText(activity, getString(R.string.string_video_download_error_with_copyright))
                            downloadProgressDialog.dismiss()
                            isLoadingView = false
                        }, onComplete = {
                            isLoadingView = false
                        })
            } else {
                isLoadingView = false
                downloadProgressDialog.dismiss()
                toastCustomText(activity, String.format(getString(R.string.string_video_save_format), "DCIM/Camera"))
            }

        }
    }


    private val mVideoPlayListener: SSKVideoPlayListener = object : SSKVideoPlayListener() {
        private var startPosition = 0L

        override fun onCompletion() {
            val feed = videoTexture.playFeed

            if (feed != null) {
                val source = context?.getChangeSource(chainFeedSource)
                if (accountManager.hasAccount()) {

                    spider.manuallyEvent(SpiderEventNames.Player.END_PLAY)
                            .put("source", source ?: "")
                            .put("masterID", feed.masterFeedId ?: "")
                            .put("contentID", feed.id)
                            .put("contentDuration", videoTexture.duration.toString())
                            .put("userID", accountManager.account()?.userId
                                    ?: 0).track()
                } else {
                    spider.manuallyEvent(SpiderEventNames.Player.END_PLAY)
                            .put("source", source ?: "")
                            .put("masterID", feed.masterFeedId ?: "")
                            .put("contentID", feed.id)
                            .put("contentDuration", videoTexture.duration.toString()).track()
                }

                SensorsDataAPI.sharedInstance()
                        .track("end_play", JSONObject()
                                .put("feed_id", feed.id)
                                .put("masterID", feed.masterFeedId ?: "")
                                .put("duration", videoTexture.duration.toString())
                                .put("source", source))

                if (BuildConfig.DEBUG) {
                    //showLongToast("视频播放完成 title = \r\n《${feed.title}》 \r\n 打点 ${SpiderEventNames.END_PLAY}")
                    Timber.e("视频播放完成 title = ${feed.title} ")
                }
            }
        }

        var bufferBeginTimeMillis = 0L
        var isBufferBeigin = false

        override fun onInfo(what: Int, extra: Int) {
            super.onInfo(what, extra)
            when (what) {
                PLOnInfoListener.MEDIA_INFO_VIDEO_RENDERING_START -> {

                    val playFeed = videoTexture.playFeed
                    playFeed?.let {

                        val source = videoTexture.context.getChangeSource(chainFeedSource)
                        if (accountManager.hasAccount()) {
                            spider.manuallyEvent(SpiderEventNames.Player.START_PLAY).put("userID", accountManager.account()?.userId
                                    ?: 0)
                                    .put("source", source)
                                    .put("contentID", it.id)
                                    .put("channelId", it.channelId ?: "")
                                    .put("contentDuration", videoTexture.duration)
                                    .track()
                        } else {
                            spider.manuallyEvent(SpiderEventNames.Player.START_PLAY)
                                    .put("source", source)
                                    .put("channelId", it.channelId ?: "")
                                    .put("contentID", it.id)
                                    .put("contentDuration", videoTexture.duration)
                                    .track()
                        }

                        SensorsDataAPI.sharedInstance()
                                .track("start_play_video", JSONObject()
                                        .put("feed_id", it.id)
                                        .put("duration", videoTexture.duration)
                                        .put("source", source))
                    }

                    startPosition = videoTexture.currentPosition
                }
                PLOnInfoListener.MEDIA_INFO_BUFFERING_START -> {

                    isBufferBeigin = true

                    bufferBeginTimeMillis = System.currentTimeMillis()
                }
                PLOnInfoListener.MEDIA_INFO_BUFFERING_END -> {
                    if (isBufferBeigin) {
                        isBufferBeigin = false
                        Timber.e("spider -- 视频 MEDIA_INFO_BUFFERING_START $bufferBeginTimeMillis pos = ${videoTexture.currentPosition}")

                        spider.manuallyEvent(SpiderEventNames.Player.VIDEO_STAND_STILL)
                                .put("CatchRes", "NetWork")
                                .put("CatchPosition", videoTexture.currentPosition)
                                .put("CatchTime", System.currentTimeMillis() - bufferBeginTimeMillis)
                                .track()
                    }
                }
            }
        }

        override fun onStopPlayback() {
            super.onStopPlayback()
            val playFeed = videoTexture.playFeed

            if (playFeed != null) {
                val endPosition = if (videoTexture.currentPosition == 0L) videoTexture.duration else videoTexture.currentPosition
                val source = requireActivity().getChangeSource(chainFeedSource)
                val tracker: EventCreator = spider.manuallyEvent(SpiderEventNames.Player.PAUSE_PLAY)
                        .put("source", source)
                        .put("contentID", playFeed.id)
                        .put("contentDuration", videoTexture.duration.toString())
                        .put("startPosition", 0)
                        .put("endPosition", endPosition)
                        .put("PauseType", "Automatic")


                if (accountManager.hasAccount()) {
                    tracker.put("userID", accountManager.account()?.userId ?: 0)
                }

                SensorsDataAPI.sharedInstance()
                        .track("pause_play", JSONObject()
                                .put("feed_id", playFeed.id)
                                .put("duration", videoTexture.duration.toString())
                                .put("source", source)
                                .put("s_positon", 0)
                                .put("e_position", endPosition)
                                .put("p_type", "Automatic"))

                tracker.track()
            }
        }

        override fun onError(errorCode: Int): Boolean {

            val feed = videoTexture.playFeed
            if (feed != null) {
                if (accountManager.hasAccount()) {
                    spider.manuallyEvent(SpiderEventNames.Player.VIDEO_LOAD_FAILED)
                            .put("sm_id", SmAntiFraud.getDeviceId())
                            .put("errorCode", errorCode)
                            .put("url", feed)
                            .put("feedID", feed.id)
                            .put("userID", accountManager.account()?.userId ?: "")
                            .track()
                } else {
                    spider.manuallyEvent(SpiderEventNames.Player.VIDEO_LOAD_FAILED)
                            .put("sm_id", SmAntiFraud.getDeviceId())
                            .put("errorCode", errorCode)
                            .put("url", feed)
                            .put("feedID", feed.id)
                            .track()
                }

            }
            return false
        }


    }


    var isLoginResult = false
    var before: Account? = null
    var showChestHandler = Handler()

    override fun onAccountChanged(before: Account?, after: Account?) {
        this.before = before
        this.isLoginResult = this.accountManager.hasAccount() && before == null
    }


    private fun onShowChestWindow() {
        if (!isLoginResult) {
            return
        }

        if (accountManager.hasAccount() && before == null) {
            showChestHandler.postDelayed({
                mFrameChestFloat?.visibility = View.GONE
                mChestHelper?.showResultCallOpen(accountManager, activity, activity?.window?.decorView
                        ?: view, mAcceptGiftListener)
                isLoginResult = false
            }, 500)

        }
    }

    private fun showVipHomeExploreDialogInNeeded() {

        if (intentParam?.feedSource != ChainFeedSource.VIP_HOME) {
            return
        }
        activity?.let {
            videoTexture.hostPause()
            VideoHallExploreDialog.show(it, object : VideoHallExploreDialog.OnVideoHallExploreListener {
                override fun onOnVideoHallExplore() {
                    videoTexture.hostResume()
                }
            })
        }
    }

    private fun showPlayEndDialogIfNeeded(position: Int) {
        if (intentParam?.feedSource != ChainFeedSource.VIP_HOME) {
            return
        }

        if (specialFeedListAdapter.data.size - 1 == position) {
            val feed = specialFeedListAdapter.getItem(position)
            feed?.let {
                if (feed.channelId != null && feed.channelName != null && feed.channelIcon != null) {
                    VideoHallPlayEndDialog.show(activity!!, feed.channelId!!, feed.channelName!!, feed.channelIcon!!)
                }
            }
        }

    }

    private fun checkHasWaitActionToFinish() {
        val chainCenterView = specialFeedListAdapter.findChainCenterView()
        val chainCenterViewAdapter = chainCenterView?.adapter as? VideoChainAdapter
                ?: return
        chainCenterViewAdapter.setFollowViewState()
        chainCenterViewAdapter.setUpViewStatus()
    }


}
