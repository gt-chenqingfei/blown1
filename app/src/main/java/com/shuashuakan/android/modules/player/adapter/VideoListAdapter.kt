package com.shuashuakan.android.ui.player.adapter

import android.arch.lifecycle.GenericLifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.luck.picture.lib.tools.ScreenUtils
import com.pili.pldroid.player.PLOnInfoListener
import com.shuashuakan.android.BuildConfig
import com.shuashuakan.android.R
import com.shuashuakan.android.config.AppConfig
import com.shuashuakan.android.danmaku.SSKDanmakuCache
import com.shuashuakan.android.danmaku.SSKDanmakuParser
import com.shuashuakan.android.danmaku.SSKDanmakuSource
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.event.ActivityEvent
import com.shuashuakan.android.event.DanmakaControlEvent
import com.shuashuakan.android.event.DanmakaSendEvent
import com.shuashuakan.android.event.UploadSuccessEvent
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.player.fragment.VideoListFragment
import com.shuashuakan.android.modules.share.ShareHelper
import com.shuashuakan.android.modules.widget.customview.RecommendVideoViewPage
import com.shuashuakan.android.modules.widget.customview.ViewPlayBottomlProgressBar
import com.shuashuakan.android.modules.widget.loadmoreview.SskSecLoadMoreView
import com.shuashuakan.android.modules.widget.up.PeriscopeLayout
import com.shuashuakan.android.player.SSKOnScrollListener
import com.shuashuakan.android.player.SSKVideoPlayListener
import com.shuashuakan.android.player.SSKVideoTextureView
import com.shuashuakan.android.player.SSKViewPagerLayoutManager
import com.shuashuakan.android.service.PullService
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.utils.extension.getRealId
import com.shuashuakan.android.utils.extension.loadBlur
import com.shuashuakan.android.utils.getSpider
import com.shuashuakan.android.utils.getUserId
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import master.flame.danmaku.controller.DrawHandler
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuTimer
import master.flame.danmaku.danmaku.model.IDisplayer
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import master.flame.danmaku.ui.widget.DanmakuView
import org.json.JSONArray
import timber.log.Timber
import java.util.*

/**
 * Author:  lijie
 * Date:   2018/11/19
 * Email:  2607401801@qq.com
 */
class VideoListAdapter constructor(
        private val videoPlayContainer: ViewGroup,
        private val apiService: ApiService,
        private val appConfig: AppConfig,
        private val accountManager: AccountManager,
        private val shareHelper: ShareHelper,
        private val fromMark: Int? = null,
        private val feedSource: String? = null,
        private val fragment: VideoListFragment

) : BaseQuickAdapter<Feed, BaseViewHolder>(R.layout.video_list_item), GenericLifecycleObserver {

    private lateinit var mCurrentFloorLayoutManager: SSKViewPagerLayoutManager
    private val videoTexture: SSKVideoTextureView = videoPlayContainer.findViewById(R.id.video_texture)
    private val backBlurView = videoPlayContainer.findViewById<SimpleDraweeView>(R.id.back_blur_view)
    private val upLayout: PeriscopeLayout = videoPlayContainer.findViewById(R.id.up_layout)
    private val mDanmakuView: DanmakuView = videoPlayContainer.findViewById(R.id.danmakuView)

    private val compositeDisposable = CompositeDisposable()
    private var mActivityEvent: ActivityEvent? = null
    private var mDanmakaEvent: DanmakaControlEvent? = null
    private var mSelectChainPosition: Int? = null

//    private val mHandler = Handler()
//    private val mHideFakeCoverTask = Runnable {d
//        videoTexture.fakeCoverView?.visibility = View.INVISIBLE
//        videoTexture.visibility = View.VISIBLE
//    }

    private val mVideoPlayListener: SSKVideoPlayListener = object : SSKVideoPlayListener() {

        override fun onCompletion() {
            super.onCompletion()
            var playChainPosition = (videoTexture as View).getTag(R.id.chainPosition) as? Int ?: 0
            ++playChainPosition

            val centerChainView = mCurrentFloorLayoutManager.findCenterView()?.findViewById<RecyclerView>(R.id.special_view_page_root)
            val currentChainAdapter = centerChainView?.adapter as? VideoChainAdapter

            Timber.e("RecommendVideoAdapter mVideoPlayListener 视频播放完成 data 0 = ${currentChainAdapter!!.data[0].title} : ${currentChainAdapter.data[0].userName} position = $playChainPosition chainSize = ${currentChainAdapter.data.size}")
            if (playChainPosition < currentChainAdapter.data.size) {//播放下个接龙视频
                val currentChainLayoutManager = centerChainView.layoutManager as SSKViewPagerLayoutManager

                currentChainLayoutManager.smoothScrollToPosition(playChainPosition)
                Timber.e("RecommendVideoAdapter mVideoPlayListener 播放下个接龙视频 mCurrentChainLayoutManager = $currentChainLayoutManager")


            } else {//播放下个楼层视频
                var playFloorPosition = videoTexture.getTag(R.id.floorPosition) as? Int ?: 0
                ++playFloorPosition
                Timber.e("RecommendVideoAdapter mVideoPlayListener  - 播放下个楼层视频 playFloorPosition = $playFloorPosition  data.size = ${data.size}")
                if (playFloorPosition < data.size) {
                    mCurrentFloorLayoutManager.smoothScrollToPosition(playFloorPosition)
                }
            }
        }


        override fun onError(errorCode: Int): Boolean {
            Timber.e("RecommendVideoAdapter mVideoPlayListener onError errorCode = $errorCode")
            if (BuildConfig.DEBUG) {
                Toast.makeText(recyclerView.context, "播放器出错 errorCode = $errorCode", Toast.LENGTH_LONG).show()
            }
            return super.onError(errorCode)
        }


        override fun onPrepared(preparedTime: Int) {
            super.onPrepared(preparedTime)
            Timber.e("RecommendVideoAdapter mFloorPagerListener mVideoPlayListener onPrepared preparedTime = $preparedTime  videoTexture.isVideoSetted = $videoTexture.isVideoSetted")
            val floorCenterView = mCurrentFloorLayoutManager.findCenterView()

            val chainCenterView = videoTexture.fakeCoverView

            videoPlayContainer.translationX = chainCenterView?.left?.toFloat() ?: 0f
            videoPlayContainer.translationY = floorCenterView?.y ?: 0f

            videoTexture.start()
        }


        override fun onInfo(what: Int, extra: Int) {
            super.onInfo(what, extra)
            when (what) {
                PLOnInfoListener.MEDIA_INFO_VIDEO_RENDERING_START -> {
                    videoTexture.fakeCoverView?.visibility = View.INVISIBLE
                    videoTexture.visibility = View.VISIBLE
                    Timber.e("RecommendVideoAdapter mFloorPagerListener mVideoPlayListener MEDIA_INFO_VIDEO_RENDERING_START")
                }
            }
        }

        override fun onProgressUpdate(currentProgress: Long) {
            super.onProgressUpdate(currentProgress)
            val currentFloorCenterView = mCurrentFloorLayoutManager.findCenterView()
            //  Timber.e("RecommendVideoAdapter onProgressUpdate currentProgress = $currentProgress ")
            val progress = currentFloorCenterView?.findViewById<ViewPlayBottomlProgressBar>(R.id.video_progress)
            val currentPosition = progress?.tag as? Int ?: 0
            val subProgress = progress?.getCurrentProgressBar(currentPosition)
            subProgress?.progress = currentProgress.toInt()

            if (currentProgress >= 600) {

                val currentChainPagerView = currentFloorCenterView?.findViewById<RecyclerView>(R.id.special_view_page_root)
                val currentChainAdapter = currentChainPagerView?.adapter as? VideoChainAdapter
                        ?: return

                if (currentProgress >= 800) {
                    currentChainAdapter.showShareAnimation(videoTexture.playFeed)
                }
            }
        }

        override fun onPause() {
            super.onPause()
            if (mDanmakaEvent?.isOpen == true && mDanmakaEvent?.isShow == true) {
                if (!mDanmakuView.isPaused) {
                    mDanmakuView.pause()
                }
            }
        }

        override fun onStart() {
            super.onStart()
            if (mDanmakaEvent?.isOpen == true && mDanmakaEvent?.isShow == true) {
                if (mDanmakuView.isPaused) {
                    mDanmakuView.resume()
                }
            }

        }
    }


    private var mParser: SSKDanmakuParser? = null
    private val mDanmakuContext = DanmakuContext.create()

    init {
        videoTexture.addVideoPlayListener(mVideoPlayListener)

        if (appConfig.isDanmakaOpen()) {
            // 设置最大显示行数
            val maxLinesPair = HashMap<Int, Int>()
            maxLinesPair[BaseDanmaku.TYPE_SCROLL_RL] = 15 // 滚动弹幕最大显示5行
            // 设置是否禁止重叠
            val overlappingEnablePair = HashMap<Int, Boolean>()
            overlappingEnablePair[BaseDanmaku.TYPE_SCROLL_RL] = true
            overlappingEnablePair[BaseDanmaku.TYPE_FIX_TOP] = true
            //val mDanmakuContext = DanmakuContext.create()
            mDanmakuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, ScreenUtils.dip2px(videoTexture.context, 0.5f).toFloat())
                    .setDuplicateMergingEnabled(true)
                    .setScrollSpeedFactor(1f)
                    .setScaleTextSize(1.2f)
                    .setTypeface(Typeface.createFromAsset(videoTexture.context.assets, "fonts/NotoSans-CondensedBold.ttf"))
                    //.setCacheStuffer( SpannedCacheStuffer(), mCacheStufferAdapter) // 图文混排使用SpannedCacheStuffer
                    // .setCacheStuffer(new BackgroundCacheStuffer())  // 绘制背景使用BackgroundCacheStuffer
                    .setMaximumLines(maxLinesPair)
                    .preventOverlapping(overlappingEnablePair).setDanmakuMargin(10)

            mDanmakuView.setCallback(object : DrawHandler.Callback {
                override fun updateTimer(timer: DanmakuTimer) {}

                override fun drawingFinished() {

                }

                override fun danmakuShown(danmaku: BaseDanmaku) {
                }

                override fun prepared() {
                    if (mDanmakaEvent?.isShow == true) {
                        mDanmakuView.start()
                        mDanmakuView.show()
                    }
                }
            })

            mDanmakuView.enableDanmakuDrawingCache(true)
            mDanmakaEvent = DanmakaControlEvent(true, appConfig.isDanmakaShow())
        } else {
            mDanmakaEvent = DanmakaControlEvent(false, appConfig.isDanmakaShow())
        }

        fragment.lifecycle.addObserver(this@VideoListAdapter)
    }


    override fun onStateChanged(source: LifecycleOwner?, event: Lifecycle.Event?) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            fragment.lifecycle.removeObserver(this)
            compositeDisposable.clear()
            mDanmakuView.release()
        }
    }

    private fun prepareDanmaku(feedId: String, stream: JSONArray) {
        val parser = SSKDanmakuParser()
        parser.load(SSKDanmakuSource(stream))
        mDanmakuView.release()
        mDanmakuView.prepare(parser, mDanmakuContext)
        mDanmakuView.tag = feedId
        mParser = parser
    }


    private val mFloorPagerListener = FloorOnScrollListener()

    private inner class FloorOnScrollListener : SSKOnScrollListener {
        private var mFloorCenterView: View? = null
        private var mNewState: Int = RecyclerView.SCROLL_STATE_IDLE


        override fun onInitComplete(layoutManager: SSKViewPagerLayoutManager, recyclerView: RecyclerView) {
            Timber.e("ChainOnScrollListener Floor  onInitComplete ------- ")
            if (mSelectChainPosition == null || mSelectChainPosition == -1) {
                videoTexture.postOnAnimationDelayed({
                    //延迟播放，防止七牛回调出问题
                    initFloorCenterView()
                }, 60)
            }
        }


        override fun onScrollStateChanged(layoutManager: SSKViewPagerLayoutManager, recyclerView: RecyclerView, newState: Int) {
            mNewState = newState
            when (newState) {
                RecyclerView.SCROLL_STATE_IDLE -> {
                    val centerFloorView = layoutManager.findCenterView()

                    mFloorCenterView?.tag?.let {
                        val floorViewFeed = it as? Feed
                        if (centerFloorView == mFloorCenterView && floorViewFeed?.masterFeedId.getRealId() == videoTexture.playFeed?.masterFeedId.getRealId()) {
                            Timber.e("ChainOnScrollListener Floor  pauseAndRemoveInvalidVideo --fid =" +
                                    " ${floorViewFeed?.masterFeedId.getRealId()}---pID = ${videoTexture.playFeed?.masterFeedId.getRealId()}-- ")
                            videoTexture.fakeCoverView?.visibility = View.INVISIBLE
                            videoPlayContainer.translationY = centerFloorView?.y ?: 0f
                            videoTexture.start()
                            return
                        }
                    }


                    if (centerFloorView?.id == R.id.videoItemContainer && centerFloorView.tag != videoTexture.playFeed) {
                        Timber.e("ChainOnScrollListener Floor  onScrolled ----bindFeed--mFloorCenterView = $mFloorCenterView  - new view = $centerFloorView")
                        val chainViewPager = centerFloorView.findViewById<RecyclerView>(R.id.special_view_page_root)
                        val chainLayoutManager = chainViewPager.layoutManager as SSKViewPagerLayoutManager

                        val chainItemView = chainLayoutManager.findCenterView()

                        val masterFeed = chainItemView?.tag as? Feed

                        mFloorCenterView = centerFloorView

                        videoTexture.fakeCoverView = chainItemView?.findViewById(R.id.frame_fake_cover)

                        videoTexture.bindFeed(masterFeed)

                        val floorPosition = centerFloorView.getTag(R.id.floorPosition) as? Int ?: 0

                        val chainPosition = chainItemView?.getTag(R.id.chainPosition) as? Int ?: 0

                        val progress = centerFloorView.findViewById<ViewPlayBottomlProgressBar>(R.id.video_progress)
                        progress?.setProgressShow(chainPosition)
                        setPlayVideo(floorPosition, chainPosition)
                    }

                }
            }
            Timber.e("ChainOnScrollListener Floor onScrollStateChanged newState = $newState")
        }

        override fun onScrolled(layoutManager: SSKViewPagerLayoutManager, recyclerView: RecyclerView, dx: Int, dy: Int) {
            videoPlayContainer.translationX = 0f
            videoPlayContainer.translationY = mFloorCenterView?.y ?: 0f

//            if (mFloorCenterView != null && !layoutManager.isViewInWindow(mFloorCenterView)) {

            mFloorCenterView?.tag?.let {
                val floorViewFeed = it as? Feed
                if (mNewState == RecyclerView.SCROLL_STATE_DRAGGING && !layoutManager.isViewInWindow(mFloorCenterView) && floorViewFeed?.masterFeedId.getRealId() == videoTexture.playFeed?.masterFeedId.getRealId()) {
                    Timber.e("ChainOnScrollListener Floor  pauseAndRemoveInvalidVideo --fid = ${floorViewFeed?.masterFeedId.getRealId()}---pID = ${videoTexture.playFeed?.masterFeedId.getRealId()}-- ")
                    videoTexture.pause()
                }
            }

//            }


        }


        fun initFloorCenterView() {
            if (mSelectChainPosition == null || mSelectChainPosition == -1) {
                mSelectChainPosition = 0
            }
            val chainPosition = mSelectChainPosition

            val centerView = mCurrentFloorLayoutManager.findCenterView()
            Timber.e("ChainOnScrollListener Floor  centerView =  $centerView")
            if (centerView?.id == R.id.videoItemContainer) {
                val chainViewPager = centerView.findViewById<RecyclerView>(R.id.special_view_page_root)
                val chainLayoutManager = chainViewPager.layoutManager as SSKViewPagerLayoutManager
                val chainItemView = chainLayoutManager.findCenterView()
                val position = chainLayoutManager.getPosition(chainItemView)
                Timber.e("ChainOnScrollListener Floor initFloorCenterView position = $position  chainPosition = $chainPosition  ")
                if (position == chainPosition) {
                    if (chainPosition != 0) {
                        mSelectChainPosition = null
                    }

                    val feed = chainItemView?.tag as? Feed

                    if (feed != videoTexture.playFeed) {
                        Timber.e("ChainOnScrollListener Floor  initFloorCenterView ------feed = ${feed?.title}")
                        mFloorCenterView = centerView

                        videoTexture.fakeCoverView = chainItemView?.findViewById(R.id.frame_fake_cover)

                        videoTexture.bindFeed(feed)

                        val progress = centerView.findViewById<ViewPlayBottomlProgressBar>(R.id.video_progress)

                        progress.setProgressShow(chainPosition)

                        val floorPosition = centerView.getTag(R.id.floorPosition) as? Int ?: 0

                        setPlayVideo(floorPosition, chainPosition)
                    }
                }
            }
        }
    }

    private fun setPlayVideo(floorPosition: Int?, chainPosition: Int?) {
        videoTexture.bindFeed?.let { validFeed ->
            floorPosition?.let {
                (videoTexture as View).setTag(R.id.floorPosition, it)
            }

            chainPosition?.let {
                (videoTexture as View).setTag(R.id.chainPosition, it)
            }

            val screenWidth = ScreenUtils.getScreenWidth(videoTexture.context)
            videoTexture.layoutParams.width = screenWidth
            videoTexture.layoutParams.height = screenWidth * validFeed.height / validFeed.width

//            videoTexture.requestLayout()

            videoTexture.visibility = View.INVISIBLE

            Timber.e("ChainOnScrollListener setPlayVideo ------screentWidth = ${screenWidth} - videoTexture.measuredWidth= ${videoTexture.measuredWidth}  videoTexture.layoutParams.height = ${videoTexture.layoutParams.height} videoTexture.layoutParams.width = ${videoTexture.layoutParams.width}")

            upLayout.removeAllViews()
            backBlurView.loadBlur(validFeed.firstFrame
                    ?: validFeed.cover, validFeed.width, validFeed.height)

            videoTexture.startPlayFeed()
            loadDanmaku(validFeed.id)
        }
    }

    /**
     * 加载弹幕
     */
    private fun loadDanmaku(feedId: String?) {
        if (feedId == null) return
        if (mDanmakaEvent?.isOpen == true && mDanmakaEvent?.isShow == true) {
            if (mDanmakuView.tag != feedId) {
                mDanmakuView.hide()
                val barrageArray = SSKDanmakuCache.getBarrageFromMemory(feedId)
                if (barrageArray == null) {
                    apiService.getListBarrage(feedId)
                            .applySchedulers()
                            .subscribeApi(
                                    onNext = {
                                        val bytes = it.bytes()
                                        if (bytes == null) {
                                            SSKDanmakuCache.putBarrageToMemory(feedId, JSONArray())
                                        } else {
                                            val json = String(bytes)
                                            if (!TextUtils.isEmpty(json)) {
                                                val array = JSONArray(json)
                                                SSKDanmakuCache.putBarrageToMemory(feedId, array)
                                                prepareDanmaku(feedId, array)
                                            }
                                        }
                                    }
                            )

                } else {
                    prepareDanmaku(feedId, barrageArray)
                }
            }
        }
    }


    private inner class ChainOnScrollListener : SSKOnScrollListener {
        private var mChainCenterView: View? = null
        private var mNewState: Int = RecyclerView.SCROLL_STATE_IDLE


        override fun onInitComplete(layoutManager: SSKViewPagerLayoutManager, recyclerView: RecyclerView) {
            initChainCenterView(layoutManager)
            Timber.e("ChainOnScrollListener chain  onInitComplete ------- isChildHolderDetached = ")
        }

        private fun initChainCenterView(layoutManager: SSKViewPagerLayoutManager) {
            val centerView = layoutManager.findCenterView()
            if (centerView?.id == R.id.play_layout) {
                mChainCenterView = centerView
                Timber.e("ChainOnScrollListener chain  onInitComplete ------  - centerView = $centerView")
            }
        }


        private fun startPlayChain() {
            val centerView = mChainCenterView
            val chainPosition = centerView?.getTag(R.id.chainPosition) as? Int ?: 0
            setPlayVideo(null, chainPosition)

            val progress = centerView?.getTag(R.id.video_progress) as? ViewPlayBottomlProgressBar
            progress?.setProgressShow(chainPosition)

            Timber.e("ChainOnScrollListener PlayChainVideoTask 棒 startPlayChain ")

        }

        private fun controlBlurView(layoutManager: SSKViewPagerLayoutManager) {
            val childCount = layoutManager.childCount
            for (index in 0 until childCount) {
                val itemView = layoutManager.getChildAt(index)
                val item = itemView?.tag as? Feed
                item?.let {
                    val fakeCover = itemView.findViewById<View>(R.id.frame_fake_cover)
                    if (itemView != mChainCenterView && it.id.getRealId() != videoTexture.playFeed?.id?.getRealId()) {
                        fakeCover.visibility = View.VISIBLE
                    }/*else{
                        fakeCover.visibility = View.INVISIBLE
                    }*/
                }
            }
        }

        override fun onScrollStateChanged(layoutManager: SSKViewPagerLayoutManager, recyclerView: RecyclerView, newState: Int) {
            mNewState = newState
            mChainCenterView?.isClickable = mNewState == RecyclerView.SCROLL_STATE_IDLE
            when (newState) {
                RecyclerView.SCROLL_STATE_IDLE -> {
                    val centerChainView = layoutManager.findCenterView()

                    trackGroupSlideSpider(centerChainView)

                    if (mChainCenterView == centerChainView) {
                        val floorViewFeed = mChainCenterView?.tag as? Feed
                        if (floorViewFeed?.id.getRealId() == videoTexture.playFeed?.id.getRealId()) {
                            videoTexture.fakeCoverView?.visibility = View.INVISIBLE

                            videoPlayContainer.translationX = mChainCenterView?.x ?: 0f
//                            videoPlayContainer.translationY =  0f
                            videoTexture.start()

                            Timber.e("ChainOnScrollListener chain  start ------- ")
                            return
                        }
                    }

                    if (centerChainView?.id == R.id.play_layout) {
                        val chainFeed = centerChainView.tag as? Feed
                        mChainCenterView = centerChainView
                        videoTexture.fakeCoverView = mChainCenterView?.findViewById(R.id.frame_fake_cover)
                        videoTexture.bindFeed(chainFeed)
                        Timber.e("ChainOnScrollListener chain   bindFeed --${chainFeed?.title}----- ")
                        startPlayChain()
                    }


                }
            }
            Timber.e("ChainOnScrollListener chain onScrollStateChanged newState = $newState")
        }

        override fun onScrolled(layoutManager: SSKViewPagerLayoutManager, recyclerView: RecyclerView, dx: Int, dy: Int) {
            videoPlayContainer.translationX = mChainCenterView?.x ?: 0f
            videoPlayContainer.translationY = mChainCenterView?.y ?: 0f


//            pauseAndRemoveInvalidChainVideo(layoutManager)
            if (mNewState == RecyclerView.SCROLL_STATE_DRAGGING && !layoutManager.isViewInWindow(mChainCenterView)) {
                val floorViewFeed = mChainCenterView?.tag as? Feed
                if (floorViewFeed?.id.getRealId() == videoTexture.playFeed?.id.getRealId()) {
                    videoTexture.pause()
                    Timber.e("ChainOnScrollListener chain  pause ------- ")
                }
            }
            controlBlurView(layoutManager)
        }
    }

    //组内视频滑动打点
    private fun trackGroupSlideSpider(itemView: View?) {
        itemView?.let {
            val viewTag = it.tag
            val context = it.context
            if (viewTag is Feed && context != null) {
                context.getSpider().manuallyEvent(SpiderEventNames.GROUP_VIDEO_SLIDE)
                        .put("feedID", viewTag.id)
                        .put("MasterFeedID", viewTag.masterFeedId.toString())
                        .put("feedLevel", viewTag.properties?.floor.toString())
                        .put("userID", context.getUserId())
                        .track()
            }
        }
    }

    override fun convert(holder: BaseViewHolder, item: Feed) {
        holder.itemView.tag = item
        holder.itemView.setTag(R.id.floorPosition, holder.adapterPosition)

        val pager = holder.getView<RecommendVideoViewPage>(R.id.special_view_page_root)

        val adapter = pager.adapter as VideoChainAdapter
        val progress = holder.getView<ViewPlayBottomlProgressBar>(R.id.video_progress)

        val masterFeed = data[holder.adapterPosition]
        progress.setProgressNum((masterFeed.solitaireNum ?: 0) + 1)
        progress.setProgressShow(0)
        Timber.e("ChainOnScrollListener convert title = ${masterFeed.title} uname = ${masterFeed.userName}")
        //重置接龙数据
        adapter.setNewData(mutableListOf(masterFeed))
        adapter.reset(masterFeed, progress, mSelectChainPosition
                ?: 0, mActivityEvent, mDanmakaEvent)

    }

    override fun createBaseViewHolder(view: View?): BaseViewHolder {
        return BaseViewHolder(view)
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        Timber.e("RecommendVideoAdapter onCreateDefViewHolder")
        val holder = super.onCreateDefViewHolder(parent, viewType)

        val pager = holder.getView<RecommendVideoViewPage>(R.id.special_view_page_root)
        pager.setHasFixedSize(true)
        pager.setItemViewCacheSize(0)
        pager.itemAnimator = null


        val lm = SSKViewPagerLayoutManager(pager, parent.context, RecyclerView.HORIZONTAL)
        lm.orientation = RecyclerView.HORIZONTAL

        val chainOnScrollListener = ChainOnScrollListener()
        lm.addSSKOnScrollListener(chainOnScrollListener)

        pager.layoutManager = lm

        val adapter = VideoChainAdapter(videoPlayContainer, apiService, accountManager, shareHelper, fromMark, feedSource, fragment)
        adapter.setEnableLoadMore(true)
        adapter.setPreLoadNumber(3)
        adapter.setLoadMoreView(SskSecLoadMoreView(true))
        pager.adapter = adapter

        return holder
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        Timber.e("RecommendVideoAdapter onAttachedToRecyclerView")
        mCurrentFloorLayoutManager = recyclerView.layoutManager as SSKViewPagerLayoutManager
        mCurrentFloorLayoutManager.addSSKOnScrollListener(mFloorPagerListener)
        videoTexture.clearUpDateSeekTask()
        RxBus.get().toFlowable().subscribe { event ->
            when (event) {
                is ActivityEvent -> {
                    mActivityEvent = event
                    notifyDataSetChanged()
                }
                is DanmakaControlEvent -> {
                    if (event.isOpen && event.isShow) {
                        if (mDanmakuView.tag == videoTexture.playFeed?.id) {
                            mDanmakuView.show()
                        } else {
                            loadDanmaku(videoTexture.playFeed?.id)
                        }
                    } else {
                        mDanmakuView.hide()
                    }
                    appConfig.setDanmakaShow(event.isShow)
                    mDanmakaEvent = event
                }
                is DanmakaSendEvent -> {
                    addDanmaku(event)
                }
                is UploadSuccessEvent -> {
                    if (event.source == PullService.UploadEntity.TYPE_ADD_SOLITAIRE) {//接龙视频发送成功，更新页面
                        val childCount = mCurrentFloorLayoutManager.childCount
                        for (i in 0 until childCount) {
                            val child = mCurrentFloorLayoutManager.getChildAt(i) as? ViewGroup
                            val childFeed = child?.tag as? Feed
                            if (childFeed?.masterFeedId == event.feed.masterFeedId) {
                                val matchRecyclerView = child?.findViewById<RecyclerView>(R.id.special_view_page_root)
                                val videoProgress = child?.findViewById<ViewPlayBottomlProgressBar>(R.id.video_progress)
                                val matchAdapter = matchRecyclerView?.adapter as? VideoChainAdapter
                                videoProgress?.setProgressNum(videoProgress.childCount + 1)
                                videoProgress?.setProgressShow(videoProgress.tag as? Int ?: 0)
                                matchAdapter?.addData(matchAdapter.data.size, event.feed)
                            }
                        }
                    }

                }

            }
        }.addTo(compositeDisposable)
    }

    private fun addDanmaku(event: DanmakaSendEvent) {
        SSKDanmakuCache.addBarrage(event.feedId, event)
        val parser = mParser
        if (parser == null) {
            val barrageArray = SSKDanmakuCache.getBarrageFromMemory(event.feedId)
            barrageArray?.let {
                prepareDanmaku(event.feedId, it)
            }
        } else {
            val danmaku = parser.parseItem(event)
            if (danmaku == null || event.feedId.getRealId() != videoTexture.playFeed?.getRealFeedId()) {
                return
            }
            mDanmakuView.addDanmaku(danmaku)
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)

//        handler.removeCallbacks(mHideFakeCoverTask)
    }

    fun moveToChainIndex(chainData: List<Feed>?, selectChainPosition: Int) {
        mSelectChainPosition = selectChainPosition
        Timber.e("ChainOnScrollListener PlayFloorVideoTask mSelectChainPosition  棒 $mSelectChainPosition ")
        recyclerView.postOnAnimation {
            if (chainData != null) {
                val centerChainView = mCurrentFloorLayoutManager.findCenterView()?.findViewById<RecyclerView>(R.id.special_view_page_root)
                val currentChainAdapter = centerChainView?.adapter as? VideoChainAdapter
                currentChainAdapter?.addData(chainData)
                centerChainView?.scrollToPosition(selectChainPosition)
                centerChainView?.postOnAnimationDelayed({
                    Timber.e("ChainOnScrollListener PlayFloorVideoTask moveToChainIndex  棒 ")
                    mFloorPagerListener.initFloorCenterView()
                }, 60)
            }
        }
    }

    /**
     * 加载从h5页面进来的接龙数据
     */
    fun loadChain(feedId: String?, floorFeedId: String? = null) {
        feedId?.let {
            apiService.getChainsFeeds(feedId,
                    null,
                    "DOWN",
                    1,
                    floorFeedId,
                    null,
                    null)
                    .applySchedulers()
                    .subscribeApi(
                            onNext = {
                                val list = it.feedList
                                val feed = list?.getOrNull(0)
                                if (feed != null) {
                                    setNewData(mutableListOf(feed))
                                }
                            },
                            onApiError = {
                            })

        }

    }

    fun findChainCenterView(): RecyclerView? {
        return mCurrentFloorLayoutManager.findCenterView()?.findViewById(R.id.special_view_page_root)
    }
}


















