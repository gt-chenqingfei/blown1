package com.shuashuakan.android.modules.discovery

import android.os.Bundle
import com.gyf.barlibrary.ImmersionBar
import com.shuashuakan.android.R
import com.shuashuakan.android.ui.base.FishActivity
import com.shuashuakan.android.modules.discovery.fragment.DiscoveryFragment

/**
 * 发现页
 *
 * Author: ZhaiDongyang
 * Date: 2019/3/7
 */
//@Link("ssr://explore")
class DiscoveryActivity : FishActivity() {

//  @LinkQuery("id")
//  lateinit var userId: String


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
//    bindLinkParams()
    ImmersionBar.with(this).init()
    setContentView(R.layout.activity_explore)
    supportFragmentManager.beginTransaction().replace(R.id.home_container, DiscoveryFragment.create()).commit()
  }

  override fun onDestroy() {
    super.onDestroy()
    ImmersionBar.with(this).destroy()
  }
}
