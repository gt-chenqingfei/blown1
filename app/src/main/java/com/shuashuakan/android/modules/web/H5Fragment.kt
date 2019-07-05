package com.shuashuakan.android.modules.web

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.graphics.Bitmap
import android.hardware.SensorEvent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.support.v7.widget.Toolbar
import android.util.Patterns
import android.view.*
import android.webkit.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.os.bundleOf
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI
import com.shuashuakan.android.BuildConfig
import com.shuashuakan.android.R
import com.shuashuakan.android.commons.util.ShakeSensor
import com.shuashuakan.android.commons.util.ShakeSensor.OnShakeListener
import com.shuashuakan.android.commons.util.hideSoftInput
import com.shuashuakan.android.commons.util.startCallIntent
import com.shuashuakan.android.commons.util.triggerVibrate
import com.shuashuakan.android.data.api.model.detail.ShareContent
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.js.JsMessage
import com.shuashuakan.android.js.MethodProcessor
import com.shuashuakan.android.js.RainbowBridge
import com.shuashuakan.android.js.appendUserAgents
import com.shuashuakan.android.modules.ACCOUNT_PAGE
import com.shuashuakan.android.modules.SMS_INVITE
import com.shuashuakan.android.modules.share.ShareHelper
import com.shuashuakan.android.ui.base.FishFragment
import com.shuashuakan.android.ui.base.OnBackClickListener
import com.shuashuakan.android.modules.web.method.MethodScope
import com.shuashuakan.android.modules.web.method.ViewMethodProcessor.ViewController
import com.shuashuakan.android.utils.*
import com.squareup.moshi.Moshi
import com.tbruyelle.rxpermissions2.RxPermissions
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class H5Fragment : FishFragment(), ViewController, OnBackClickListener {

    private var httpUrl: String? = null
    override fun inviteBySms() {
        requireActivity().startActivity(SMS_INVITE)
    }

    override fun onBackClick(): Boolean {
        return if (webView.canGoBack()) {
            webView.goBack()
            true
        } else {
            false
        }
    }

    companion object {
        private const val REQUEST_SELECT_FILE = 100
        private const val LOGIN_REQUEST = 1
        private const val SELECT_ADDRESS = 2
        private const val TAG = "H5Fragment"
        private const val SHOW_NAVIGATION_ICON = "extra_show_navigation_icon"
        private const val HTTP_URL = "extra_http_url"

        fun create(showNavigationIcon: Boolean, url: String): H5Fragment {
            val arguments = bundleOf(
                    HTTP_URL to url,
                    SHOW_NAVIGATION_ICON to showNavigationIcon
            )
            val h5Fragment = H5Fragment()
            h5Fragment.arguments = arguments
            return h5Fragment
        }
    }


    private var uploadMessage: ValueCallback<Array<Uri>>? = null

    @Inject
    lateinit var methodProcessor: Set<@JvmSuppressWildcards MethodProcessor>
    @Inject
    lateinit var accountManager: AccountManager
    @Inject
    lateinit var shareHelper: ShareHelper
    @Inject
    lateinit var moshi: Moshi

    private lateinit var gestureDetector: GestureDetector

    private val toolbar by bindView<Toolbar>(R.id.toolbar)
    private val webView by bindView<WebView>(R.id.webView)
    private val loadingBar by bindView<View>(R.id.loadingBar)
    private val progressBar by bindView<ProgressBar>(R.id.progressBar)

    private val shakeSensor: ShakeSensor by lazy {
        val shakeSensor = ShakeSensor(requireContext())
        shakeSensor.shakeListener = object : OnShakeListener {
            override fun onShakeComplete(event: SensorEvent) {
                rainbowBridge.postMessageToJs(
                        JsMessage.create(scope = MethodScope.DEVICE.scope, name = "shakeAround") {})
            }
        }
        return@lazy shakeSensor
    }

    private val rainbowBridge by lazy { createRainbowBridge() }
    private var shareContent: ShareContent? = null

    private val webChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(
                view: WebView,
                newProgress: Int
        ) {
            progressBar.progress = newProgress
        }

        override fun onReceivedTitle(
                view: WebView?,
                title: String?
        ) {
            super.onReceivedTitle(view, title)
            title?.let { setupTitle(it) }
        }

        override fun onJsPrompt(
                view: WebView?,
                url: String?,
                message: String?,
                defaultValue: String?,
                result: JsPromptResult?
        ): Boolean {
            Timber.tag(TAG)
                    .d("onJsPrompt: $message")
            return super.onJsPrompt(view, url, message, defaultValue, result)
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams): Boolean {
            if (uploadMessage != null) {
                uploadMessage!!.onReceiveValue(null)
                uploadMessage = null
            }
            uploadMessage = filePathCallback
            val intent = fileChooserParams.createIntent()
            try {
                startActivityForResult(intent, REQUEST_SELECT_FILE)
            } catch (e: ActivityNotFoundException) {
                uploadMessage = null
                Toast.makeText(context, "Cannot Open File Chooser", Toast.LENGTH_LONG).show()
                return false
            }
            return true
        }
    }

    private val webClient = object : WebViewClient() {
        override fun onPageFinished(
                view: WebView?,
                url: String?
        ) {
            progressBar.visibility = View.GONE
        }

        override fun onPageStarted(
                view: WebView?,
                url: String?,
                favicon: Bitmap?
        ) {
            progressBar.visibility = View.VISIBLE
        }
    }

    private fun createRainbowBridge(): RainbowBridge {
        return RainbowBridge.create(webView) {
            version = BuildConfig.VERSION_NAME
            chromeClient = webChromeClient
            webViewClient = webClient
            methodProcessors = methodProcessor.toList()
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_h5, container, false)
    }


    private var downX: Int = 0
    private var downY: Int = 0

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val showNavigationIcon = arguments?.getBoolean(SHOW_NAVIGATION_ICON) ?: true
        if (showNavigationIcon) {
            toolbar.navigationIcon(R.drawable.ic_close_white_24dp) { requireActivity().finish() }
        } else {
            toolbar.navigationIcon = null
        }
        toolbar.inflateMenu(R.menu.menu_share)
        toolbar.menu.findItem(R.id.action_share)
                .isVisible = false
        toolbar.setOnMenuItemClickListener {
            showShare(shareContent)
            true
        }
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                downX = e.x.toInt()
                downY = e.y.toInt()
            }
        })

        httpUrl = arguments?.getString(HTTP_URL)
        if (httpUrl == null || httpUrl!!.isBlank())
            requireActivity().finish()
        else setup(httpUrl!!)
    }

    @SuppressLint("CheckResult")
    private fun setup(url: String) {
        methodProcessor.forEach { Timber.d(it::class.java.simpleName) }
        rainbowBridge.setup()
        rainbowBridge.enableDebug(BuildConfig.DEBUG)
        webView.appendUserAgents(listOf("${BuildConfig.APP_UA}/${BuildConfig.VERSION_NAME}"))
        SensorsDataAPI.sharedInstance().showUpWebView(webView, false)
        // the last
        Timber.tag(TAG)
                .d("loading $url")
        webView.loadUrl(url)
        webView.setOnLongClickListener {


            val result: WebView.HitTestResult? = webView.hitTestResult
            if (null == result) {
                false
            } else {
                if (result.type == WebView.HitTestResult.IMAGE_TYPE) {
                    RxPermissions(this@H5Fragment)
                            .requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .subscribe { permission ->
                                when {
                                    permission.granted -> {
                                        val imgurl = result.extra
                                        longClickPhotoProcess(imgurl, downX, downY + 10)
                                    }
                                    permission.shouldShowRequestPermissionRationale -> showToast(getString(R.string.string_can_next_with_agree_tips))
                                    else -> setGoSettingDialog()
                                }
                            }
                }
                true
            }
        }
    }

    private fun longClickPhotoProcess(url: String, x: Int, y: Int) {
        val items = arrayOf(getString(R.string.string_save_pic_storage))
        android.support.v7.app.AlertDialog.Builder(requireContext())
                .setItems(items) { _, which ->
                    if (which == 0) {
                        val fileName = "刷刷看_${TimeUtil.getCurrentTime("yyyyMMddHHmmss")}.png"
                        val file = File("${Environment.getExternalStorageDirectory()}/DCIM/Camera/$fileName")
                        if (!file.exists()) {
                            file.mkdirs()
                        }
                        saveImageFromDataSource(requireContext(), url, file.absolutePath)
                    }
                }
                .show()
    }

    private fun setGoSettingDialog() {
        var dialog: AlertDialog = AlertDialog.Builder(requireActivity())
                .setMessage(getString(com.shuashuakan.android.base.ui.R.string.string_guide_permission))
                .setPositiveButton(getString(com.shuashuakan.android.base.ui.R.string.string_setting)) { dialogInterface, i ->
                    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData(Uri.fromParts("package", requireActivity().packageName, null)))
                    dialogInterface.cancel()
                }
                .create()
        dialog.show()
        var button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        if (button != null) {
            button.setTextColor(resources.getColor(R.color.black20))
        }

    }

    private fun setupTitle(title: String) {
        if (Patterns.WEB_URL.matcher(title).matches()) return
        toolbar.title = title
    }

    override fun openPage(url: String) {
        if (url.startsWith(ACCOUNT_PAGE)) {
            requireActivity().startActivityForResultByLink(url, LOGIN_REQUEST)
        } else {
            requireActivity().startActivity(url)
        }
    }

    override fun closePage() {
        requireActivity().finish()
    }

    override fun setToolbarTitle(title: String) {
        toolbar.title = title
    }

    override fun showLoadingBar() {
        loadingBar.visibility = View.VISIBLE
    }

    override fun dismissLoadingBar() {
        loadingBar.visibility = View.GONE
    }

    override fun makeCall(tels: List<String>) {
        val numbers = tels.toTypedArray()
        AlertDialog.Builder(requireContext())
                .setItems(numbers
                ) { _, which ->
                    if (which >= 0 && which < numbers.size) {
                        val call = numbers[which]
                        startCallIntent(requireContext(), call)
                    }
                }
                .setNegativeButton(getString(com.shuashuakan.android.base.ui.R.string.string_cancel), null)
                .show()
    }

    override fun dismissKeyboard() {
        webView.hideSoftInput()
    }

    override fun showToast(message: String) {
        requireContext().applicationContext.showLongToast(message)
    }

    override fun share(shareContent: ShareContent) {
        if (shareContent.trigger) {
            showShare(shareContent)
        } else {
            showShareMenu(shareContent)
        }
    }

    override fun setClipBoard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.primaryClip = ClipData.newPlainText("", text)
    }

    private fun showShare(shareContent: ShareContent?) {
        shareHelper.doShare(requireActivity(), shareContent, null, false, false, null, null, true, httpUrl)
    }

    private fun showShareMenu(shareContent: ShareContent) {
        toolbar.menu.findItem(R.id.action_share)
                .isVisible = true
        this.shareContent = shareContent
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView.destroy()
    }

    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == LOGIN_REQUEST) {
            // login success
            val accessToken = accountManager.accessToken()
            // post message to js
            if (accessToken != null) {
                val message = JsMessage.create(scope = "User", name = "loginSuccess") {
                    put("token", accessToken)
                }
                rainbowBridge.postMessageToJs(message)
            }
        } else if (resultCode == Activity.RESULT_CANCELED && requestCode == LOGIN_REQUEST) {
            // post message to js
            val message = JsMessage.create(scope = "User", name = "loginCanceled") {}
            rainbowBridge.postMessageToJs(message)
        } else if (requestCode == REQUEST_SELECT_FILE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (uploadMessage == null)
                return
            uploadMessage!!.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data))
            uploadMessage = null
        }
        super.onActivityResult(requestCode, resultCode, data)
        shareHelper.handleShareActivityCallback(requireActivity(), requestCode, resultCode, data)
    }


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            shakeSensor.register()
        } else {
            shakeSensor.unregister()
        }
    }

    override fun vibrate() {
        triggerVibrate(requireContext())
    }
}