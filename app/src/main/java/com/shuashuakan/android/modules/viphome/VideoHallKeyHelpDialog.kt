package com.shuashuakan.android.modules.viphome

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.view.*
import com.gyf.barlibrary.ImmersionBar
import com.shuashuakan.android.R
import kotlinx.android.synthetic.main.dialog_video_hall_key_help.*


class VideoHallKeyHelpDialog : DialogFragment() {

    companion object {
        const val TAG = "VideoHallPlayEndDialog"
        fun show(activity: FragmentActivity): VideoHallKeyHelpDialog {
            val fragment = VideoHallKeyHelpDialog()
            val args = Bundle()
            fragment.arguments = args
            fragment.show(activity.supportFragmentManager, "VideoHallPlayEndDialog")
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_video_hall_key_help, dialog.window?.findViewById(android.R.id.content) as ViewGroup, false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#99000000")))
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        ImmersionBar.with(this).init()
        ImmersionBar.with(this).navigationBarColor(R.color.transparent).init()
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        ImmersionBar.with(this).destroy()
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
        iv_dialog_video_key_help_close.setOnClickListener {
            finishDialog()
        }
    }

    fun finishDialog() {
        dismissAllowingStateLoss()
    }


}
