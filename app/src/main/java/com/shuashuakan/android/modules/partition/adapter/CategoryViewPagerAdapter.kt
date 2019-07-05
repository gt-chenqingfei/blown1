package com.shuashuakan.android.modules.partition.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.shuashuakan.android.data.api.model.partition.PartitionData

/**
 * @author hushiguang
 * @since 2019-06-17.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */

class CategoryViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
    private var fragments = arrayListOf<Fragment>()
    private val mTitles = ArrayList<PartitionData>()

    fun initFragments(fragmentPages: List<Fragment>, titles: List<PartitionData>) {
        fragments.clear()
        mTitles.clear()
        mTitles.addAll(titles)
        fragments.addAll(fragmentPages)
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mTitles[position].name
    }
}