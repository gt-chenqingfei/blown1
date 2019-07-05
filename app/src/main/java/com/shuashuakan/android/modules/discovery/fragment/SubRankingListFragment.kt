package com.shuashuakan.android.modules.discovery.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.shuashuakan.android.R
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.explore.RankListModel
import com.shuashuakan.android.data.api.model.explore.RankingListModel
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.event.FocusListRefreshEvent
import com.shuashuakan.android.event.LoginSuccessEvent
import com.shuashuakan.android.event.RefreshRankingEvent
import com.shuashuakan.android.event.SubscribeEvent
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.ACCOUNT_PAGE
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.discovery.RankingListActivity
import com.shuashuakan.android.modules.discovery.presenter.RankingListApiView
import com.shuashuakan.android.modules.discovery.presenter.RankingListPresenter
import com.shuashuakan.android.modules.profile.UserProfileActivity
import com.shuashuakan.android.modules.widget.EmptyView
import com.shuashuakan.android.modules.widget.FollowButton
import com.shuashuakan.android.modules.widget.SpiderAction
import com.shuashuakan.android.modules.widget.loadmoreview.SskLoadMoreView
import com.shuashuakan.android.modules.widget.ranktop.RankingTopView
import com.shuashuakan.android.spider.Spider
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.ui.base.FishFragment
import com.shuashuakan.android.utils.*
import com.shuashuakan.android.utils.extension.showCancelFollowDialog
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * 榜单的子页面（日榜、周榜）
 */
class SubRankingListFragment : FishFragment(), RankingListApiView<RankingListModel>, SwipeRefreshLayout.OnRefreshListener, BaseQuickAdapter.RequestLoadMoreListener {

    private val refresh by bindView<SwipeRefreshLayout>(R.id.refresh)
    private val recycler by bindView<RecyclerView>(R.id.recycler)
    private var tabName: String? = null
    private var page: Int = 0
    private var channelId: String? = null
    private var categoryId: String? = null
    private val errorView by bindView<View>(R.id.error_view)
    private val emptyLayout by bindView<View>(R.id.empty_layout)
    private val emptyView by bindView<EmptyView>(R.id.empty_view)
    private lateinit var adapter: BaseQuickAdapter<RankListModel, BaseViewHolder>
    @Inject
    lateinit var presenter: RankingListPresenter
    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var accountManager: AccountManager
    @Inject
    lateinit var spider: Spider
    private val compositeDisposable = CompositeDisposable()
    private var totalCount: Int = 0
    private var ruleLink: String? = null
    private lateinit var callBackToActivity: CallBackToActivity

    companion object {
        private const val EXTRA_TAB_NAME = "extra_tab_name"
        const val EXTRA_CHANNEL_ID = "extra_tab_channel"
        const val EXTRA_CATEGORY_ID = "extra_tab_category"
        fun create(tabName: String): SubRankingListFragment {
            return this.create(tabName, null, null)
        }

        fun create(tabName: String, channelId: String?, categoryId: String?): SubRankingListFragment {
            val fragment = SubRankingListFragment()
            val bundle = Bundle()
            bundle.putString(EXTRA_TAB_NAME, tabName)
            bundle.putString(EXTRA_CHANNEL_ID, channelId)
            bundle.putString(EXTRA_CATEGORY_ID, categoryId)

            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        arguments?.let {
            tabName = it.getString(EXTRA_TAB_NAME)
            channelId = it.getString(EXTRA_CHANNEL_ID)
            categoryId = it.getString(EXTRA_CATEGORY_ID)
        }
        initView()
        onRefresh()
    }

    private fun initView() {
        errorView.setOnClickListener {
            errorView.visibility = View.GONE
            onRefresh()
        }
        emptyView.setTitle(getString(R.string.string_not_has_data))
        recycler.layoutManager = LinearLayoutManager(requireActivity())

        adapter = object : BaseQuickAdapter<RankListModel, BaseViewHolder>(R.layout.item_ranking2) {
            override fun convert(helper: BaseViewHolder, item: RankListModel) {
                val followButton = helper.getView<FollowButton>(R.id.follow_btn)
                val followTv = helper.getView<TextView>(R.id.sub_title_follow)
                val avatarImage = helper.getView<SimpleDraweeView>(R.id.image)
                val sameTv = helper.getView<TextView>(R.id.same_friend_tv)
                val identificationImage = helper.getView<SimpleDraweeView>(R.id.identification)
                helper.setText(R.id.title, item.nickName)
                        .setText(R.id.sub_title_up, String.format(getString(com.shuashuakan.android.base.ui.R.string.string_up_value_format),
                                numFormat(item.upCount)))
                        .setText(R.id.sub_title_follow, String.format(getString(com.shuashuakan.android.base.ui.R.string.string_follow_number_format),
                                numFormat(item.fansCount)))
                avatarImage.setImageUrl2Webp(item.avatar, requireActivity().dip(60), requireActivity().dip(60))
                item.tags?.takeIf { it.isNotEmpty() }.apply {
                    identificationImage.setImageURI(this?.get(0)?.icon)
                }

                if (helper.adapterPosition + 3 > 10) {
                    helper.getView<TextView>(R.id.rank_num).visibility = View.GONE
                } else {
                    helper.getView<TextView>(R.id.rank_num).visibility = View.VISIBLE
                }

                helper.setText(R.id.rank_num, "${helper.adapterPosition + 3}")
                followButton.setFollowStatus(item.isFollow, item.is_fans)
                if (item.properties?.commonFollowDes != null) {
                    followTv.visibility = View.GONE
                    sameTv.visibility = View.VISIBLE
                    sameTv.text = item.properties?.commonFollowDes
                } else {
                    if (item.fansCount == 0)
                        followTv.visibility = View.GONE
                    else
                        followTv.visibility = View.VISIBLE
                    sameTv.visibility = View.GONE
                }
                helper.itemView.noDoubleClick {
                    requireActivity().startActivity(item.redirectUrl)
                }
                avatarImage.noDoubleClick {
                    requireActivity().startActivity(
                            Intent(requireActivity(), UserProfileActivity::class.java)
                                    .putExtra("id", item.userId.toString())
                                    .putExtra("source", SpiderAction.PersonSource.RANKING_LIST.source))
                }
                followButton.noDoubleClick {
                    if (!accountManager.hasAccount()) {
                        waitFollow = item.isFollow
                        waitUserId = item.userId.toString()
                        waitFollowButton = followButton
                        waitItem = item
                        waitFollowTv = followTv
                        requireActivity().startActivity(ACCOUNT_PAGE)
                        return@noDoubleClick
                    }
                    setFollowClick(item.isFollow, item.userId.toString(), followButton, item, followTv) {}
                }
            }
        }
        adapter.setOnLoadMoreListener(this, recycler)
        adapter.setLoadMoreView(SskLoadMoreView(SskLoadMoreView.IMAGE))
        recycler.adapter = adapter
        refresh.setOnRefreshListener(this)
        registerEvent()
    }

    private fun registerEvent() {
        RxBus.get().toFlowable().subscribe {
            when (it) {
                is FocusListRefreshEvent -> {
                    page = 0
                    refresh.isRefreshing = true
                    requestApi()
                }
                is LoginSuccessEvent -> {
                    onLoginActionToFollow()
                }
                is RefreshRankingEvent -> {
                    spider.manuallyEvent(SpiderEventNames.LEADER_BOARD_EXPOSURE)
                            .put("boardType", if (tabName == getString(R.string.string_week_top)) "Week" else "Day")
                            .put("userID", requireContext().getUserId())
                            .track()
                    if (RankingListActivity.clickBtn) {
                        page = 0
                        refresh.isRefreshing = true
                        requestApi()
                    }
                }
            }
        }.addTo(compositeDisposable)
    }

    private fun setFollowClick(follow: Boolean, userId: String, followButton: FollowButton,
                               item: RankListModel, followTv: TextView, followSuccess: () -> Unit) {
        if (follow) {
            val cancelFollow = {
                apiService.cancelFollow(userId).applySchedulers().subscribeApi(onNext = {
                    followSuccess.invoke()
                    if (it.result.isSuccess) {
                        FollowCacheManager.putFollowUserToCache(userId, false)
                        RankingListActivity.clickBtn = true
                        followButton.setFollowStatus(false, item.is_fans)
                        item.isFollow = false
                        item.fansCount = item.fansCount!! - 1
                        followTv.text = String.format(getString(com.shuashuakan.android.base.ui.R.string.string_follow_number_format),
                                numFormat(item.fansCount))
                        RxBus.get().post(SubscribeEvent())
                        spider.manuallyEvent(SpiderEventNames.USER_FOLLOW)
                                .put("userID", accountManager.account()?.userId ?: 0)
                                .put("TargetUserID", userId)
                                .put("method", "unfollow")
                                .track()
                    } else {
                        requireActivity().showLongToast(getString(R.string.string_un_follow_error))
                    }
                }, onApiError = {
                    followSuccess.invoke()
                    if (it is ApiError.HttpError) {
                        requireActivity().showLongToast(it.displayMsg)
                    } else {
                        requireActivity().showLongToast(getString(R.string.string_un_follow_error))
                    }
                })
            }
            activity?.showCancelFollowDialog(item.nickName, cancelFollow)
        } else {
            apiService.createFollow(userId).applySchedulers().subscribeApi(onNext = {
                if (it.result.isSuccess) {
                    FollowCacheManager.putFollowUserToCache(userId, true)
                    RankingListActivity.clickBtn = true
                    followButton.setFollowStatus(true, item.is_fans)
                    item.isFollow = true
                    item.fansCount = item.fansCount!! + 1
                    followTv.text = String.format(getString(com.shuashuakan.android.base.ui.R.string.string_follow_number_format),
                            numFormat(item.fansCount))
                    RxBus.get().post(SubscribeEvent())
                    spider.manuallyEvent(SpiderEventNames.USER_FOLLOW)
                            .put("userID", accountManager.account()?.userId ?: 0)
                            .put("TargetUserID", userId)
                            .put("method", "follow")
                            .track()
                } else {
                    requireActivity().showLongToast(getString(R.string.string_attention_error))
                }
            }, onApiError = {
                if (it is ApiError.HttpError) {
                    requireActivity().showLongToast(it.displayMsg)
                } else {
                    requireActivity().showLongToast(getString(R.string.string_attention_error))
                }
            })
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sub_ranking_list, container, false)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        presenter.attachView(this)
        callBackToActivity = requireActivity() as CallBackToActivity
    }

    override fun onDetach() {
        super.onDetach()
        presenter.detachView(false)
    }

    override fun showData(data: RankingListModel) {
        ruleLink = data.link.redirectUrl
        callBackToActivity.sendDataToActivity(ruleLink ?: "", data.title ?: "")
        val dataList = data.dataList
        totalCount += dataList.size
        if (refresh.isRefreshing) {
            setHeader(dataList)
            if (dataList.size > 3)
                adapter.setNewData(dataList.subList(3, dataList.size))
            refresh.isRefreshing = false
        } else {
            adapter.addData(dataList)
            if (dataList.size < 10) {
                adapter.loadMoreEnd()
            } else {
                adapter.loadMoreComplete()
            }
        }
        if (totalCount == 0) {
            ViewHelper.crossfade(emptyLayout, errorView, recycler)
        } else {
            ViewHelper.crossfade(recycler, errorView, emptyLayout)
        }
        if (RankingListActivity.clickBtn) {
            RankingListActivity.clickBtn = false
        }
    }

    private fun setHeader(data: List<RankListModel>) {
        val headerView = layoutInflater.inflate(R.layout.layout_ranking_header, recycler, false)
        val topView = headerView.findViewById<RankingTopView>(R.id.ranking_top_view)
        topView.setData(data, apiService, spider, accountManager)
        adapter.setHeaderView(headerView)
    }

    override fun showMessage(message: String) {
        if (refresh.isRefreshing) {
            refresh.isRefreshing = false
            ViewHelper.crossfade(errorView, recycler, emptyLayout)
        } else {
            adapter.loadMoreFail()
        }
    }

    override fun onLoadMoreRequested() {
        page++
        requestApi()
    }

    override fun onRefresh() {
        totalCount = 0
        refresh.isRefreshing = true
        page = 0
        requestApi()
    }

    private fun requestApi() {
        presenter.requestApi(page, tabName, channelId, categoryId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
    }


    private var waitFollow: Boolean? = null
    private var waitUserId: String? = null
    private var waitFollowButton: FollowButton? = null
    private var waitItem: RankListModel? = null
    private var waitFollowTv: TextView? = null
    private fun onLoginActionToFollow() {
        waitFollow ?: return
        waitUserId ?: return
        waitFollowButton ?: return
        waitItem ?: return
        waitFollowTv ?: return
        setFollowClick(waitFollow!!, waitUserId!!,
                waitFollowButton!!, waitItem!!, waitFollowTv!!) {
            waitFollow = null
            waitUserId = null
            waitFollowButton = null
            waitItem = null
            waitFollowTv = null
        }

    }


}

interface CallBackToActivity {
    fun sendDataToActivity(url: String, title: String)
}
