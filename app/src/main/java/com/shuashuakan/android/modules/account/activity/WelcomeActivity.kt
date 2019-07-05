package com.shuashuakan.android.modules.account.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import com.gyf.barlibrary.ImmersionBar
import com.shuashuakan.android.R
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.account.ChannelModel
import com.shuashuakan.android.data.api.model.account.GuideModel
import com.shuashuakan.android.event.FinishWelcome
import com.shuashuakan.android.event.WeChatBindEvent
import com.shuashuakan.android.modules.account.vm.AccountViewModel
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.modules.HOME_PAGE
import com.shuashuakan.android.ui.ProgressDialog
import com.shuashuakan.android.ui.base.FishActivity
import com.shuashuakan.android.utils.getSpider
import com.shuashuakan.android.utils.startActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_welcome.*

/**
 * @author qingfei.chen
 * @Date 2019-05-06
 */

class WelcomeActivity : FishActivity(), View.OnClickListener {
    private val dialog by lazy {
        return@lazy ProgressDialog.progressDialog(this, getString(R.string.string_loading))
    }

    private val mCompositeDisposable = CompositeDisposable()
    private lateinit var mAccountViewModel: AccountViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        ImmersionBar.with(this).navigationBarColor(R.color.black).init()
        ImmersionBar.setTitleBar(this, toolbar)
        mAccountViewModel = ViewModelProviders.of(this).get(AccountViewModel::class.java)

        tv_welcome_login_by_mobile.setOnClickListener(this)
        tv_welcome_home.setOnClickListener(this)
        iv_welcome_login_by_wx.setOnClickListener(this)
        tv_welcome_protocol.setOnClickListener(this)
        initObservable()
        registerLiveDataObserver()
    }

    private fun initObservable() {
        RxBus.get().toFlowable().subscribe {
            when (it) {
                is FinishWelcome -> {
                    finish()
                }
                is WeChatBindEvent -> {
                    handleWeChatAuthIntent(it.state, it.code)
                }
            }
        }.addTo(mCompositeDisposable)
    }


    override fun onClick(v: View) {
        when (v) {
            tv_welcome_login_by_mobile -> {
                getSpider().manuallyEvent(SpiderEventNames.GUIDE_PHONE_LOGIN_BUTTON_CLICK).track()
                goLoginWithMobile()
            }
            tv_welcome_home -> {
                getSpider().manuallyEvent(SpiderEventNames.GUIDE_SKIP_BUTTON_CLICK).track()
                goHome()
            }
            iv_welcome_login_by_wx -> {
                getSpider().manuallyEvent(SpiderEventNames.GUIDE_WECHAT_LOGIN_BUTTON_CLICK).track()
                goLoginWithWeChat()
            }
            tv_welcome_protocol -> {
                getSpider().manuallyEvent(SpiderEventNames.GUIDE_PRIVACY_BUTTON_CLICK).track()
                startActivity("https://topic.shuashuakan.net/user-agreement.html")
            }
        }
    }

    private fun handleWeChatAuthIntent(state: String?, code: String?) {
        dialog.show()
        if (state != null && code != null) {
            mAccountViewModel.loginWithWeChat(code, state)
        }
    }

    private fun registerLiveDataObserver() {

        mAccountViewModel.mLoginStateLiveData.observe(this, Observer {
            dialog.dismiss()
            if (mAccountViewModel.loginType != AccountViewModel.LOGIN_TYPE_WECHAT) {
                return@Observer
            }
            if (mAccountViewModel.needBindMobile()) {
                MobileModifyActivity.launchWithBind(this)
            } else {
                goHome()
            }
        })

        mAccountViewModel.mUserGuideLiveData.observe(this, Observer {
            performUserGuide(it)
        })
    }

    private fun performUserGuide(userGuide: GuideModel?) {
        if (userGuide == null) {
            goHome()
            return
        }

        if (userGuide.requireGuide) {
            userGuide.pageTypes?.let {
                if (it.isNotEmpty()) {
                    PerfectProfileActivity.launch(this, userGuide.data?.avatar,
                            userGuide.data?.userName, userGuide.pageTypes as ArrayList<String>,
                            userGuide.data?.channelList as ArrayList<ChannelModel>)
                    finish()
                }
            }
        } else {
            goHome()
        }
    }

    private fun goLoginWithMobile() {
        LoginActivity.launchFromWelcome(this)
    }

    private fun goLoginWithWeChat() {
        mAccountViewModel.requestWeChatAuth()
    }

    private fun goHome() {
        startActivity(HOME_PAGE)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mCompositeDisposable.clear()
        ImmersionBar.with(this).destroy()
        dialog.dismiss()
    }
}