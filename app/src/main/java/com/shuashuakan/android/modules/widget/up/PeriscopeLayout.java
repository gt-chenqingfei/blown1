package com.shuashuakan.android.modules.widget.up;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.shuashuakan.android.R;

import java.util.Random;

/**
 * Created by dada on 2016/8/3
 */
public class PeriscopeLayout extends RelativeLayout {
    private static final int DEFAULT_ITEM_WIDTH = 30; // dips
    private static final int DEFAULT_ITEM_HEIGHT = 30; // dips
    private static final int DEFAULT_ITEM_MARGIN_BOTTOM = 90; // dips
    private static final int DEFAULT_ITEM_MARGIN_RIGHT = 20; // dips


    private Interpolator line = new LinearInterpolator();//线性
    private Interpolator acc = new AccelerateInterpolator();//加速
    private Interpolator dce = new DecelerateInterpolator();//减速
    private Interpolator accdce = new AccelerateDecelerateInterpolator();//先加速再减速
    private Interpolator[] interpolators;


    private int[] animationItemRes = new int[]{R.drawable.emoji_1, R.drawable.emoji_2, R.drawable.emoji_3, R.drawable.emoji_4, R.drawable.emoji_5};

    private int mWidth, mHeight;//父容器的宽高

    private LayoutParams layoutParams;//图片的属性

    private Drawable[] itemDrawables;//图片的数组

    private int itemWidth;
    private int itemHeight;
    private boolean itemAlignParentLeft;
    private boolean itemAlignParentTop;
    private boolean itemAlignParentRight;
    private boolean itemAlignParentBottom;
    private int itemMarginLeft;
    private int itemMarginTop;
    private int itemMarginRight;
    private int itemMarginBottom;

    private Random random = new Random();//创建随机数的变量

    public PeriscopeLayout(Context context) {
        this(context, null);
    }

    public PeriscopeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PeriscopeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final float density = context.getResources().getDisplayMetrics().density;

        TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.PeriscopeLayout);
        itemWidth = t.getDimensionPixelSize(R.styleable.PeriscopeLayout_itemWidth, (int) (DEFAULT_ITEM_WIDTH * density));
        itemHeight = t.getDimensionPixelSize(R.styleable.PeriscopeLayout_itemHeight, (int) (DEFAULT_ITEM_HEIGHT * density));

        itemAlignParentLeft = t.getBoolean(R.styleable.PeriscopeLayout_itemAlignParentLeft, false);
        itemAlignParentTop = t.getBoolean(R.styleable.PeriscopeLayout_itemAlignParentTop, false);
        itemAlignParentRight = t.getBoolean(R.styleable.PeriscopeLayout_itemAlignParentRight, false);
        itemAlignParentBottom = t.getBoolean(R.styleable.PeriscopeLayout_itemAlignParentBottom, true);

        itemMarginLeft = t.getDimensionPixelSize(R.styleable.PeriscopeLayout_itemMarginLeft, 0);
        itemMarginTop = t.getDimensionPixelSize(R.styleable.PeriscopeLayout_itemMarginTop, 0);
        itemMarginRight = t.getDimensionPixelSize(R.styleable.PeriscopeLayout_itemMarginRight, (int) (DEFAULT_ITEM_MARGIN_RIGHT * density));
        itemMarginBottom = t.getDimensionPixelSize(R.styleable.PeriscopeLayout_itemMarginBottom, (int) (DEFAULT_ITEM_MARGIN_BOTTOM * density));

        t.recycle();


        init();
    }

    private void init() {
        itemDrawables = new Drawable[animationItemRes.length];
        Drawable itemDrawable;
        for (int i = 0; i < animationItemRes.length; i++) {
            itemDrawable = getResources().getDrawable(animationItemRes[i]);
            itemDrawables[i] = itemDrawable;
        }
        interpolators = new Interpolator[]{line, acc, dce, accdce};
    }

    /**
     * 获取容器的宽高
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }


    /**
     * 添加心形图片
     */
    public void addHeart() {
        ImageView imageView = new ImageView(getContext());
        imageView.setImageDrawable(itemDrawables[random.nextInt(5)]);//随机选取一张图片
        layoutParams = new LayoutParams(itemWidth, itemHeight);
        if (itemAlignParentLeft) {
            layoutParams.addRule(ALIGN_PARENT_LEFT, TRUE);
        }
        if (itemAlignParentTop) {
            layoutParams.addRule(ALIGN_PARENT_TOP, TRUE);
        }
        if (itemAlignParentRight) {
            layoutParams.addRule(ALIGN_PARENT_RIGHT, TRUE);
        }
        if (itemAlignParentBottom) {
            layoutParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);
        }
        layoutParams.setMargins(itemMarginLeft, itemMarginTop, itemMarginRight, itemMarginBottom);
        imageView.setLayoutParams(layoutParams);//给图片设置已经设定好的参数

        addView(imageView);//向容器中添加视图

        Animator animator = getAnimator(imageView);//获取动画
        animator.addListener(new AnimatorEndListener(imageView));//添加结束的监听
        animator.start();
    }

    /**
     * 添加心形图片
     */
    public void addHeartWithDelay(MotionEvent motionEvent) {
        for (int i = 0; i < 4; i++) {
            addHeartWithActionEvent(motionEvent, 80 * i);
        }
    }

    private void addHeartWithActionEvent(MotionEvent motionEvent, long delay) {
        ImageView imageView = new ImageView(getContext());
        imageView.setImageDrawable(itemDrawables[random.nextInt(5)]);//随机选取一张图片
        float rawX = motionEvent.getRawX();
        float rawY = motionEvent.getRawY();
        layoutParams = new LayoutParams(itemWidth, itemHeight);
        layoutParams.addRule(ALIGN_PARENT_RIGHT, TRUE);
        layoutParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);
        layoutParams.rightMargin = (int) (getContext().getResources().getDisplayMetrics().widthPixels - rawX - itemWidth / 2);
        layoutParams.bottomMargin = (int) (getContext().getResources().getDisplayMetrics().heightPixels - rawY - itemHeight / 2);
        imageView.setLayoutParams(layoutParams);//给图片设置已经设定好的参数

        addView(imageView);//向容器中添加视图

        Animator animator = getAnimator(imageView);//获取动画
        animator.addListener(new AnimatorEndListener(imageView));//添加结束的监听
        animator.setStartDelay(delay);
        animator.start();
    }

    /**
     * 获取动画的方法
     */
    private Animator getAnimator(View view) {
        //获取入场动画的集合
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, View.ALPHA, 0.2f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 0.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 0.2f, 1f);

        AnimatorSet enter = new AnimatorSet();
        enter.setDuration(400);
        enter.playTogether(alpha, scaleX, scaleY);
        enter.setInterpolator(new LinearInterpolator());
        enter.setTarget(view);

        int parentRightMargin = 0;
        int parentBottomMargin = 0;
        if (getLayoutParams() instanceof MarginLayoutParams) {
            MarginLayoutParams mlp = (MarginLayoutParams) getLayoutParams();
            parentRightMargin = mlp.rightMargin;
            parentBottomMargin = mlp.bottomMargin;
        }

        //获取图片过程中的动画
        BezierEvaluator evaluator = new BezierEvaluator(getPoint(2), getPoint(1));
        //初始化一个属性动画,传入了计算器和起始点终止点
        int itemStartX = mWidth - parentRightMargin - layoutParams.rightMargin - layoutParams.width;
        int itemStartY = mHeight - parentBottomMargin - layoutParams.bottomMargin - layoutParams.height;
        ValueAnimator animator = ValueAnimator.ofObject(evaluator, new PointF(itemStartX, itemStartY),
                new PointF(random.nextInt(getWidth()), 0));

        animator.addUpdateListener(new BezierListener(view));
        animator.setDuration(2000);
        animator.setTarget(view);


        //设置动画集合
        AnimatorSet finalSet = new AnimatorSet();
        finalSet.playSequentially(enter);
        finalSet.playSequentially(enter, animator);
        finalSet.setInterpolator(interpolators[random.nextInt(4)]);
        finalSet.setTarget(view);
        return finalSet;
    }

    /**
     * 获取中间2个点的坐标
     *
     * @param scale
     * @return
     */
    private PointF getPoint(float scale) {
        PointF pointf = new PointF();
        pointf.x = random.nextInt(mWidth);
        pointf.y = random.nextInt(mHeight - 50) / scale;
        return pointf;
    }

    /**
     * 动画更新监听器
     */
    private class BezierListener implements ValueAnimator.AnimatorUpdateListener {
        private View view;

        public BezierListener(View view) {
            this.view = view;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            //将获取到的贝塞尔曲线计算出来的xy值，赋值给view，这样图片就能按着曲线走了
            PointF pointf = (PointF) animation.getAnimatedValue();
            view.setX(pointf.x);
            view.setY(pointf.y);
            //给图片设置一个透明度
            view.setAlpha(1 - animation.getAnimatedFraction());
        }
    }

    /**
     * 动画结束时的监听
     */
    private class AnimatorEndListener extends AnimatorListenerAdapter {
        private View view;

        public AnimatorEndListener(View view) {
            this.view = view;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            if (view != null && view.getParent() != null) {
                removeView(view);
            }
        }
    }

}
