package com.shuashuakan.android.modules.player;

import android.content.Context;
import android.content.pm.PackageManager;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import com.shuashuakan.android.commons.di.AppContext;
import com.shuashuakan.android.modules.widget.timeline.TimeLinePlayer;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import okhttp3.OkHttpClient;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.support.v4.content.ContextCompat.checkSelfPermission;
import static com.google.android.exoplayer2.upstream.cache.CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR;
import static com.shuashuakan.android.data.api.ApiModuleKt.DOMAIN;

@Singleton
public class ExoPlayerHelper {
    private static final long CACHE_SIZE = 500 * 1024 * 1024; // 500MB
    private static final String CACHE_DIR_NAME = "duck_cache_videos";
    private static final String APP_NAME = "duck";
    private final DefaultBandwidthMeter bandwidthMeter;
    private final String userAgent;
    private final Context applicationContext;
    private final OkHttpClient okHttpClient;
    private final File cacheDir;
    private DataSource.Factory sourceFactory;
    private RenderersFactory renderersFactory;
    private TrackSelector trackSelector;
    private TrackSelection.Factory adaptiveTrackSelection;
    private DefaultLoadControl defaultLoadControl;
    private TimeLinePlayer timeLinePlayer;

    public TimeLinePlayer getTimeLinePlayer() {
        return timeLinePlayer;
    }

    public void setTimeLinePlayer(TimeLinePlayer timeLinePlayerView) {
        this.timeLinePlayer = timeLinePlayerView;
    }

    @Inject
    public ExoPlayerHelper(@AppContext Context context, @Named(DOMAIN) OkHttpClient okHttpClient) {
        this.applicationContext = context;
        this.bandwidthMeter = new DefaultBandwidthMeter();
        this.okHttpClient = okHttpClient;
        this.userAgent = Util.getUserAgent(applicationContext, APP_NAME);
        this.cacheDir = createExternalCacheDir(applicationContext, CACHE_DIR_NAME);
    }

    public BandwidthMeter bandwidthMeter() {
        return bandwidthMeter;
    }

    public synchronized DataSource.Factory dataSourceFactory() {
        if (sourceFactory == null) {
            DataSource.Factory upstreamFactory =
                new OkHttpDataSourceFactory(okHttpClient, userAgent, bandwidthMeter, null);
            LeastRecentlyUsedCacheEvictor lruCache = new LeastRecentlyUsedCacheEvictor(CACHE_SIZE);
            sourceFactory =
                new CacheDataSourceFactory(new SimpleCache(cacheDir, lruCache), upstreamFactory,
                    FLAG_IGNORE_CACHE_ON_ERROR);
        }
        return sourceFactory;
    }

    public static File createExternalCacheDir(Context context, String name) {
        if (checkSelfPermission(context, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return new File(context.getExternalCacheDir(), name);
        } else {
            return new File(context.getCacheDir(), name);
        }
    }

    public static String getVideoDir(Context context) {
        File cache;
        if (checkSelfPermission(context, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            cache = new File(context.getExternalCacheDir(), CACHE_DIR_NAME);
        }else{
            cache = new File(context.getCacheDir(), CACHE_DIR_NAME);
        }
        if (!cache.exists()){
            // For some reason the cache directory doesn't exist. Make a best effort to create it.
            cache.mkdirs();
        }
        return cache.getAbsolutePath();
    }

    public RenderersFactory renderersFactory() {
        if (renderersFactory == null) {
            renderersFactory = new DefaultRenderersFactory(applicationContext);
        }
        return renderersFactory;
    }

    public TrackSelector trackSelector() {
        if (trackSelector == null) {
            trackSelector = new DefaultTrackSelector(adaptiveTrackSelection());
        }
        return trackSelector;
    }

    public TrackSelection.Factory adaptiveTrackSelection() {
        if (adaptiveTrackSelection == null) {
            adaptiveTrackSelection = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        }
        return adaptiveTrackSelection;
    }

    public DefaultLoadControl loadControl() {
        if (defaultLoadControl == null) {
          defaultLoadControl = new DefaultLoadControl();
        }
        return defaultLoadControl;
    }
}