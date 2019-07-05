package com.shuashuakan.android.modules.topic.adapter

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.shuashuakan.android.R
import com.shuashuakan.android.modules.timeline.profile.ProfileTimeLineFragment
import com.shuashuakan.android.modules.topic.TopicDetailRecommendFragment

class TopicDetailViewPageAdapter(val context: Context, fm: FragmentManager, private val titles: List<String>, private val channelId: Long) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (titles[position]) {
            context.getString(R.string.string_recommend) -> TopicDetailRecommendFragment.create(channelId)
            context.getString(R.string.string_dynamic_label) -> ProfileTimeLineFragment.create(channelId)
            else -> {
                throw IllegalArgumentException("Unknown Fragment")
            }
        }
    }

    override fun getCount(): Int = titles.size

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }
}