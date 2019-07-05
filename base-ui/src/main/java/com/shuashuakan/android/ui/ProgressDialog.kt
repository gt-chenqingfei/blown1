package com.shuashuakan.android.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.TextView
import com.shuashuakan.android.base.ui.R

class ProgressDialog {
  companion object {
    fun progressDialog(context: Context, message: String, cancelable: Boolean = true): Dialog {
      val dialog = Dialog(context)
      val view = LayoutInflater.from(context).inflate(R.layout.layout_progress_dialog, null)
      val textView = view.findViewById<TextView>(R.id.loadingTextView)
      textView.text = message
      dialog.setContentView(view)
      dialog.setCancelable(cancelable)
      dialog.window.apply {
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
      }
      return dialog
    }

  }
}