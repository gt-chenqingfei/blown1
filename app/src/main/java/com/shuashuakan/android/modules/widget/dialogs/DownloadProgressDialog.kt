package com.shuashuakan.android.modules.widget.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.shuashuakan.android.R


class DownloadProgressDialog(context: Context, val cancelable: Boolean = true) : Dialog(context) {
    private lateinit var circleProgressBar: CustomCircleProgressBar
    private lateinit var progressBar: ProgressBar
    private lateinit var progressContent: TextView

    companion object {
        @SuppressLint("InflateParams")
        fun progressDialog(context: Context, cancelable: Boolean = true): DownloadProgressDialog {
            val dialog = DownloadProgressDialog(context, cancelable)

            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.layout_progress_dialog_percent, null)
        circleProgressBar = view.findViewById(R.id.progress_percent)
        progressBar = view.findViewById(R.id.progressbar)
        progressContent = view.findViewById(R.id.progress_hint)
        this.setContentView(view)
        this.setCancelable(cancelable)
        this.setCanceledOnTouchOutside(cancelable)
        this.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    fun setCircleProgressBarPercent(percent: Int) {
        circleProgressBar.setProgress(percent)
    }

    fun setProgressBarVisibility(content: String) {
        progressBar.visibility = View.VISIBLE
        circleProgressBar.visibility = View.GONE
        progressContent.text = content
    }

    fun setCircleProgressBarPercentVisibility(content: String) {
        progressBar.visibility = View.GONE
        circleProgressBar.visibility = View.VISIBLE
        progressContent.text = content
    }
}