package com.shuashuakan.android.modules.viphome

import android.arch.lifecycle.Observer
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.view.*
import com.gyf.barlibrary.ImmersionBar
import com.luck.picture.lib.tools.ScreenUtils
import com.shuashuakan.android.R
import com.shuashuakan.android.modules.account.activity.LoginActivity
import com.shuashuakan.android.utils.startActivity
import com.shuashuakan.android.modules.HOME_PAGE
import com.shuashuakan.android.ui.ProgressDialog
import com.shuashuakan.android.utils.daggerComponent
import com.shuashuakan.android.utils.imageUrl2WebP2
import kotlinx.android.synthetic.main.dialog_video_hall_play_end.*


class VideoHallPlayEndDialog : DialogFragment(), View.OnClickListener {

    companion object {
        const val TAG = "VideoHallPlayEndDialog"
        const val EXTRA_CHANNEL_ID = "CHANNEL_ID"
        const val EXTRA_CHANNEL_NAME = "CHANNEL_NAME"
        const val EXTRA_CHANNEL_ICON = "CHANNEL_ICON"
        var isShown = false

        fun show(activity: FragmentActivity, channelId: Int, channelName: String, channelIcon: String) {
            if (isShown) {
                return
            }
            isShown = true
            val fragment = VideoHallPlayEndDialog()
            val args = Bundle()
            args.putInt(EXTRA_CHANNEL_ID, channelId)
            args.putString(EXTRA_CHANNEL_NAME, channelName)
            args.putString(EXTRA_CHANNEL_ICON, channelIcon)
            fragment.arguments = args
            fragment.show(activity.supportFragmentManager, "VideoHallPlayEndDialog")
        }
    }

    private val mProgressDialog by lazy {
        return@lazy ProgressDialog.progressDialog(context!!, getString(R.string.string_loading))
    }

    private lateinit var mViewModel: VideoHallViewModel
    private var channelId: Int? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_video_hall_play_end, dialog.window?.findViewById(android.R.id.content) as ViewGroup, false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#99000000")))
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        ImmersionBar.with(this).init()
        ImmersionBar.with(this).navigationBarColor(R.color.transparent).init()
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.let {
            mViewModel = VideoHallViewModel(it.application)
            mViewModel.mChannelTopicInfoLiveData.observe(this, Observer {
                it?.let { topicInfo ->
                    if (topicInfo.hasSubscribe == true) {
                        tv_video_hall_play_end_subscript.text = getString(R.string.string_has_subscription)
                        tv_video_hall_play_end_subscript.isEnabled = false
                        tv_video_hall_play_end_subscript.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    } else {
                        tv_video_hall_play_end_subscript.isEnabled = true
                        tv_video_hall_play_end_subscript.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add, 0, 0, 0)
                    }
                    tv_video_hall_play_end_subscript_num.text = String.format(getString(R.string.string_subscript_count), topicInfo.subscribedCount)
                }

            })

            mViewModel.mChannelSubscribeLiveData.observe(this, Observer {
                mProgressDialog.dismiss()
                tv_video_hall_play_end_subscript.isEnabled = false
                tv_video_hall_play_end_subscript.text = getString(R.string.string_has_subscription)
                tv_video_hall_play_end_subscript.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            })

            val arguments = arguments ?: return
            channelId = arguments.getInt(EXTRA_CHANNEL_ID)
            val channelName = arguments.getString(EXTRA_CHANNEL_NAME)
            val channelIcon = arguments.getString(EXTRA_CHANNEL_ICON)
            mViewModel.getChannelTopicInfo(channelId.toString())
            tv_video_hall_play_end_title.text = channelName

            channelIcon?.let {
                val avatarUrl = imageUrl2WebP2(channelIcon, ScreenUtils.dip2px(context, 65f),
                        ScreenUtils.dip2px(context, 65f))
                iv_video_hall_play_end_image.setImageURI(avatarUrl)
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.apply {
            isFocusableInTouchMode = true
            requestFocus()
            setOnKeyListener(object : View.OnKeyListener {
                override fun onKey(view: View, keyCode: Int, keyEvent: KeyEvent): Boolean {
                    if (keyEvent.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                        finishDialog()
                        return true
                    }
                    return false
                }
            })
        }

        iv_video_hall_play_end_close.setOnClickListener(this)
        tv_video_hall_play_end_ok_btn.setOnClickListener(this)
        tv_video_hall_play_end_subscript.setOnClickListener(this)
    }

    fun finishDialog() {
        dismissAllowingStateLoss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isShown = false
        ImmersionBar.with(this).destroy()
    }

    override fun onClick(v: View) {
        if (v.id == R.id.iv_video_hall_play_end_close) {
            finishDialog()
        } else if (v.id == R.id.tv_video_hall_play_end_ok_btn) {
            requireContext().startActivity(HOME_PAGE)
            finishDialog()
        } else if (v.id == R.id.tv_video_hall_play_end_subscript) {
            val accountManager = v.context.applicationContext.daggerComponent().accountManager()
            if (!accountManager.hasAccount()) {
                LoginActivity.launch(v.context)
                return
            }
            channelId?.let {
                mProgressDialog.show()
                mViewModel.subscribe(it)
            }
        }
    }


}
