package com.shuashuakan.android.ui

import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.shuashuakan.android.R
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.ui.base.FishActivity
import com.shuashuakan.android.utils.*
import java.util.*
import javax.inject.Inject

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/10/17
 * Description: 普罗米修斯神秘页面
 */
class PrometheusActivity : FishActivity() {

    private val toolbar by bindView<Toolbar>(R.id.toolbar)
    private val channelIdEt by bindView<EditText>(R.id.channel_id_et)
    private val getVideoBtn by bindView<Button>(R.id.get_video_btn)
    private val addCommentEdit by bindView<Button>(R.id.add_comment_edit)
    private val editLl by bindView<LinearLayout>(R.id.edit_ll)
    private val minusCommentEdit by bindView<Button>(R.id.minus_comment_edit)
    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var accountManager: AccountManager

    private val dialog by lazy {
        return@lazy ProgressDialog.progressDialog(this@PrometheusActivity, getString(R.string.string_get_feed_ing))
    }
    private val commentDialog by lazy {
        return@lazy ProgressDialog.progressDialog(this@PrometheusActivity, getString(R.string.string_comment_ing))
    }
    private val feeds: MutableList<Feed> = mutableListOf()
    private var feedId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.prometheus_activity)

        toolbar.title = getString(R.string.string_mysterious_base)
        toolbar.inflateMenu(R.menu.menu_comment)
        toolbar.setOnMenuItemClickListener {
            if (accountManager.hasAccount()) {
                comment()
            } else {
                showLongToast(getString(R.string.string_please_login))
            }
            true
        }
        toolbar.setNavigationOnClickListener {
            finish()
        }

        getVideoBtn.setOnClickListener {
            feedId = null
            dialog.show()
            feeds.clear()
            getAllFeed()
        }

        addCommentEdit.setOnClickListener {
            addCommentEdit()
        }
        minusCommentEdit.setOnClickListener {
            if (editLl.childCount > 0) {
                editLl.removeViewAt(editLl.childCount - 1)
            }
        }
    }

    private val comments: MutableList<String> = mutableListOf()

    private fun comment() {
        commentDialog.show()
        comments.clear()
        index = 0
        errorCount = 0
        successCount = 0

        for (i in 0..editLl.childCount) {
            val item = editLl.getChildAt(i) as? EditText
            if (item != null) {
                comments.add(item.text.toString())
            }
        }
        commentFeed()
    }

    private var index = 0
    private var errorCount = 0//失败次数
    private var successCount = 0//成功次数

    private fun commentFeed() {
        if (feeds.size > index) {
            val s: String = comments[Random().nextInt(comments.size)]
            apiService.createComment(feeds[index].id, "FEED", s).applySchedulers().subscribeApi(onNext = {
                index++
                successCount++
                commentFeed()
            }, onApiError = {
                index++
                errorCount++
                commentFeed()
            }, onComplete = {
                commentDialog.window?.findViewById<TextView>(R.id.loadingTextView)?.text = String.format(getString(R.string.string_current_index_format), index)
            })
        } else {
            commentDialog.dismiss()
            showLongToast(String.format(getString(R.string.string_result_number_format), successCount, errorCount))
        }
    }

    private fun getAllFeed() {
        apiService.getChannelFeeds(feedId, channelIdEt.text.toString()).applySchedulers().subscribeApi(
                onNext = {
                    feeds.addAll(it.feeds)
                    if (!it.feeds.isEmpty()) {
                        feedId = it.feeds[it.feeds.size - 1].id
                        getAllFeed()
                    } else {
                        dialog.dismiss()
                        getVideoBtn.text = String.format(getString(R.string.string_get_video_size_format), feeds.size)
                    }
                }, onApiError = {
            dialog.dismiss()
            showLongToast(getString(R.string.string_read_error))
        })
    }

    private fun addCommentEdit() {
        val param = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        param.setMargins(0, dip(10f), 0, 0)

        val edit = EditText(this@PrometheusActivity)
        edit.hint = getString(R.string.string_please_input_comment_content)
        edit.setHintTextColor(Color.WHITE)
        edit.setTextColor(Color.WHITE)
        edit.layoutParams = param

        editLl.addView(edit)
    }
}
