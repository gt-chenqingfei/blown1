package com.shuashuakan.android.modules.profile

import android.os.Bundle
import com.gyf.barlibrary.ImmersionBar
import com.shuashuakan.android.R
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.modules.profile.fragment.ProfileFragment
import com.shuashuakan.android.ui.base.FishActivity
import com.shuashuakan.android.utils.getSpider
import com.shuashuakan.android.utils.getUserId
import me.twocities.linker.annotations.Link
import me.twocities.linker.annotations.LinkQuery
import timber.log.Timber

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/10/19
 * Description:
 */
@Link("ssr://user/profile")
class UserProfileActivity : FishActivity() {

  @LinkQuery("id")
  lateinit var userId: String


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    bindLinkParams()
    ImmersionBar.with(this).init()
    setContentView(R.layout.activity_user_profile)
    val hasUnread = intent.getBooleanExtra(ProfileFragment.EXTRA_HAS_UNREAD,false)
    supportFragmentManager.beginTransaction().replace(R.id.home_container, ProfileFragment.create(userId, true,hasUnread)).commit()
    this.getSpider().pageTracer().reportPageCreated(this)

    this.getSpider().manuallyEvent(SpiderEventNames.PERSON_PAGE)
        .put("TargetUserID", userId)
        .put("userID", this.getUserId())
        .put("source", intent.getStringExtra("source") ?: "")
        .put("feedID", intent.getStringExtra("feedId") ?: "")
        .track()
  }

  override fun onResume() {
    super.onResume()
    this.getSpider().pageTracer().reportPageShown(this, "ssr://user/profile?id=" + userId, "")
  }

  override fun onDestroy() {
    super.onDestroy()
    ImmersionBar.with(this).destroy()
  }
}
