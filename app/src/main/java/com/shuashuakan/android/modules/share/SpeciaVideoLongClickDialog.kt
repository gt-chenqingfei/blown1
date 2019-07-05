package com.shuashuakan.android.modules.share

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.shuashuakan.android.R
import com.shuashuakan.android.commons.util.ACache
import com.shuashuakan.android.config.AppConfig
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.account.activity.LoginActivity
import com.shuashuakan.android.service.PullService
import com.shuashuakan.android.modules.publisher.PermissionRequestFragment
import com.shuashuakan.android.modules.widget.dialogs.BindPhoneDialog
import com.shuashuakan.android.utils.SpUtil
import com.shuashuakan.android.utils.noDoubleClick
import com.shuashuakan.android.utils.showLongToast
import com.shuashuakan.android.utils.showShortToast

/**
 * 主视频页面长按发布接龙视频弹框
 *
 * Author: ZhaiDongyang
 * Date: 2019/3/19
 */
class SpeciaVideoLongClickDialog(
        private val activity: FragmentActivity,
        private val apiService: ApiService? = null,
        private val accountManager: AccountManager? = null,
        private val feedId: String? = "",
        private val feed: Feed? = null
) : Dialog(activity, R.style.showCommentShareDialog) {

    override fun setOnDismissListener(listener: DialogInterface.OnDismissListener?) {
        super.setOnDismissListener(listener)
        dismiss()
    }

    private lateinit var imageView: ImageView
    private var alertDialog: AlertDialog? = null
    private var chainsData: Feed? = null

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_special_video_long_click,
                null, false)
        setContentView(view)
        getView(view)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val window = window
        window!!.setGravity(Gravity.CENTER)
        initListener()
    }

    private fun initListener() {
        imageView.noDoubleClick {
            SpUtil.saveOrUpdateBoolean(AppConfig.CHAIN_BOTTOM_FLOAT, true)
            if (!SpUtil.findBoolean(AppConfig.SHOW_GUIDE_DIALOG)) {
                showGuideDialog()
            } else {
                chainsMethod()
            }
        }
    }

    private fun getView(view: View) {
        imageView = view.findViewById(R.id.iv_video)
    }

    private fun showGuideDialog() {
        val dialogLayout = LayoutInflater.from(activity).inflate(R.layout.layout_dialog_feed_guide, null)
        val goFeedBtn = dialogLayout.findViewById<TextView>(R.id.go_feed_btn)
        val startChain = dialogLayout.findViewById<TextView>(R.id.start_chain)
        val closeIv = dialogLayout.findViewById<ImageView>(R.id.close_iv)
        val videoLayout = dialogLayout.findViewById<LinearLayout>(R.id.demo_video_layout)

        alertDialog = android.app.AlertDialog.Builder(activity, R.style.CustomDialog)
                .setView(dialogLayout)
                .create()
        if (alertDialog != null)
            alertDialog?.window?.setWindowAnimations(R.style.BottomDialog_Animation)

        alertDialog!!.show()
        SpUtil.saveOrUpdateBoolean(AppConfig.SHOW_GUIDE_DIALOG, true)
        closeIv.setOnClickListener {
            alertDialog?.dismiss()
        }
        startChain.noDoubleClick {
            //开始接龙
            chainsMethod()
        }
//        val demoVideoId: String = ACache.get(activity).getAsString(ACache.KEY_DEMO_VIDEO_ID)
//        if (demoVideoId.isEmpty()) {
//            videoLayout.visibility = View.GONE
//        } else {
//            videoLayout.visibility = View.VISIBLE
//        }
//        goFeedBtn.setOnClickListener {
//            activity.startActivity(AllChainsActivity.create(activity, demoVideoId, ""))
//            //startActivity(AllChainsActivity.create(requireActivity(), ChainsListIntentParam.demoVideoOpen(demoVideoId)))
//        }
    }

    private fun chainsMethod() {
        dismiss()
        if (!accountManager?.hasAccount()!!) {
            LoginActivity.launch(activity)
            return
        }
        if (alertDialog != null && alertDialog!!.isShowing) {
            alertDialog!!.dismiss()
        }
        if (SpUtil.find(AppConfig.LOGIN_TYPE) == "WeChat" && SpUtil.find(AppConfig.PHONE_NUM).isNullOrEmpty()
                && ACache.get(activity).getAsString(AppConfig.SHOE_BIND_PHONE) != "show") {
            BindPhoneDialog.create(BindPhoneDialog.CHAIN_FEED).show(activity.supportFragmentManager, "all_chain")
            ACache.get(activity).put(AppConfig.SHOE_BIND_PHONE, "show", AppConfig.BIND_DIALOG_SAVE_TIME)
        } else {
            realChain()
        }
    }

    private fun realChain() {
        if (PullService.canUpload()) {
            SpUtil.saveOrUpdateBoolean(AppConfig.CHAIN_BOTTOM_FLOAT, true)
            chainsData = feed
            if (chainsData?.type == "SOLITAIRE") {
                goPermissionPage(chainsData?.masterFeedId)
            } else if (chainsData?.type == "NORMAL") {
                goPermissionPage(chainsData?.id)
            }
        } else {
            activity.showLongToast(activity.getString(R.string.string_publish_update_ing_tips))
        }
    }

    private fun goPermissionPage(id: String?) {
        if (PullService.canUpload()) {
            PermissionRequestFragment.create(PullService.UploadEntity.TYPE_ADD_SOLITAIRE, id)
                    .show(activity.supportFragmentManager, "chain_feed_fragment")
        } else {
            activity.showShortToast(activity.getString(R.string.string_publish_wait_edit))
        }
    }

}