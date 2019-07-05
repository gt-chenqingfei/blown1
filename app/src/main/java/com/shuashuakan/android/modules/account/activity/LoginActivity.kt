package com.shuashuakan.android.modules.account.activity

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.View
import com.gyf.barlibrary.ImmersionBar
import com.shuashuakan.android.R
import com.shuashuakan.android.data.REDIRECT_LINK
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.account.ChannelModel
import com.shuashuakan.android.data.api.model.account.GuideModel
import com.shuashuakan.android.event.FinishWelcome
import com.shuashuakan.android.event.WeChatBindEvent
import com.shuashuakan.android.modules.account.vm.AccountViewModel
import com.shuashuakan.android.modules.HOME_PAGE
import com.shuashuakan.android.ui.ProgressDialog
import com.shuashuakan.android.ui.base.FishActivity
import com.shuashuakan.android.utils.hideKeyboard
import com.shuashuakan.android.utils.startActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_login_new.*
import me.twocities.linker.annotations.Link

/**
 * @author qingfei.chen
 * @Date 2019-05-06
 */

@Link("ssr://oauth2/login")
class LoginActivity : FishActivity(), View.OnClickListener {
    companion object {
        const val FROM_WELCOME = 1002
        const val EXTRA_PAGE_FROM = "extra_page_from"
        var preTimeMillis: Long = 0
        /**
         * Called from welcome
         */
        fun launchFromWelcome(context: Context) {
            if (doubleClick()) {
                return
            }
            context.startActivity(Intent(context, LoginActivity::class.java)
                    .putExtra(EXTRA_PAGE_FROM, FROM_WELCOME))
        }


        fun launch(context: Context) {
            if (doubleClick()) {
                return
            }
            context.startActivity(Intent(context, LoginActivity::class.java))
        }

        fun launchForResult(context: Context, requestCode: Int) {
            if (doubleClick()) {
                return
            }
            (context as FragmentActivity)
                    .startActivityForResult(Intent(context, LoginActivity::class.java), requestCode)
        }

        /**
         * LoginActivity cannot be set to SingleTask because it requires return data from
         * startActivityForResult mode, so the current policy is used to resolve
         * starting multiple LoginActivity
         */
        private fun doubleClick(): Boolean {
            if (System.currentTimeMillis() - preTimeMillis < 1000) {
                return true
            }
            preTimeMillis = System.currentTimeMillis()
            return false
        }
    }

    private val dialog by lazy {
        return@lazy ProgressDialog.progressDialog(this, getString(R.string.string_loading))
    }

    private val compositeDisposable = CompositeDisposable()
    private lateinit var mAccountViewModel: AccountViewModel
    private var mPageFrom = 0
    private var redirectUrl: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_new)
        ImmersionBar.with(this).init()
        ImmersionBar.with(this).navigationBarColor(R.color.black).init()
        redirectUrl = intent.getStringExtra(REDIRECT_LINK)
        mPageFrom = intent.getIntExtra(EXTRA_PAGE_FROM, -1)
        mAccountViewModel = ViewModelProviders.of(this).get(AccountViewModel::class.java)
        initListener()
        registerLiveDataObserver()
        initObservable()
        et_login_mobile.requestFocus()
    }

    override fun onDestroy() {
        super.onDestroy()
        ImmersionBar.with(this).destroy()
        dialog.dismiss()
        compositeDisposable.clear()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishThis()
    }

    private fun initObservable() {
        RxBus.get().toFlowable().subscribe {
            when (it) {
                is WeChatBindEvent -> {
                    dialog.show()
                    if (it.state != null && it.code != null) {
                        mAccountViewModel.loginWithWeChat(it.code, it.state)
                    }
                }
            }
        }.addTo(compositeDisposable)
    }

    override fun onClick(v: View?) {
        when (v) {
            iv_login_close -> {
                finishThis()
            }
            tv_login_next -> {
                hideKeyboard()
                dialog.show()
                mAccountViewModel.loginWithMobile(et_login_mobile.getPhoneNumber(),
                        vw_login_verification_code.getValidCode())
            }
            iv_login_by_wx -> {
                mAccountViewModel.requestWeChatAuth()
            }
            tv_welcome_protocol -> {
                startActivity("https://topic.shuashuakan.net/user-agreement.html")
            }
        }
    }

    private fun initListener() {
        iv_login_close.setOnClickListener(this)
        tv_login_next.setOnClickListener(this)
        iv_login_by_wx.setOnClickListener(this)
        tv_welcome_protocol.setOnClickListener(this)
        vw_login_verification_code.attach(mAccountViewModel, et_login_mobile)
        et_login_mobile.attach(mAccountViewModel)
    }

    private fun registerLiveDataObserver() {
        mAccountViewModel.mExcuteBtnStateLiveData.observe(this, Observer {
            tv_login_next.isEnabled = it == true
        })

        mAccountViewModel.mLoginStateLiveData.observe(this, Observer {
            dialog.dismiss()
            if (mAccountViewModel.loginType != AccountViewModel.LOGIN_TYPE_WECHAT) {
                return@Observer
            }
            if (mAccountViewModel.needBindMobile()) {
                MobileModifyActivity.launchWithBind(this)
            } else {
                redirectByFromPage()
            }
        })

        mAccountViewModel.mUserGuideLiveData.observe(this, Observer {
            performUserGuide(it)
        })
    }

    private fun performUserGuide(userGuide: GuideModel?) {
        if (userGuide == null) {
            redirectByFromPage()
            return
        }

        if (userGuide.requireGuide) {
            userGuide.pageTypes?.let {
                if (it.isNotEmpty()) {
                    PerfectProfileActivity.launch(this, userGuide.data?.avatar,
                            userGuide.data?.userName, userGuide.pageTypes as ArrayList<String>,
                            userGuide.data?.channelList as ArrayList<ChannelModel>)
                    hideKeyboard()
                    RxBus.get().post(FinishWelcome())
                    finish()
                }
            }
        } else {
            redirectByFromPage()
        }
    }

    private fun redirectByFromPage() {
        hideKeyboard()

        redirectUrl?.takeIf { it.isNotBlank() }?.let {
            startActivity(it)
            RxBus.get().post(FinishWelcome())
            finish()
            return
        }

        if (mPageFrom == FROM_WELCOME) {
            RxBus.get().post(FinishWelcome())
            startActivity(HOME_PAGE)
        } else {
            setResult(Activity.RESULT_OK)
        }

        finish()
    }

    private fun finishThis() {
        setResult(Activity.RESULT_OK)
        hideKeyboard()
        this.finish()
    }
}