package com.shuashuakan.android.modules.widget.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AppCompatDialogFragment
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import arrow.core.getOrElse
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI
import com.shuashuakan.android.FishInjection
import com.shuashuakan.android.R
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.CommonResult
import com.shuashuakan.android.data.api.model.FishApiError
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.event.BindSuccessEvent
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.modules.account.presenter.SendSMSApiView
import com.shuashuakan.android.modules.account.presenter.SendSMSPresenter
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.modules.track.ClickAction
import com.shuashuakan.android.utils.*
import com.tencent.bugly.crashreport.CrashReport.getUserId
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * Author:  lijie
 * Date:   2018/12/29
 * Email:  2607401801@qq.com
 * 绑定手机号的弹窗
 */
class BindPhoneDialog : AppCompatDialogFragment(), SendSMSApiView<CommonResult.Result> {

  private lateinit var phoneEdit: EditText
  private lateinit var codeEdit: EditText
  private lateinit var getVerifyBtn: TextView
  private lateinit var phoneClear: ImageView
  private lateinit var goBindBtn: TextView
  private lateinit var bird: ImageView
  private lateinit var closeIv: ImageView
  private lateinit var errorTip: TextView
  private var phoneHave: Boolean = false
  private var codeHave: Boolean = false
  private var countDownTimer: CountDownTimer? = null
  private val compositeDisposable = CompositeDisposable()
  private var type: String? = ""

  @Inject
  lateinit var sendSMSPresenter: SendSMSPresenter
  @Inject
  lateinit var apiService: ApiService

  companion object {
    const val COMMENT="comment"
    const val CHAIN_FEED = "chain_feed"
    const val CHAIN_ALL="chain_all"
    const val HOME_CENTER = "home_center"
    const val CHANNEL_DETAIL = "channel_detail"

    fun create(type: String): BindPhoneDialog {
      val fragment = BindPhoneDialog()
      val bundle = Bundle()
      bundle.putString("type", type)
      fragment.arguments = bundle
      return fragment
    }
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    type = arguments?.getString("type")
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val inflater = activity!!.layoutInflater
    val view = inflater.inflate(R.layout.layout_bind_phone_dialog, null)
    val dialog = Dialog(requireActivity())
    dialogSetting(dialog, view)
    initView(view)
    countDownTimer = (object : CountDownTimer(60 * 1000, 1000) {
      override fun onFinish() {
        getVerifyBtn.text = getString(R.string.string_renew_send)
        getVerifyBtn.isEnabled = true
        getVerifyBtn.setTextColor(getVerifyBtn.context.getColor1(R.color.ricebook_color_1))
      }

      @SuppressLint("SetTextI18n")
      override fun onTick(millisUntilFinished: Long) {
        getVerifyBtn.text = String.format(getString(com.shuashuakan.android.base.ui.R.string.string_second_format),millisUntilFinished / 1000)
        getVerifyBtn.isEnabled = false
      }
    })
    phoneEdit.requestFocus()
    initListener(dialog)
    return dialog
  }

  private fun dialogSetting(dialog: Dialog, view: View) {
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(view)
    // 设置宽度为屏宽、位置靠近屏幕底部
    val window = dialog.window
    window!!.setBackgroundDrawableResource(R.color.transparent)
    val wlp = window.attributes
    wlp.gravity = Gravity.CENTER
    wlp.width = WindowManager.LayoutParams.MATCH_PARENT
    wlp.height = WindowManager.LayoutParams.MATCH_PARENT
    window.attributes = wlp
  }

  private fun initView(view: View) {
    phoneEdit = view.findViewById(R.id.edit_phone)
    codeEdit = view.findViewById(R.id.edit_code)
    getVerifyBtn = view.findViewById(R.id.get_verify_btn)
    phoneClear = view.findViewById(R.id.phone_clear)
    goBindBtn = view.findViewById(R.id.go_bind_btn)
    bird = view.findViewById(R.id.bird)
    closeIv = view.findViewById(R.id.close_iv)
    errorTip = view.findViewById(R.id.error_tip)
  }

  override fun onAttach(context: Context?) {
    FishInjection.inject(this)
    super.onAttach(context)
    sendSMSPresenter.attachView(this)
  }

  override fun onDetach() {
    super.onDetach()
    sendSMSPresenter.detachView(false)
  }

  private fun initListener(dialog: Dialog) {
    phoneClear.setOnClickListener {
      phoneEdit.setText("")
    }
    closeIv.setOnClickListener {
      dismiss()
    }
    phoneEdit.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(s: Editable?) {
      }

      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
      }

      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (s != null && s.isNotEmpty()) {
          phoneHave = true
          phoneClear.visibility = View.VISIBLE
          getVerifyBtn.setTextColor(phoneClear.context.getColor1(R.color.ricebook_color_1))
        } else {
          phoneHave = false
          phoneClear.visibility = View.GONE
          getVerifyBtn.setTextColor(phoneClear.context.getColor1(R.color.color_normal_b6b6b6))
        }
        checkGoBind()
      }

    })
    codeEdit.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(s: Editable?) {
      }

      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
      }

      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        codeHave = s != null && s.isNotEmpty()
        checkGoBind()
      }
    })

    phoneEdit.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
      if (hasFocus)
        bird.setImageResource(R.drawable.ic_bird)
    }
    codeEdit.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
      if (hasFocus)
        bird.setImageResource(R.drawable.ic_bird_sleep)
    }
    getVerifyBtn.noDoubleClick {
      if (!isPhoneValidate()) {
        errorTip.visibility = View.VISIBLE
        errorTip.text = getString(R.string.string_error_phone_input)
        return@noDoubleClick
      }
      sendSMSPresenter.requestApi(phoneEdit.text.toString())
      getVerifyBtn.requestFocus()
    }
    goBindBtn.setOnClickListener {
      bindMethod()
    }
  }

  private fun bindMethod() {
    apiService.bindMobilePhone(phoneEdit.text.toString(), codeEdit.text.toString())
        .applySchedulers()
        .subscribeApi(onNext = {
          requireActivity().showLongToast(getString(R.string.string_bind_phone_success))
          requireActivity().getSpider().manuallyEvent(SpiderEventNames.BIND_PHONE)
              .put("userID", getUserId())
              .track()
          RxBus.get().post(BindSuccessEvent(type ?: ""))
          //首次绑定手机号
          SensorsDataAPI.sharedInstance().profileSetOnce(ClickAction.INIT_HAS_BIND_MOBILE, formatTime(System.currentTimeMillis()))
          requireActivity().setResult(Activity.RESULT_OK)
          this.dialog.dismiss()
        }, onApiError = {
          errorTip.visibility = View.VISIBLE
          if (it is ApiError.HttpError) {
            val error = ((it as? ApiError.HttpError)?.apiError?.getOrElse { null } as? FishApiError)
            errorTip.text = error?.errorMsg
          }
        }).addTo(compositeDisposable)
  }

  private fun isPhoneValidate(): Boolean = parsePhone(phoneEdit.text.toString())

  private fun parsePhone(mobile: String): Boolean {
    if (mobile.isEmpty()) return false
    val p = Pattern.compile("\\d{11}")
    val m = p.matcher(mobile)
    return m.matches()
  }

  private fun checkGoBind() {
    goBindBtn.isEnabled = phoneHave && codeHave && codeEdit.text.toString().length == 6&&phoneEdit.text.toString().length ==11
  }

  override fun showData(data: CommonResult.Result) {
    if (data.isSuccess) {
      requireActivity().showLongToast(getString(R.string.string_code_has_send))
      getVerifyBtn.text = getString(R.string.string_second_60)
      countDownTimer?.start()
      getVerifyBtn.setTextColor(getVerifyBtn.context.getColor1(R.color.color_normal_b6b6b6))
    } else {
      requireActivity().showLongToast(getString(R.string.string_code_send_error))
    }
  }

  override fun showMessage(message: String) {
    requireActivity().showLongToast(message)
  }

  override fun onDestroy() {
    super.onDestroy()
    compositeDisposable.clear()
    countDownTimer?.cancel()
  }
}