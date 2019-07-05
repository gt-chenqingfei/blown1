package com.shuashuakan.android.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.AppCompatRadioButton
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import cn.jpush.android.api.JPushInterface
import com.ishumei.smantifraud.SmAntiFraud
import com.shuashuakan.android.R
import com.shuashuakan.android.commons.util.ACache
import com.shuashuakan.android.commons.util.ACache.KEY_SWITCH_ENVIRONMENT
import com.shuashuakan.android.data.api.COMMON
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.modules.account.JMessageFunc
import com.shuashuakan.android.spider.Spider
import com.shuashuakan.android.ui.base.FishFragment
import com.shuashuakan.android.utils.*
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class DebugDrawerFragment : FishFragment() {
    private val jsButton: Button by bindView(R.id.jsSdkButton)
    private val postButton: Button by bindView(R.id.postButton)
    private val accountButton: Button by bindView(R.id.accountButton)
    private val smIdButton: Button by bindView(R.id.smIdButton)
    private val linkResolveButton: Button by bindView(R.id.applinkButton)
    private val linkResolveEditView: EditText by bindView(R.id.applinkEditView)
    private val environmentGroup: RadioGroup by bindView(R.id.environment_group)
    private val releaseRadio: AppCompatRadioButton by bindView(R.id.release_rb)
    private val debugRadio: AppCompatRadioButton by bindView(R.id.debug_rb)
    private val pushIdButton: Button by bindView(R.id.pushIdButton)
    private val title: TextView by bindView(R.id.title)

    @Inject
    lateinit var apiService: ApiService
    @Inject
    @field:Named(COMMON)
    lateinit var httpClient: OkHttpClient
    @Inject
    lateinit var spider: Spider
    @Inject
    lateinit var logout: Logout
    @Inject
    lateinit var jMessageFunc: JMessageFunc

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_debug_drawer, container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        jsButton.setOnClickListener {
            activity?.startActivity("https://app.shuashuakan.net/test")
        }

        title.setOnClickListener {
            requireContext().startActivity(Intent(requireContext(), PrometheusActivity::class.java))
        }

        postButton.setOnClickListener {
            testPost()
        }

        linkResolveButton.setOnClickListener {
            val link = linkResolveEditView.text.toString()
            if (link.isBlank()) {
                context?.showLongToast("Please input the link")
            } else {
                context?.startActivity(link)
            }
        }
        pushIdButton.setOnClickListener {
            requireContext().copyString(JPushInterface.getRegistrationID(requireContext()), getString(R.string.string_content_has_to_board))
        }
        smIdButton.setOnClickListener {
            requireContext().copyString(SmAntiFraud.getDeviceId(), getString(R.string.string_content_has_to_board))
        }

        val s = ACache.get(requireContext()).getAsString(KEY_SWITCH_ENVIRONMENT)

        if (s != null) {
            if (s == "release") {
                releaseRadio.isChecked = true
            } else {
                debugRadio.isChecked = true
            }
        } else {
            debugRadio.isChecked = true
        }

        accountButton.setOnClickListener {
            logout.logout()
            jMessageFunc.logout()
            requireContext().showLongToast(getString(R.string.string_logout_success))
        }

        environmentGroup.setOnCheckedChangeListener { _, p1 ->
            if (p1 == R.id.release_rb) {
                ACache.get(requireContext()).put(KEY_SWITCH_ENVIRONMENT, "release")
            } else if (p1 == R.id.debug_rb) {
                ACache.get(requireContext()).put(KEY_SWITCH_ENVIRONMENT, "debug")
            }
            logout.logout()
            jMessageFunc.logout()
            requireContext().showLongToast(getString(R.string.string_please_stant_app))
            System.exit(0)
        }
    }


    private fun testPost() {
        val url = HttpUrl.parse("https://opentest.seriousapps.cn/3/oauth2/send_taobao_totp_code.json")!!
        val form = FormBody.Builder().add("client_id", "100018")
                .add("client_secret", "311fc54fe8869b182ecc85c0ecc40b35")
                .add("mobile_phone", "15901116008")
                .build()

        val request = Request.Builder().url(url)
                .post(form)
                .build()
        Completable.fromAction {
            try {
                val response = httpClient.newCall(request).execute()
                Timber.tag("HTTP").d("Response: ${response.body()?.string()}")
            } catch (e: Exception) {
                Timber.tag("HTTP").e(e, "error")
            }
        }.subscribeOn(Schedulers.io())
                .subscribe()
    }
}