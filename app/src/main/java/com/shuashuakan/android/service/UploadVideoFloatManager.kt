package com.shuashuakan.android.service

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.FragmentActivity
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.shuashuakan.android.R
import com.shuashuakan.android.commons.util.ACache
import com.shuashuakan.android.config.AppConfig
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.event.UploadFailedEvent
import com.shuashuakan.android.event.UploadQiniuProgressEvent
import com.shuashuakan.android.event.UploadSuccessEvent
import com.shuashuakan.android.modules.account.activity.LoginActivity
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.modules.profile.UserProfileActivity
import com.shuashuakan.android.modules.publisher.PermissionRequestFragment
import com.shuashuakan.android.modules.widget.dialogs.BindPhoneDialog
import com.shuashuakan.android.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo

/**
 *@author: zhaoningqiang
 *@time: 2019/5/22
 *@Description:
 */
class UploadVideoFloatManager(private val application: Application) {
    private val mErrorBackgroundColor = Color.parseColor("#aaff3939")
    private val mNormalBackgroundColor = Color.parseColor("#aa000000")

    private var mUploadProgressView: View
    private var progressBar: ProgressBar
    private var progressTv: TextView
    private var progressIv: ImageView
    private var progressReUpload: ImageView
    private var progressClose: ImageView
    private var seeNow: TextView
    private var continueChain: TextView


    private val mLayoutParams: FrameLayout.LayoutParams

    private val compositeDisposable = CompositeDisposable()
    private val mHandler = Handler()

    private var mActivity: Activity? = null


    init {
        val uploadProgressView = LayoutInflater.from(application).inflate(R.layout.view_upload_video_progress, null, false)
        progressBar = uploadProgressView.findViewById(R.id.progress)
        progressTv = uploadProgressView.findViewById(R.id.progress_tv)
        progressIv = uploadProgressView.findViewById(R.id.progress_iv)
        progressReUpload = uploadProgressView.findViewById(R.id.re_upload)
        progressClose = uploadProgressView.findViewById(R.id.upload_layout_close)
        seeNow = uploadProgressView.findViewById(R.id.seeNow)
        continueChain = uploadProgressView.findViewById(R.id.continueChain)


        progressClose.setOnClickListener {
            PullService.deleteEntityFile(application)
            removeWindowView()
        }
        progressReUpload.setOnClickListener {
            resetFloatView()
            PullService.replyUpload(application)
        }

        seeNow.setOnClickListener {
            val accountManager = application.daggerComponent().accountManager()
            if (!accountManager.hasAccount()) {
                launchLogin()

            } else {
                val intent = Intent(application, UserProfileActivity::class.java)
                intent.putExtra("id", accountManager.account()!!.userId.toString())
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                application.startActivity(intent)
            }
            removeWindowView()
        }

        continueChain.setOnClickListener {
            if (PullService.canUpload()) {
                val accountManager = application.daggerComponent().accountManager()
                if (!accountManager.hasAccount()) {
                    launchLogin()
                } else {
                    val activity = mActivity
                    if (activity != null && activity is FragmentActivity && !activity.isFinishing) {
                        if (SpUtil.find(AppConfig.LOGIN_TYPE) == "WeChat" && SpUtil.find(AppConfig.PHONE_NUM).isNullOrEmpty()
                                && ACache.get(application).getAsString(AppConfig.SHOE_BIND_PHONE) != "show") {

                            BindPhoneDialog.create(BindPhoneDialog.CHAIN_FEED).show(activity.supportFragmentManager, "all_chain")
                            ACache.get(application).put(AppConfig.SHOE_BIND_PHONE, "show", AppConfig.BIND_DIALOG_SAVE_TIME)

                        } else {
                            val feed = mSuccessUploadFeed
                            when (feed?.type) {
                                "SOLITAIRE" -> {
                                    PermissionRequestFragment.create(PullService.UploadEntity.TYPE_ADD_SOLITAIRE, feed.masterFeedId)
                                            .show(activity.supportFragmentManager, "chain_feed_fragment")
                                }
                                "NORMAL" -> {
                                    PermissionRequestFragment.create(PullService.UploadEntity.TYPE_ADD_SOLITAIRE, feed.id)
                                            .show(activity.supportFragmentManager, "chain_feed_fragment")
                                }
                            }
                            application.getSpider().manuallyEvent(SpiderEventNames.CONTINUE_POST_SOLITAIRE_CLICK)
                                    .put("feedID", feed?.id.toString())
                                    .track()
                        }
                    }
                }



            } else {
                application.showLongToast(application.getString(R.string.string_publish_update_ing_tips))
            }

            removeWindowView()
        }

        mLayoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        mLayoutParams.gravity = Gravity.TOP
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            uploadProgressView.setPadding(0, application.getStatusBarHeight(), 0, 0)
        }
        mUploadProgressView = uploadProgressView
    }

    private val mActivityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityPaused(activity: Activity?) {
        }

        override fun onActivityResumed(activity: Activity?) {
            mActivity = activity
            if (!PullService.canUpload()) {
                addWindowView()
            }
        }

        override fun onActivityStarted(activity: Activity?) {
        }

        override fun onActivityDestroyed(activity: Activity?) {

        }

        override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
        }

        override fun onActivityStopped(activity: Activity?) {
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

        }
    }

    private fun initUploadProgressView(event: UploadQiniuProgressEvent) {
        addWindowView()
        progressBar.progress = event.progress
        progressTv.text = application.getString(R.string.string_publish_sending)
    }

    private var mSuccessUploadFeed: Feed? = null
    private fun videoUploadSuccess(event: UploadSuccessEvent) {
        mSuccessUploadFeed = event.feed

        progressTv.text = application.getString(R.string.string_publish_success)

        progressBar.visibility = View.VISIBLE

        progressIv.visibility = View.GONE

        progressClose.visibility = View.VISIBLE
        seeNow.visibility = View.VISIBLE
        continueChain.visibility = View.VISIBLE
        mHandler.postDelayed({
            removeWindowView()
        }, 3000)

        //用户发布主视频成功打点
        if (event.source == PullService.UploadEntity.TYPE_ADD_HOME_VIDEO || event.source == PullService.UploadEntity.TYPE_ADD_CHANNEL_VIDEO) {
            application.getSpider().manuallyEvent(SpiderEventNames.MASTER_FEED_RELEASE)
                    .put("feedID", event.feed.id)
                    .put("title", event.feed.title)
                    .put("channelID", event.feed.channelId.toString())
                    .put("userID", application.getUserId())
                    .track()
        }
    }


    private fun videoUploadFail(event: UploadFailedEvent) {
        mUploadProgressView.setBackgroundColor(mErrorBackgroundColor)

        progressIv.visibility = View.VISIBLE
        progressBar.visibility = View.GONE

        progressTv.text = application.getString(R.string.string_publish_error_resend)
        progressClose.visibility = View.VISIBLE
        progressReUpload.visibility = View.VISIBLE
    }


    private fun addWindowView() {
        when (PullService.getEntity()?.uploadType) {
            PullService.UploadEntity.TYPE_ADD_SOLITAIRE,
            PullService.UploadEntity.TYPE_ADD_HOME_VIDEO,
            PullService.UploadEntity.TYPE_ADD_CHANNEL_VIDEO,
            PullService.UploadEntity.TYPE_ADD_EDITED_VIDEO -> {
                val activity = mActivity
                if (activity != null && !activity.isFinishing) {
                    if (mUploadProgressView.tag != activity) {
                        val parent = mUploadProgressView.parent
                        if (parent != null && parent is ViewGroup) {
                            parent.removeView(mUploadProgressView)
                        }
                        activity.addContentView(mUploadProgressView, mLayoutParams)
                        mUploadProgressView.tag = activity
                    }
                }
            }
        }
    }

    private fun removeWindowView() {
        val parent = mUploadProgressView.parent
        if (parent != null && parent is ViewGroup) {
            parent.removeView(mUploadProgressView)
        }
        resetFloatView()
    }

    private fun resetFloatView() {
        progressTv.text = application.getString(R.string.string_publish_sending)
        mUploadProgressView.setBackgroundColor(mNormalBackgroundColor)
        progressBar.progress = 0
        progressBar.visibility = View.VISIBLE
        progressIv.visibility = View.GONE

        progressClose.visibility = View.GONE
        progressReUpload.visibility = View.GONE

        seeNow.visibility = View.GONE
        continueChain.visibility = View.GONE
        mUploadProgressView.tag = null
    }

    fun register() {
        application.registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks)
        RxBus.get().toFlowable().observeOn(AndroidSchedulers.mainThread()).subscribe {
            when (it) {

                is UploadQiniuProgressEvent -> {
                    initUploadProgressView(it)
                }
                is UploadSuccessEvent -> {
                    videoUploadSuccess(it)
                }
                is UploadFailedEvent -> {
                    videoUploadFail(it)
                }

            }
        }.addTo(compositeDisposable)
    }

    private fun launchLogin() {
        val activity = mActivity
        if (activity != null && !activity.isFinishing) {
            LoginActivity.launch(activity)
        } else {
            val intent = Intent(application, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            application.startActivity(intent)
        }
    }


}