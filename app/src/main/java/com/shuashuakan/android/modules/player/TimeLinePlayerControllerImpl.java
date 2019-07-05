package com.shuashuakan.android.modules.player;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.facebook.drawee.view.SimpleDraweeView;
import com.shuashuakan.android.R;
import com.shuashuakan.android.modules.widget.OnPlayChangeListener;

/**
 * TimeLine 页面使用的播放控制器
 * <p>
 * Author: ZhaiDongyang
 * Date: 2019/1/15
 */
public class TimeLinePlayerControllerImpl extends IVideoPlayerController {
  private ProgressBar progressBar;
  private SimpleDraweeView profileTimelineCoverView;
  private OnPlayChangeListener playChangeListener;

  public TimeLinePlayerControllerImpl(Context context) {
    super(context);
    LayoutInflater.from(context).inflate(R.layout.layout_timeline_video_view_profile, this, true);
    profileTimelineCoverView = findViewById(R.id.timeline_profile_type_subscribe_sv);
    progressBar = findViewById(R.id.timeline_video_view_profile_progress);
  }

  public void setPlayChangeListener(OnPlayChangeListener playChangeListener) {
    this.playChangeListener = playChangeListener;
  }

  public OnPlayChangeListener getPlayChangeListener() {
    return playChangeListener;
  }

  @Override
  public void setTitle(String title) {

  }

  @Override
  public void setImage(int resId) {

  }

  @Override
  public ImageView imageView() {
    return null;
  }

  @Override
  public void setLength(long length) {

  }

  private long startPoint = 0;
  public long endPoint = 0;

  private long catchPostion = 0;
  private long catchTime = 0;

  @Override
  protected void onPlayStateChanged(int playState) {
    mVideoPlayer.setVolume(0f, 0f);
    if (playState == VideoPlayer.STATE_PREPARING) {
      progressBar.setVisibility(View.VISIBLE);
    } else {
      progressBar.setVisibility(View.GONE);
    }
    if (playState == VideoPlayer.STATE_IDLE ||
        playState == VideoPlayer.STATE_PREPARING ||
        playState == VideoPlayer.STATE_PREPARED) {
      profileTimelineCoverView.setVisibility(View.VISIBLE);
    } else {
      profileTimelineCoverView.setVisibility(View.GONE);
    }
    if (playState == VideoPlayer.STATE_PLAYING) {

      progressBar.setVisibility(View.GONE);
      startPoint = mVideoPlayer.getCurrentPosition();
    } else if (playState == VideoPlayer.STATE_PAUSED) {
      endPoint = mVideoPlayer.getCurrentPosition();
      if (playChangeListener != null) {
        playChangeListener.onVideoPauseRecord(mVideoPlayer, startPoint, endPoint, false);
      }
    } else if (playState == VideoPlayer.STATE_BUFFERING_PAUSED) {
      catchTime = System.currentTimeMillis();
      catchPostion = mVideoPlayer.getCurrentPosition();
      progressBar.setVisibility(View.VISIBLE);
    } else if (playState == VideoPlayer.STATE_BUFFERING_PLAYING) {
      if (playChangeListener != null) {
        playChangeListener.onVideoStandStill(mVideoPlayer, catchPostion, catchTime - System.currentTimeMillis());
      }
      progressBar.setVisibility(View.GONE);
    } else if (playState == VideoPlayer.STATE_PREPARING) {
      progressBar.setVisibility(View.VISIBLE);
    } else if (playState == VideoPlayer.STATE_IDLE) {
      progressBar.setVisibility(View.GONE);
    } else if (playState == VideoPlayer.STATE_COMPLETED) {
      mVideoPlayer.restart();
    }
  }

  @Override
  protected void onPlayModeChanged(int playMode) {

  }

  @Override
  protected void onLoopDone() {
    if (playChangeListener != null) {
      playChangeListener.onRepeatPlay(mVideoPlayer);
    }
  }

  @Override
  protected void onVideoError(int errorCode) {
    if (playChangeListener != null) {
      playChangeListener.onVideoLoadError(mVideoPlayer.getUrl(), errorCode);
    }
  }

  @Override
  protected void reset() {

  }

  @Override
  protected void updateProgress() {

  }

  public void setVideoPlayerBackground(String url) {
    profileTimelineCoverView.setImageURI(url);
  }
}
