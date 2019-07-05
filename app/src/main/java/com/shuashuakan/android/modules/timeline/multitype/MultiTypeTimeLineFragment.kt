package com.shuashuakan.android.modules.timeline.multitype

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Environment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.util.forEach
import androidx.util.isEmpty
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.shuashuakan.android.FishInjection
import com.shuashuakan.android.R
import com.shuashuakan.android.config.AppConfig
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.data.api.model.home.multitypetimeline.*
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.event.*
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.FeedTransportManager
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.account.activity.LoginActivity
import com.shuashuakan.android.modules.discovery.ItemDataPair
import com.shuashuakan.android.modules.player.VideoPlayer
import com.shuashuakan.android.modules.player.VideoPlayerManager
import com.shuashuakan.android.modules.player.activity.VideoPlayActivity
import com.shuashuakan.android.modules.publisher.isStoragePermission
import com.shuashuakan.android.modules.share.ShareHelper
import com.shuashuakan.android.modules.timeline.view.TimeLineEmptyHeaderView
import com.shuashuakan.android.modules.timeline.view.TimeLineLoginHeaderView
import com.shuashuakan.android.modules.timeline.vm.MutitypeTimeLineViewModel
import com.shuashuakan.android.modules.widget.MultiTypeTimeLinePlayerView
import com.shuashuakan.android.modules.widget.OnPlayChangeListener
import com.shuashuakan.android.modules.widget.SpiderAction
import com.shuashuakan.android.modules.widget.dialogs.DownloadProgressDialog
import com.shuashuakan.android.modules.widget.loadmoreview.SskLoadMoreView
import com.shuashuakan.android.spider.Spider
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.ui.base.FishFragment
import com.shuashuakan.android.utils.*
import com.shuashuakan.android.utils.download.DownloadManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * 多 Type 类型的 TimeLine 页面
 *
 * Author: ZhaiDongyang
 * Date: 2019/2/25
 */
class MultiTypeTimeLineFragment : FishFragment(),
        MultiTypeTimeLineApiView<MultiTypeTimeLineModel>,
        SwipeRefreshLayout.OnRefreshListener,
        BaseQuickAdapter.RequestLoadMoreListener {

    companion object {
        private const val ENTER_TYPE = "enter_type"
        const val DATA_TYPE_NOT_LOGIN = "NOT_LOGIN"
        const val DATA_TYPE_UN_FOLLOW = "NOT_FOLLOW"
        const val DATA_TYPE_FOLLOW = "FOLLOW"


        fun create(enterType: String): MultiTypeTimeLineFragment {
            val fragment = MultiTypeTimeLineFragment()
            val bundle = Bundle()
            bundle.putString(ENTER_TYPE, enterType)
            fragment.arguments = bundle
            return fragment
        }
    }

    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var shareHelper: ShareHelper
    @Inject
    lateinit var spider: Spider

    @Inject
    lateinit var presenterMultiTypeTimeLine: MultiTypeTimeLinePresenter
    @Inject
    lateinit var appConfig: AppConfig
    @Inject
    lateinit var accountManager: AccountManager

    private var player: VideoPlayer? = null
    private val compositeDisposable = CompositeDisposable()
    private val swipeRefreshLayout by bindView<SwipeRefreshLayout>(R.id.multitype_timeline_swiperefreshlayout)
    private val recyclerView by bindView<RecyclerView>(R.id.multitype_timeline_recyclerview)
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var errorView: View
    private lateinit var adapterMultiTypeTimeLine: MultiTypeTimeLineAdapter
    private lateinit var downloadFeedId: String
    private var firstItem = 0
    private var lastItem = 0
    private var visibleCount = 0
    private var fragmentIsResumeStatus: Boolean = false
    private lateinit var recyclerViewScrollListener: RecyclerView.OnScrollListener
    private lateinit var recyclerViewLayoutChangeListener: View.OnLayoutChangeListener
    private val pageCount = 10 // 页面显示数量
    private var page: Int = 0 // 当前页数
    private var timeLineSize = 0 // 根据此来判断加载更多是否有数据，显示没有更多
    private var nextId: String? = null
    private val sparsArrayCards = SparseArray<CardsType>() // 需要计算的 Cards
    private val sparsArrayTotalCards = SparseArray<CardsType>() // 每次刷新获取的所有 Cards
    private val listFeed = ArrayList<Feed>()
    private val listFeedTotal = ArrayList<Feed>()
    private var startTime: Long = 0L
    private var endTime: Long = 0L // 埋点需要
    private var adapterItemSize = -1
    private var currentVideoItemPlay = -1 // 当前 RecyclerView 中播放的 Position
    private var isHide: Boolean = true
    private var exitOnHiddenChanged = false // 是否是从点击页面上方tab标签出去的
    private var exitPageSelected = false // 是否是从点击页面下方tab标签出现的
    private var loginTopViewView: TimeLineLoginHeaderView? = null
    private var emptyTopHeadView: TimeLineEmptyHeaderView? = null

    private var mutitypeTimeLineViewModel: MutitypeTimeLineViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? = inflater.inflate(R.layout.fragment_multitype_timeline, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        spider.pageTracer().reportPageCreated(this)
        spider.pageTracer().reportPageShown(this, "ssr://home?index=${SpiderAction.PersonSource.FOLLOW_TIMELINE.source}", "")
        loginTopViewView = TimeLineLoginHeaderView(requireContext())
        emptyTopHeadView = TimeLineEmptyHeaderView(requireContext())

        mutitypeTimeLineViewModel = ViewModelProviders.of(this).get(MutitypeTimeLineViewModel::class.java)

        setViewMarginTop(requireActivity(), swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener(this)

        adapterMultiTypeTimeLine = MultiTypeTimeLineAdapter(childFragmentManager, apiService, shareHelper, null, mutitypeTimeLineViewModel!!)
        adapterMultiTypeTimeLine.setLoadMoreView(SskLoadMoreView(SskLoadMoreView.IMAGE))
        adapterMultiTypeTimeLine.setOnLoadMoreListener(this, recyclerView)
        mLayoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = mLayoutManager
        recyclerView.adapter = adapterMultiTypeTimeLine
        errorView = layoutInflater.inflate(R.layout.profile_timeline_errorview, recyclerView.parent as ViewGroup, false)

        addListener()
        addRxBusEvent()
        initData()
        initObservable()
    }


    private fun initObservable() {
        mutitypeTimeLineViewModel?.followUserLiveData!!.observe(this, Observer<FeedFollowChangeEvent> {
            it?.let { feed ->
                compositeDisposable.add(Observable.timer(200, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io()).observeOn(
                                AndroidSchedulers.mainThread()).subscribe {
                            updateFollowOrUnFollowStatus(feed)
                        })
            }
        })
    }

    private fun addListener() {
        recyclerView.setRecyclerListener {
            if (recyclerView.adapter is MultiTypeTimeLineAdapter) {
                val playerView = it.itemView.findViewById<MultiTypeTimeLinePlayerView>(R.id.player_view)
                if (playerView != null) {
                    val player = playerView.getPlayer()
                    if (player == VideoPlayerManager.instance().getCurrentVideoPlayer(adapterMultiTypeTimeLine.uuid)) {
                        VideoPlayerManager.instance().releaseVideoPlayer(adapterMultiTypeTimeLine.uuid)
                    }
                }
            }
        }
        errorView.setOnClickListener {
            swipeRefreshLayout.isRefreshing = true
            initData()
        }

        adapterMultiTypeTimeLine.listener = object : MultiTypeTimeLineAdapter.OnAdapterPlayerViewClickListener {
            override fun onAdapterPlayerViewClickListener(position: Int) {
                val itemData = adapterMultiTypeTimeLine.getItem(position) as ItemDataPair
                val timeLineFeedTypeModel = itemData.data as Feed
                timeLineFeedTypeModel.avatar = timeLineFeedTypeModel.author?.avatar
                val intentParam = FeedTransportManager.jumpToVideoFromFollow(timeLineFeedTypeModel, listFeedTotal.toList(), nextId)
                startActivity(VideoPlayActivity.create(requireContext(), intentParam))
            }
        }
        adapterMultiTypeTimeLine.setOnItemChildClickListener { _, view, _ ->
            when (view.id) {
                R.id.topic_channel_subscribe_tv -> {
                    var tag = view.tag
                    tag?.let { feedTag ->
                        if (feedTag is Feed) {
                            if (accountManager.hasAccount()) {
                                mutitypeTimeLineViewModel?.followOrUnFollowUser(apiService, feedTag.userId.toString(), feedTag.hasFollowUser!!) {
                                    (view as com.shuashuakan.android.modules.widget.FollowTextView).followSuccessGone()
                                }
                            } else {
                                mutitypeTimeLineViewModel?.mFollowUserWithLoginCache = feedTag
                                LoginActivity.launch(requireContext())
                            }
                        }
                    }
                }
            }
        }

        recyclerViewScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (recyclerView.adapter is MultiTypeTimeLineAdapter) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        autoPlayVideo()?.startPlay()
                    }
                }
            }
        }

        recyclerViewLayoutChangeListener = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            if (recyclerView.adapter is MultiTypeTimeLineAdapter) {
                findItem()
                autoPlayVideo()?.startPlay()
                val currentCompletelyVisibleItem = mLayoutManager.findFirstCompletelyVisibleItemPosition()
                getCurrentItemViewData(currentCompletelyVisibleItem)
            }
        }
        addVideoPlayListener()
    }

    private fun addRxBusEvent() {
        RxBus.get().toFlowable().subscribe {
            when (it) {
                is FeedCommentDismissEvent -> {
                    if (fragmentIsResumeStatus) {
                        VideoPlayerManager.instance().resumeVideoPlayer(adapterMultiTypeTimeLine.uuid)
                    }
                }
                is DownloadVideoEvent -> {
                    if (it.tag == DownloadManager.DOWNLOAD_TAG_FRAGMENT_MULTITYPE) {
                        downloadFeedId = it.feedId
                        downloadVideo(it.feedId)
                    }
                }
                is SaveVideoEvent -> {
                    if (::downloadFeedId.isInitialized) {
                        if (it.isSuccess) {
                            spider.shareDetailsEvent(requireContext(), downloadFeedId, true, "SaveIntoAlbum")
                        } else {
                            spider.shareDetailsEvent(requireContext(), downloadFeedId, false, "SaveIntoAlbum")
                        }
                    }
                }
                is LoginSuccessEvent -> {
                    // 登录回来是否请求关注接口
                    adapterMultiTypeTimeLine.loginAction()
                    mutitypeTimeLineViewModel?.loginEventFollow(apiService)
                    loginTopViewView?.let { loginView ->
                        loginView.updateLoginView(true)
                    }
                }
                is PauseMultiTypeTimelineVideoEvent -> {
                    if (it.isPlay) {
                        if (!exitOnHiddenChanged) {
                            startPlay()
                        }
                    } else {
                        exitPageSelected = true
                        if (!exitOnHiddenChanged) {
                            pausePlay()
                        }
                    }
                }
                is FeedFollowChangeEvent -> {
                    updateFollowOrUnFollowStatus(it)
                }
                is MoreDialogFollowEvent -> {
                    mutitypeTimeLineViewModel?.targetUserId = it.targetUserId
                }
                is EditVideoSuccessEvent -> {
                    // 修改完视频刷新本地数据，不请求网络
                    if (!::adapterMultiTypeTimeLine.isInitialized) return@subscribe
                    val multiItemEntity = adapterMultiTypeTimeLine.data
                    for ((index, item) in multiItemEntity.withIndex()) {
                        val dataPair = item as ItemDataPair
                        if (dataPair.type == MultiTypeTimeLineAdapter.MULTITYPE_TIMELINE_VIDEO ||
                                dataPair.type == MultiTypeTimeLineAdapter.MULTITYPE_TIMELINE_VIDEO_TOPIC) {
                            val feed = dataPair.data as Feed
                            if (feed.id == it.feedId) {
                                feed.title = it.content
                                if (!it.coverUrl!!.isEmpty()) {
                                    feed.cover = it.coverUrl
                                }
                                feed.properties!!.editInfo!!.canEdit = it.canEdit
                                feed.properties!!.editInfo!!.editableCount = it.editCount - 1
                                adapterMultiTypeTimeLine.notifyItemChanged(index)
                            }
                        }
                    }
                }
                is LoginOutEvent -> {
                    mutitypeTimeLineViewModel?.isNeedRefreshData = true
                }
            }
        }.addTo(compositeDisposable)
    }

    // 更新关注和非关注的状态
    private fun updateFollowOrUnFollowStatus(it: FeedFollowChangeEvent) {
        if (!::adapterMultiTypeTimeLine.isInitialized) return
        val multiItemEntity = adapterMultiTypeTimeLine.data
        for ((index, item) in multiItemEntity.withIndex()) {
            val dataPair = item as ItemDataPair
            when (dataPair.type) {
                MultiTypeTimeLineAdapter.MULTITYPE_TIMELINE_VIDEO,
                MultiTypeTimeLineAdapter.MULTITYPE_TIMELINE_VIDEO_TOPIC -> {
                    val feed = dataPair.data as Feed
                    if (feed.getUserId() == it.userId) {
                        FollowCacheManager.putFollowUserToCache(it.userId, it.state)
                        feed.hasFollowUser = it.state
                        adapterMultiTypeTimeLine.notifyItemChanged(index + adapterMultiTypeTimeLine.headerLayoutCount)
                    }
                }

                MultiTypeTimeLineAdapter.MULTITYPE_TIMELINE_INTEREST -> {
                    var parentPosition = mutitypeTimeLineViewModel?.mRecommendUserPosition
                    var recyclerView = adapterMultiTypeTimeLine.getViewByPosition(parentPosition!!, R.id.multitype_timeline_interest_rl)
                    recyclerView?.let { recyclerView ->
                        val feed = dataPair.data as RecommendUserContent
                        feed.data?.list?.let { recommendUsers ->
                            for (position in recommendUsers.indices) {
                                var recommendUser = recommendUsers[position]
                                if (recommendUser.user_id!!.toString() == it.userId) {
                                    FollowCacheManager.putFollowUserToCache(it.userId, it.state)
                                    recommendUser.is_follow = it.state
                                    with((recyclerView as RecyclerView).adapter as BaseQuickAdapter<*, *>) {
                                        notifyItemChanged(position)
                                        compositeDisposable.add(Observable.timer(2, TimeUnit.SECONDS)
                                                .subscribeOn(Schedulers.io()).observeOn(
                                                        AndroidSchedulers.mainThread()).subscribe {
                                                    this.data.removeAt(position)
                                                    notifyItemRemoved(position)
                                                    notifyItemRangeChanged(position, data.size)
                                                })

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initData() {
        swipeRefreshLayout.isRefreshing = true
        onRefresh()
    }

    private fun requestData() {
        if (page > 0) {
            if (adapterMultiTypeTimeLine.timelineModel == DATA_TYPE_NOT_LOGIN) {
                return
            }
            presenterMultiTypeTimeLine.requestApi(nextId, count = pageCount)
        } else {
            presenterMultiTypeTimeLine.requestApi(count = pageCount)
        }
    }

    override fun onRefresh() {
        page = 0
        listFeed.clear()
        listFeedTotal.clear()
        sparsArrayCards.clear()
        sparsArrayTotalCards.clear()
        requestData()
        if (mLayoutManager.findFirstVisibleItemPosition() != 0) {
            recyclerView.scrollToPosition(0)
        }
    }

    override fun onLoadMoreRequested() {
        requestData()
    }

    override fun showMultiTypeTimeLineData(data: MultiTypeTimeLineModel) {
        adapterMultiTypeTimeLine.timelineModel = data.source ?: ""
        // 两个接口获取数据后合并
        if (page == 0 && swipeRefreshLayout.isRefreshing && data.timeline?.isNotEmpty()!!) {
            // 只有在刷新的时候才请求 Cards 数据
            getCardsData(data)
        } else {
            showData(data)
        }
    }

    private fun getCardsData(data: MultiTypeTimeLineModel) {
        apiService.getMultiTypeCardsData()
                .applySchedulers()
                .subscribeApi(onNext = {
                    it.forEachIndexed { _: Int, cards: CardsType ->
                        if (cards is SubscribedChannelContent) {
                            sparsArrayTotalCards.put(cards.index!!, cards)
                        }
                        if (cards is TimeLineContent) {
                            sparsArrayTotalCards.put(cards.index!!, cards)
                        }
                        if (cards is RecommendUserContent) {
                            sparsArrayTotalCards.put(cards.index!!, cards)
                        }
                        if (cards is UserLeaderBoard) {
                            sparsArrayTotalCards.put(cards.index!!, cards)
                        }
                        if (cards is RecommendChannelContent) {
                            sparsArrayTotalCards.put(cards.index!!, cards)
                        }
                        if (cards is FollowUserContent) {
                            sparsArrayTotalCards.put(cards.index!!, cards)
                        }
                        if (cards is RecommendUserFeedContent) {
                            sparsArrayTotalCards.put(cards.index!!, cards)
                        }
                    }
                    showData(data)
                }, onApiError = {
                    Timber.e("获取 Cards 失败 $it")
                })
    }

    private fun showData(data: MultiTypeTimeLineModel) {
        addHeadNeedShow(data)
        val list = recombineData(data)
        if (list.isNotEmpty()) {
            if (data.cursor != null && data.cursor!!.next_id != null) {
                nextId = data.cursor!!.next_id!!
            }
            page++
        }
        if (swipeRefreshLayout.isRefreshing) {
            swipeRefreshLayout.isRefreshing = false
            adapterMultiTypeTimeLine.setNewData(list)
        } else {
            adapterMultiTypeTimeLine.addData(list)
        }

        adapterItemSize = adapterMultiTypeTimeLine.data.size
        if (timeLineSize == 0) {// 加载更多的策略是如果返回数据为空则为没有更多了，如果小于或等于pagecount则可以继续加载
            adapterMultiTypeTimeLine.loadMoreEnd(false)
        } else if (timeLineSize <= pageCount) {
            adapterMultiTypeTimeLine.setEnableLoadMore(true)
            adapterMultiTypeTimeLine.loadMoreComplete()
        }
    }

    private fun addHeadNeedShow(data: MultiTypeTimeLineModel) {
        when (data.source) {
            DATA_TYPE_NOT_LOGIN -> {
                adapterMultiTypeTimeLine.removeAllHeaderView()
                adapterMultiTypeTimeLine.addHeaderView(loginTopViewView)
                loginTopViewView?.updateLoginView(false)
            }
            DATA_TYPE_UN_FOLLOW -> {
                adapterMultiTypeTimeLine.removeAllHeaderView()
                adapterMultiTypeTimeLine.addHeaderView(emptyTopHeadView)
            }
            DATA_TYPE_FOLLOW -> {
                adapterMultiTypeTimeLine.removeAllHeaderView()
            }
            else -> {
            }
        }
    }

    /**
     * 把 cards 里的数据插入到 timeline 中，其中的位置也要和 timeline 中的一致
     */
    private fun recombineData(data: MultiTypeTimeLineModel): MutableList<MultiItemEntity> {
        val listAdapter: MutableList<MultiItemEntity> = mutableListOf()
        listFeed.clear()
        if (data.timeline != null && data.timeline!!.isNotEmpty()) {
            timeLineSize = data.timeline!!.size
            listFeed.addAll(data.timeline!!)
        } else {
            timeLineSize = 0
            return listAdapter
        }
        // 当是第一页时，取出 sparsArrayCards 中第一页的数据，第二页时取出第二页数据，角标减去page*pageCount
        sparsArrayCards.clear()
        sparsArrayTotalCards.forEach { key, value ->
            if (key >= page * pageCount && key < pageCount * (page + 1)) {
                sparsArrayCards.put(key - (page * pageCount), value)
            }
        }
        for (indexExtra in 0 until sparsArrayCards.size()) {
            val keyCards = sparsArrayCards.keyAt(indexExtra)
            val valueCards = sparsArrayCards[keyCards]
            for (indexFeed in 0 until listFeed.size) {
                if (indexFeed == keyCards) { // 要插入的位置
                    if (valueCards is RecommendUserContent) {
                        if (valueCards.data != null && valueCards.data!!.list != null && !valueCards.data!!.list!!.isEmpty()) {
                            listAdapter.add(ItemDataPair(valueCards, MultiTypeTimeLineAdapter.MULTITYPE_TIMELINE_INTEREST))
                        }
                    }

                    if (valueCards is FollowUserContent) {
                        if (valueCards.data != null && valueCards.data!!.list != null && !valueCards.data!!.list!!.isEmpty()) {
                            listAdapter.add(ItemDataPair(valueCards, MultiTypeTimeLineAdapter.MULTITYPE_TIMELINE_FOLLOW_USER))
                        }
                    }

                    if (valueCards is RecommendUserFeedContent) {
                        if (valueCards.data != null && valueCards.data!!.list != null && !valueCards.data!!.list!!.isEmpty()) {
                            listAdapter.add(ItemDataPair(valueCards, MultiTypeTimeLineAdapter.MULTITYPE_TIMELINE_RECOMMEN_USER_FEED))
                        }
                    }

                    if (valueCards is UserLeaderBoard) {
                        if (valueCards.data != null && valueCards.data!!.list != null && !valueCards.data!!.list!!.isEmpty()) {
                            listAdapter.add(ItemDataPair(valueCards, MultiTypeTimeLineAdapter.MULTITYPE_TIMELINE_USER_LEADRER_BOARD))
                        }
                    }

                    if (valueCards is SubscribedChannelContent) {
                        if (valueCards.data != null && valueCards.data!!.list != null && !valueCards.data!!.list!!.isEmpty()) {
                            listAdapter.add(ItemDataPair(valueCards, MultiTypeTimeLineAdapter.MULTITYPE_TIMELINE_SUBSCRIBED_TOPIC))
                        }
                    }
                    if (valueCards is RecommendChannelContent) {
                        if (valueCards.data != null && valueCards.data!!.list != null && !valueCards.data!!.list!!.isEmpty()) {
                            listAdapter.add(ItemDataPair(valueCards, MultiTypeTimeLineAdapter.MULTITYPE_TIMELINE_RECOMMEND_TOPIC))
                        }
                    }
                    if (valueCards is TimeLineContent) {
                        if (valueCards.data != null && valueCards.data!!.list != null && !valueCards.data!!.list!!.isEmpty()) {
                            listAdapter.add(ItemDataPair(valueCards.data!!.list!![0], MultiTypeTimeLineAdapter.MULTITYPE_TIMELINE_VIDEO_TOPIC))
                            listFeedTotal.add(valueCards.data!!.list!![0])
                        }
                    }
                    if (((indexExtra + 1) < sparsArrayCards.size() && indexFeed < sparsArrayCards.keyAt(indexExtra + 1))
                            || (indexExtra + 1) == sparsArrayCards.size()) {
                        listAdapter.add(ItemDataPair(listFeed[indexFeed], MultiTypeTimeLineAdapter.MULTITYPE_TIMELINE_VIDEO))
                        listFeedTotal.add(listFeed[indexFeed])
                    }
                } else if (indexExtra == sparsArrayCards.size() - 1 && indexFeed > keyCards) {
                    listAdapter.add(ItemDataPair(listFeed[indexFeed], MultiTypeTimeLineAdapter.MULTITYPE_TIMELINE_VIDEO))
                    listFeedTotal.add(listFeed[indexFeed])
                } else if (indexFeed > keyCards && (indexExtra + 1) < sparsArrayCards.size()
                        && indexFeed < sparsArrayCards.keyAt(indexExtra + 1)) {
                    listAdapter.add(ItemDataPair(listFeed[indexFeed], MultiTypeTimeLineAdapter.MULTITYPE_TIMELINE_VIDEO))
                    listFeedTotal.add(listFeed[indexFeed])
                } else if (indexFeed < keyCards && indexFeed == listAdapter.size) {
                    listAdapter.add(ItemDataPair(listFeed[indexFeed], MultiTypeTimeLineAdapter.MULTITYPE_TIMELINE_VIDEO))
                    listFeedTotal.add(listFeed[indexFeed])
                }
            }
        }
        if (sparsArrayCards.isEmpty()) {
            for (indexFeed in 0 until listFeed.size) {
                listAdapter.add(ItemDataPair(listFeed[indexFeed], MultiTypeTimeLineAdapter.MULTITYPE_TIMELINE_VIDEO))
                listFeedTotal.add(listFeed[indexFeed])
            }
        }
        return listAdapter
    }

    private fun addVideoPlayListener() {
        recyclerView.addOnScrollListener(recyclerViewScrollListener)
        recyclerView.addOnLayoutChangeListener(recyclerViewLayoutChangeListener)
    }

    override fun showError() {
        if (swipeRefreshLayout.isRefreshing) {
            swipeRefreshLayout.isRefreshing = false
            if (recyclerView.adapter !is MultiTypeTimeLineAdapter) {
                recyclerView.adapter = adapterMultiTypeTimeLine
                adapterMultiTypeTimeLine.emptyView = errorView
            }
        } else {
            adapterMultiTypeTimeLine.loadMoreFail()
        }
    }

    override fun showMessage(message: String) {
        toastCustomText(requireContext(), message)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        FishInjection.inject(this)
        presenterMultiTypeTimeLine.attachView(this)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (::adapterMultiTypeTimeLine.isInitialized) {
            if (isVisibleToUser) {
                startPlay()
                if (mutitypeTimeLineViewModel?.isNeedRefreshData!!) {
                    mutitypeTimeLineViewModel?.isNeedRefreshData = false
                    initData()
                }
            } else pausePlay()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            exitOnHiddenChanged = false
            startPlay()
            startTime = System.currentTimeMillis()
        } else {
            exitOnHiddenChanged = true
            pausePlay()
            endTime = System.currentTimeMillis()
//      spider.followTimeLineEvent(requireContext(), SpiderEventNames.FOLLOW_TIMELINE_PAGE_DURATION, duration = endTime - startTime)
        }
    }

    override fun onPause() {
        super.onPause()
        endTime = System.currentTimeMillis()
//    spider.followTimeLineEvent(requireContext(), SpiderEventNames.FOLLOW_TIMELINE_PAGE_DURATION, duration = endTime - startTime)
        fragmentIsResumeStatus = false
        pausePlay()
    }

    override fun onResume() {
        super.onResume()
        startTime = System.currentTimeMillis()
        fragmentIsResumeStatus = true
        if (!exitOnHiddenChanged && !exitPageSelected) {
            startPlay()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        VideoPlayerManager.instance().releaseVideoPlayer(adapterMultiTypeTimeLine.uuid)
        compositeDisposable.clear()
        removeVideoPlayListener()
    }

    private fun removeVideoPlayListener() {
        recyclerView.removeOnScrollListener(recyclerViewScrollListener)
        recyclerView.removeOnLayoutChangeListener(recyclerViewLayoutChangeListener)
    }

    override fun onDetach() {
        super.onDetach()
        presenterMultiTypeTimeLine.detachView(false)
    }

    private fun findItem() {
        firstItem = mLayoutManager.findFirstVisibleItemPosition()
        lastItem = mLayoutManager.findLastVisibleItemPosition()
        visibleCount = lastItem - firstItem
    }

    private fun autoPlayVideo(): MultiTypeTimeLinePlayerView? {
        for (i in 0..visibleCount) {
            val childView = recyclerView.getChildAt(i) ?: continue
            val playerView: MultiTypeTimeLinePlayerView? = (recyclerView.getChildViewHolder(childView)
                    as BaseViewHolder).getView(R.id.player_view) ?: continue
            if (isHide) continue
            val rect = Rect()
            playerView!!.getLocalVisibleRect(rect)
            val videoHeight = playerView.height//获取视频的高度
            if (rect.top != 0 || rect.bottom != videoHeight) continue
            val position = recyclerView.getChildAdapterPosition(childView)
            if (position == -1 || position > adapterMultiTypeTimeLine.data.size) continue
            if (playerView.controllerTimeLine.playChangeListener == null) {
                playerView.controllerTimeLine.playChangeListener = playChangeListener
            }
            return playerView
        }
        return null
    }

    private fun startPlay() {
        isHide = false
        VideoPlayerManager.instance().resumeVideoPlayer(adapterMultiTypeTimeLine.uuid)
    }

    private fun pausePlay() {
        isHide = true
        VideoPlayerManager.instance().suspendVideoPlayer(adapterMultiTypeTimeLine.uuid)
    }


    private fun downloadVideo(feedId: String) {
        if (isStoragePermission()) {
            toastCustomText(requireContext(), getString(R.string.string_open_file_write))
            return
        }
        val path = Environment.getExternalStorageDirectory().path + "/DCIM/Camera/$feedId.mp4"
        val file = File(path)
        val downloadProgressDialog: DownloadProgressDialog =
                DownloadProgressDialog.progressDialog(requireContext(), false)
        if (!file.exists()) {
            downloadProgressDialog.show()
            downloadProgressDialog.setProgressBarVisibility(getString(R.string.string_loading))
            apiService.getWatermarkUrl(feedId)
                    .applySchedulers()
                    .subscribeApi(onNext = {
                        downloadProgressDialog.setCircleProgressBarPercentVisibility(getString(R.string.string_downloading))
                        val downloadManager = DownloadManager(requireContext(), path, it.downloadUrl, downloadProgressDialog, feedId)
                        downloadManager.startTask()
                    }, onApiError = {
                        RxBus.get().post(SaveVideoEvent(false, feedId))
                        toastCustomText(requireContext(), getString(R.string.string_video_download_error_with_copyright))
                        downloadProgressDialog.dismiss()
                    })
        } else {
            toastCustomText(requireContext(), String.format(getString(R.string.string_video_save_format), "DCIM/Camera"))
        }
    }

    private val playChangeListener = object : OnPlayChangeListener {
        //视频结束
        override fun onRepeatPlay(player: VideoPlayer) {
            this@MultiTypeTimeLineFragment.player = player
            player.restart()
        }

        //视频快进快退
        override fun onSeekChange(player: VideoPlayer, startPos: Long, endPos: Long) {
            this@MultiTypeTimeLineFragment.player = player
        }

        //视频卡顿
        override fun onVideoStandStill(player: VideoPlayer, catchPosition: Long, catchTime: Long) {
            this@MultiTypeTimeLineFragment.player = player
        }

        //视频暂停
        override fun onVideoPauseRecord(player: VideoPlayer, pointStart: Long, pointStop: Long, isActive: Boolean) {
            this@MultiTypeTimeLineFragment.player = player
        }

        override fun onVideoLoadError(url: String?, errorCode: Int) {
        }
    }

    private fun getCurrentItemViewData(currentItem: Int) {
        if (currentItem != currentVideoItemPlay && currentItem >= 0 && currentItem < adapterItemSize) {
            currentVideoItemPlay = currentItem
            val adapterTemp = recyclerView.adapter
            if (adapterTemp is MultiTypeTimeLineAdapter) {
                val multiItemEntity = adapterTemp.data[currentItem]
                if (multiItemEntity is ItemDataPair) {
                    val dataTemp = multiItemEntity.data
                    when (multiItemEntity.type) {
                        MultiTypeTimeLineAdapter.MULTITYPE_TIMELINE_VIDEO -> {
                            if (dataTemp is Feed) {
                                spider.followTimeLineEvent(requireContext(),
                                        SpiderEventNames.FOLLOW_TIMELINE_FEED_PLAY,
                                        feedID = dataTemp.id, type = "fromUser")
                            }
                        }
                        MultiTypeTimeLineAdapter.MULTITYPE_TIMELINE_VIDEO_TOPIC -> {
                            if (dataTemp is Feed) {
                                spider.followTimeLineEvent(requireContext(),
                                        SpiderEventNames.FOLLOW_TIMELINE_FEED_PLAY,
                                        feedID = dataTemp.id, type = "fromChannel")
                            }
                        }
                    }
                }
            }
        }
    }
}