package com.shuashuakan.android.modules.profile

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import com.jude.easyrecyclerview.EasyRecyclerView
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter
import com.jude.easyrecyclerview.decoration.DividerDecoration
import com.shuashuakan.android.R
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.account.FocusModel
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.event.FeedFollowChangeEvent
import com.shuashuakan.android.event.FocusListRefreshEvent
import com.shuashuakan.android.event.RefreshProfileEvent
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.account.activity.LoginActivity
import com.shuashuakan.android.modules.profile.adapter.FocusListAdapter
import com.shuashuakan.android.modules.profile.presenter.FocusListApiView
import com.shuashuakan.android.modules.profile.presenter.FocusListPresenter
import com.shuashuakan.android.modules.widget.EmptyView
import com.shuashuakan.android.modules.widget.FollowButton
import com.shuashuakan.android.modules.widget.SpiderAction
import com.shuashuakan.android.ui.base.FishActivity
import com.shuashuakan.android.utils.*
import com.shuashuakan.android.utils.extension.showCancelFollowDialog
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import me.twocities.linker.annotations.Link
import me.twocities.linker.annotations.LinkQuery
import javax.inject.Inject

//我的粉丝，我的关注列表
@Link("ssr://user/connection")
class FocusListActivity : FishActivity(), FocusListApiView<List<FocusModel>>, FocusListAdapter.BtnClick {

    private val toolbar by bindView<Toolbar>(R.id.toolbar)
    private val recycler by bindView<EasyRecyclerView>(R.id.recycler)
    private val emptyView by bindView<EmptyView>(R.id.empty_view)

    @Inject
    lateinit var focusPresenter: FocusListPresenter

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var accountManager: AccountManager
//  @Inject
//  lateinit var spider: Spider

    @LinkQuery("user_id")
    @JvmField
    var userId: String? = null

    var adapterUserId: String? = ""
    private var isMine: Boolean = true

    @LinkQuery("type")
    @JvmField
    var type: String? = null

    private lateinit var adapter: FocusListAdapter

    private var pages: Int = 0
    private var requestType = REQUEST_TYPE_REFRESH
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    companion object {
        private const val REQUEST_TYPE_REFRESH = 1
        private const val REQUEST_TYPE_LOAD_MORE = 2

        const val EXTRA_FOCUS_INFO_ID = "user_id"
        const val EXTRA_FOCUS_INFO_TYPE = "type"

        const val FOCUS_TYPE = "follow_list"
        const val FANS_TYPE = "fans_list"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fans_list)
        spider.pageTracer().reportPageCreated(this)
        bindLinkParams()
        focusPresenter.attachView(this)
        getExtra()
        setToolbar()
        initView()
        refreshData()
    }

    private fun initView() {
        recycler.setLayoutManager(LinearLayoutManager(this))

        if (accountManager.hasAccount()) {
            adapterUserId = accountManager.account()!!.userId.toString()
        }

        adapter = FocusListAdapter(this, this, adapterUserId)
        adapter.setNoMore(R.layout.view_new_no_more)
        recycler.setRefreshListener {
            refreshData()
        }
        adapter.setMore(R.layout.view_more, object : RecyclerArrayAdapter.OnMoreListener {
            override fun onMoreClick() {
            }

            override fun onMoreShow() {
                loadMoreData()
            }
        })

        recycler.addItemDecoration(DividerDecoration(this.resources.getColor(R.color.colorAccent), dip(0.5f)))
        recycler.adapter = adapter

        setEmpty()
    }

    private fun setEmpty() {
        if (type == FOCUS_TYPE) {
            emptyView.setTitle(getString(R.string.no_attention))
            if (isMine) {
                emptyView.setContent(getString(R.string.no_attention_mine))
            } else {
                emptyView.setContent(getString(R.string.no_attention_other))
            }
        } else {
            emptyView.setTitle(getString(R.string.no_fans))
            if (isMine) {
                emptyView.setContent(getString(R.string.no_fans_mine))
            } else {
                emptyView.setContent(getString(R.string.no_fans_other))
            }
        }
    }

    private fun refreshData() {
        requestType = REQUEST_TYPE_REFRESH
        focusPresenter.requestApi(type, isMine, 0, userId)
    }

    private fun loadMoreData() {
        pages++
        requestType = REQUEST_TYPE_LOAD_MORE
        focusPresenter.requestApi(type, isMine, pages, userId)
    }

    private fun getExtra() {
        if (accountManager.hasAccount()) {
            isMine = accountManager.account()!!.userId.toString() == userId
        } else {
            isMine = false
        }
    }


    private fun setToolbar() {
        toolbar.background = ColorDrawable(resources.getColor(R.color.colorPrimaryDark))
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        if (type == FOCUS_TYPE) {
            if (isMine) {
                toolbar.title = getString(R.string.string_owner_follow)
            } else {
                toolbar.title = getString(R.string.string_other_follow)
            }
        } else {
            if (isMine) {
                toolbar.title = getString(R.string.string_owner_fans)
            } else {
                toolbar.title = getString(R.string.string_other_fans)
            }
        }
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

    }


    override fun showData(data: List<FocusModel>) {
        if (requestType == REQUEST_TYPE_REFRESH) {//刷新成功
            pages = 0
            adapter.clear()
            adapter.addAll(data)
            adapter.notifyDataSetChanged()

            if (data.isEmpty()) {
                ViewHelper.crossfade(emptyView, recycler)
            } else {
                ViewHelper.crossfade(recycler, emptyView)
            }

        } else {
            adapter.addAll(data)
            adapter.notifyDataSetChanged()
        }
    }

    override fun showError() {
        recycler.showError()
    }

    override fun showMessage(message: String) {
        showLongToast(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        focusPresenter.detachView(false)
        compositeDisposable.clear()
    }

    override fun theClick(data: FocusModel, followBtn: FollowButton, userId: String) {
        if (!accountManager.hasAccount()) {
            LoginActivity.launch(this)
            return
        }

        if (followBtn.isFollow) {
            val cancelFollow = {
                apiService.cancelFollow(userId).applySchedulers().subscribeApi(onNext = {
                    if (it.result.isSuccess) {
                        FollowCacheManager.putFollowUserToCache(userId, false)
                        followBtn.setFollowStatus(false, data.is_fans)
                        data.follow = false
                        RxBus.get().post(FeedFollowChangeEvent(userId, false))
                        RxBus.get().post(RefreshProfileEvent())
                        spider.userFollowEvent(this, userId.toString(),
                                SpiderAction.VideoPlaySource.FAN_LIST.source, false)
                    } else {
                        data.follow = true
                        showLongToast(getString(R.string.string_un_follow_error))
                    }
                }, onApiError = {
                    data.follow = true
                    if (it is ApiError.HttpError) {
                        showLongToast(it.displayMsg)
                    } else {
                        showLongToast(getString(R.string.string_un_follow_error))
                    }
                })
            }
            showCancelFollowDialog(data.nickName, cancelFollow)
        } else {
            apiService.createFollow(userId).applySchedulers().subscribeApi(onNext = {
                if (it.result.isSuccess) {
                    FollowCacheManager.putFollowUserToCache(userId, true)
                    followBtn.setFollowStatus(true)
                    data.follow = true
                    RxBus.get().post(FeedFollowChangeEvent(userId, true))
                    RxBus.get().post(RefreshProfileEvent())
                    spider.userFollowEvent(this, userId,
                            SpiderAction.VideoPlaySource.FAN_LIST.source, true)
                } else {
                    data.follow = false
                    showLongToast(getString(R.string.string_attention_error))
                }
            }, onApiError = {
                data.follow = false
                if (it is ApiError.HttpError) {
                    showLongToast(it.displayMsg)
                } else {
                    showLongToast(getString(R.string.string_attention_error))
                }
            })
        }
    }

    //头像点击
    override fun avatarClick(userId: String) {
        startActivity(Intent(this, UserProfileActivity::class.java)
                .putExtra("id", userId)
                .putExtra("source",
                        if (type == FOCUS_TYPE)
                            SpiderAction.PersonSource.FOLLOW_LIST.source
                        else
                            SpiderAction.PersonSource.FAN_LIST.source))
    }

    override fun onResume() {
        super.onResume()
        if (userId != null) {
            var strSrr = ""
            strSrr = if (type == FOCUS_TYPE) {
                "ssr://user/follow_list?user_id=" + userId
            } else {
                "ssr://user/fans_list?user_id=" + userId
            }
            spider.pageTracer().reportPageShown(this, strSrr, "")
        }
        RxBus.get().toFlowable(FocusListRefreshEvent::class.java).subscribe {
            refreshData()
        }.addTo(compositeDisposable)
    }
}
