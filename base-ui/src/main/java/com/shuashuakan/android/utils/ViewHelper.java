package com.shuashuakan.android.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.LinearLayout;

import java.lang.reflect.Field;

/**
 * Created by twocity on 14-12-18.
 */
public class ViewHelper {
    private static final long DEFAULT_DURATION = 300;

    private ViewHelper() {
    }

    public static void animateShow(final View view) {
        animateShow(view, DEFAULT_DURATION);
    }

    public static void animateShow(final View view, final long duration) {
        if (view == null) return;
        if (view.getVisibility() == View.VISIBLE) return;
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f);
        animator.setDuration(duration).addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                view.setVisibility(View.VISIBLE);
            }
        });
        animator.start();
    }

    public static void dismiss(final View view) {
        if (view == null) return;
        view.setVisibility(View.GONE);
    }

    public static void showViews(View... views) {
        for (View view : views) {
            view.setVisibility(View.VISIBLE);
        }
    }

    public static void goneViews(View... views) {
        for (View view : views)
            view.setVisibility(View.GONE);
    }

    public static void dismissViews(View... views) {
        for (View view : views)
            view.setVisibility(View.INVISIBLE);
    }

    public static int getRelativeTop(View view) {
        if (view.getParent() == view.getRootView()) {
            return view.getTop();
        } else {
            return view.getTop() + getRelativeTop((View) view.getParent());
        }
    }

    public static int getRelativeRight(View view) {
        if (view.getParent() == view.getRootView()) {
            return view.getRight();
        } else {
            return view.getRight() + getRelativeRight((View) view.getParent());
        }
    }

    /**
     * 切换视图渐变效果
     *
     * @param toShow  要显示的View
     * @param toGones 要隐藏的View
     */
    public static void crossfade(final View toShow, View... toGones) {
        // hide
        for (View view : toGones) {
            if (view instanceof SwipeRefreshLayout && ((SwipeRefreshLayout) view).isRefreshing()) {
                ((SwipeRefreshLayout) view).setRefreshing(false);
            }
            view.setVisibility(View.GONE);
        }
        // show
        if (toShow == null) return;
        if (toShow instanceof ContentLoadingProgressBar) {
            toShow.setVisibility(View.VISIBLE);
        } else if (toShow instanceof SwipeRefreshLayout && toShow.getVisibility() == View.VISIBLE) {
            ((SwipeRefreshLayout) toShow).setRefreshing(false);
        } else if (toShow.getVisibility() != View.VISIBLE) {
            toShow.setVisibility(View.VISIBLE);
            toShow.setAlpha(0f);
            toShow.animate().alpha(1f).setDuration(DEFAULT_DURATION);
        }
    }

    public static int getColorWithAlpha(float alpha, int baseColor) {
        int a = Math.min(255, Math.max(0, (int) (alpha * 255))) << 24;
        int rgb = 0x00ffffff & baseColor;
        return a + rgb;
    }

    public static void setUpIndicatorWidth(TabLayout tabLayout, int marginStart, int marginEnd) {
        Class<?> tabLayoutClass = tabLayout.getClass();
        Field tabStrip = null;
        try {
            tabStrip = tabLayoutClass.getDeclaredField("mTabStrip");
            tabStrip.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        LinearLayout layout = null;
        try {
            if (tabStrip != null) {
                layout = (LinearLayout) tabStrip.get(tabLayout);
            }
            if (layout != null) {
                for (int i = 0; i < layout.getChildCount(); i++) {
                    View child = layout.getChildAt(i);
                    child.setPadding(0, 0, 0, 0);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
                    params.setMarginStart(marginStart);
                    params.setMarginEnd(marginEnd);
                    child.setLayoutParams(params);
                    child.invalidate();
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
