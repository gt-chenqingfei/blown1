package com.luck.picture.lib;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.EventEntity;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.VideoCompressListener;
import com.luck.picture.lib.observable.ImagesObservable;
import com.luck.picture.lib.rxbus2.RxBus;
import com.luck.picture.lib.tools.ToastManage;
import com.luck.picture.lib.tools.VoiceUtils;
import com.qiniu.pili.droid.shortvideo.PLMediaFile;
import com.qiniu.pili.droid.shortvideo.PLShortVideoTranscoder;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PictureVideoPlayActivity extends PictureBaseActivity implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, View.OnClickListener {
  private String video_path = "";
  private ImageView picture_left_back;
  //private MediaController mMediaController;
  private VideoView mVideoView;
  private ImageView iv_play;
  private int mPositionWhenPaused = -1;
  private LinearLayout ll_check;
  private TextView check;
  private List<LocalMedia> selectImages = new ArrayList<>();
  private List<LocalMedia> images = new ArrayList<>();
  private int position;
  private TextView rightTv;
  //private VideoTrimmerBar trimmerBar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.picture_activity_video_play);
    video_path = getIntent().getStringExtra("video_path");
    picture_left_back = findViewById(R.id.picture_left_back);
    ll_check = findViewById(R.id.ll_check);
    check = findViewById(R.id.check);
    rightTv=findViewById(R.id.picture_right);
   // trimmerBar=findViewById(R.id.trimmerBar);

    mVideoView = findViewById(R.id.video_view);
    mVideoView.setBackgroundColor(Color.BLACK);
    iv_play = findViewById(R.id.iv_play);
    //mMediaController = new MediaController(this);
    mVideoView.setOnCompletionListener(this);
    mVideoView.setOnPreparedListener(this);
    //mVideoView.setMediaController(mMediaController);
    picture_left_back.setOnClickListener(this);
    iv_play.setOnClickListener(this);


    selectImages = (List<LocalMedia>) getIntent().
        getSerializableExtra(PictureConfig.EXTRA_SELECT_LIST);
    images = ImagesObservable.getInstance().readLocalMedias();
    position = getIntent().getIntExtra(PictureConfig.EXTRA_POSITION, 0);


    initListener();
    initVideoView();
    //initTrimmerBar();
  }

  private void initTrimmerBar() {
//    trimmerBar.setVideoView(mVideoView);
//    trimmerBar.setVideoPath(video_path);
  }

  private void initListener() {
    ll_check.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (images != null && images.size() > 0) {
          LocalMedia image = images.get(position);
          String pictureType = selectImages.size() > 0 ?
              selectImages.get(0).getPictureType() : "";
          if (!TextUtils.isEmpty(pictureType)) {
            boolean toEqual = PictureMimeType.
                mimeToEqual(pictureType, image.getPictureType());
            if (!toEqual) {
              ToastManage.s(mContext, getString(R.string.picture_rule));
              return;
            }
          }
          // 刷新图片列表中图片状态
          boolean isChecked;
          if (!check.isSelected()) {
            isChecked = true;
            check.setSelected(true);
            //check.startAnimation(animation);
          } else {
            isChecked = false;
            check.setSelected(false);
          }
          if (selectImages.size() >= config.maxSelectNum && isChecked) {
            ToastManage.s(mContext, getString(R.string.picture_message_max_num, config.maxSelectNum));
            check.setSelected(false);
            return;
          }
          if (isChecked) {
            VoiceUtils.playVoice(mContext, config.openClickSound);
            // 如果是单选，则清空已选中的并刷新列表(作单一选择)

            selectImages.add(image);
            image.setNum(selectImages.size());
            if (config.checkNumMode) {
              check.setText(String.valueOf(image.getNum()));
            }
          } else {
            for (LocalMedia media : selectImages) {
              if (media.getPath().equals(image.getPath())) {
                selectImages.remove(media);
//                                subSelectPosition();
//                                notifyCheckChanged(media);
                break;
              }
            }
          }
          //onSelectNumChange(true);
        }

        EventEntity obj = new EventEntity(PictureConfig.UPDATE_FLAG, selectImages, position);
        RxBus.getDefault().post(obj);
      }
    });
    rightTv.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if(selectImages.size()==0){
          LocalMedia image = images.get(position);
          selectImages.add(image);
        }
        compressVideo(selectImages);
      }
    });
  }
  //压缩视频
  private void compressVideo(final List<LocalMedia> images) {
    //我们是单选，所以先只处理单选的情况
    if (images.size() != 0) {
      showPleaseDialog(config.isUploadVideo);
      scaleVideo(images.get(0).getPath(), new VideoCompressListener() {
        @Override
        public void onProgress(float progress) {

        }

        @Override
        public void onSuccess(String scaleFilePath) {
          if(PictureVideoPlayActivity.this.isFinishing()){
            return ;
          }
          images.get(0).setCompressPath(scaleFilePath);
          onResult(images);
        }

        @Override
        public void onFailed(String errorMsg) {
          if(PictureVideoPlayActivity.this.isFinishing()){
            return ;
          }
          dismissDialog(config.isUploadVideo);
          Toast.makeText(PictureVideoPlayActivity.this,  getResources().getString(R.string.string_video_compress_error), Toast.LENGTH_SHORT).show();

        }
      });
    }

  }

  @Override
  public void onResult(List<LocalMedia> images) {
    RxBus.getDefault().post(new EventEntity(PictureConfig.PREVIEW_DATA_FLAG, images));
    onBackPressed();
    dismissDialog(config.isUploadVideo);
  }

  private void scaleVideo(String filePath, final VideoCompressListener listener) {

    String VIDEO_STORAGE_DIR = Environment.getExternalStorageDirectory() + "/com.shuashuakan.android/";

    File folder = new File(VIDEO_STORAGE_DIR);
    if (!folder.isDirectory()) {
      folder.delete();
    }
    if (!folder.exists())
      folder.mkdirs();

    String dstFilePath = VIDEO_STORAGE_DIR + "format_" + System.currentTimeMillis() + ".mp4";

    PLShortVideoTranscoder transcoder = new PLShortVideoTranscoder(this, filePath, dstFilePath);

    final PLMediaFile plMediaFile = new PLMediaFile(filePath);

    float width;
    float height;
    float contentExpectWidth = 720f;

    if (plMediaFile.getVideoWidth() >= plMediaFile.getVideoHeight() && plMediaFile.getVideoWidth() > (int) contentExpectWidth) {
      width = contentExpectWidth;
      height = contentExpectWidth / plMediaFile.getVideoWidth() * plMediaFile.getVideoHeight();
    } else {
      height = contentExpectWidth;
      width = contentExpectWidth / plMediaFile.getVideoHeight() * plMediaFile.getVideoWidth();
    }

    transcoder.transcode((int) width, (int) height, 2 * 1000 * 1000, new PLVideoSaveListener() {
      @Override
      public void onSaveVideoSuccess(final String s) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            if (s != null && listener!= null)
              listener.onSuccess(s);
          }
        });
        plMediaFile.release();
      }

      @Override
      public void onSaveVideoFailed(final int i) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            listener.onFailed(String.valueOf(i));
          }
        });

        plMediaFile.release();
      }

      @Override
      public void onSaveVideoCanceled() {
        plMediaFile.release();
      }

      @Override
      public void onProgressUpdate(float v) {
        if (v != 0)
          listener.onProgress(Float.parseFloat(String.valueOf(v * 100)));
      }
    });
  }
  private void initVideoView() {
    //mMediaController
    mVideoView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        boolean playing = mVideoView.isPlaying();
        if (playing) {
          mVideoView.pause();
          iv_play.setVisibility(View.VISIBLE);
        }
      }
    });
    iv_play.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mVideoView.start();
        iv_play.setVisibility(View.GONE);
      }
    });
  }


  @Override
  public void onStart() {
    // Play Video
    mVideoView.setVideoPath(video_path);
    mVideoView.start();
    super.onStart();
  }

  @Override
  public void onPause() {
    // Stop video when the activity is pause.
    mPositionWhenPaused = mVideoView.getCurrentPosition();
    mVideoView.stopPlayback();

   // trimmerBar.onPause();
    super.onPause();
  }

  @Override
  protected void onDestroy() {
    //mMediaController = null;
    mVideoView.stopPlayback();
    mVideoView = null;
    iv_play = null;
    //trimmerBar.onDestroy();
    super.onDestroy();
  }

  @Override
  public void onResume() {
    // Resume video player
    if (mPositionWhenPaused >= 0) {
      mVideoView.seekTo(mPositionWhenPaused);
      mPositionWhenPaused = -1;
    }

   // trimmerBar.onResume();
    super.onResume();
  }

  @Override
  public boolean onError(MediaPlayer player, int arg1, int arg2) {
    return false;
  }

  @Override
  public void onCompletion(MediaPlayer mp) {
    if (null != iv_play) {
      iv_play.setVisibility(View.VISIBLE);
    }

  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.picture_left_back) {
      finish();
    } else if (id == R.id.iv_play) {
      mVideoView.start();
      iv_play.setVisibility(View.INVISIBLE);
    }
  }

  @Override
  protected void attachBaseContext(Context newBase) {
    super.attachBaseContext(new ContextWrapper(newBase) {
      @Override
      public Object getSystemService(String name) {
        if (Context.AUDIO_SERVICE.equals(name)) {
          return getApplicationContext().getSystemService(name);
        }
        return super.getSystemService(name);
      }
    });
  }

  @Override
  public void onPrepared(MediaPlayer mp) {
    mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
      @Override
      public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
          // video started
          mVideoView.setBackgroundColor(Color.TRANSPARENT);
          return true;
        }
        return false;
      }
    });
  }
}
