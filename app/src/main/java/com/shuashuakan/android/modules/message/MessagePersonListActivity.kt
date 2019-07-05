package com.shuashuakan.android.modules.message

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.shuashuakan.android.R
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.message.ActionUserInfoListItem
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.event.RefreshProfileEvent
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.account.activity.LoginActivity
import com.shuashuakan.android.modules.profile.UserProfileActivity
import com.shuashuakan.android.modules.widget.FollowButton
import com.shuashuakan.android.modules.widget.SpiderAction
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.ui.base.FishActivity
import com.shuashuakan.android.utils.FollowCacheManager
import com.shuashuakan.android.utils.bindView
import com.shuashuakan.android.utils.extension.showCancelFollowDialog
import com.shuashuakan.android.utils.getUserId
import com.shuashuakan.android.utils.showLongToast
import javax.inject.Inject

class MessagePersonListActivity : FishActivity() {
    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var accountManager: AccountManager

    private val toolbar by bindView<Toolbar>(R.id.toolbar)
    private val recyclerView by bindView<RecyclerView>(R.id.recycler_view)
    var msgId: Long = 0
    var type: String = ""
    private lateinit var adapter: BaseQuickAdapter<ActionUserInfoListItem, BaseViewHolder>

    companion object {
        const val EXTRA_CREATE_ID = "extra_create_id"
        const val EXTRA_TYPE = "extra_type"

        fun create(context: Context, id: Long, type: String): Intent {
            return Intent(context, MessagePersonListActivity::class.java).putExtra(EXTRA_CREATE_ID, id)
                    .putExtra(EXTRA_TYPE, type)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_person_list)
        getExtra()
        getData()
        initView()
    }

    private fun getData() {
        apiService.noticeSubListData(msgId)
                .applySchedulers()
                .subscribeApi(onNext = {
                    adapter.setNewData(it)
                }, onApiError = {
                })
    }

    private fun getExtra() {
        msgId = intent.getLongExtra(EXTRA_CREATE_ID, 0)
        type = intent.getStringExtra(EXTRA_TYPE)
    }

    private fun initView() {
        when (type) {
            "FOLLOW" -> toolbar.title = getString(R.string.string_follow_with_you)
            "LIKE_COMMENT" -> toolbar.title = getString(R.string.string_like_comment_with_you)
            "LIKE_FEED" -> toolbar.title = getString(R.string.string_up_video_wtih_you)
        }

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = object : BaseQuickAdapter<ActionUserInfoListItem, BaseViewHolder>(R.layout.item_focus_list) {
            override fun convert(helper: BaseViewHolder, item: ActionUserInfoListItem) {
                val followButton = helper.getView<FollowButton>(R.id.follow_btn)
                helper.getView<TextView>(R.id.focus_name).text = item.nickName
                helper.getView<TextView>(R.id.focus_content).text = item.bio
                val avatar = helper.getView<SimpleDraweeView>(R.id.focus_avatar)
                avatar.setImageURI(item.avatar)

                if (item.isFollow != null) {
                    if (item.isFollow!!) {
                        followButton.setFollowStatus(true)
                    } else {
                        followButton.setFollowStatus(false, item.isFans)
                    }
                } else {
                    followButton.setFollowStatus(false, item.isFans)
                }
                followButton.setOnClickListener {
                    setFollow(followButton, item)
                }
                avatar.setOnClickListener {
                    startActivity(Intent(this@MessagePersonListActivity, UserProfileActivity::class.java)
                            .putExtra("id", item.userId)
                            .putExtra("source", SpiderAction.PersonSource.MESSAGE_CENTER.source))
                }
            }
        }
        recyclerView.adapter = adapter
    }

    private fun setFollow(button: FollowButton, user: ActionUserInfoListItem) {

        if (!accountManager.hasAccount()) {
            LoginActivity.launch(this)
            return@setFollow
        }

        if (button.isFollow) {
            val cancelFollow = {
                apiService.cancelFollow(user.userId).applySchedulers().subscribeApi(onNext = {
                    if (it.result.isSuccess) {
                        FollowCacheManager.putFollowUserToCache(user.userId, false)
                        button.setFollowStatus(false, user.isFans)
                        RxBus.get().post(RefreshProfileEvent())
                        spider.manuallyEvent(SpiderEventNames.USER_FOLLOW)
                                .put("userID", this.getUserId())
                                .put("TargetUserID", user.userId)
                                .put("method", "unfollow")
                                .track()
                    } else {
                        showLongToast(getString(R.string.string_un_follow_error))
                    }
                }, onApiError = {
                    if (it is ApiError.HttpError) {
                        showLongToast(it.displayMsg)
                    } else {
                        showLongToast(getString(R.string.string_un_follow_error))
                    }
                })
            }
            showCancelFollowDialog(user.nickName, cancelFollow)
        } else {
            apiService.createFollow(user.userId).applySchedulers().subscribeApi(onNext = {
                if (it.result.isSuccess) {
                    FollowCacheManager.putFollowUserToCache(user.userId, true)
                    button.setFollowStatus(true)
                    RxBus.get().post(RefreshProfileEvent())
                    spider.manuallyEvent(SpiderEventNames.USER_FOLLOW)
                            .put("userID", this.getUserId())
                            .put("TargetUserID", user.userId)
                            .put("method", "follow")
                            .track()
                } else {
                    showLongToast(getString(R.string.string_attention_error))
                }
            }, onApiError = {
                if (it is ApiError.HttpError) {
                    showLongToast(it.displayMsg)
                } else {
                    showLongToast(getString(R.string.string_attention_error))
                }
            })
        }
    }
}
