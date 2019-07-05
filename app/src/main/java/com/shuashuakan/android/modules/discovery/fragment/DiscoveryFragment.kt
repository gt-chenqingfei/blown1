package com.shuashuakan.android.modules.discovery.fragment

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.gyf.barlibrary.ImmersionBar
import com.shuashuakan.android.R
import com.shuashuakan.android.config.AppConfig
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.explore.*
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.event.FocusListRefreshEvent
import com.shuashuakan.android.event.LoginOutEvent
import com.shuashuakan.android.event.LoginSuccessEvent
import com.shuashuakan.android.event.SubscribeEvent
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.FeedTransportManager
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.discovery.DiscoveryActivity
import com.shuashuakan.android.modules.discovery.adapter.DiscoveryMultipleAdapter
import com.shuashuakan.android.modules.player.activity.VideoPlayActivity
import com.shuashuakan.android.modules.widget.loadmoreview.SskLoadMoreView
import com.shuashuakan.android.spider.Spider
import com.shuashuakan.android.ui.base.FishFragment
import com.shuashuakan.android.utils.bindView
import com.shuashuakan.android.utils.noDoubleClick
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject


/**
 * Author:  lijie
 * Date:   2018/11/30
 * Email:  2607401801@qq.com
 */
class DiscoveryFragment : FishFragment(), SwipeRefreshLayout.OnRefreshListener {

    private val recyclerView by bindView<RecyclerView>(R.id.recycler_view)
    private val swipeRefreshLayout by bindView<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
    private val exploreName by bindView<TextView>(R.id.fragment_explore_name)
    private val exploreBack by bindView<ImageView>(R.id.iv_explore_back)
    private val compositeDisposable = CompositeDisposable()

    @Inject
    lateinit var apiService: ApiService

    private lateinit var adapter: DiscoveryMultipleAdapter

    private lateinit var errorView: View
    @Inject
    lateinit var accountManager: AccountManager
    @Inject
    lateinit var spider: Spider
    @Inject
    lateinit var appConfig: AppConfig

    var currentPage = 0
    private var rankingList: MutableList<Feed> = mutableListOf()

    companion object {
        fun create(): DiscoveryFragment {
            return DiscoveryFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_explore_layout, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
        initData()
        registerEvent()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    private fun registerEvent() {
        RxBus.get().toFlowable().subscribe {
            when (it) {
                is SubscribeEvent -> {
                    onRefresh()
                }
                is LoginSuccessEvent -> {
                    if (!::adapter.isInitialized) return@subscribe
                    adapter.onLoginChainFollow()
                    adapter.onLoginChainUserFollow()
                    adapter.onLoginHotChannelSubscribe()
                }
                is FocusListRefreshEvent -> {
                    onRefresh()
                }
                is LoginOutEvent -> {
                    onRefresh()
                }
            }
        }.addTo(compositeDisposable)
    }

    private fun initView() {
        ImmersionBar.setTitleBar(activity, exploreName)
        ImmersionBar.setTitleBar(activity, exploreBack)
        if (appConfig.isShowNewHomePage()) {
            exploreName.visibility = View.INVISIBLE
            exploreBack.visibility = View.INVISIBLE
        }
        if (activity is DiscoveryActivity) {
            exploreName.visibility = View.VISIBLE
            exploreBack.visibility = View.VISIBLE
        }
        exploreBack.noDoubleClick {
            (activity as DiscoveryActivity).finish()
        }
        swipeRefreshLayout.setOnRefreshListener(this)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = DiscoveryMultipleAdapter(requireContext(), null, accountManager, spider, apiService)
        recyclerView.adapter = adapter
        adapter.setLoadMoreView(SskLoadMoreView())
        adapter.listener = object : DiscoveryMultipleAdapter.OnAdapterPlayerViewClickListener {
            override fun onAdapterPlayerViewClickListener(position: Int) {
                val itemData = adapter.getItem(position) as com.shuashuakan.android.modules.discovery.ItemDataPair
                val exploreFeedTypeModel = itemData.data as Feed
                for (feed in rankingList) {
                    feed.hasFollowUser = feed.author?.isFollow
                }
                val intentParam = FeedTransportManager.jumpToVideoFromExploreExcellentChains(rankingList.toList(), exploreFeedTypeModel, exploreFeedTypeModel.channelId?.toLong())
                startActivity(VideoPlayActivity.create(requireContext(), intentParam))
            }
        }

        errorView = layoutInflater.inflate(R.layout.view_error, recyclerView.parent as ViewGroup, false)
        errorView.setOnClickListener { initData() }
        adapter.setOnLoadMoreListener({
        }, recyclerView)
    }

    private fun initData() {
        swipeRefreshLayout.isRefreshing = true
        currentPage = 0
        getData()
    }

    override fun onRefresh() {
        initData()
    }

    fun getData() {
        apiService.getExplore(currentPage).applySchedulers().subscribeApi(
                onNext = {

                    val tempList: MutableList<MultiItemEntity> = mutableListOf()
                    for (item in it.classificationList!!) {
                        when (item) {
                            is ExploreBannerModel -> tempList.add(com.shuashuakan.android.modules.discovery.ItemDataPair(item, DiscoveryMultipleAdapter.BANNER))
                            is ExploreCategoryModel -> tempList.add(com.shuashuakan.android.modules.discovery.ItemDataPair(item, DiscoveryMultipleAdapter.CATEGORY_ENTRANCE))
                            is ExploreUserModel -> tempList.add(com.shuashuakan.android.modules.discovery.ItemDataPair(item, DiscoveryMultipleAdapter.CHAIN_USER_LIST))
                            is ExploreChannelModel -> tempList.add(com.shuashuakan.android.modules.discovery.ItemDataPair(item, DiscoveryMultipleAdapter.HOT_CHANNEL))
                            is ExploreRankingModel -> tempList.add(com.shuashuakan.android.modules.discovery.ItemDataPair(item, DiscoveryMultipleAdapter.CHAIN_RAKING_TITLE))
                        }
                    }
                    for (item in it.classificationList!!) {
                        if (item is ExploreRankingModel) {
                            rankingList.addAll(item.dataList)
                            for (subItem in item.dataList) {
                                tempList.add(com.shuashuakan.android.modules.discovery.ItemDataPair(subItem, DiscoveryMultipleAdapter.CHAIN_RANKING_ITEM))
                            }
                        }
                    }
                    adapter.setRankingList(rankingList as ArrayList<Feed>)

                    adapter.setNewData(tempList)
                    swipeRefreshLayout.isRefreshing = false
                    adapter.loadMoreEnd(false)
                }, onApiError = {
            swipeRefreshLayout.isRefreshing = false
            adapter.emptyView = errorView
        })
    }

}