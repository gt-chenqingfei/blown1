package com.shuashuakan.android.modules.discovery.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.shuashuakan.android.modules.discovery.fragment.SubRankingListFragment

/**
 * Author:  liJie
 * Date:   2019/1/14
 * Email:  2607401801@qq.com
 */
class RankingListAdapter(
    fm :FragmentManager,
    private val categories: ArrayList<String>) : FragmentStatePagerAdapter(fm){
  override fun getItem(position: Int): Fragment {
    return SubRankingListFragment.create(categories[position])
  }

  override fun getCount(): Int {
    return categories.size
  }

  override fun getPageTitle(position: Int): CharSequence? {
    return categories[position]
  }
}