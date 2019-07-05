package com.shuashuakan.android.player;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLOnBufferingUpdateListener;
import com.pili.pldroid.player.PLOnCompletionListener;
import com.pili.pldroid.player.PLOnErrorListener;
import com.pili.pldroid.player.PLOnInfoListener;
import com.pili.pldroid.player.PLOnPreparedListener;
import com.pili.pldroid.player.PLOnVideoSizeChangedListener;
import com.pili.pldroid.player.widget.PLVideoTextureView;
import com.shuashuakan.android.BuildConfig;
import com.shuashuakan.android.data.api.model.home.Feed;
import com.shuashuakan.android.data.api.model.home.VideoDetail;

import java.util.ArrayList;
import java.util.List;

import static com.shuashuakan.android.utils.CacheFileUtilKt.getVideoCacheDir;

public class SSKVideoTextureView extends PLVideoTextureView implements PLOnInfoListener
        , PLOnVideoSizeChangedListener
        , PLOnBufferingUpdateListener
        , PLOnCompletionListener
        , PLOnErrorListener
        , PLOnPreparedListener {

    public SSKVideoTextureView(Context context) {
        super(context);
        init(context);
    }

    public SSKVideoTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SSKVideoTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public SSKVideoTextureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private long upDateProgressDuration = 500L;
    private long defaultProgressMaxValue = 1000L;
    private ArrayList<SSKVideoPlayListener> mListeners = new ArrayList<>(4);

    private boolean clearUpDateSeekFlag = false;
    private boolean mIsPreparedAllowPlay = true;

    private Handler mHandler = new Handler();

    private Feed mBindFeed;
    private Feed mPlayFeed;
    private View fakeCoverView;

    public View getFakeCoverView() {
        return fakeCoverView;
    }

    public void setFakeCoverView(View coverView) {
        this.fakeCoverView = coverView;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacks(upDateSeekTask);
    }

    private final Runnable upDateSeekTask = new Runnable() {
        @Override
        public void run() {
            long duration = getDuration();
            if (getDuration() > 0 && !clearUpDateSeekFlag) {
                long pos = defaultProgressMaxValue * getCurrentPosition() / duration;
                for (int i = 0; i < mListeners.size(); i++) {
                    mListeners.get(i).onProgressUpdate(pos);
                }
            }

            if (!clearUpDateSeekFlag) {
                mHandler.postDelayed(this, upDateProgressDuration);
            }
        }
    };

    private void init(Context context) {
        AVOptions options = new AVOptions();
        // the unit of timeout is ms
        options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 20 * 1000);
        // 1 -> hw codec enable, 0 -> disable [recommended]
        options.setInteger(AVOptions.KEY_MEDIACODEC, AVOptions.MEDIA_CODEC_AUTO);
        int logLevel = 5;
        if (BuildConfig.DEBUG) {
            logLevel = 5;
        }
        options.setInteger(AVOptions.KEY_OPEN_RETRY_TIMES, 6);
        options.setInteger(AVOptions.KEY_LOG_LEVEL, logLevel);
        options.setString(AVOptions.KEY_CACHE_DIR, getVideoCacheDir(context));
        options.setInteger(AVOptions.KEY_PREFER_FORMAT, 2);
        setDisplayAspectRatio(ASPECT_RATIO_PAVED_PARENT);

        setAVOptions(options);

        //listener
        setOnInfoListener(this);
        setOnVideoSizeChangedListener(this);
        setOnBufferingUpdateListener(this);
        setOnCompletionListener(this);
        setOnErrorListener(this);
        setOnPreparedListener(this);
    }

    @Override
    public void start() {
        if (mIsPreparedAllowPlay) {
            super.start();
            clearUpDateSeekFlag = false;
            mHandler.post(upDateSeekTask);
            for (int i = 0; i < mListeners.size(); i++) {
                mListeners.get(i).onStart();
            }
        }
    }

    @Override
    public void pause() {
        super.pause();
        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).onPause();
        }
    }

    public void startPlayFeed() {
        if (mBindFeed != null) {
            List<VideoDetail> videoDetails = mBindFeed.getVideoDetails();
            VideoDetail originalVideDetail = null;
            for (VideoDetail videoDetail : videoDetails) {
                if (videoDetail.getClarity().equalsIgnoreCase("ORIGINAL")) {
                    originalVideDetail = videoDetail;
                }
            }
            if (originalVideDetail != null) {
                for (int i = 0; i < mListeners.size(); i++) {
                    mListeners.get(i).onStopPlayback();
                }
                stopPlayback();

                init(getContext());
                mPlayFeed = mBindFeed;
                setVideoPath(originalVideDetail.getUrl());
            }

        }
    }


    public void addVideoPlayListener(SSKVideoPlayListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeVideoPlayListener(SSKVideoPlayListener listener) {
        mListeners.remove(listener);
    }

    public void clearUpDateSeekTask() {
        clearUpDateSeekFlag = true;
    }

    public Feed getPlayFeed() {
        return mPlayFeed;
    }

    public void bindFeed(Feed feed) {
        mBindFeed = feed;
    }

    public Feed getBindFeed() {
        return mBindFeed;
    }

    public void hostPause() {
        mIsPreparedAllowPlay = false;
        pause();
    }

    public void hostResume() {
        mIsPreparedAllowPlay = true;
        start();
    }


    //
    @Override
    public void onBufferingUpdate(int precent) {
        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).onBufferingUpdate(precent);
        }
    }

    @Override
    public void onCompletion() {
        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).onCompletion();
        }
    }

    @Override
    public boolean onError(int errorCode) {
        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).onError(errorCode);
        }
        return false;
    }

    @Override
    public void onInfo(int what, int extra) {
        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).onInfo(what, extra);
        }
    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).onVideoSizeChanged(width, height);
        }
    }

    @Override
    public void onPrepared(int preparedTime) {
        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).onPrepared(preparedTime);
        }
    }
}
