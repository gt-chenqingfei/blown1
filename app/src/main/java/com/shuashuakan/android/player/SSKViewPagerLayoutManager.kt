package com.shuashuakan.android.player

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewConfiguration
import com.chad.library.adapter.base.loadmore.LoadMoreView
import timber.log.Timber

class SSKViewPagerLayoutManager(pager: RecyclerView, context: Context, @RecyclerView.Orientation orientation: Int) : LinearLayoutManager(context, orientation, false) {
    private val mSSKOnScrollListeners = ArrayList<SSKOnScrollListener>(4)

    private val pagerSnapHelper = PagerSnapHelper()

    private val mTouchSlop: Float

    private lateinit var mRecyclerView: RecyclerView

    var canScroll = true


    private val mScrollerListener = object : RecyclerView.OnScrollListener() {
        var isInitComplete = false
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            mSSKOnScrollListeners.forEach {
                it.onScrollStateChanged(this@SSKViewPagerLayoutManager, recyclerView, newState)
            }

        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (!isInitComplete) {
                isInitComplete = true
                mSSKOnScrollListeners.forEach {
                    it.onInitComplete(this@SSKViewPagerLayoutManager, recyclerView)
                }
            }

            mSSKOnScrollListeners.forEach {
                it.onScrolled(this@SSKViewPagerLayoutManager, recyclerView, dx, dy)
            }

        }
    }

    init {
        val config = ViewConfiguration.get(context)
        mTouchSlop = config.scaledTouchSlop.toFloat()
        pager.addOnScrollListener(mScrollerListener)
    }


    fun addSSKOnScrollListener(scrollListener: SSKOnScrollListener) {
        if (!mSSKOnScrollListeners.contains(scrollListener)) {
            mSSKOnScrollListeners.add(scrollListener)
        }
    }

    fun removeSSKOnScrollListener(scrollListener: SSKOnScrollListener) {
        if (mSSKOnScrollListeners.contains(scrollListener)) {
            mSSKOnScrollListeners.remove(scrollListener)
        }
    }


    override fun onAttachedToWindow(view: RecyclerView) {
        super.onAttachedToWindow(view)
        mRecyclerView = view
        pagerSnapHelper.attachToRecyclerView(view)
    }

    override fun onDetachedFromWindow(view: RecyclerView, recycler: RecyclerView.Recycler?) {
        super.onDetachedFromWindow(view, recycler)
        mScrollerListener.isInitComplete = false
    }

    override fun canScrollVertically(): Boolean {
        return canScroll && orientation == VERTICAL
    }

    fun findCenterView(): View? {
        return pagerSnapHelper.findSnapView(this)
//        return mRecyclerView.findChildViewUnder(mRecyclerView.width * 0.5f, mRecyclerView.height * 0.5f)
    }

    /**
     * 判断view在RecyclerView上是否滑动出屏幕
     */
    fun isViewInWindow(v: View?): Boolean {
        if (v == null || v is LoadMoreView) {
            return false
        }
        val firstView = mRecyclerView.findChildViewUnder(mTouchSlop, mTouchSlop)
        val lastView = mRecyclerView.findChildViewUnder(mRecyclerView.width - mTouchSlop, mRecyclerView.height - mTouchSlop)
        return firstView == v || lastView == v
    }

    fun smoothScrollToPosition(position: Int) {
        mRecyclerView.smoothScrollToPosition(position)
    }

    fun getRecycleView() =
            mRecyclerView
}

interface SSKOnScrollListener {
    fun onInitComplete(layoutManager: SSKViewPagerLayoutManager, recyclerView: RecyclerView)
    fun onScrollStateChanged(layoutManager: SSKViewPagerLayoutManager, recyclerView: RecyclerView, newState: Int)
    fun onScrolled(layoutManager: SSKViewPagerLayoutManager, recyclerView: RecyclerView, dx: Int, dy: Int)

}