//package com.shuashuakan.android.ui.widget.customview;
//
//import android.support.v4.view.ViewPager;
//import android.support.v7.widget.CardView;
//import android.view.View;
//
//
//public class ShadowTransformer implements ViewPager.OnPageChangeListener, ViewPager.PageTransformer {
//
//    private ViewPager mViewPager;
//    private CardAdapter mAdapter;
//    private float mLastOffset;
//    private boolean mScalingEnabled;
//
//    public ShadowTransformer(ViewPager viewPager, CardAdapter adapter) {
//        mViewPager = viewPager;
//        viewPager.addOnPageChangeListener(this);
//        mAdapter = adapter;
//    }
//
//    @Override
//    public void transformPage(View page, float position) {
//
//    }
//
//    @Override
//    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//        int realCurrentPosition;
//        int nextPosition;
//        float baseElevation = mAdapter.getBaseElevation();
//        float realOffset;
//        boolean goingLeft = mLastOffset > positionOffset;
//
//        // If we're going backwards, onPageScrolled receives the last position
//        // instead of the current one
//        if (goingLeft) {
//            realCurrentPosition = position + 1;
//            nextPosition = position;
//            realOffset = 1 - positionOffset;
//        } else {
//            nextPosition = position + 1;
//            realCurrentPosition = position;
//            realOffset = positionOffset;
//        }
//
//        // Avoid crash on overscroll
//        if (nextPosition > mAdapter.getCount() - 1
//                || realCurrentPosition > mAdapter.getCount() - 1) {
//            return;
//        }
//
//        CardView currentCard = mAdapter.getCardViewAt(realCurrentPosition);
//
//        // This might be null if a fragment is being used
//        // and the views weren't created yet
//
//        mLastOffset = positionOffset;
//    }
//
//    @Override
//    public void onPageSelected(int position) {
//
//    }
//
//    @Override
//    public void onPageScrollStateChanged(int state) {
//
//    }
//}
