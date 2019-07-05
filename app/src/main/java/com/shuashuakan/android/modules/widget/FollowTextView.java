package com.shuashuakan.android.modules.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.support.annotation.Keep;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.luck.picture.lib.tools.ScreenUtils;
import com.shuashuakan.android.R;

import static com.luck.picture.lib.tools.ScreenUtils.dip2px;


/**
 * @author hushiguang
 * @since 2019-06-11.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
public class FollowTextView extends AppCompatTextView {

    private float mRightMarkPathLength;
    private boolean hasFollow = false;
    private Path mRightMarkPath = new Path();
    private Paint mRightMarkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mAddMarkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private ObjectAnimator mRightMarkAnimator;
    private ValueAnimator mWidthMarkAnimator;
    private int markDefaultColor = 0xff111217;
    private int width, height;
    // 是否使用自己绘制的加号
    private boolean isDrawAddOperator = false;
    private boolean isShowDrawerAnimation = false;
    private boolean isOverCircle = false;
    private ViewStatus viewStatus = ViewStatus.GONE;

    enum ViewStatus {
        GONE,
        VISIBLE,
        INVISIBLE
    }


    public FollowTextView(Context context) {
        this(context, null);
    }

    public FollowTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FollowTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        formatAttr(context, attrs);
        init();
    }

    private void formatAttr(Context context, AttributeSet attributeSet) {
        TypedArray ta = context.obtainStyledAttributes(attributeSet, R.styleable.FollowTextView);
        markDefaultColor = ta.getColor(R.styleable.FollowTextView_markColor, markDefaultColor);
        isDrawAddOperator = ta.getBoolean(R.styleable.FollowTextView_needDrawAddOperator, false);
        isShowDrawerAnimation = ta.getBoolean(R.styleable.FollowTextView_showDrawerAnimation, false);
        isOverCircle = ta.getBoolean(R.styleable.FollowTextView_overCircle, false);
        ta.recycle();
    }


    private void init() {
        mAddMarkPaint.setStrokeWidth(dip2px(getContext(), .5f));
        mAddMarkPaint.setColor(markDefaultColor);
        mRightMarkPaint.setStyle(Paint.Style.FILL);
        mRightMarkPaint.setStrokeWidth(dip2px(getContext(), 2));
        mRightMarkPaint.setStrokeCap(Paint.Cap.ROUND);
        mRightMarkPaint.setStyle(Paint.Style.STROKE);
        mRightMarkPaint.setColor(markDefaultColor);
        initMarkAnim();
    }

    private void initMarkAnim() {
        mRightMarkAnimator = ObjectAnimator.ofFloat(this, "phase", 0.0f, 1.0f);
        mRightMarkAnimator.setDuration(500);
        mRightMarkAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // 是否有收起动画
                if (isShowDrawerAnimation) {
                    initDrawerAnim();
                    mWidthMarkAnimator.start();
                }
                setViewStatus();

            }
        });
    }


    private void initDrawerAnim() {
        mWidthMarkAnimator = ValueAnimator.ofInt(ScreenUtils.dip2px(getContext(), 32f),
                ScreenUtils.dip2px(getContext(), 6f));
        mWidthMarkAnimator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            System.out.println("animatedValue " + animatedValue);
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            if (layoutParams instanceof RelativeLayout.LayoutParams) {
                ((RelativeLayout.LayoutParams) layoutParams).leftMargin = animatedValue;
            }
            setLayoutParams(layoutParams);
        });
        mWidthMarkAnimator.setDuration(500);
    }

    public void followSuccessGone() {
        viewStatus = ViewStatus.GONE;
        startRightAnimation();
    }

    public void followSuccessInInvisible() {
        viewStatus = ViewStatus.INVISIBLE;
        startRightAnimation();
    }

    public void followSuccessVisible() {
        viewStatus = ViewStatus.VISIBLE;
        startRightAnimation();
    }

    private void startRightAnimation() {
        hasFollow = true;
        mRightMarkAnimator.start();
    }

    public void reset() {
        hasFollow = false;
        viewStatus = ViewStatus.GONE;
        if (isShowDrawerAnimation) {
            if (getLayoutParams() instanceof RelativeLayout.LayoutParams) {
                ((RelativeLayout.LayoutParams) getLayoutParams()).leftMargin = ScreenUtils.dip2px(getContext(), 32f);
            }
        }
        invalidate();
    }


    private void setViewStatus() {
        switch (viewStatus) {
            case GONE:
                setVisibility(View.GONE);
                break;
            case INVISIBLE:
                setVisibility(View.INVISIBLE);
                break;
            case VISIBLE:
                setVisibility(View.VISIBLE);
                break;
            default:
                setVisibility(View.GONE);
                break;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mRightMarkAnimator.cancel();
        if (mWidthMarkAnimator != null) {
            mWidthMarkAnimator.cancel();
        }
        if (!isShowDrawerAnimation) {
            hasFollow = false;
        }
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        mRightMarkPath.reset();
        if (isOverCircle) {
            drawCircleRightMarkPath();
        } else {
            drawRectRightMarkPath(w, h);
        }
        // 重新关联Path
        PathMeasure pathMeasure = new PathMeasure(mRightMarkPath, false);
        mRightMarkPathLength = pathMeasure.getLength();
    }


    // 在长方形的对勾路径
    private void drawRectRightMarkPath(int w, int h) {
        mRightMarkPath.moveTo(0.357f * w, 0.5f * h);
        mRightMarkPath.lineTo(0.442f * w, 0.675f * h);
        mRightMarkPath.lineTo(0.642f * w, 0.325f * h);
    }

    // 在圆形和正方形的对勾
    private void drawCircleRightMarkPath() {
        // 对号起点
        float startX = (float) (0.3 * width);
        float startY = (float) (0.5 * height);
        mRightMarkPath.moveTo(startX, startY);

        // 对号拐角点
        float cornerX = (float) (0.43 * width);
        float cornerY = (float) (0.66 * height);
        mRightMarkPath.lineTo(cornerX, cornerY);

        // 对号终点
        float endX = (float) (0.75 * width);
        float endY = (float) (0.4 * height);
        mRightMarkPath.lineTo(endX, endY);
    }

    @Keep
    public void setPhase(float phase) {
        DashPathEffect pathEffect = new DashPathEffect(new float[]{mRightMarkPathLength, mRightMarkPathLength}, mRightMarkPathLength - phase * mRightMarkPathLength);
        mRightMarkPaint.setPathEffect(pathEffect);
        invalidate();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        if (!hasFollow) {
            super.onDraw(canvas);
            drawAddIfNeed(canvas);
        } else {
            canvas.drawPath(mRightMarkPath, mRightMarkPaint);
        }
    }

    private void drawAddIfNeed(Canvas canvas) {
        if (isDrawAddOperator) {
            int horizontalLeft = width / 2 - dip2px(getContext(), 10f) / 2;
            int horizontalTop = height / 2 - dip2px(getContext(), 1f);
            int horizontalRight = horizontalLeft + dip2px(getContext(), 10f);
            int horizontalBottom = height / 2 + dip2px(getContext(), 1f);

            int verticalLeft = width / 2 - dip2px(getContext(), 1f);
            int verticalTop = height / 2 - dip2px(getContext(), 10f) / 2;
            int verticalRight = width / 2 + dip2px(getContext(), 1f);
            int verticalBottom = verticalTop + dip2px(getContext(), 10f);

            canvas.drawRoundRect(new RectF(horizontalLeft, horizontalTop, horizontalRight,
                            horizontalBottom), dip2px(getContext(), 1f), dip2px(getContext(), 1f),
                    mAddMarkPaint);
            canvas.drawRoundRect(new RectF(verticalLeft, verticalTop, verticalRight,
                            verticalBottom), dip2px(getContext(), 1f), dip2px(getContext(), 1f),
                    mAddMarkPaint);
        }
    }
}
