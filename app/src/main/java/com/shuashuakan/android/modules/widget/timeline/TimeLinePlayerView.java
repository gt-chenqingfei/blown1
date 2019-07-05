/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shuashuakan.android.modules.widget.timeline;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.FrameLayout;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Player.DiscontinuityReason;
import com.google.android.exoplayer2.Player.VideoComponent;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.ResizeMode;
import com.google.android.exoplayer2.ui.spherical.SphericalSurfaceView;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.video.VideoListener;
import com.shuashuakan.android.R;

import timber.log.Timber;

public class TimeLinePlayerView extends FrameLayout implements TextureView.SurfaceTextureListener {
  private AspectRatioFrameLayout contentFrame;
  private TextureView surfaceView;
  private ComponentListener componentListener;

  private Player player;
  private boolean keepContentOnPlayerReset;
  private SurfaceTexture savedSurfaceTexture;

  public TimeLinePlayerView(Context context) {
    this(context, null);
  }

  public TimeLinePlayerView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public TimeLinePlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    if (isInEditMode()) {
      contentFrame = null;
      surfaceView = null;
      return;
    }
    int resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH;
    LayoutInflater.from(context).inflate(R.layout.layout_time_line_player_view, this);

    componentListener = new ComponentListener();
    setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

    // Content frame.
    contentFrame = findViewById(R.id.content_frame);
    surfaceView = findViewById(R.id.surface_view);
    setResizeModeRaw(contentFrame, resizeMode);
    surfaceView.setSurfaceTextureListener(this);
  }

  /**
   * Switches the view targeted by a given {@link Player}.
   *
   * @param player        The player whose target view is being switched.
   * @param oldPlayerView The old view to detach from the player.
   * @param newPlayerView The new view to attach to the player.
   */
  public static void switchTargetView(
      @NonNull Player player,
      @Nullable TimeLinePlayerView oldPlayerView,
      @Nullable TimeLinePlayerView newPlayerView) {
    if (oldPlayerView == newPlayerView) {
      return;
    }
    // We attach the new view before detaching the old one because this ordering allows the player
    // to swap directly from one surface to another, without transitioning through a state where no
    // surface is attached. This is significantly more efficient and achieves a more seamless
    // transition when using platform provided video decoders.
    if (newPlayerView != null) {
      newPlayerView.setPlayer(player);
    }
    if (oldPlayerView != null) {
      oldPlayerView.setPlayer(null);
    }
  }

  /**
   * Returns the player currently set on this view, or null if no player is set.
   */
  public Player getPlayer() {
    return player;
  }

  /**
   * Set the {@link Player} to use.
   *
   * <p>To transition a {@link Player} from targeting one view to another, it's recommended to use
   * {@link #switchTargetView(Player, TimeLinePlayerView, TimeLinePlayerView)} rather than this method. If you do
   * wish to use this method directly, be sure to attach the player to the new view <em>before</em>
   * calling {@code setPlayer(null)} to detach it from the old one. This ordering is significantly
   * more efficient and may allow for more seamless transitions.
   *
   * @param player The {@link Player} to use, or {@code null} to detach the current player. Only
   *               players which are accessed on the main thread are supported ({@code
   *               player.getApplicationLooper() == Looper.getMainLooper()}).
   */
  public void setPlayer(@Nullable Player player) {
    Assertions.checkState(Looper.myLooper() == Looper.getMainLooper());
    Assertions.checkArgument(
        player == null || player.getApplicationLooper() == Looper.getMainLooper());
    if (this.player == player) {
      return;
    }
    if (this.player != null) {
      this.player.removeListener(componentListener);
      VideoComponent oldVideoComponent = this.player.getVideoComponent();
      if (oldVideoComponent != null) {
        oldVideoComponent.removeVideoListener(componentListener);
        oldVideoComponent.clearVideoSurface();
      }
    }
    this.player = player;
    if (player != null) {
      VideoComponent newVideoComponent = player.getVideoComponent();
      if (newVideoComponent != null) {
        if (!surfaceView.isAvailable()
            && savedSurfaceTexture != null
            && surfaceView.getSurfaceTexture() != savedSurfaceTexture) {
          surfaceView.setSurfaceTexture(savedSurfaceTexture);
        }
        newVideoComponent.addVideoListener(componentListener);
      }
      player.addListener(componentListener);
    }
  }

  /**
   * Sets the {@link ResizeMode}.
   *
   * @param resizeMode The {@link ResizeMode}.
   */
  public void setResizeMode(@ResizeMode int resizeMode) {
    Assertions.checkState(contentFrame != null);
    contentFrame.setResizeMode(resizeMode);
  }

  /**
   * Returns the {@link ResizeMode}.
   */
  public @ResizeMode
  int getResizeMode() {
    Assertions.checkState(contentFrame != null);
    return contentFrame.getResizeMode();
  }

  /**
   * Sets whether the currently displayed video frame or media artwork is kept visible when the
   * player is reset. A player reset is defined to mean the player being re-prepared with different
   * media, {@link Player#stop(boolean)} being called with {@code reset=true}, or the player being
   * replaced or cleared by calling {@link #setPlayer(Player)}.
   *
   * <p>If enabled, the currently displayed video frame or media artwork will be kept visible until
   * the player set on the view has been successfully prepared with new media and loaded enough of
   * it to have determined the available tracks. Hence enabling this option allows transitioning
   * from playing one piece of media to another, or from using one player instance to another,
   * without clearing the view's content.
   *
   * <p>If disabled, the currently displayed video frame or media artwork will be hidden as soon as
   * the player is reset. Note that the video frame is hidden by making {@code exo_shutter} visible.
   * Hence the video frame will not be hidden if using a custom layout that omits this view.
   *
   * @param keepContentOnPlayerReset Whether the currently displayed video frame or media artwork is
   *                                 kept visible when the player is reset.
   */
  public void setKeepContentOnPlayerReset(boolean keepContentOnPlayerReset) {
    if (this.keepContentOnPlayerReset != keepContentOnPlayerReset) {
      this.keepContentOnPlayerReset = keepContentOnPlayerReset;
    }
  }

  /**
   * Set the {@link AspectRatioFrameLayout.AspectRatioListener}.
   *
   * @param listener The listener to be notified about aspect ratios changes of the video content or
   *                 the content frame.
   */
  public void setAspectRatioListener(AspectRatioFrameLayout.AspectRatioListener listener) {
    Assertions.checkState(contentFrame != null);
    contentFrame.setAspectRatioListener(listener);
  }

  /**
   * Gets the view onto which video is rendered. This is a:
   *
   * <ul>
   * <li>{@link SurfaceView} by default, or if the {@code surface_type} attribute is set to {@code
   * surface_view}.
   * <li>{@link TextureView} if {@code surface_type} is {@code texture_view}.
   * <li>{@link SphericalSurfaceView} if {@code surface_type} is {@code spherical_view}.
   * <li>{@code null} if {@code surface_type} is {@code none}.
   * </ul>
   *
   * @return The {@link SurfaceView}, {@link TextureView}, {@link SphericalSurfaceView} or {@code
   * null}.
   */
  public TextureView getVideoSurfaceView() {
    return surfaceView;
  }

  @SuppressWarnings("ResourceType")
  private static void setResizeModeRaw(AspectRatioFrameLayout aspectRatioFrame, int resizeMode) {
    aspectRatioFrame.setResizeMode(resizeMode);
  }

  @Override
  public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
    Timber.d("onSurfaceTextureAvailable:" + i + " " + i1);
    if (player == null || player.getVideoComponent() == null) {
      return;
    }
    if (this.savedSurfaceTexture == null) {
      this.savedSurfaceTexture = surfaceTexture;
      player.getVideoComponent().setVideoSurface(new Surface(savedSurfaceTexture));
    } else {
      surfaceView.setSurfaceTexture(savedSurfaceTexture);
    }
  }

  @Override
  public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
    Timber.d("onSurfaceTextureSizeChanged:" + i + " " + i1);
  }

  @Override
  public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
    return savedSurfaceTexture == null;
  }

  @Override
  public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
  }

  public void release() {
    if (savedSurfaceTexture != null)
      savedSurfaceTexture.release();
    savedSurfaceTexture = null;
  }

  private final class ComponentListener
      implements Player.EventListener,
      VideoListener {

    // VideoListener implementation
    @Override
    public void onVideoSizeChanged(
        int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
      Timber.d("onVideoSizeChanged:width:" + width + ",height:" + height);
      float videoAspectRatio =
          (height == 0 || width == 0) ? 1 : (width * pixelWidthHeightRatio) / height;

      contentFrame.setAspectRatio(videoAspectRatio);
    }

    @Override
    public void onRenderedFirstFrame() {
    }

    @Override
    public void onTracksChanged(TrackGroupArray tracks, TrackSelectionArray selections) {
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
    }

    @Override
    public void onPositionDiscontinuity(@DiscontinuityReason int reason) {
    }
  }
}
