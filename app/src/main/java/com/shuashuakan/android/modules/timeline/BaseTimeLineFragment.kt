package com.shuashuakan.android.modules.timeline

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.spider.Spider
import com.shuashuakan.android.ui.base.FishFragment
import com.shuashuakan.android.modules.share.ShareHelper
import com.shuashuakan.android.utils.setViewMarginTop
import javax.inject.Inject

/**
 * TODO 待实现
 *
 * Author: ZhaiDongyang
 * Date: 2019/2/26
 */
abstract class BaseTimeLineFragment: FishFragment(), SwipeRefreshLayout.OnRefreshListener,
    BaseQuickAdapter.RequestLoadMoreListener{

  @Inject
  lateinit var apiService: ApiService
  @Inject
  lateinit var shareHelper: ShareHelper
  @Inject
  lateinit var spider: Spider

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
      View? = inflater.inflate(onLayout(), container, false)

  abstract fun onLayout(): Int
  abstract fun onTopView(): View

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    setViewMarginTop(requireActivity(), onTopView())
  }


  override fun onRefresh() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun onLoadMoreRequested() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}