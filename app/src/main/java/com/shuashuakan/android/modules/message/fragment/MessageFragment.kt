package com.shuashuakan.android.modules.message.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.facebook.drawee.view.SimpleDraweeView
import com.gyf.barlibrary.ImmersionBar
import com.shuashuakan.android.R
import com.shuashuakan.android.commons.util.ACache
import com.shuashuakan.android.config.AppConfig
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.message.ActionUserInfoListItem
import com.shuashuakan.android.data.api.model.message.NewMessageRes
import com.shuashuakan.android.data.api.model.message.NormalMsgItemData
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.event.FocusListRefreshEvent
import com.shuashuakan.android.event.LoginSuccessEvent
import com.shuashuakan.android.event.RefreshProfileEvent
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.account.activity.LoginActivity
import com.shuashuakan.android.modules.message.MessageActivity
import com.shuashuakan.android.modules.message.SystemNoticeActivity
import com.shuashuakan.android.modules.message.adapter.MultiMessageAdapter
import com.shuashuakan.android.modules.message.badage.BadgeEvent
import com.shuashuakan.android.modules.message.badage.BadgeManager
import com.shuashuakan.android.modules.profile.UserProfileActivity
import com.shuashuakan.android.modules.widget.FollowButton
import com.shuashuakan.android.modules.widget.SpiderAction
import com.shuashuakan.android.modules.widget.loadmoreview.SskLoadMoreView
import com.shuashuakan.android.spider.Spider
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.ui.base.FishFragment
import com.shuashuakan.android.utils.FollowCacheManager
import com.shuashuakan.android.utils.bindView
import com.shuashuakan.android.utils.extension.showCancelFollowDialog
import com.shuashuakan.android.utils.getUserId
import com.shuashuakan.android.utils.showLongToast
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.*
import javax.inject.Inject

/**
 * Author:  lijie
 * Date:   2018/12/10
 * Email:  2607401801@qq.com
 */
class MessageFragment : FishFragment(), MultiMessageAdapter.OnMessageCenterListener, SwipeRefreshLayout.OnRefreshListener {
    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var spider: Spider
    @Inject
    lateinit var accountManager: AccountManager
    @Inject
    lateinit var appConfig: AppConfig
    @Inject
    lateinit var badgeManager: BadgeManager

    private val recyclerView by bindView<RecyclerView>(R.id.recycler_view)
    private val swipeRefreshLayout by bindView<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
    private val messageTitle by bindView<TextView>(R.id.fragment_message_title)
    private val messageBack by bindView<ImageView>(R.id.iv_message_back)
    private val toolbar by bindView<Toolbar>(R.id.toolbar)
    //    private lateinit var errorView: View
    private lateinit var alertIcon: ImageView
    private lateinit var alertTitle: TextView
    private lateinit var alertMsg: TextView
    private lateinit var emptyView: View
    private lateinit var headerView: View

    private var notify_id: Long? = null
    private lateinit var adapter: MultiMessageAdapter
    private val totalList: MutableList<MultiItemEntity> = mutableListOf()
    private lateinit var addCommentPopup: com.shuashuakan.android.modules.comment.AddCommentPopup


    private var redDot: View? = null
    private lateinit var headerContent: LinearLayout
    private lateinit var headerSysContentTv: TextView
    private lateinit var headerAvatar: SimpleDraweeView
    private lateinit var headerSysTitle: TextView

    private var personalRedDot: View? = null
    private lateinit var headerPersonalContent: LinearLayout
    private lateinit var headerPersonalSysContentTv: TextView
    private lateinit var headerPersonalAvatar: SimpleDraweeView
    private lateinit var headerPersonalSysTitle: TextView


    private val compositeDisposable = CompositeDisposable()

    companion object {

        fun create(): MessageFragment {
            return MessageFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_message, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initView()
        requestData()
        initListener()
        registerEvent()
        badgeManager.clearNonSystemBadge()
    }

    private fun registerEvent() {
        RxBus.get().toFlowable().subscribe {
            when (it) {
                is LoginSuccessEvent -> {
                    adapter.setNewData(null)
                    onRefresh()
                }
                is FocusListRefreshEvent -> {
                    adapter.setNewData(null)
                    swipeRefreshLayout.isRefreshing = true
                    onRefresh()
                }
                is BadgeEvent -> {
                    badgeManager.clearNonSystemBadge()
                }
            }
        }.addTo(compositeDisposable)
    }

    private fun initView() {
        ImmersionBar.setTitleBar(activity, toolbar)
        swipeRefreshLayout.setOnRefreshListener(this)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        adapter = MultiMessageAdapter(requireActivity(), null)
        adapter.setHeaderAndEmpty(true)
        recyclerView.adapter = adapter
        adapter.setCenterListener(this)
        adapter.setOnLoadMoreListener({
            swipeRefreshLayout.isRefreshing = false
            requestData()
        }, recyclerView)
        swipeRefreshLayout.isRefreshing = true
        adapter.setLoadMoreView(SskLoadMoreView())


        emptyView = layoutInflater.inflate(R.layout.empty_view, recyclerView, false)
        alertIcon = emptyView.findViewById(R.id.alertIcon)
        alertTitle = emptyView.findViewById(R.id.alertTitle)
        alertMsg = emptyView.findViewById(R.id.alertMsg)
        emptyView.setOnClickListener {
            swipeRefreshLayout.isRefreshing = true
            totalList.clear()
            notify_id = null
            requestData()
        }
        adapter.emptyView = emptyView

        if (!(appConfig.isShowNewHomePage() && activity is MessageActivity)) {
            toolbar.navigationIcon = null
        }

        toolbar.setNavigationOnClickListener {
            (activity as MessageActivity).finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
        addCommentPopup.destroy()
    }

    private fun requestData() {
        apiService.getNewMessageNotification(notify_id)
                .applySchedulers()
                .subscribeApi(onNext = {
                    notify_id = it.nextCursor?.maxId
                    setHeadView(it)
                    val list = it.notificationsList
                    val tempList: MutableList<MultiItemEntity> = mutableListOf()
                    if (list != null) {
                        for (item in list) {
                            val userCount = item.actionUserCount
                            when (item.type) {
                                "LIKE_FEED" -> {
                                    if (userCount == 1) {
                                        tempList.add(com.shuashuakan.android.modules.discovery.ItemDataPair(item, MultiMessageAdapter.SIMPLE_LAYOUT))
                                    } else if (userCount > 1) {
                                        tempList.add(com.shuashuakan.android.modules.discovery.ItemDataPair(item, MultiMessageAdapter.MULTIPLE_LAYOUT))
                                    }
                                }
                                "LIKE_COMMENT" -> {
                                    if (userCount == 1) {
                                        tempList.add(com.shuashuakan.android.modules.discovery.ItemDataPair(item, MultiMessageAdapter.SIMPLE_LAYOUT))
                                    } else if (userCount > 1) {
                                        tempList.add(com.shuashuakan.android.modules.discovery.ItemDataPair(item, MultiMessageAdapter.MULTIPLE_LAYOUT))
                                    }
                                }
                                "COMMENT" -> {
                                    tempList.add(com.shuashuakan.android.modules.discovery.ItemDataPair(item, MultiMessageAdapter.COMMENT))
                                }
                                "FOLLOW" -> {
                                    if (userCount == 1) {
                                        tempList.add(com.shuashuakan.android.modules.discovery.ItemDataPair(item, MultiMessageAdapter.SIMPLE_LAYOUT))
                                    } else if (userCount > 1) {
                                        tempList.add(com.shuashuakan.android.modules.discovery.ItemDataPair(item, MultiMessageAdapter.MULTIPLE_LAYOUT))
                                    }
                                }
                            }
                        }
                        if (swipeRefreshLayout.isRefreshing) {
                            adapter.setNewData(tempList)
                            swipeRefreshLayout.isRefreshing = false
                        } else {
                            adapter.addData(tempList)
                            if (tempList.size < 10) {
                                adapter.loadMoreEnd(false)
                            } else {
                                adapter.loadMoreComplete()
                            }
                        }
                    } else {
                        swipeRefreshLayout.isRefreshing = false
                    }
                    if (adapter.data.isEmpty()) {
                        emptyView.visibility = View.VISIBLE
                        alertIcon.setImageResource(R.drawable.ic_timeline_empty)
                        alertTitle.setText(R.string.string_no_message)
                        alertMsg.setText(R.string.string_please_go_to_solitaire)
                    }

                }, onApiError = {
                    if (adapter.headerLayoutCount > 0) {
                        adapter.removeAllHeaderView()
                    }
                    swipeRefreshLayout.isRefreshing = false

                    emptyView.visibility = View.VISIBLE
                    alertIcon.setImageResource(R.drawable.ic_network_error)
                    alertTitle.setText(R.string.string_load_error)
                    alertMsg.setText(R.string.string_check_net_with_click)

                })
    }


    private fun setHeadView(notice: NewMessageRes) {
        val headerData = notice.sysNotificationSummaries
        val personalData = notice.sysNotificationPersonal
        if (adapter.headerLayoutCount == 0) {
            setHeaderView()
        }

        if (headerData == null) {
            headerContent.visibility = View.GONE
            headerView.findViewById<View>(R.id.topLine).visibility = View.GONE
        } else {
            if (headerData.actionUserInfoList.isNotEmpty()) {
                headerAvatar.setImageURI(headerData.actionUserInfoList[0].avatar)
                headerSysTitle.text = headerData.actionUserInfoList[0].nickName
                headerSysContentTv.text = if (headerData.referenceItem?.title != null) headerData.referenceItem?.title else headerData.referenceItem?.content
            }
        }

        if (personalData == null) {
            headerPersonalContent.visibility = View.GONE
            headerView.findViewById<View>(R.id.bottomLine).visibility = View.GONE
        } else {
            if (personalData.actionUserInfoList.isNotEmpty()) {
                headerPersonalAvatar.setImageURI(personalData.actionUserInfoList[0].avatar)
                headerPersonalSysTitle.text = personalData.actionUserInfoList[0].nickName
                headerPersonalSysContentTv.text = if (personalData.referenceItem?.title != null)
                    personalData.referenceItem?.title else personalData.referenceItem?.content
            }
        }

        showBadge()
        if (adapter.headerLayoutCount == 0) {
            adapter.addHeaderView(headerView)
        }
    }

    private fun setHeaderView() {
        headerView = LayoutInflater.from(requireActivity()).inflate(R.layout.item_notice_system, recyclerView, false)
        headerContent = headerView.findViewById(R.id.container_ll)
        headerSysContentTv = headerView.findViewById(R.id.noticeSysContent)
        headerAvatar = headerView.findViewById(R.id.avatar)
        headerSysTitle = headerView.findViewById(R.id.sys_title)
        redDot = headerView.findViewById(R.id.sys_dot)

        headerPersonalContent = headerView.findViewById(R.id.personalContainer)
        headerPersonalSysContentTv = headerView.findViewById(R.id.personalNoticeSysContent)
        headerPersonalAvatar = headerView.findViewById(R.id.personalAvatar)
        headerPersonalSysTitle = headerView.findViewById(R.id.personalSysTitle)
        personalRedDot = headerView.findViewById(R.id.personalSysDot)

        headerContent.setOnClickListener {
            badgeManager.updateSystemBadge(false)
            SystemNoticeActivity.launcher(requireContext(), SystemNoticeActivity.NOTIFICATION_SYSTEM)
        }
        headerPersonalContent.setOnClickListener {
            badgeManager.updateSystemPersonalBadge(false)
            spider.manuallyEvent(SpiderEventNames.SHUASHUA_AUTHOR).track()
            SystemNoticeActivity.launcher(requireContext(), SystemNoticeActivity.NOTIFICATION_PERSONAL)
        }
    }


    override fun onAvatarClick(user_id: String) {
        requireActivity().startActivity(Intent(requireActivity(), UserProfileActivity::class.java)
                .putExtra("id", user_id)
                .putExtra("source", SpiderAction.PersonSource.MESSAGE_CENTER.source))
    }

    override fun onFollowClick(button: FollowButton, user: ActionUserInfoListItem) {
        if (!accountManager.hasAccount()) {
            LoginActivity.launch(requireContext())
            return
        }

        if (button.isFollow) {
            val cancelFollow = {
                apiService.cancelFollow(user.userId).applySchedulers().subscribeApi(onNext = {
                    if (it.result.isSuccess) {
                        button.setFollowStatus(false, user.isFans)
                        FollowCacheManager.putFollowUserToCache(user.userId, false)
                        RxBus.get().post(RefreshProfileEvent())

                        spider.manuallyEvent(SpiderEventNames.USER_FOLLOW)
                                .put("userID", requireContext().getUserId())
                                .put("TargetUserID", user.userId)
                                .put("method", "unfollow")
                                .track()
                    } else {
                        requireActivity().showLongToast(getString(R.string.string_un_follow_error))
                    }
                }, onApiError = {
                    if (it is ApiError.HttpError) {
                        requireActivity().showLongToast(it.displayMsg)
                    } else {
                        requireActivity().showLongToast(getString(R.string.string_un_follow_error))
                    }
                })
            }
            activity?.showCancelFollowDialog(user.nickName, cancelFollow)
        } else {
            apiService.createFollow(user.userId).applySchedulers().subscribeApi(onNext = {
                if (it.result.isSuccess) {
                    button.setFollowStatus(true)
                    FollowCacheManager.putFollowUserToCache(user.userId, true)
                    RxBus.get().post(RefreshProfileEvent())
                    spider.manuallyEvent(SpiderEventNames.USER_FOLLOW)
                            .put("userID", requireContext().getUserId())
                            .put("TargetUserID", user.userId)
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

    override fun onItemClick(data: NormalMsgItemData, position: Int) {
        if (data.referenceItem?.content != null && data.actionUserInfoList.isNotEmpty()) {
            addCommentPopup.reply(recyclerView, data.referenceItem?.targetId, data.actionUserInfoList[0].nickName, position)
        }
    }

    private fun initListener() {
        val defaultCommentList: ArrayList<*>? = ACache.get(requireActivity()).getAsObject(ACache.KEY_DEFAULT_COMMENT) as ArrayList<*>?

        val hint = if (defaultCommentList != null && !defaultCommentList.isEmpty()) {
            defaultCommentList[Random().nextInt(defaultCommentList.size)] as String
        } else {
            getString(R.string.string_luck_to_speak)
        }
        addCommentPopup = com.shuashuakan.android.modules.comment.AddCommentPopup(requireActivity(), hint, false, object : com.shuashuakan.android.modules.comment.AddCommentPopup.OnCommentListener {
            override fun onReply(commentId: Long, content: String, position: Int) {
                addReply(commentId, content)
            }

            override fun onComment(content: String?, media_path: String?) {
            }

            override fun onShowAlbum() {
            }

            override fun onRefreshTextView(text: String?, path: String?, replyAuthorName: String?) {
            }
        })
    }

    private fun addReply(commentId: Long, content: String) {
        apiService.createComment(commentId.toString(), "COMMENT", content).applySchedulers().subscribeApi(onNext = {
            addCommentPopup.clearText()
            requireActivity().showLongToast(getString(R.string.string_reply_success_tips))

        }, onApiError = {
            if (it is ApiError.HttpError) {
                requireActivity().showLongToast(it.displayMsg)
            }
        })
    }

    override fun onRefresh() {
        totalList.clear()
        notify_id = null
        requestData()
    }

    override fun onResume() {
        super.onResume()
        showBadge()
    }


    private fun showBadge() {
        redDot?.let {
            showBadge(it, badgeManager.isShowSystemBadge())
        }

        personalRedDot?.let {
            showBadge(it, badgeManager.isShowPersonalBadge())
        }
    }

    private fun showBadge(badgeView: View, isShowBadge: Boolean) {
        badgeView.visibility = if (isShowBadge) View.VISIBLE else View.GONE
    }
}