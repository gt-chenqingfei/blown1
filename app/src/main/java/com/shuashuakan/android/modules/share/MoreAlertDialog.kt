package com.shuashuakan.android.modules.share

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.shuashuakan.android.DuckApplication
import com.shuashuakan.android.R
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.Complain
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.event.FeedFollowChangeEvent
import com.shuashuakan.android.event.FocusListRefreshEvent
import com.shuashuakan.android.event.MoreDialogFollowEvent
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.ACCOUNT_PAGE
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.publisher.RecordDataModel
import com.shuashuakan.android.modules.publisher.chains.ChainsPublishActivity
import com.shuashuakan.android.service.PullService
import com.shuashuakan.android.utils.*
import com.shuashuakan.android.utils.download.DownloadManager
import com.shuashuakan.android.utils.extension.showCancelFollowDialog
import com.umeng.socialize.UMShareListener

class MoreAlertDialog constructor(
        private val activity: Activity,
        private val apiService: ApiService,
        private val accountManager: AccountManager,
        private val shareListener: UMShareListener,
        private val feedId: String?,
        private val showDelete: Boolean,
        private val chainFeedSource: String?,
        private val channelId: String?,
        private val web: String?,
        private val videoUrl: String,
        private val videoCoverUrl: String,
        private val channelName: String,
        private val title: String,
        private val tag: String,
        private val allowDownload: Boolean? = false,
        private val canEdit: Boolean? = false,
        private val message: String? = "",
        private val editableCount: Int? = 0,
        private val targetUserId: String? = "",
        private val isFollowed: Boolean? = false,
        private val type: String? = "",
        private val targetUserName: String? = "",
        private val isFans:Boolean? = false
) : AlertDialog.Builder(activity) {

    private var showData = mutableListOf<String>()

    init {
        setData()
        val ss = android.R.layout.simple_list_item_1
        val adapter2 = object : ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, showData) {
            @SuppressLint("ViewHolder")
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val complain = getItem(position)
                val view = LayoutInflater.from(activity).inflate(android.R.layout.simple_list_item_1, parent, false)
                val text1 = view.findViewById<TextView>(android.R.id.text1)
                text1.text = complain
                return view
            }
        }
        setAdapter(adapter2) { _, p1 ->
            when (showData[p1]) {
                activity.getString(R.string.string_edit_current_content) -> {
                    if (canEdit!!) {
                        toastCustomText(activity, String.format(activity.getString(R.string.string_change_number_format), editableCount!!))
                        context.getSpider().editVideoEvent(context, feedId!!, true)
                        editVideo(canEdit, editableCount)
                    } else {
                        toastCustomText(activity, message!!)
                        context.getSpider().editVideoEvent(context, feedId!!, false)
                    }
                }
                activity.getString(R.string.string_share_to_friend) -> {
                    val shareHelper = ShareHelper(apiService, accountManager)
                    shareHelper.shareType = ShareConfig.SHARE_TYPE_VIDEO
                    shareHelper.doShare(activity, null, feedId,
                            false, false, null,
                            tag = DownloadManager.DOWNLOAD_TAG_FRAGMENT_MULTITYPE,
                            allowDownload = allowDownload)
                }
                activity.getString(R.string.string_un_follow_label) -> {
                    cancelFollow(isFollowed!!, targetUserId!!)
                }
                activity.getString(R.string.string_follow) -> {
                    cancelFollow(isFollowed!!, targetUserId!!)
                }
                activity.getString(R.string.string_report_label) -> {
                    reportMethod()
                }
            }
        }
    }

    private fun setData() {
        if (targetUserId != activity.getUserId().toString()) {
            if (isFollowed != null) {
                if (isFollowed) {
                    showData.add(activity.getString(R.string.string_un_follow_label))
                } else {
                    if (isFans == true){
                        showData.add(activity.getString(R.string.string_follow_fans))
                    }else{
                        showData.add(activity.getString(R.string.string_follow))
                    }
                }
            }
            showData.add(activity.getString(R.string.string_share_to_friend))
            showData.add(activity.getString(R.string.string_report_label))
        } else {
            if (canEdit!!) {
                showData.add(activity.getString(R.string.string_edit_current_content))
            }
            showData.add(activity.getString(R.string.string_share_to_friend))
        }
    }

    private fun editVideo(canEdit: Boolean, editableCount: Int) {
        context.startActivity(ChainsPublishActivity.create(context, RecordDataModel(
                videoCoverUrl, null, PullService.UploadEntity.TYPE_ADD_EDITED_VIDEO,
                null, channelId, channelName), feedId!!, videoUrl, title, canEdit, editableCount))
    }


    private fun cancelFollow(isFollow: Boolean, targetUserId: String) {
        if (accountManager.hasAccount()) {

            if (isFollow) {
                val cancelFollow = {
                    apiService.cancelFollow(targetUserId).applySchedulers().subscribeApi(onNext = {
                        if (it.result.isSuccess) {
                            FollowCacheManager.putFollowUserToCache(targetUserId, false)
                            toastCustomText(context, context.getString(R.string.string_un_follow_success))
                            RxBus.get().post(FocusListRefreshEvent())
                            activity.getSpider().userFollowEvent(activity, targetUserId, type!!, false)
                            RxBus.get().post(FeedFollowChangeEvent(targetUserId, false))
                        } else {
                            toastCustomText(context, context.getString(R.string.string_un_follow_error))
                        }
                    }, onApiError = {
                        if (it is ApiError.HttpError) {
                            context.showLongToast(it.displayMsg)
                        } else {
                            toastCustomText(context, context.getString(R.string.string_un_follow_error))
                        }
                    })
                }
                context.showCancelFollowDialog(targetUserName, cancelFollow)
            } else {
                apiService.createFollow(targetUserId).applySchedulers().subscribeApi(onNext = {
                    if (it.result.isSuccess) {
                        FollowCacheManager.putFollowUserToCache(targetUserId, true)
                        toastCustomText(context, context.getString(R.string.string_attention_success))
                        RxBus.get().post(FocusListRefreshEvent())
                        RxBus.get().post(FeedFollowChangeEvent(targetUserId, true))
                        activity.getSpider().userFollowEvent(activity, targetUserId, type!!, true)
                    } else {
                        toastCustomText(context, context.getString(R.string.string_attention_error))
                    }
                }, onApiError = {
                    if (it is ApiError.HttpError) {
                        context.showLongToast(it.displayMsg)
                    } else {
                        toastCustomText(context, context.getString(R.string.string_attention_error))
                    }
                })
            }
        } else {
            RxBus.get().post(MoreDialogFollowEvent(targetUserId))
            activity.startActivity(ACCOUNT_PAGE)
        }

    }

    private var reportList: List<Complain>? = null
    private fun reportMethod() {
        if (accountManager.hasAccount()) {
            if (reportList != null) {
                reportFeed(feedId, activity)
            } else {
                getReportList(feedId, activity)
            }
        } else {
            activity.startActivity(ACCOUNT_PAGE)
        }
    }

    private fun reportFeed(feedId: String?, context: Context) {
        val adapter = ComplainArrayAdapter(context, android.R.layout.simple_list_item_1, reportList!!)
        AlertDialog.Builder(context)
                .setAdapter(adapter) { _, p1 ->
                    val complain = reportList!![p1]
                    if (complain.url != null) {
                        context.startActivity(complain.url)
                    } else {
                        apiService.createComplain(feedId, complain.type).applySchedulers().subscribeApi(onNext = {
                            if (it.result.isSuccess) {
                                context.showLongToast(context.getString(R.string.string_thanks_for_you_report))
                            } else {
                                context.showLongToast(context.getString(R.string.string_operating_error))
                            }
                        }, onApiError = {
                            if (it is ApiError.HttpError) {
                                context.showLongToast(it.displayMsg)
                            } else {
                                context.showLongToast(context.getString(R.string.string_operating_error))
                            }
                        })
                    }
                }.show()
    }

    private fun getReportList(feedId: String?, context: Context) {
        apiService.getComplainList().applySchedulers().subscribeApi(onNext = {
            reportList = it
            reportFeed(feedId, context)
        })
    }
}