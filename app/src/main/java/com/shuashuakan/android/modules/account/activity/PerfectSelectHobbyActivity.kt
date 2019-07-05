package com.shuashuakan.android.modules.account.activity

import android.os.Bundle
import com.gyf.barlibrary.ImmersionBar
import com.shuashuakan.android.ui.base.FishActivity
import com.shuashuakan.android.R
import com.shuashuakan.android.modules.account.fragment.PrefectSelectHobbyFragment

/**
 * 设置兴趣页面
 *
 * Author: ZhaiDongyang
 * Date: 2019/1/28
 */
class PerfectSelectHobbyActivity : FishActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ImmersionBar.with(this).init()
    setContentView(R.layout.activity_perfect_insterest)
    supportFragmentManager.beginTransaction().replace(R.id.home_container,
            PrefectSelectHobbyFragment()).commit()
  }


  override fun onDestroy() {
    super.onDestroy()
    ImmersionBar.with(this).destroy()
  }
}
