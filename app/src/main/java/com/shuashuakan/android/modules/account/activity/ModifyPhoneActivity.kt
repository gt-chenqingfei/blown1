package com.shuashuakan.android.modules.account.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.TextView
import com.gyf.barlibrary.ImmersionBar
import com.shuashuakan.android.R
import com.shuashuakan.android.utils.bindView
import com.shuashuakan.android.utils.showLongToast
import java.util.regex.Pattern

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/07/31
 * Description:
 */
class ModifyPhoneActivity : AppCompatActivity() {

    private var phone: String? = null

    private val toolbar by bindView<Toolbar>(R.id.toolbar)
    private val phoneNumberTv by bindView<TextView>(R.id.phone_number)
    private val phoneEt by bindView<TextView>(R.id.phone_et)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_phone)
        ImmersionBar.with(this).navigationBarColor(R.color.black).init()
        ImmersionBar.setTitleBar(this, toolbar)
        phone = intent.getStringExtra("phone")
        initView()
    }

    override fun onDestroy() {
        super.onDestroy()
        ImmersionBar.with(this).destroy()
    }

    private fun initView() {
        phoneNumberTv.text = String.format(getString(R.string.string_bind_phone_format), phone)
        setupToolbar()
    }

    private fun setupToolbar() {
        toolbar.title = getString(R.string.string_phone_comfire_label)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.inflateMenu(R.menu.menu_go_next)
        toolbar.setOnMenuItemClickListener {
            goNext()
            true
        }
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun goNext() {
        if (parsePhone(phoneEt.text.toString())) {
            MobileModifyActivity.launchWithModify(this, phoneEt.text.toString())
//      startActivity(LoginActivity.modifyPhone(this, phoneEt.text.toString()))
            finish()
        } else {
            showLongToast(getString(R.string.string_error_phone_input))
        }
    }

    companion object {
        fun intent(context: Context, phone: String): Intent {
            val intent = Intent(context, ModifyPhoneActivity::class.java)
            intent.putExtra("phone", phone)
            return intent
        }
    }

    private fun parsePhone(mobile: String): Boolean {
        if (mobile.isEmpty()) return false
        val p = Pattern.compile("\\d{11}")
        val m = p.matcher(mobile)
//        val phoneNumFull = SpUtil.find(AppConfig.PHONE_NUM_FULL)
//        if (mobile != phoneNumFull) return false
        return m.matches()
    }
}
