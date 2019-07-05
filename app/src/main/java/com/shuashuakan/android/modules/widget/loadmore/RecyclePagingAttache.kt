package com.shuashuakan.android.modules.widget.loadmore

import android.support.v7.widget.RecyclerView

class RecyclePagingAttache(private val onListPagingListener: OnListPagingListener) {

  private lateinit var listener: RecyclerPagingScrollListener

  interface OnListPagingListener {
    fun onListPaging(currentPage: Int)
  }

  fun attach(view: RecyclerView): RecyclerPagingScrollListener {
    listener = object : RecyclerPagingScrollListener() {
      override fun onPaging(currentPage: Int) {
        onListPagingListener.onListPaging(currentPage)
      }
    }
    view.addOnScrollListener(listener)
    return listener
  }
}
