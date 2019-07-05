package com.shuashuakan.android.modules.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.gyf.barlibrary.ImmersionBar
import com.shuashuakan.android.R
import com.shuashuakan.android.ui.base.FishActivity
import com.shuashuakan.android.utils.bindView
import com.shuashuakan.android.utils.showAlertDialog
import com.shuashuakan.android.utils.showLongToast

class EditSignActivity : FishActivity() {

  private val toolbar by bindView<Toolbar>(R.id.toolbar)
  private val signEdit by bindView<EditText>(R.id.sign_edit)
  private val signNum by bindView<TextView>(R.id.sign_num)

  private var signText: String? = null
  private var isSign: Boolean = false

  companion object {
    const val SIGN_TEXT = "sign_text"
    fun create(context: Context, isSign: Boolean, sign_text: String): Intent {
      return Intent(context, EditSignActivity::class.java)
          .putExtra("is_sign", isSign)
          .putExtra("sign_text", sign_text)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_edit_sign)
    ImmersionBar.with(this).navigationBarColor(R.color.black).init()
    ImmersionBar.setTitleBar(this,toolbar)
    isSign = intent.getBooleanExtra("is_sign", false)
    signText = intent.getStringExtra("sign_text")

    initViews()
    setupToolbar()
  }

  override fun onDestroy() {
    super.onDestroy()
    ImmersionBar.with(this).destroy()
  }

  private fun initViews() {
    if (isSign && signText?.length ?: 0 > 0) {
      signNum.visibility = View.VISIBLE
      signNum.text = signText?.length.toString() + "/40"
    }
    signEdit.hint = if (isSign) getString(R.string.edit_bio_hint) else getString(R.string.edit_nick_hint)
    signEdit.maxLines = if (isSign) 2 else 1
    signEdit.setText(signText
        ?: if (isSign) getString(R.string.edit_bio_hint) else getString(R.string.edit_nick_hint))
    signEdit.setSelection(signText?.length ?: 0)
    signEdit.addTextChangedListener(object : TextWatcher {
      private var temp: CharSequence? = null
      private var editStart: Int = 0
      private var editEnd: Int = 0
      override fun beforeTextChanged(
          s: CharSequence?,
          start: Int,
          count: Int,
          after: Int
      ) {
        temp = s
      }

      override fun onTextChanged(
          s: CharSequence?,
          start: Int,
          before: Int,
          count: Int
      ) {
        if (isSign) {
          signNum.text = (s?.length ?: 0).toString() + "/40"
        }
      }

      override fun afterTextChanged(s: Editable) {
        editStart = signEdit.selectionStart
        editEnd = signEdit.selectionEnd
        if (isSign) {
          signNum.visibility = View.VISIBLE
          if (temp!!.length > 40) {
            showLongToast(getString(R.string.edit_bio_max_hint), Gravity.CENTER)
            s.delete(editStart - 1, editEnd)
            val tempSelection = editStart
            signEdit.text = s
            signEdit.setSelection(tempSelection)
            signNum.text = s.length.toString() + "/40"
          }
        } else {
          if (temp!!.length > 12) {
            showLongToast(getString(R.string.edit_nick_max_hint), Gravity.CENTER)
            s.delete(editStart - 1, editEnd)
            val tempSelection = editStart
            signEdit.text = s
            signEdit.setSelection(tempSelection)
          }
        }
      }
    })
  }

  private fun setupToolbar() {
    toolbar.title = if (isSign) getString(R.string.string_edit_signature) else getString(R.string.string_edit_nikename)
    toolbar.inflateMenu(R.menu.menu_confirm)
    toolbar.setOnMenuItemClickListener {
      doSave()
      true
    }
    toolbar.setNavigationOnClickListener {
      showAlertDialog(this)
    }
  }

  private fun diff(): Boolean {
    return (signText == null || signText == signEdit.text.toString())
  }

  private fun doSave() {
    intent.putExtra(SIGN_TEXT, signEdit.text.toString())
    setResult(Activity.RESULT_OK, intent)
    finish()
  }
}
