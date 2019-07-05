package com.shuashuakan.android.modules.profile.fragment

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.shuashuakan.android.R
import com.shuashuakan.android.config.AppConfig
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.event.ShareBoardDeleteFeedEvent
import com.shuashuakan.android.event.UpdateLikeFeedListEvent
import com.shuashuakan.android.modules.FeedTransportManager
import com.shuashuakan.android.modules.player.activity.VideoPlayActivity
import com.shuashuakan.android.modules.profile.presenter.LikedFeedListApiView
import com.shuashuakan.android.modules.profile.presenter.LikedFeedListPresenter
import com.shuashuakan.android.modules.widget.EmptyView
import com.shuashuakan.android.modules.widget.loadmoreview.SskLoadMoreView
import com.shuashuakan.android.ui.base.FishFragment
import com.shuashuakan.android.utils.*
import com.shuashuakan.android.utils.ViewHelper.crossfade
import com.shuashuakan.android.widget.GridDivider
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class SubFeedListFragment : FishFragment(), LikedFeedListApiView<List<Feed>> {
    @Inject
    lateinit var presenter: LikedFeedListPresenter
    @Inject
    lateinit var appConfig: AppConfig
    private val recyclerView by bindView<RecyclerView>(R.id.recycler_view)
    private val emptyView by bindView<EmptyView>(R.id.empty_view)
    private val errorView by bindView<View>(R.id.error_view)
    private val loadingBar by bindView<View>(R.id.loadingBar)
    private var isRefreshing: Boolean = false
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var adapter: BaseQuickAdapter<Feed, BaseViewHolder>
    private var currentPage: Int = 0

    companion object {
        private const val EXTRA_USER_ID = "EXTRA_USER_ID"
        private const val EXTRA_IS_MINE = "EXTRA_IS_MINE"

        fun create(userId: String, isMine: Boolean): SubFeedListFragment {
            val fragment = SubFeedListFragment()
            val argument = Bundle()
            argument.putBoolean(EXTRA_IS_MINE, isMine)
            argument.putString(EXTRA_USER_ID, userId)
            fragment.arguments = argument
            return fragment
        }
    }

    private lateinit var userId: String
    private var isMine: Boolean = false

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_sub_upload_list, container, false)


    override fun showData(data: List<Feed>) {
        currentPage++
        if (isRefreshing) {
            isRefreshing = false
            adapter.setNewData(data)
        } else {
            adapter.addData(data)
            if (data.isEmpty()) {
                adapter.loadMoreEnd(true)
            } else {
                adapter.loadMoreComplete()
            }
        }
        if (adapter.data.isNotEmpty()) {
            crossfade(recyclerView, emptyView, loadingBar, errorView)
        } else {
            crossfade(emptyView, recyclerView, loadingBar, errorView)
        }
    }

    override fun showMessage(message: String) {
        requireActivity().showLongToast(message)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        userId = arguments?.getString(EXTRA_USER_ID) ?: ""
        isMine = arguments?.getBoolean(EXTRA_IS_MINE, false) ?: false
        setupView()
        crossfade(loadingBar, recyclerView, emptyView, errorView)
        refreshData()
        RxBus.get().toFlowable().subscribe {
            when (it) {
                is UpdateLikeFeedListEvent -> refreshData()
                is ShareBoardDeleteFeedEvent -> refreshData()
            }
        }.addTo(compositeDisposable)
    }

    private fun refreshData() {
        if (!isRefreshing) {
            currentPage = 0
            isRefreshing = true
            requestApi()
        }
    }

    private fun setupView() {
        recyclerView.addItemDecoration(GridDivider(requireContext().dip(10), 3))
        recyclerView.setPadding(requireContext().dip(15), 0, requireContext().dip(15), 0)
        val layoutManager = GridLayoutManager(requireActivity(), 3)
        recyclerView.layoutManager = layoutManager

        adapter = object : BaseQuickAdapter<Feed, BaseViewHolder>(R.layout.liked_list_video) {
            override fun convert(helper: BaseViewHolder, feed: Feed) {
                val imageView = helper.getView<SimpleDraweeView>(R.id.image_view)
                var itemHeight: Int = 0
                val width = requireActivity().getScreenSize().x / 3f - requireActivity().dip(10f)
                val height = width / 0.75
                itemHeight = height.toInt()
                helper.itemView.layoutParams.height = itemHeight
                imageView.aspectRatio = 3 / 4f
                if (!feed.animationCover.isNullOrEmpty() && isWifiConnected(requireActivity())) {
                    imageView.setGifImage(feed.animationCover)
                } else {
                    imageView.setImageURI(feed.cover)
                }
                helper.getView<TextView>(R.id.liked_number_view).text = numFormat4Profile(feed.favNum)
                helper.itemView.setOnClickListener {
                    val intentParam = FeedTransportManager.jumpToVideoFromPersonalUp(currentPage, feed, adapter.data.toList())
                    startActivity(VideoPlayActivity.create(requireActivity(), intentParam, isMine))

                }
//        val chainIcon = helper.getView<ImageView>(R.id.chains_icon)

//        if (feed.masterFeedId == feed.id) {
//          chainIcon.visibility = View.GONE
//        } else {
//          chainIcon.visibility = View.VISIBLE
//        }

            }
        }
        recyclerView.adapter = adapter
        adapter.setLoadMoreView(SskLoadMoreView())

        emptyView.setTitle(getString(R.string.string_no_empty_video))
        if (isMine) {
            emptyView.setContent(getString(R.string.string_up_fa_video))
        } else {
            emptyView.setContent(getString(R.string.string_other_no_up_video))
        }

        errorView.setOnClickListener {
            crossfade(loadingBar, recyclerView, emptyView, errorView)
            refreshData()
        }
        adapter.setOnLoadMoreListener({
            requestApi()
        }, recyclerView)
    }

    override fun showError() {
        isRefreshing = false
        crossfade(errorView, recyclerView, loadingBar, emptyView)
    }

    private fun requestApi() {
        presenter.requestApi(currentPage, isMine, userId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView.adapter = null
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        presenter.attachView(this)
    }

    override fun onDetach() {
        super.onDetach()
        presenter.detachView(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}