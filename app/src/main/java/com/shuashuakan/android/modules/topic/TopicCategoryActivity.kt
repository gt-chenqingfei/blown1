package com.shuashuakan.android.modules.topic

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.widget.ListViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.AbsListView
import com.gyf.barlibrary.ImmersionBar
import com.shuashuakan.android.R
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.FeedChannel
import com.shuashuakan.android.data.api.model.TopicCategory
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.event.LoginSuccessEvent
import com.shuashuakan.android.event.SubscribeEvent
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.topic.adapter.TopicCategoryAdapter
import com.shuashuakan.android.modules.topic.adapter.TopicCategoryTitleAdapter
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.ui.base.FishActivity
import com.shuashuakan.android.utils.getUserId
import com.shuashuakan.android.utils.startActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_topic_category.*
import me.twocities.linker.annotations.Link
import me.twocities.linker.annotations.LinkQuery
import javax.inject.Inject

/**
 *@author: zhaoningqiang
 *@time: 2019/5/23
 *@Description:话题分类
 */
@Link("ssr://channel/recommendation")
class TopicCategoryActivity : FishActivity(), View.OnClickListener {

    @Inject
    lateinit var apiService: ApiService

    @LinkQuery(pageTitle)
    @JvmField
    var page: String? = null

    @LinkQuery(categoryIdExtra)
    @JvmField
    var categoryId: String? = null

    private lateinit var mImmersionBar: ImmersionBar

    private lateinit var mTopicCategoryTitleAdapter: TopicCategoryTitleAdapter

    private lateinit var mTopicCategoryAdapter: TopicCategoryAdapter

    private lateinit var mTitlesLayoutManager: LinearLayoutManager

    private var mShowType = showTypeChannel
    private val compositeDisposable = CompositeDisposable()

    companion object {
        const val showTypeChannel = 1
        const val showTypeSubscribe = 2
        const val showTypeSelect = 3
        const val resultDataFeedChannel = "result_data_feed_channel"
        private const val showTypeTitle = "showTypeTitle"
        private const val pageTitle = "page"
        private const val categoryIdExtra = "categoryId"
        private const val pageSubscribed = "subscribed"
        private const val selectedFeedChannelTitle = "selectedFeedChannelTitle"


        fun launch(context: Context, showType: Int) {
            val intent = Intent(context, TopicCategoryActivity::class.java)
            intent.putExtra(showTypeTitle, showType)
            context.startActivity(intent)
        }

        fun launchForResult(activity: FragmentActivity, requestCode: Int, selectedFeedChannel: FeedChannel?) {
            val intent = Intent(activity, TopicCategoryActivity::class.java)
            intent.putExtra(showTypeTitle, showTypeSelect)
            intent.putExtra(selectedFeedChannelTitle, selectedFeedChannel)
            activity.startActivityForResult(intent, requestCode)
        }

        fun launchForFragment(activity: FragmentActivity, fragment: Fragment, requestCode: Int, showType: Int = showTypeSubscribe, selectedFeedChannel: FeedChannel? = null) {
            val intent = Intent(activity, TopicCategoryActivity::class.java)
            intent.putExtra(showTypeTitle, showType)
            intent.putExtra(selectedFeedChannelTitle, selectedFeedChannel)
            activity.startActivityFromFragment(fragment, intent, requestCode)
        }
    }

    private fun registerEvent() {
        RxBus.get().toFlowable().subscribe {
            when (it) {
                is SubscribeEvent -> {
                    fetchData(false)
                }
                is LoginSuccessEvent -> {
                    if (!::mTopicCategoryAdapter.isInitialized) return@subscribe
                    mTopicCategoryAdapter.onLoginSubscribe()
                }
            }
        }.addTo(compositeDisposable)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        spider.pageTracer().reportPageCreated(this)
        spider.pageTracer().reportPageShown(this, "ssr://channel/recommendation", "")
        bindLinkParams()
        mImmersionBar = ImmersionBar.with(this)
        mImmersionBar.init()

        setContentView(R.layout.activity_topic_category)

        registerEvent()
        topicBack.setOnClickListener(this)

        mShowType = intent?.getIntExtra(showTypeTitle, showTypeChannel) ?: showTypeChannel

        page = intent?.getStringExtra(pageTitle)
        if (page == pageSubscribed) {
            mShowType = showTypeSubscribe
        }

        if(intent.hasExtra(categoryIdExtra)) {
            this.categoryId = intent.getStringExtra(categoryIdExtra)
        }

        mTitlesLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        mTopicCategoryTitleAdapter = TopicCategoryTitleAdapter(this)

        topicCategoryTitles.layoutManager = mTitlesLayoutManager
        topicCategoryTitles.adapter = mTopicCategoryTitleAdapter

        mTopicCategoryAdapter = TopicCategoryAdapter(apiService, this, mShowType)

        topicCategoryItems.adapter = mTopicCategoryAdapter


        mTopicCategoryAdapter.setTopicItemClickListener(object : TopicCategoryAdapter.OnTopicItemClickListener {
            override fun onSubscribeStatusClick(view: View, feedChannel: FeedChannel): Boolean {
                if (mShowType == showTypeSelect) {
                    view.isSelected = true
                    setResult(Activity.RESULT_OK, Intent().putExtra(resultDataFeedChannel, feedChannel))
                    finish()
                    return true
                } else {
                    return false
                }
            }

            override fun onTopicItemClick(view: View, feedChannel: FeedChannel) {
                when (mShowType) {
                    showTypeChannel,
                    showTypeSubscribe -> {
                        feedChannel.redirect_url?.let {
                            startActivity(it)
                        }
                    }
                    showTypeSelect -> {
                        view.isSelected = true
                        setResult(Activity.RESULT_OK, Intent().putExtra(resultDataFeedChannel, feedChannel))
                        finish()
                    }
                }
            }
        })

        mTopicCategoryTitleAdapter.setOnItemClickListener { adapter, view, position ->
            val viewTag = view.tag as? TopicCategory
            viewTag?.let {
                mTopicCategoryTitleAdapter.setSelectedItem(it)
                setTopicSelection(it)
            }
            spider.manuallyEvent(SpiderEventNames.ALL_CHANNEL_CATEGORY_CLICK)
                    .put("userID", getUserId())
                    .put("categoryName", viewTag?.name ?: "")
                    .track()
        }

        topicCategoryItems.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                val firstVisibleView = topicCategoryItems.getChildAt(0)
                val firstVisibleViewTag = firstVisibleView?.getTag(R.id.tagTopicCategory) as? TopicCategory
                firstVisibleViewTag?.let {
                    mTopicCategoryTitleAdapter.setSelectedItem(it)
                }
            }

            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
            }
        })

        refreshLayout.setOnRefreshListener {
            fetchData(false)
        }

        refreshLayout.setOnChildScrollUpCallback { parent, child ->
            ListViewCompat.canScrollList(topicCategoryItems, -1)
        }

        topicBack.setOnClickListener(this)

        fetchData()
    }


    private fun fetchData(isInitSelection: Boolean = true) {
        refreshLayout.isRefreshing = true
        apiService.getTopicCategory()
                .applySchedulers()
                .subscribeApi(
                        onNext = {
                            topicCategoryTitles.setBackgroundResource(R.drawable.bg_topic_category_titles)
                            mTopicCategoryTitleAdapter.setNewData(it)
                            mTopicCategoryAdapter.setNewData(it)

                            if (isInitSelection) {
                                when (mShowType) {
                                    showTypeChannel -> {
                                        initSelectionItemByTitle(getString(R.string.string_topic_hot), it)
                                    }
                                    showTypeSubscribe -> {
                                        initSelectionItemByTitle(getString(R.string.string_topic_subscribe), it)
                                    }
                                    showTypeSelect -> {
                                        val selectedFeedChannel: FeedChannel? = intent?.getParcelableExtra(selectedFeedChannelTitle)
                                        if (selectedFeedChannel != null) {
                                            initSelectionItemByFeedChannel(selectedFeedChannel, it)
                                        } else {
                                            initSelectionItemByTitle(getString(R.string.string_topic_hot), it)
                                        }
                                    }
                                }
                            }

                            selectionToCategoryWithId(it)
                            refreshLayout.isRefreshing = false
                        },
                        onApiError = {
                            refreshLayout.isRefreshing = false
                        }

                )
    }

    private fun initSelectionItemByTitle(selectedName: String, topicCategories: List<TopicCategory>) {
        topicCategories.forEach { topicCategory ->
            if (topicCategory.name.contains(selectedName, true)) {
                mTopicCategoryTitleAdapter.setSelectedItem(topicCategory)
                setTopicSelection(topicCategory)
                return@forEach
            }
        }
    }

    private fun initSelectionItemByFeedChannel(selectFeedChannel: FeedChannel, topicCategories: List<TopicCategory>) {

        for (topicCategory in topicCategories) {
            if (topicCategory.name.contains(getString(R.string.string_topic_hot), true)) {
                mTopicCategoryTitleAdapter.setSelectedItem(topicCategory)
                setTopicSelection(topicCategory)
            }

            if (topicCategory.id == selectFeedChannel.categroyId) {
                for (feedChannel in topicCategory.feed_channels) {
                    if (feedChannel.id == selectFeedChannel.id) {
                        val topicCategoryPosition = mTopicCategoryAdapter.mWrapperData.indexOf(feedChannel)
                        if (topicCategoryPosition != -1) {
                            mTopicCategoryAdapter.setSelection(feedChannel)
                        }
                        return
                    }
                }
            }
        }
    }

    private fun selectionToCategoryWithId(topicCategories: List<TopicCategory>) {
        categoryId?.let {
            for (topicCategory in topicCategories) {
                if (topicCategory.id.toString() == it) {
                    setTopicSelection(topicCategory)
                }
            }
        }
    }


    private fun setTopicSelection(topicCategory: TopicCategory) {
        val topicCategoryPosition = mTopicCategoryAdapter.mWrapperData.indexOf(topicCategory)
        if (topicCategoryPosition != -1) {
            topicCategoryItems.setSelection(topicCategoryPosition)
        }
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.topicBack -> {
                onBackPressed()
            }
        }
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        mImmersionBar.destroy()
        super.onDestroy()
    }

}