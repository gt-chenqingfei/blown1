package com.luck.picture.lib.widget;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.VideoView;

import com.luck.picture.lib.R;
import com.luck.picture.lib.tools.ScreenUtils;
import com.qiniu.pili.droid.shortvideo.PLMediaFile;
import com.qiniu.pili.droid.shortvideo.PLVideoFrame;

import java.io.File;

import io.reactivex.annotations.NonNull;

import static android.os.AsyncTask.Status.RUNNING;

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/12/20
 * Description:
 */
public class VideoTrimmerBar extends FrameLayout {
  private static final String TAG = VideoTrimmerBar.class.getSimpleName();

  private static final long MIN_CUT_DURATION = 3000L;// 最小剪辑时间1s
  private static final long MAX_CUT_DURATION = 30 * 1000L;//视频最aa多剪切多长时间
  private static final int MAX_COUNT_RANGE = 10;//seekBar的区域内一共有多少张图片

  private long leftProgress, rightProgress;
  private RecyclerView mRecyclerView;
  private ImageView positionIcon;
  private LinearLayout seekBarLayout;
  private RangeSeekBar seekBar;

  private VideoEditAdapter videoEditAdapter;

  private VideoView mVideoView;

  private PLMediaFile mMediaFile;
  private String mVideoPath;
  private long mDurationMs;
  private int mMaxWidth;
  private int mScaledTouchSlop;

  private float averageMsPx;//每毫秒所占的px
  private float averagePxMs;//每px所占用的ms毫秒

  private int thumbnailsCount;

  private boolean isSeeking;

  public VideoTrimmerBar(@NonNull Context context) {
    this(context, null);
  }

  public VideoTrimmerBar(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public VideoTrimmerBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    View.inflate(getContext(), R.layout.video_trimmer_bar, this);
    mRecyclerView = findViewById(R.id.id_rv_id);
    positionIcon = findViewById(R.id.positionIcon);
    seekBarLayout = findViewById(R.id.id_seekBarLayout);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

    videoEditAdapter = new VideoEditAdapter(getContext(),
        (ScreenUtils.getScreenWidth(getContext()) - ScreenUtils.dip2px(getContext(), 70)) / 10);

    mRecyclerView.setAdapter(videoEditAdapter);
    mRecyclerView.addOnScrollListener(mOnScrollListener);

    mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    mMaxWidth = ScreenUtils.getScreenWidth(getContext()) - ScreenUtils.dip2px(getContext(), 70);
  }

  public void setVideoView(VideoView mVideoView) {
    this.mVideoView = mVideoView;
  }

  public void setVideoPath(String path) {
    mVideoPath = path;
    mMediaFile = new PLMediaFile(mVideoPath);
    mDurationMs = mMediaFile.getDurationMs();

    mVideoView.setVideoPath(path);
    //设置videoview的OnPrepared监听
    mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override
      public void onPrepared(MediaPlayer mp) {
        //设置MediaPlayer的OnSeekComplete监听
        mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
          @Override
          public void onSeekComplete(MediaPlayer mp) {
            if (!isSeeking) {
              videoStart();
            }
          }
        });
      }
    });
    initEditVideo();
  }

  private void videoStart() {
    mVideoView.start();
    positionIcon.clearAnimation();
    if (animator != null && animator.isRunning()) {
      animator.cancel();
    }
    anim();
    handler.removeCallbacks(run);
    handler.post(run);
  }


  private void initEditVideo() {
    //for video edit
    long startPosition = 0;
    long endPosition = mDurationMs;
    int rangeWidth;
    boolean isOver_10_s;
    if (endPosition <= MAX_CUT_DURATION) {
      isOver_10_s = false;
      thumbnailsCount = MAX_COUNT_RANGE;
      rangeWidth = mMaxWidth;
    } else {
      isOver_10_s = true;
      thumbnailsCount = (int) (endPosition * 1.0f / (MAX_CUT_DURATION * 1.0f) * MAX_COUNT_RANGE);
      rangeWidth = mMaxWidth / MAX_COUNT_RANGE * thumbnailsCount;
    }
    mRecyclerView.addItemDecoration(new EditSpacingItemDecoration(ScreenUtils.dip2px(getContext(), 35), thumbnailsCount));

    //init seekBar
    if (isOver_10_s) {
      seekBar = new RangeSeekBar(getContext(), 0L, MAX_CUT_DURATION);
      seekBar.setSelectedMinValue(0L);
      seekBar.setSelectedMaxValue(MAX_CUT_DURATION);
    } else {
      seekBar = new RangeSeekBar(getContext(), 0L, endPosition);
      seekBar.setSelectedMinValue(0L);
      seekBar.setSelectedMaxValue(endPosition);
    }

    seekBar.setMin_cut_time(MIN_CUT_DURATION);//设置最小裁剪时间
    seekBar.setNotifyWhileDragging(true);
    seekBar.setOnRangeSeekBarChangeListener(mOnRangeSeekBarChangeListener);
    seekBarLayout.addView(seekBar);


    Log.d(TAG, "-------thumbnailsCount--->>>>" + thumbnailsCount);
    averageMsPx = mDurationMs * 1.0f / rangeWidth * 1.0f;
    Log.d(TAG, "-------rangeWidth--->>>>" + rangeWidth);
    Log.d(TAG, "-------localMedia.getDuration()--->>>>" + mDurationMs);
    Log.d(TAG, "-------averageMsPx--->>>>" + averageMsPx);
//    int extractW = (ScreenUtils.getScreenWidth(getContext()) - ScreenUtils.dip2px(getContext(), 70)) / MAX_COUNT_RANGE;
//    int extractH = ScreenUtils.dip2px(getContext(), 55);

    averageMsPx = mDurationMs * 1.0f / rangeWidth * 1.0f;

    startThread();
    //init pos icon start
    leftProgress = 0;
    if (isOver_10_s) {
      rightProgress = MAX_CUT_DURATION;
    } else {
      rightProgress = endPosition;
    }
    averagePxMs = (mMaxWidth * 1.0f / (rightProgress - leftProgress));
    Log.d(TAG, "------averagePxMs----:>>>>>" + averagePxMs);
    videoEditAdapter.setThumbnailsCount(thumbnailsCount);
  }


  private AsyncTask task;

  @SuppressLint("StaticFieldLeak")
  private void startThread() {
    task = new AsyncTask<Void, PLVideoFrame, Void>() {
      @Override
      protected Void doInBackground(Void... v) {
        for (int i = 0; i < thumbnailsCount; ++i) {
          PLVideoFrame frame = mMediaFile.getVideoFrameByTime((long) ((1.0f * i / thumbnailsCount) * mDurationMs), true,
              ScreenUtils.dip2px(getContext(), 35f), ScreenUtils.dip2px(getContext(), 65f));
          publishProgress(frame);
        }
        return null;
      }

      @Override
      protected void onProgressUpdate(PLVideoFrame... values) {
        super.onProgressUpdate(values);
        videoEditAdapter.addItemVideoInfo(values[0]);
      }
    }.execute();
  }

  private final RangeSeekBar.OnRangeSeekBarChangeListener mOnRangeSeekBarChangeListener = new RangeSeekBar.OnRangeSeekBarChangeListener() {
    @Override
    public void onRangeSeekBarValuesChanged(RangeSeekBar bar, long minValue, long maxValue, int action, boolean isMin, RangeSeekBar.Thumb pressedThumb) {
      Log.d(TAG, "-----minValue----->>>>>>" + minValue);
      Log.d(TAG, "-----maxValue----->>>>>>" + maxValue);
      leftProgress = minValue + scrollPos;
      rightProgress = maxValue + scrollPos;
      Log.d(TAG, "-----leftProgress----->>>>>>" + leftProgress);
      Log.d(TAG, "-----rightProgress----->>>>>>" + rightProgress);
      switch (action) {
        case MotionEvent.ACTION_DOWN:
          Log.d(TAG, "-----ACTION_DOWN---->>>>>>");
          isSeeking = false;
          videoPause();
          break;
        case MotionEvent.ACTION_MOVE:
          Log.d(TAG, "-----ACTION_MOVE---->>>>>>");
          isSeeking = true;
          mVideoView.seekTo((int) (pressedThumb == RangeSeekBar.Thumb.MIN ?
              leftProgress : rightProgress));
          break;
        case MotionEvent.ACTION_UP:
          Log.d(TAG, "-----ACTION_UP--leftProgress--->>>>>>" + leftProgress);
          isSeeking = false;
          mVideoView.seekTo((int) leftProgress);
          break;
        default:
          break;
      }
    }
  };

  private boolean isOverScaledTouchSlop;
  private int lastScrollX;

  private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
      super.onScrollStateChanged(recyclerView, newState);
      Log.d(TAG, "-------newState:>>>>>" + newState);
      if (newState == RecyclerView.SCROLL_STATE_IDLE) {
        isSeeking = false;
//                videoStart();
      } else {
        isSeeking = true;
        if (isOverScaledTouchSlop && mVideoView != null && mVideoView.isPlaying()) {
          videoPause();
        }
      }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
      super.onScrolled(recyclerView, dx, dy);
      isSeeking = false;
      int scrollX = getScrollXDistance();
      //达不到滑动的距离
      if (Math.abs(lastScrollX - scrollX) < mScaledTouchSlop) {
        isOverScaledTouchSlop = false;
        return;
      }
      isOverScaledTouchSlop = true;
      Log.d(TAG, "-------scrollX:>>>>>" + scrollX);
      //初始状态,why ? 因为默认的时候有35dp的空白！
      if (scrollX == -ScreenUtils.dip2px(getContext(), 35)) {
        scrollPos = 0;
      } else {
        // why 在这里处理一下,因为onScrollStateChanged早于onScrolled回调
        if (mVideoView != null && mVideoView.isPlaying()) {
          videoPause();
        }
        isSeeking = true;
        scrollPos = (long) (averageMsPx * (ScreenUtils.dip2px(getContext(), 35) + scrollX));
        Log.d(TAG, "-------scrollPos:>>>>>" + scrollPos);
        leftProgress = seekBar.getSelectedMinValue() + scrollPos;
        rightProgress = seekBar.getSelectedMaxValue() + scrollPos;
        Log.d(TAG, "-------leftProgress:>>>>>" + leftProgress);
        mVideoView.seekTo((int) leftProgress);
      }
      lastScrollX = scrollX;
    }
  };

  /**
   * 水平滑动了多少px
   *
   * @return int px
   */
  private int getScrollXDistance() {
    LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
    int position = layoutManager.findFirstVisibleItemPosition();
    View firstVisibleChildView = layoutManager.findViewByPosition(position);
    int itemWidth = firstVisibleChildView.getWidth();
    return (position) * itemWidth - firstVisibleChildView.getLeft();
  }

  private Handler handler = new Handler();
  private Runnable run = new Runnable() {

    @Override
    public void run() {
      videoProgressUpdate();
      handler.postDelayed(run, 1000);
    }
  };

  private ValueAnimator animator;

  private void anim() {
    Log.d(TAG, "--anim--onProgressUpdate---->>>>>>>" + mVideoView.getCurrentPosition());
    if (positionIcon.getVisibility() == View.GONE) {
      positionIcon.setVisibility(View.VISIBLE);
    }
    final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) positionIcon.getLayoutParams();
    int start = (int) (ScreenUtils.dip2px(getContext(), 35) + (leftProgress/*mVideoView.getCurrentPosition()*/ - scrollPos) * averagePxMs);
    int end = (int) (ScreenUtils.dip2px(getContext(), 35) + (rightProgress - scrollPos) * averagePxMs);
    animator = ValueAnimator
        .ofInt(start, end)
        .setDuration((rightProgress - scrollPos) - (leftProgress/*mVideoView.getCurrentPosition()*/ - scrollPos));
    animator.setInterpolator(new LinearInterpolator());
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        params.leftMargin = (int) animation.getAnimatedValue();
        positionIcon.setLayoutParams(params);
      }
    });
    animator.start();
  }

  private void videoProgressUpdate() {
    long currentPosition = mVideoView.getCurrentPosition();
    Log.d(TAG, "----onProgressUpdate-cp---->>>>>>>" + currentPosition);
    if (currentPosition >= (rightProgress)) {
      mVideoView.seekTo((int) leftProgress);
      positionIcon.clearAnimation();
      if (animator != null && animator.isRunning()) {
        animator.cancel();
      }
      anim();
    }
  }

  private void videoPause() {
    isSeeking = false;
    if (mVideoView != null && mVideoView.isPlaying()) {
      mVideoView.pause();
      handler.removeCallbacks(run);
    }
    Log.d(TAG, "----videoPause----->>>>>>>");
    if (positionIcon.getVisibility() == View.VISIBLE) {
      positionIcon.setVisibility(View.GONE);
    }
    positionIcon.clearAnimation();
    if (animator != null && animator.isRunning()) {
      animator.cancel();
    }
  }

  public void onPause() {
    if (mVideoView != null && mVideoView.isPlaying()) {
      videoPause();
    }
  }

  public void onResume() {
    if (mVideoView != null) {
      mVideoView.seekTo((int) leftProgress);
    }
  }

  public void onDestroy() {
    if (animator != null) {
      animator.cancel();
    }
    if (mVideoView != null) {
      mVideoView.stopPlayback();
    }
    if (task.getStatus() == RUNNING) {
      task.cancel(false);
    }
    mRecyclerView.removeOnScrollListener(mOnScrollListener);
    handler.removeCallbacksAndMessages(null);
  }

  private long scrollPos = 0;

}
