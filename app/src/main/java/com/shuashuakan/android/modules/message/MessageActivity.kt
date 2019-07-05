package com.shuashuakan.android.modules.message

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.gyf.barlibrary.ImmersionBar
import com.shuashuakan.android.R
import com.shuashuakan.android.modules.message.fragment.MessageFragment

class MessageActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_message)
    ImmersionBar.with(this).init()
    supportFragmentManager.beginTransaction().replace(R.id.activity_message_content, MessageFragment()).commit()
  }

  override fun onDestroy() {
    super.onDestroy()
    ImmersionBar.with(this).destroy()
  }
}
