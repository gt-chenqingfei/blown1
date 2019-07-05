package com.shuashuakan.android.modules.profile.adapter

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.shuashuakan.android.R
import com.shuashuakan.android.modules.profile.fragment.SubFeedListFragment
import com.shuashuakan.android.modules.timeline.profile.ProfileTimeLineFragment

class ProfileViewPageAdapter(val context: Context,fm: FragmentManager, private val titles: List<String>, private val userId: String, private val isMine: Boolean = false) : FragmentStatePagerAdapter(fm) {

  override fun getItem(position: Int): Fragment {

    return when (titles[position]) {
      context.getString(R.string.string_up_video_label) -> SubFeedListFragment.create(userId, isMine)
      context.getString(R.string.string_dynamic_label) -> ProfileTimeLineFragment.create(userId, isMine)
//      "发布的视频" -> SubUploadFeedListFragment.create(userId,isMine)
      else -> {
          SubFeedListFragment.create(userId, isMine)
      }
    }
  }

  override fun getCount(): Int = titles.size

  override fun getPageTitle(position: Int): CharSequence? {
    return titles[position]
  }
}