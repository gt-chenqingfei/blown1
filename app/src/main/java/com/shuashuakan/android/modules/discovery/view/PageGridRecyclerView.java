package com.shuashuakan.android.modules.discovery.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.shuashuakan.android.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


/**
 * @author hushiguang
 * @since .
 * Copyright © 2019 SSk Technology Co.,Ltd. All rights reserved.
 */
public class PageGridRecyclerView extends RecyclerView {
    private int mRows = 0;
    private int mColums = 0;
    private int mPadding = 0;
    private int mPageSize = 0;
    private int mOnePageSize = 0;
    LayoutManager layoutManager;

    public PageGridRecyclerView(Context context) {
        this(context, null);
    }

    public PageGridRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageGridRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //根据行数和列数判断是否
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PageGridRecyclerView);
        mRows = array.getInteger(R.styleable.PageGridRecyclerView_PagingRows, 0);
        mColums = array.getInteger(R.styleable.PageGridRecyclerView_PagingColums, 0);
        mPadding = array.getInteger(R.styleable.PageGridRecyclerView_PagingPadding, 0);
        if (mRows < 0 || mColums < 0) {
            throw new RuntimeException("行数或列数不能为负数");
        }
        if (mRows == 0 && mColums == 0) {
            throw new RuntimeException("行数和列数不能都为0");
        }
        layoutManager = new StaggeredGridLayoutManager(mRows, HORIZONTAL);
        addOnScrollListener(new PagingScrollListener());
        array.recycle();
        setLayoutManager(layoutManager);
    }

    public void changeRows(int rows) {
        layoutManager = new StaggeredGridLayoutManager(rows, HORIZONTAL);
        setLayoutManager(layoutManager);
    }

    @Override
    public final void setAdapter(Adapter adapter) {
        formatRows((BaseQuickAdapter) adapter);
        super.setAdapter(adapter);
        if (pageIndicator != null && pageIndicaotrNeedInit) {
            pageIndicator.InitIndicatorItems(mPageSize);
            pageIndicator.onPageByOffset(0);
            pageIndicaotrNeedInit = false;
            scrollX = 0f;
        }
    }

    @NotNull
    private void formatRows(BaseQuickAdapter adapter) {
        BaseQuickAdapter pagingAdapter = adapter;
        List data = pagingAdapter.getData();
        if (data.size() <= mColums) {
            changeRows(1);
        } else {
            changeRows(2);
        }

        mOnePageSize = mRows * mColums;
        if (data.size() > 0 && data.size() <= mOnePageSize) {
            mOnePageSize = 1;
            mPageSize = 1;
        } else {
            mPageSize = data.size() / mOnePageSize + (data.size() % mOnePageSize == 0 ? 0 : 1);
        }

        //对数据进行重排序
        if (mPageSize <= 1) {
            List formatData = new ArrayList();
            if (data.size() % mOnePageSize != 0) {
                mPageSize++;
            }
            for (int p = 0; p < mPageSize; p++) {
                for (int c = 0; c < mColums; c++) {
                    for (int r = 0; r < mRows; r++) {
                        int index = c + r * mColums + p * mOnePageSize;
                        if (index > data.size() - 1) {

                        } else {
                            formatData.add(data.get(index));
                        }
                    }
                }
            }
            data.clear();
            data.addAll(formatData);
        }
    }

    private PageIndicator pageIndicator;
    //需要初始化pageIndicator
    private boolean pageIndicaotrNeedInit = false;

    public void setPageIndicator(PageIndicator pageIndicator) {
        this.pageIndicator = pageIndicator;
        pageIndicaotrNeedInit = true;
        if (getAdapter() != null) {
            formatRows((BaseQuickAdapter) getAdapter());
            pageIndicator.InitIndicatorItems(mPageSize);
            pageIndicator.onPageByOffset(0);
            pageIndicaotrNeedInit = false;
            scrollX = 0f;
        }
    }

    //分页指示器
    public interface PageIndicator {

        void InitIndicatorItems(int itemsNumber);

        void onPageByOffset(double offset);
    }


    public int getPageSize() {
        return mPageSize;
    }

    public interface OnItemClickListener {
        void onItemClick(PageGridRecyclerView pageGridRecyclerView, int position);
    }


    float scrollX = 0f;
    int recyclerViewWidth = 0;

    public class PagingScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            scrollX += dx;
            int screenWidth = (getContext().getResources().getDisplayMetrics().widthPixels - dip2px(getContext(), mPadding));
            if (recyclerViewWidth == 0) {
                int itemCount = recyclerView.getAdapter().getItemCount() / 2 + (recyclerView.getAdapter().getItemCount() % 2 == 0 ? 0 : 1);
                recyclerViewWidth = screenWidth / mColums * itemCount;
            }
            float p = scrollX / (recyclerViewWidth - screenWidth);
            if (pageIndicator != null) {
                pageIndicator.onPageByOffset(Math.abs(p));
            }
        }
    }

    int dip2px(Context context, double dpValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5);
    }
}
