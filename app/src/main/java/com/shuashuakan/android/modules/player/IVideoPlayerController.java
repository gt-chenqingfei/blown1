package com.shuashuakan.android.modules.player;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2019/01/02
 * Description:
 */
public abstract class IVideoPlayerController
    extends FrameLayout {

  protected VideoPlayer mVideoPlayer;

  private Timer mUpdateProgressTimer;
  private TimerTask mUpdateProgressTimerTask;

  public IVideoPlayerController(Context context) {
    super(context);
  }

  public void setVideoPlayer(VideoPlayer mVideoPlayer) {
   this. mVideoPlayer = mVideoPlayer;
  }

  /**
   * 设置播放的视频的标题
   *
   * @param title 视频标题
   */
  public abstract void setTitle(String title);

  /**
   * 视频底图
   *
   * @param resId 视频底图资源
   */
  public abstract void setImage(@DrawableRes int resId);

  /**
   * 视频底图ImageView控件，提供给外部用图片加载工具来加载网络图片
   *
   * @return 底图ImageView
   */
  public abstract ImageView imageView();

  /**
   * 设置总时长.
   */
  public abstract void setLength(long length);

  /**
   * 当播放器的播放状态发生变化，在此方法中国你更新不同的播放状态的UI
   *
   * @param playState 播放状态：
   *                  <ul>
   *                  <li>{@link VideoPlayer#STATE_IDLE}</li>
   *                  <li>{@link VideoPlayer#STATE_PREPARING}</li>
   *                  <li>{@link VideoPlayer#STATE_PREPARED}</li>
   *                  <li>{@link VideoPlayer#STATE_PLAYING}</li>
   *                  <li>{@link VideoPlayer#STATE_PAUSED}</li>
   *                  <li>{@link VideoPlayer#STATE_BUFFERING_PLAYING}</li>
   *                  <li>{@link VideoPlayer#STATE_BUFFERING_PAUSED}</li>
   *                  <li>{@link VideoPlayer#STATE_ERROR}</li>
   *                  <li>{@link VideoPlayer#STATE_COMPLETED}</li>
   *                  </ul>
   */
  protected abstract void onPlayStateChanged(int playState);

  /**
   * 当播放器的播放模式发生变化，在此方法中更新不同模式下的控制器界面。
   *
   * @param playMode 播放器的模式：
   *                 <ul>
   *                 <li>{@link VideoPlayer#MODE_NORMAL}</li>
   *                 <li>{@link VideoPlayer#MODE_FULL_SCREEN}</li>
   *                 </ul>
   */
  protected abstract void onPlayModeChanged(int playMode);

  protected abstract void onLoopDone();

  protected abstract void onVideoError(int errorCode);

  /**
   * 重置控制器，将控制器恢复到初始状态。
   */
  protected abstract void reset();

  /**
   * 开启更新进度的计时器。
   */
  protected void startUpdateProgressTimer() {
    cancelUpdateProgressTimer();
    if (mUpdateProgressTimer == null) {
      mUpdateProgressTimer = new Timer();
    }
    if (mUpdateProgressTimerTask == null) {
      mUpdateProgressTimerTask = new TimerTask() {
        @Override
        public void run() {
          IVideoPlayerController.this.post(new Runnable() {
            @Override
            public void run() {
              updateProgress();
            }
          });
        }
      };
    }
    mUpdateProgressTimer.schedule(mUpdateProgressTimerTask, 0, 400);
  }

  /**
   * 取消更新进度的计时器。
   */
  protected void cancelUpdateProgressTimer() {
    if (mUpdateProgressTimer != null) {
      mUpdateProgressTimer.cancel();
      mUpdateProgressTimer = null;
    }
    if (mUpdateProgressTimerTask != null) {
      mUpdateProgressTimerTask.cancel();
      mUpdateProgressTimerTask = null;
    }
  }

  /**
   * 更新进度，包括进度条进度，展示的当前播放位置时长，总时长等。
   */
  protected abstract void updateProgress();
}