package com.shuashuakan.android.modules.message

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.shuashuakan.android.R
import com.shuashuakan.android.data.api.model.message.NormalMsgItemData
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.ui.base.FishActivity
import com.shuashuakan.android.utils.TimeUtil
import com.shuashuakan.android.utils.bindView
import com.shuashuakan.android.utils.startActivity
import javax.inject.Inject

//系统通知页面
class SystemNoticeActivity : FishActivity(), SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var apiService: ApiService

    private val toolbar by bindView<Toolbar>(R.id.toolbar)
    private val recyclerView by bindView<RecyclerView>(R.id.recycler_view)
    private val swipeRefreshLayout by bindView<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
    private lateinit var emptyView: View
    private lateinit var alertIcon: ImageView
    private lateinit var alertTitle: TextView
    private lateinit var alertMsg: TextView

    private lateinit var adapter: BaseQuickAdapter<NormalMsgItemData, BaseViewHolder>
    private var cursorId: Long? = null
    private var mNotificationType = 1


    companion object {
        const val EXTRA_NOTIFICATION_TYPE: String = "extra_notification_type"
        const val NOTIFICATION_SYSTEM: Int = 1
        const val NOTIFICATION_PERSONAL: Int = 2

        fun launcher(context: Context, notificationType: Int) {
            val intent = Intent(context, SystemNoticeActivity::class.java)
            intent.putExtra(EXTRA_NOTIFICATION_TYPE, notificationType)
            context.startActivity(intent)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_system_notice)
        mNotificationType = intent.getIntExtra(EXTRA_NOTIFICATION_TYPE, NOTIFICATION_SYSTEM)
        toolbar.title = if (mNotificationType == NOTIFICATION_SYSTEM)
            getString(R.string.string_system_notify) else getString(R.string.string_shuashua_author)
        initView()
        initListener()
        refreshPage()
    }


    private fun initView() {
        swipeRefreshLayout.setOnRefreshListener(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = object : BaseQuickAdapter<NormalMsgItemData, BaseViewHolder>(R.layout.item_system_notice) {
            override fun convert(helper: BaseViewHolder, item: NormalMsgItemData) {
                if (item.actionUserInfoList.isNotEmpty())
                    helper.getView<SimpleDraweeView>(R.id.avatar).setImageURI(item.actionUserInfoList[0].avatar)
                val channelIcon = helper.getView<ImageView>(R.id.sys_channel_icon)
                val title = helper.getView<TextView>(R.id.sys_channel_name)
                val sysItemTime = helper.getView<TextView>(R.id.sys_item_time)
                val content = helper.getView<TextView>(R.id.sys_item_content)
                val coverImage = helper.getView<SimpleDraweeView>(R.id.sys_item_iv)

                sysItemTime.text = TimeUtil.formateSysTimeTwo(item.createAt)
                sysItemTime.visibility = View.VISIBLE
                content.text = item.referenceItem?.content
                helper.getView<ImageView>(R.id.sys_video_button).visibility = View.GONE

                if (item.referenceItem != null) {
                    title.text = item.referenceItem?.title
                    title.visibility = View.VISIBLE
                } else {
                    title.visibility = View.GONE
                }
                Glide.with(this@SystemNoticeActivity).load(item.referenceItem?.titleIcon).into(channelIcon)
                channelIcon.visibility = View.GONE
                if (item.notificationLink?.coverUrl != null && item.notificationLink?.coverUrl!!.isNotEmpty()) {
                    helper.getView<LinearLayout>(R.id.container).layoutParams =
                            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    helper.getView<FrameLayout>(R.id.sys_iv_container).visibility = View.VISIBLE

                    coverImage.setImageURI(item.notificationLink?.coverUrl)
                    coverImage.setOnClickListener {
                        startActivity(item.notificationLink?.redirectUrl)
                    }
                } else {
                    helper.getView<FrameLayout>(R.id.sys_iv_container).visibility = View.GONE
                    helper.getView<LinearLayout>(R.id.container).layoutParams =
                            LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                }
                if (item.referenceItem?.type == "CHANNEL") {
                    if (!item.referenceItem?.title?.isEmpty()!!) {
                        channelIcon.visibility = View.VISIBLE
                    }
                } else if (item.referenceItem?.type == "FEED") {
                    helper.getView<ImageView>(R.id.sys_video_button).visibility = View.VISIBLE
                }

                if (!item.referenceItem?.url.isNullOrEmpty()) {
                    val detail = SpannableString(getString(R.string.string_look_detail))
                    detail.setSpan(ForegroundColorSpan(Color.parseColor("#ffef30")), 0, 5, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                    content.append(detail)
                }
                helper.itemView.setOnClickListener {
                    if (!item.referenceItem?.url.isNullOrEmpty()) {
                        startActivity(item.referenceItem?.url)
                    }
                }
            }
        }

        adapter.setPreLoadNumber(3)
        adapter.setEnableLoadMore(true)

        emptyView = View.inflate(this, R.layout.empty_view, null)
        alertIcon = emptyView.findViewById<ImageView>(R.id.alertIcon)
        alertTitle = emptyView.findViewById<TextView>(R.id.alertTitle)
        alertMsg = emptyView.findViewById<TextView>(R.id.alertMsg)
        alertIcon.setImageResource(R.drawable.ic_network_error)
        emptyView.visibility = View.INVISIBLE
        adapter.emptyView = emptyView


        adapter.setOnLoadMoreListener({
            fetchData(false)
        }, recyclerView)

        recyclerView.adapter = adapter
    }

    private fun initListener() {
        emptyView.setOnClickListener {
            refreshPage()
        }

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }


    private fun refreshPage() {
        cursorId = null
        swipeRefreshLayout.isRefreshing = true
        fetchData()
    }

    private fun fetchData(isRefresh: Boolean = true) {
        val newMessageNotification =
                if (mNotificationType == NOTIFICATION_PERSONAL) {
                    apiService.getSystemPersonalList(cursorId)
                } else {
                    apiService.getSystemMessageList(cursorId)
                }
        newMessageNotification
                .applySchedulers()
                .subscribeApi(onNext = { newNoticeModel ->
                    cursorId = newNoticeModel.nextCursor?.maxId
                    swipeRefreshLayout.isRefreshing = false
                    val notificationsList = newNoticeModel.notificationsList
                    if (notificationsList == null || notificationsList.isEmpty()) {
                        adapter.loadMoreEnd()
                    } else {
                        if (isRefresh) {
                            adapter.setNewData(notificationsList)
                        } else {
                            adapter.addData(notificationsList)
                        }

                        if (notificationsList.size < 10) {
                            adapter.loadMoreEnd(false)
                        } else {
                            adapter.loadMoreComplete()
                        }
                    }

                    if (adapter.data.isEmpty()) {
                        emptyView.visibility = View.VISIBLE
                        alertIcon.setImageResource(R.drawable.ic_timeline_empty)
                        alertTitle.setText(R.string.string_no_message)
                        alertMsg.setText(R.string.string_please_go_to_solitaire)
                    }

                }, onApiError = {
                    swipeRefreshLayout.isRefreshing = false
                    adapter.loadMoreFail()

                    emptyView.visibility = View.VISIBLE
                    alertIcon.setImageResource(R.drawable.ic_network_error)
                    alertTitle.setText(R.string.string_load_error)
                    alertMsg.setText(R.string.string_check_net_with_click)

                })
    }

    override fun onRefresh() {
        refreshPage()
    }
}
