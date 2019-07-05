package com.shuashuakan.android.modules.timeline.profile

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Environment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.shuashuakan.android.FishInjection
import com.shuashuakan.android.R
import com.shuashuakan.android.config.AppConfig
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.account.Account
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.data.api.model.home.TopicTimeLineModel
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.event.*
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.ACCOUNT_PAGE
import com.shuashuakan.android.modules.FeedTransportManager
import com.shuashuakan.android.modules.JOIN_PAGE
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.player.VideoPlayer
import com.shuashuakan.android.modules.player.VideoPlayerManager
import com.shuashuakan.android.modules.player.activity.VideoPlayActivity
import com.shuashuakan.android.modules.publisher.PermissionRequestFragment
import com.shuashuakan.android.modules.publisher.isStoragePermission
import com.shuashuakan.android.modules.share.ShareHelper
import com.shuashuakan.android.modules.timeline.multitype.MultiTypeTimeLineAdapter
import com.shuashuakan.android.modules.timeline.vm.MutitypeTimeLineViewModel
import com.shuashuakan.android.modules.widget.EmptyView
import com.shuashuakan.android.modules.widget.OnPlayChangeListener
import com.shuashuakan.android.modules.widget.TimeLinePlayerView
import com.shuashuakan.android.modules.widget.dialogs.DownloadProgressDialog
import com.shuashuakan.android.modules.widget.loadmoreview.SskLoadMoreView
import com.shuashuakan.android.service.PullService
import com.shuashuakan.android.spider.Spider
import com.shuashuakan.android.ui.base.FishFragment
import com.shuashuakan.android.utils.*
import com.shuashuakan.android.utils.download.DownloadManager
import com.shuashuakan.android.utils.extension.showShortToast
import com.shuashuakan.android.widget.GridDivider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * 个人页面的 TimeLine
 *
 * Author: ZhaiDongyang
 * Date: 2019/1/15
 */
class ProfileTimeLineFragment :
        FishFragment(),
        ProfileTimeLineApiView<List<Feed>>,
        TopicTimeLineApiView<TopicTimeLineModel>,
        AccountManager.AccountChangedListener, OnPlayChangeListener,
        SwipeRefreshLayout.OnRefreshListener,
        BaseQuickAdapter.RequestLoadMoreListener {

    @Inject
    lateinit var exoPlayerHelper: com.shuashuakan.android.modules.player.ExoPlayerHelper

    @Inject
    lateinit var appConfig: AppConfig

    @Inject
    lateinit var timeLinePresenter: TopicTimeLinePresenter
    @Inject
    lateinit var profileTimeLinePresenter: ProfileTimeLinePresenter


    @Inject
    lateinit var accountManager: AccountManager

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var shareHelper: ShareHelper

    private val compositeDisposable = CompositeDisposable()

    private lateinit var profileTimelineAdapter: ProfileTimelineAdapter

    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var mGridLayoutManager: GridLayoutManager
    private lateinit var timeLineViewModel: MutitypeTimeLineViewModel

//  private val emptyView by bindView<EmptyView>(R.id.profile_timeline_empty_view)

    private val swipeRefreshLayout by bindView<SwipeRefreshLayout>(R.id.profile_timeline_swiperefreshlayout)

    private val recyclerView by bindView<RecyclerView>(R.id.profile_timeline_recyclerview)

    private lateinit var errorView: View

    private var firstItem = 0
    private var LastItem = 0
    private var VisiableCount = 0

    private var page: Int = 0

    private var isHide: Boolean = true

    // 页面是网格还是列表，默认是列表形式，如果默认是网格还需要更改点击事件的isList
    private var isList: Boolean = true
    // 切换网格或者列表始终是true，刷新或者加载更多始终是false
    private var isSwitchList: Boolean = true

    private var nextId: String? = null
    private var hasMore: Boolean = false

    @Inject
    lateinit var spider: Spider

    private var mChannelId: Long = 0L // 没值就是个人页面的动态
    private lateinit var userId: String
    private var isMine: Boolean = false

    val parentList: MutableList<Feed> = mutableListOf()

    private var player: VideoPlayer? = null

    private var fragmentIsResumeStatus: Boolean = false

    private lateinit var emptyView: View

    private lateinit var downloadFeedId: String

    companion object {
        private const val EXTRA_CHANNEL_ID = "extra_channel_id"
        private const val EXTRA_USER_ID = "extra_user_id"
        private const val EXTRA_IS_MINE = "extra_is_mine"

        private const val PAGE_MOST_COUNT = 10
        private const val REQUEST_LOGIN_FOR_UPLOAD = 3

        fun create(userId: String, isMine: Boolean): ProfileTimeLineFragment {
            val fragment = ProfileTimeLineFragment()
            val argument = Bundle()
            argument.putString(EXTRA_USER_ID, userId)
            argument.putBoolean(EXTRA_IS_MINE, isMine)
            fragment.arguments = argument
            return fragment
        }

        fun create(channelId: Long): ProfileTimeLineFragment {
            val fragment = ProfileTimeLineFragment()
            val argument = Bundle()
            argument.putLong(EXTRA_CHANNEL_ID, channelId)
            fragment.arguments = argument
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_profile_timeline, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        timeLineViewModel = ViewModelProviders.of(this).get(MutitypeTimeLineViewModel::class.java)
        initObservable()
        mChannelId = arguments?.getLong(EXTRA_CHANNEL_ID) ?: 0L
        userId = arguments?.getString(EXTRA_USER_ID) ?: ""
        isMine = arguments?.getBoolean(EXTRA_IS_MINE, false) ?: false
        swipeRefreshLayout.setOnRefreshListener(this)
        swipeRefreshLayout.isEnabled = false
        mLayoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = mLayoutManager
        mGridLayoutManager = GridLayoutManager(requireContext(), 3)
        accountManager.addAccountChangedListener(this)
        recyclerView.setRecyclerListener {
            val playerView = it.itemView.findViewById<TimeLinePlayerView>(R.id.player_view)
            if (playerView != null) {
                val player = playerView.getPlayer()
                if (player == VideoPlayerManager.instance().getCurrentVideoPlayer(profileTimelineAdapter.uuid)) {
                    VideoPlayerManager.instance().releaseVideoPlayer(profileTimelineAdapter.uuid)
                }
            }
        }
        profileTimelineAdapter = ProfileTimelineAdapter(childFragmentManager,
                accountManager, apiService, shareHelper,
                mChannelId, userId, null)
        errorView = layoutInflater.inflate(R.layout.profile_timeline_errorview, recyclerView.parent as ViewGroup, false)
        errorView.setOnClickListener {
            initData()
            swipeRefreshLayout.isRefreshing = true
        }
        recyclerView.adapter = profileTimelineAdapter
        profileTimelineAdapter.setLoadMoreView(SskLoadMoreView(SskLoadMoreView.IMAGE))
        profileTimelineAdapter.listener = object : ProfileTimelineAdapter.OnAdapterPlayerViewClickListener {
            override fun onAdapterPlayerViewClickListener(positon: Int) {
                val act = activity
                if (act != null && !act.isFinishing) {
                    if (profileTimelineAdapter.getItem(positon) == null) {
                        return
                    }
                    val itemData = profileTimelineAdapter.getItem(positon) as com.shuashuakan.android.modules.discovery.ItemDataPair
                    val timeLineFeedTypeModel = itemData.data as Feed
                    if (mChannelId == 0L) {

                        val intentParam = FeedTransportManager.jumpToVideoFromPersonalNewest(page, parentList.toList(), timeLineFeedTypeModel)
                        startActivity(VideoPlayActivity.create(act, intentParam, isMine))

                    } else {
                        val intentParam = FeedTransportManager.jumpToVideoFromTopicDetailTimeline(mChannelId, nextId, parentList.toList(), timeLineFeedTypeModel)
                        startActivity(VideoPlayActivity.create(act, intentParam))

                    }
                }

            }
        }
//    emptyView = layoutInflater.inflate(R.layout.profile_timeline_emptyview, recyclerView.parent as ViewGroup, false)
//    val view = emptyView.findViewById<EmptyView>(R.id.empty_view)
        emptyView = layoutInflater.inflate(R.layout.view_new_empty_timeline, recyclerView.parent as ViewGroup, false)
        val view = emptyView.findViewById<EmptyView>(R.id.empty_view)
        if (isMine) {
            view.setIv(R.drawable.icon_guide_publish)
                    .hideTitle()
                    .setContent(if (appConfig.isShowCreateFeed())
                        getString(R.string.string_guide_publish_first)
                    else
                        getString(R.string.string_guide_publish))

                    .setClickBtn(if (appConfig.isShowCreateFeed())
                        getString(R.string.string_publish_video)
                    else
                        getString(R.string.string_guide_publish_up_master)) {

                        if (!accountManager.hasAccount()) {
                            requireContext().startActivityForResultByLink(ACCOUNT_PAGE, REQUEST_LOGIN_FOR_UPLOAD)
                        } else {
                            if (appConfig.isShowCreateFeed()) {
                                goPermissionPage()
                            } else {
                                requireContext().startActivity(JOIN_PAGE)
                            }
                        }
                    }
        } else {
            view.setTitle(getString(R.string.string_no_dynamic))
                    .setContent(getString(R.string.string_other_not_has_dynamic))
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE) {
                    autoPlayVideo()?.startPlay()
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                findItem()
            }
        })
        recyclerView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            autoPlayVideo()?.startPlay()
        }
        profileTimelineAdapter.setOnLoadMoreListener(this, recyclerView)
        initData()

        // ProfileFragment 页面中的 listOrGrid 切换视图按钮点击事件触发
        RxBus.get().toFlowable().subscribe {
            when (it) {
                is ProfileTimeLineGridOrListEvent -> {
                    if (mChannelId != 0L) return@subscribe
                    isSwitchList = true
                    val listData = ArrayList<Feed>()
                    profileTimelineAdapter.data.forEach {
                        listData.add((it as com.shuashuakan.android.modules.discovery.ItemDataPair).data as Feed)
                    }

                    isList = it.isList
                    recyclerView.removeAllViews()
                    if (it.isList) {
                        recyclerView.addItemDecoration(GridDivider(requireContext().dip(-10), 3))
                        recyclerView.setPadding(0, 0, 0, 0)
                        recyclerView.layoutManager = mLayoutManager
                        profileTimelineAdapter.setLoadMoreView(SskLoadMoreView(SskLoadMoreView.IMAGE))
                        showDataFeed(listData)
                        VideoPlayerManager.instance().resumeVideoPlayer(profileTimelineAdapter.uuid)
                    } else {
                        recyclerView.addItemDecoration(GridDivider(requireContext().dip(10), 3))
                        recyclerView.setPadding(requireContext().dip(15), 0, requireContext().dip(15), 0)
                        recyclerView.layoutManager = mGridLayoutManager
                        profileTimelineAdapter.setLoadMoreView(SskLoadMoreView(SskLoadMoreView.LOAD_MORE_NONE_END))
                        showDataFeed(listData)
                        VideoPlayerManager.instance().suspendVideoPlayer(profileTimelineAdapter.uuid)
                    }
                    profileTimelineAdapter.notifyDataSetChanged()
                }
                is FeedCommentDismissEvent -> {
                    if (fragmentIsResumeStatus) {
                        VideoPlayerManager.instance().resumeVideoPlayer(profileTimelineAdapter.uuid)
                    }
                }
                is ShareBoardDeleteFeedEvent -> {
                    initData()
                }
                is UpdatePublishFeedListEvent -> {
                    initData()
                }
                is EditVideoSuccessEvent -> {
                    // 修改完视频刷新本地数据，不请求网络
                    if (!::profileTimelineAdapter.isInitialized) return@subscribe
                    val multiItemEntity = profileTimelineAdapter.data
                    for ((index, item) in multiItemEntity.withIndex()) {
                        val dataPair = item as com.shuashuakan.android.modules.discovery.ItemDataPair
                        val feed = dataPair.data as Feed
                        if (feed.id == it.feedId) {
                            feed.title = it.content
                            if (!it.coverUrl!!.isEmpty()) {
                                feed.cover = it.coverUrl
                            }
                            feed.properties!!.editInfo!!.canEdit = it.canEdit
                            feed.properties!!.editInfo!!.editableCount = it.editCount + 1
                            profileTimelineAdapter.notifyItemChanged(index)
                        }
                    }
                }
                is DownloadVideoEvent -> {
                    if (it.tag == DownloadManager.DOWNLOAD_TAG_FRAGMENT_PROFILETIMELINE) {
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

                is MoreDialogFollowEvent -> {
                    timeLineViewModel.targetUserId = it.targetUserId
                }
                is LoginSuccessEvent -> {
                    timeLineViewModel.loginEventFollow(apiService)
                    if (!::profileTimelineAdapter.isInitialized) return@subscribe
                    profileTimelineAdapter.let {
                        profileTimelineAdapter.onLoginUpStatus()
                    }
                }
                is FeedFollowChangeEvent -> {
                    updateFollowOrUnFollowStatus(it)
                }
            }
        }.addTo(compositeDisposable)
    }

    private fun initObservable() {
        timeLineViewModel.followUserLiveData.observe(this, Observer<FeedFollowChangeEvent> {
            it?.let { feed ->
                compositeDisposable.add(Observable.timer(200, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io()).observeOn(
                                AndroidSchedulers.mainThread()).subscribe {
                            updateFollowOrUnFollowStatus(feed)
                        })
            }
        })
    }

    private fun updateFollowOrUnFollowStatus(followEvent: FeedFollowChangeEvent) {
        if (!::profileTimelineAdapter.isInitialized) return
        val multiItemEntity = profileTimelineAdapter.data
        for ((index, item) in multiItemEntity.withIndex()) {
            val dataPair = item as com.shuashuakan.android.modules.discovery.ItemDataPair
            if (dataPair.type == MultiTypeTimeLineAdapter.MULTITYPE_TIMELINE_VIDEO ||
                    dataPair.type == MultiTypeTimeLineAdapter.MULTITYPE_TIMELINE_VIDEO_TOPIC) {
                val feed = dataPair.data as Feed
                if (feed.getUserId() == followEvent.userId) {
                    FollowCacheManager.putFollowUserToCache(followEvent.userId, followEvent.state)
                    feed.hasFollowUser = followEvent.state
                    profileTimelineAdapter.notifyItemChanged(index)
                }
            }
        }
    }

    private fun goPermissionPage() {
        if (PullService.canUpload()) {
            PermissionRequestFragment.create(PullService.UploadEntity.TYPE_ADD_HOME_VIDEO)
                    .show(childFragmentManager, "home")
        } else {
            showShortToast(getString(R.string.string_publish_wait_edit))
        }
    }

    override fun onRefresh() {
        page = 0
        hasMore = false
        nextId = null
        isSwitchList = false
        parentList.clear()
//    profileTimelineAdapter.setEnableLoadMore(true)
        requestData()
    }

    override fun onLoadMoreRequested() {
        isSwitchList = false
        requestData()
    }

    fun requestData() {
        if (mChannelId == 0L) {
            profileTimeLinePresenter.requestApi(page, isMine, userId)
        } else {
            if (hasMore) {
                timeLinePresenter.requestApi(mChannelId, nextId)
            } else {
                timeLinePresenter.requestApi(mChannelId, "")
            }
        }
    }

    /**
     * 个人页-动态-展示数据
     */
    override fun showProfileTimeLineData(data: List<Feed>) {
        if (data.isNotEmpty()) {
            page++
            parentList.addAll(data)
        }
        showDataFeed(data)
    }

    /**
     * 话题页-动态-展示数据
     */
    override fun showTopicTimeLineData(topicTimeLineModel: TopicTimeLineModel) {
        hasMore = topicTimeLineModel.hasMore!!
        nextId = topicTimeLineModel.cursor?.nextId

        val listData = ArrayList<Feed>()
        topicTimeLineModel.feedList.forEach {
            listData.add((it.data))
        }

        if (listData.isNotEmpty()) {
//      RxBus.get().post(ShowProfileTimeLineEmptyGridListButtonEvent(true))
            parentList.addAll(listData)
        }
        showDataFeed(listData)
    }

    fun showDataFeed(data: List<Feed>) {
        if (page == 0 && data.isEmpty()) {
            isList = true
            recyclerView.removeAllViews()
            recyclerView.addItemDecoration(GridDivider(requireContext().dip(-10), 3))
            recyclerView.setPadding(0, 0, 0, 0)
            mLayoutManager = LinearLayoutManager(requireContext())
            recyclerView.layoutManager = mLayoutManager
            profileTimelineAdapter.emptyView = emptyView
            RxBus.get().post(ShowProfileTimeLineEmptyGridListButtonEvent(true))
        } else {
            RxBus.get().post(ShowProfileTimeLineEmptyGridListButtonEvent(false))
        }
        val tempList: MutableList<MultiItemEntity> = mutableListOf()
        for (item in data) {
            if (isList) {
                tempList.add(com.shuashuakan.android.modules.discovery.ItemDataPair(item, ProfileTimelineAdapter.TIMELINE))
            } else {
                tempList.add(com.shuashuakan.android.modules.discovery.ItemDataPair(item, ProfileTimelineAdapter.TIMELINE_GRID))
            }
        }

        // 切换网格或列表时必须走 profileTimelineAdapter.setNewData(tempList)
        if (swipeRefreshLayout.isRefreshing || isSwitchList) {
            swipeRefreshLayout.isRefreshing = false // 关闭刷新动画
            profileTimelineAdapter.setNewData(tempList)
        } else {
            profileTimelineAdapter.addData(tempList)
        }
        if (tempList.isEmpty()) {
            profileTimelineAdapter.loadMoreEnd(false)
        } else if (tempList.size == PAGE_MOST_COUNT) {
            profileTimelineAdapter.setEnableLoadMore(true)
            profileTimelineAdapter.loadMoreComplete()
        }
    }

    private fun initData() {
        swipeRefreshLayout.isRefreshing = true
        onRefresh()
    }

    override fun showError() {
        if (swipeRefreshLayout.isRefreshing) {
            swipeRefreshLayout.isRefreshing = false
            profileTimelineAdapter.emptyView = errorView
        } else {
            profileTimelineAdapter.loadMoreFail()
        }
    }

    private fun findItem() {
        firstItem = mLayoutManager.findFirstVisibleItemPosition()
        LastItem = mLayoutManager.findLastVisibleItemPosition()
        VisiableCount = LastItem - firstItem
    }

    private fun autoPlayVideo(): TimeLinePlayerView? {
        for (i in 0..VisiableCount) {
            val childView = recyclerView.getChildAt(i) ?: continue
            var playerView = (recyclerView.getChildViewHolder(childView) as BaseViewHolder).getView<TimeLinePlayerView>(R.id.player_view)
            if (playerView == null) continue
            if (isHide) continue
            val rect = Rect()
            playerView.getLocalVisibleRect(rect)
            val videoHeight = playerView.height//获取视频的高度
            if (rect.top != 0 || rect.bottom != videoHeight) continue
            val position = recyclerView.getChildAdapterPosition(childView)
            if (position == -1 || position > profileTimelineAdapter.data.size) continue
            if (playerView.controllerTimeLine.playChangeListener == null) {
                playerView.controllerTimeLine.playChangeListener = this
            }
            return playerView
        }
        return null
    }

    override fun showMessage(message: String) {
        requireContext().showLongToast(message)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        FishInjection.inject(this)
        timeLinePresenter.attachView(this)
        profileTimeLinePresenter.attachView(this)
    }

    override fun onDetach() {
        super.onDetach()
        timeLinePresenter.detachView(false)
        profileTimeLinePresenter.detachView(false)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (::profileTimelineAdapter.isInitialized) {
            if (isVisibleToUser) startPlay() else pausePlay()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            startPlay()
        } else {
            pausePlay()
        }
    }

    private fun startPlay() {
        isHide = false
        VideoPlayerManager.instance().resumeVideoPlayer(profileTimelineAdapter.uuid)
    }

    fun pausePlay() {
        isHide = true
        VideoPlayerManager.instance().suspendVideoPlayer(profileTimelineAdapter.uuid)
    }

    override fun onPause() {
        super.onPause()
        fragmentIsResumeStatus = false
        pausePlay()
    }

    override fun onResume() {
        super.onResume()
        fragmentIsResumeStatus = true
        startPlay()
    }

    override fun onDestroy() {
        super.onDestroy()
        accountManager.removeAccountChangedListener(this)
        VideoPlayerManager.instance().releaseVideoPlayer(profileTimelineAdapter.uuid)
        compositeDisposable.clear()
    }

    override fun onSeekChange(player: VideoPlayer, startPos: Long, endPos: Long) {
        this.player = player
    }

    override fun onVideoStandStill(player: VideoPlayer, catchPosition: Long, catchTime: Long) {
        this.player = player
    }

    override fun onVideoPauseRecord(player: VideoPlayer, pointStart: Long, pointStop: Long, isActive: Boolean) {
        this.player = player
    }

    override fun onVideoLoadError(url: String?, errorCode: Int) {
    }

    override fun onRepeatPlay(player: VideoPlayer) {
        this.player = player
    }

    override fun onAccountChanged(before: Account?, after: Account?) {
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

}


