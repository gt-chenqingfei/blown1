package com.shuashuakan.android.modules.partition

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import com.shuashuakan.android.R
import com.shuashuakan.android.data.api.model.partition.PartitionData
import com.shuashuakan.android.modules.partition.adapter.CategoryMagicIndicatorAdapter
import com.shuashuakan.android.modules.partition.adapter.CategoryViewPagerAdapter
import com.shuashuakan.android.modules.partition.fragment.CategoryContentFragment
import com.shuashuakan.android.modules.partition.vm.CategoryIndexViewModel
import com.shuashuakan.android.ui.base.FishActivity
import com.shuashuakan.android.utils.categoryDetailExposureEvent
import com.shuashuakan.android.utils.getSpider
import kotlinx.android.synthetic.main.activity_partition_index.*
import me.twocities.linker.annotations.Link
import me.twocities.linker.annotations.LinkQuery
import net.lucode.hackware.magicindicator.ViewPagerHelper

/**
 * @author hushiguang
 * @since 2019-06-17.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
@Link("ssr://category/detail")
class CategoryIndexActivity : FishActivity() {

    private lateinit var mCategoryAdapter: CategoryMagicIndicatorAdapter
    private lateinit var mCategoryViewPagerAdapter: CategoryViewPagerAdapter
    private lateinit var categoryIndexViewModel: CategoryIndexViewModel

    @LinkQuery("id")
    @JvmField
    var categoryId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partition_index)
        bindLinkParams()

        spider.pageTracer().reportPageCreated(this)
        spider.pageTracer().reportPageShown(this, "ssr://category/detail", "")

        categoryIndexViewModel = ViewModelProviders.of(this).get(CategoryIndexViewModel::class.java)
        initListener()
        setupMagicIndicator()
        initObservable()
        categoryIndexViewModel.getPartitionTab()
        categoryId?.let {
            getSpider().categoryDetailExposureEvent(it)
        }

    }

    private fun initListener() {
        mPartitionBack.setOnClickListener { finish() }
        mErrorView.setOnClickListener { categoryIndexViewModel.getPartitionTab() }
    }

    private fun setupMagicIndicator() {
        mCategoryViewPagerAdapter = CategoryViewPagerAdapter(supportFragmentManager)
        mPartitionViewPager.adapter = mCategoryViewPagerAdapter
        mCategoryAdapter = CategoryMagicIndicatorAdapter(this, mPartitionViewPager)
        mCategoryAdapter.attach(mPartitionMagicIndicator)
        ViewPagerHelper.bind(mPartitionMagicIndicator, mPartitionViewPager)
        mPartitionViewPager.setHasScrollAnim(false)
        mPartitionViewPager.setScroll(true)
    }

    private fun initObservable() {
        categoryIndexViewModel.partitionListLiveData.observe(this, Observer<List<PartitionData>> {
            it?.let { list: List<PartitionData> ->
                if (list.isEmpty()) {
                    mErrorView.visibility = View.VISIBLE
                    return@Observer
                }
                mErrorView.visibility = View.GONE
                val fragment = arrayListOf<CategoryContentFragment>()
                for (partition in list) {
                    fragment.add(CategoryContentFragment.createFragment(partition.id!!))
                }
                mCategoryViewPagerAdapter.initFragments(fragment, list)
                mCategoryAdapter.notifyDataSetChanged()

                for (position in list.indices) {
                    if (categoryId == list[position].id.toString()) {
                        mPartitionViewPager.currentItem = position
                    }
                }
            }
        })
    }
}