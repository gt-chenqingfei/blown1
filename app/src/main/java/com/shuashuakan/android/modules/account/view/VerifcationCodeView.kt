package com.shuashuakan.android.modules.account.view

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.content.Context
import android.os.CountDownTimer
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.RelativeLayout
import com.luck.picture.lib.tools.ToastManage
import com.shuashuakan.android.R
import com.shuashuakan.android.modules.account.vm.AccountViewModel
import com.shuashuakan.android.utils.PhoneCheckUtil
import kotlinx.android.synthetic.main.widget_verification_code_layout.view.*

/**
 * @author hushiguang
 * @since 2019-05-07.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
class VerifcationCodeView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RelativeLayout(context, attrs, defStyleAttr), TextWatcher {


    private val mContext: Context = context
    private lateinit var mAccountViewModel: AccountViewModel

    init {
        val mRootView = LayoutInflater.from(mContext).inflate(R.layout.widget_verification_code_layout, null)

        addView(mRootView)
        et_login_verification_code.addTextChangedListener(this)
    }

    fun attach(accountViewModel: AccountViewModel, etMobilePhone: EditText) {
        mAccountViewModel = accountViewModel
        accountViewModel.mValidCodeLiveData.observe(context as AppCompatActivity, Observer {
            if (it?.isSuccess == true) {
                ToastManage.s(mContext, mContext.getString(R.string.string_code_has_send))
                tv_login_get_valid_code.text = mContext.getString(R.string.string_second_60)
                countDownTimer.start()
            } else {
                ToastManage.s(mContext, mContext.getString(R.string.string_code_send_error))
            }
        })

        mAccountViewModel.mPhoneEditorLiveData.observe(context as AppCompatActivity, Observer {
            tv_login_get_valid_code.isEnabled = it == true
        })

        tv_login_get_valid_code.setOnClickListener {
            getVerificationCode(etMobilePhone.text.toString())
        }
    }

    override fun afterTextChanged(s: Editable?) {
        val text = s.toString()
        mAccountViewModel.updateValidCodeState(text.isNotEmpty() && text.length == 6)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }


    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    fun getVerificationCode(phoneNumber: String) {
        if (!PhoneCheckUtil.isChinaPhoneLegal(phoneNumber)) {
            ToastManage.s(mContext, mContext.getString(R.string.string_error_phone_input))
            return
        }

        mAccountViewModel.getValidCode(phoneNumber)
    }

    override fun onDetachedFromWindow() {
        countDownTimer.cancel()
        super.onDetachedFromWindow()
    }

    private val countDownTimer: CountDownTimer by lazy {
        object : CountDownTimer(60 * 1000, 1000) {
            override fun onFinish() {
                tv_login_get_valid_code.text = mContext.getString(R.string.string_send_code)
                tv_login_get_valid_code.isEnabled = true
            }

            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                tv_login_get_valid_code.text = String.format(mContext.getString(R.string.string_second_format), millisUntilFinished / 1000)
                tv_login_get_valid_code.isEnabled = false
            }
        }
    }

    fun getValidCode(): String {
        return et_login_verification_code.text.toString()
    }
}