package com.shuashuakan.android.modules.discovery.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.shuashuakan.android.R;

/**
 * @author hushiguang
 * @since 2019-06-19.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
public class HorizontalLinePageIndicator extends FrameLayout implements PageGridRecyclerView.PageIndicator {

    private View offsetView;
    private float parentWidth;
    private float remainingWidth;

    public HorizontalLinePageIndicator(Context context) {
        this(context, null);
    }

    public HorizontalLinePageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackground(getResources().getDrawable(R.drawable.bg_indicator_category));
        offsetView = LayoutInflater.from(getContext()).inflate(R.layout.widget_category_indicator, null);
        LayoutParams layoutParams = new LayoutParams(dip2px(getContext(), 20f), LayoutParams.MATCH_PARENT);
        addView(offsetView, layoutParams);
    }


    @Override
    public void InitIndicatorItems(int itemsNumber) {
        if (itemsNumber == 1) {
            removeAllViews();
            getLayoutParams().width = 0;
            return;
        }
        parentWidth = dip2px(getContext(), 20f) * itemsNumber;
        getLayoutParams().width = (int) parentWidth;
        remainingWidth = (parentWidth - offsetView.getLayoutParams().width);
    }

    @Override
    public void onPageByOffset(double offset) {
        offsetView.setTranslationX((float) (remainingWidth * offset));
    }

    public void resetPosition() {
        offsetView.animate().translationXBy(-offsetView.getTranslationX()).start();
    }

    public int dip2px(Context context, double dpValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5);
    }

}
