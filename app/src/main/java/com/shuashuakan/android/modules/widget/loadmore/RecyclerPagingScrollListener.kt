package com.shuashuakan.android.modules.widget.loadmore

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

abstract class RecyclerPagingScrollListener : RecyclerView.OnScrollListener() {

  private val visibleThreshold = 3
  var currentPage = 0
  private var previousTotalItemCount = 0
  var loadMore = true

  fun reset() {
    loadMore = true
    currentPage = 0
    previousTotalItemCount = 0
  }

  abstract fun onPaging(currentPage: Int)

  override fun onScrollStateChanged(recyclerView: RecyclerView?, i: Int) {}

  override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
//    if (dx == 0 && dy == 0) return
    val totalItemCount = recyclerView!!.adapter.itemCount
    if (recyclerView.layoutManager is LinearLayoutManager) {
      val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
      if (totalItemCount == 0) return
      if (linearLayoutManager.findLastVisibleItemPosition() + visibleThreshold >= totalItemCount - 1
          && totalItemCount != previousTotalItemCount && loadMore) {
        currentPage++
        onPaging(currentPage)
        previousTotalItemCount = totalItemCount
      }
    }
  }
}