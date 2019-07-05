package com.shuashuakan.android.modules.viphome

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.view.*
import com.gyf.barlibrary.ImmersionBar
import com.shuashuakan.android.R
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.modules.FeedTransportManager
import com.shuashuakan.android.utils.getSpider
import kotlinx.android.synthetic.main.dialog_video_hall_explore.*

@SuppressLint("ValidFragment")
class VideoHallExploreDialog(val listener: OnVideoHallExploreListener?) : DialogFragment(), View.OnClickListener {

    interface OnVideoHallExploreListener {
        fun onOnVideoHallExplore()
    }

    companion object {
        const val TAG = "VideoHallExploreDialog"
        fun show(activity: FragmentActivity, listener: OnVideoHallExploreListener): VideoHallExploreDialog {
            val fragment = VideoHallExploreDialog(listener)
            val args = Bundle()
            fragment.arguments = args
            fragment.show(activity.supportFragmentManager, "VideoHallExploreDialog")
            return fragment
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_video_hall_explore, dialog.window?.findViewById(android.R.id.content) as ViewGroup, false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#99000000")))
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        ImmersionBar.with(this).init()
        ImmersionBar.with(this).navigationBarColor(R.color.transparent).init()
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (FeedTransportManager.message == null) {
            finishDialog()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iv_video_hall_explore_close.setOnClickListener(this)
        tv_video_hall_explore.setOnClickListener(this)
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

        fillData()
    }

    override fun onDestroy() {
        super.onDestroy()
        FeedTransportManager.message = null
        ImmersionBar.with(this).destroy()
    }

    private fun fillData() {
        tv_video_hall_explore_content.text = FeedTransportManager.message?.content
        tv_video_hall_explore_author.text = FeedTransportManager.message?.author
    }

    fun finishDialog() {
        dismissAllowingStateLoss()
        listener?.apply {
            onOnVideoHallExplore()
        }
    }

    override fun onClick(v: View) {
        when (v) {
            iv_video_hall_explore_close -> {
                finishDialog()

            }
            tv_video_hall_explore -> {
                finishDialog()
                context?.getSpider()?.manuallyEvent(SpiderEventNames.VipRoom.ROOM_EXPLORE_CLICK)?.track()
            }
        }


    }

}
