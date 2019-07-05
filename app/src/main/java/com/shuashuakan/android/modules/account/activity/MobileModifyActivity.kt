package com.shuashuakan.android.modules.account.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import com.gyf.barlibrary.ImmersionBar
import com.shuashuakan.android.R
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.account.ChannelModel
import com.shuashuakan.android.data.api.model.account.GuideModel
import com.shuashuakan.android.event.FinishWelcome
import com.shuashuakan.android.modules.account.vm.AccountViewModel
import com.shuashuakan.android.modules.HOME_PAGE
import com.shuashuakan.android.ui.ProgressDialog
import com.shuashuakan.android.ui.base.FishActivity
import com.shuashuakan.android.utils.hideKeyboard
import com.shuashuakan.android.utils.startActivity
import kotlinx.android.synthetic.main.activity_bind_mobile.*

/**
 * @author qingfei.chen
 * @Date 2019-05-06
 */
class MobileModifyActivity : FishActivity(), View.OnClickListener {
    companion object {
        const val TYPE_BIND = 1
        const val TYPE_MODIFY = 2
        const val EXTRA_OLD_MOBILE = "extra_old_mobile"
        const val EXTRA_TYPE = "extra_type"
        fun launchWithBind(context: Context) {
            context.startActivity(Intent(context, MobileModifyActivity::class.java)
                    .putExtra(EXTRA_TYPE, TYPE_BIND))
        }

        fun launchWithModify(context: Context, oldMobile: String) {
            context.startActivity(Intent(context, MobileModifyActivity::class.java)
                    .putExtra(EXTRA_OLD_MOBILE, oldMobile)
                    .putExtra(EXTRA_TYPE, TYPE_MODIFY))
        }
    }

    lateinit var mAccountViewModel: AccountViewModel
    var oldMobile: String? = null
    var type: Int = TYPE_BIND

    private val dialog by lazy {
        return@lazy ProgressDialog.progressDialog(this, getString(R.string.string_loading))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bind_mobile)
        ImmersionBar.with(this).navigationBarColor(R.color.black).init()
        oldMobile = intent.getStringExtra(EXTRA_OLD_MOBILE)
        type = intent.getIntExtra(EXTRA_TYPE, TYPE_BIND)

        mAccountViewModel = ViewModelProviders.of(this).get(AccountViewModel::class.java)
        initListener()
        registerLiveDataObserver()
    }

    override fun onDestroy() {
        super.onDestroy()
        ImmersionBar.with(this).destroy()
        dialog.dismiss()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (type == TYPE_BIND && event.keyCode == KeyEvent.KEYCODE_BACK) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }


    override fun onClick(v: View?) {
        when (v) {
            tv_bind_mobile_no -> {
                goHome()
            }
            tv_bind_next -> {
                hideKeyboard()
                dialog.show()
                mAccountViewModel.bindMobile(oldMobile, et_bind_mobile_phone.getPhoneNumber(),
                        vw_bind_verification_code.getValidCode())
            }
        }
    }

    private fun initListener() {
        tv_bind_mobile_no.setOnClickListener(this)
        tv_bind_next.setOnClickListener(this)
        vw_bind_verification_code.attach(mAccountViewModel, et_bind_mobile_phone)
        et_bind_mobile_phone.attach(mAccountViewModel)
    }

    private fun registerLiveDataObserver() {
        mAccountViewModel.mExcuteBtnStateLiveData.observe(this, Observer {
            tv_bind_next.isEnabled = it == true
        })

        mAccountViewModel.mBindMobileLiveData.observe(this, Observer {
            if (it != true) {
                dialog.dismiss()
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
                            userGuide.data?.userName, userGuide.pageTypes as ArrayList<String>, userGuide.data?.channelList as ArrayList<ChannelModel>)
                    hideKeyboard()
                    finish()
                }
            }
        } else {
            goHome()
        }
    }


    private fun goHome() {
        hideKeyboard()
        startActivity(HOME_PAGE)
        finish()
        RxBus.get().post(FinishWelcome())
    }


}