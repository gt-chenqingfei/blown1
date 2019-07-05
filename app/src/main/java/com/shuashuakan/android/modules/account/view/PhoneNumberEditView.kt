package com.shuashuakan.android.modules.account.view

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.EditText
import com.shuashuakan.android.modules.account.vm.AccountViewModel
import java.util.regex.Pattern

/**
 * Author:ricebook
 * Time:2019/5/8  下午6:39
 */
class PhoneNumberEditView(context: Context, attrs: AttributeSet) : EditText(context, attrs), TextWatcher {
    lateinit var mViewModel: AccountViewModel

    init {
        this.addTextChangedListener(this)
    }

    fun attach(viewModel: AccountViewModel) {
        mViewModel = viewModel
    }

    override fun afterTextChanged(s: Editable?) {
        mViewModel.updatePhoneNumberState(isPhoneValidate(s.toString()))
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    private fun isPhoneValidate(mobile: String): Boolean {
        if (mobile.isEmpty()) return false
        val p = Pattern.compile("\\d{11}")
        val m = p.matcher(mobile)
        return m.matches()
    }

    fun getPhoneNumber():String{
        return text.toString()
    }

}