package com.shuashuakan.android.modules.partition.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.shuashuakan.android.R
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.event.LoginSuccessEvent
import com.shuashuakan.android.modules.partition.PartitionConstant
import com.shuashuakan.android.modules.partition.adapter.CategoryContentAdapter
import com.shuashuakan.android.modules.partition.vm.CategoryContentViewModel
import com.shuashuakan.android.modules.widget.loadmoreview.SskLoadMoreView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_category_content.*

/**
 * @author hushiguang
 * @since 2019-06-17.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
class CategoryContentFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    companion object {
        fun createFragment(partitionId: Int): CategoryContentFragment {
            val partitionContentFragment = CategoryContentFragment()
            val bundle = Bundle()
            bundle.putInt(PartitionConstant.PARTITION_ID, partitionId)
            partitionContentFragment.arguments = bundle
            return partitionContentFragment
        }
    }

    private var categoryId: Int = 0
    private lateinit var categoryContentViewModel: CategoryContentViewModel
    private lateinit var errorView: View
    private lateinit var mCategoryItemAdapter: CategoryContentAdapter

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_category_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        categoryContentViewModel = ViewModelProviders.of(this).get(CategoryContentViewModel::class.java)
//        categoryContentViewModel.attachContext(requireContext())
        initAdapter()
        initErrorView()
        initListener()
        initObservable()
        formatIntent()

        observerLoginStatus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
    }

    private fun observerLoginStatus() {
        RxBus.get().toFlowable().subscribe { event ->
            when (event) {
                is LoginSuccessEvent -> {
                    mCategoryItemAdapter.loginAction()
                }
            }
        }.addTo(compositeDisposable)
    }

    private fun initAdapter() {
        mCategoryItemAdapter = CategoryContentAdapter(categoryId, categoryContentViewModel.apiService, arrayListOf())
        mPartitionContentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        mCategoryItemAdapter.bindToRecyclerView(mPartitionContentRecyclerView)
        mCategoryItemAdapter.setLoadMoreView(SskLoadMoreView())
        mCategoryItemAdapter.setOnLoadMoreListener({
        }, mPartitionContentRecyclerView)

        mCategoryItemAdapter.onItemChildClickListener =
                BaseQuickAdapter.OnItemChildClickListener { adapter,
                                                            view, position ->
                    when (view?.id) {
                        R.id.mItemPartitionHotItemView -> {
                            categoryContentViewModel.onHotLeaderBoardClick(categoryId.toString(), requireContext(), position)
                        }
                    }
                }
    }

    private fun formatIntent() {
        arguments?.let {
            categoryId = it.getInt(PartitionConstant.PARTITION_ID)
            mPartitionContentRefreshLayout.isRefreshing = true
            getPartitionData()
        }
    }


    private fun initObservable() {
        categoryContentViewModel.partitionLiveData.observe(this, Observer<MutableList<MultiItemEntity>> {
            it?.let { partitionData ->
                mCategoryItemAdapter.categoryId = categoryId
                mPartitionContentRefreshLayout.isRefreshing = false
                if (partitionData.isEmpty()) {
                    mCategoryItemAdapter.emptyView = errorView
                } else {
                    mCategoryItemAdapter.setNewData(partitionData)
                    mCategoryItemAdapter.loadMoreEnd(false)
                }
            }
        })
//        categoryContentViewModel.mLoginEventLiveData.observe(this, Observer<Boolean> {
//            it?.let {
//                mCategoryItemAdapter.loginAction()
//            }
//        })
    }

    private fun initErrorView() {
        errorView = layoutInflater.inflate(R.layout.view_error, mPartitionContentRecyclerView.parent as ViewGroup, false)
        errorView.setOnClickListener { onRefresh() }
    }

    private fun initListener() {
        mPartitionContentRefreshLayout.setOnRefreshListener(this)
    }

    private fun getPartitionData() {
        categoryContentViewModel.getPartitionDetail(categoryId)
    }

    override fun onRefresh() {
        getPartitionData()
    }
}