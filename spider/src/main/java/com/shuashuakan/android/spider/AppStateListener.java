package com.shuashuakan.android.spider;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by twocity on 18/04/2017.
 */
public interface AppStateListener {
  int FOREGROUND = 100;
  int BACKGROUND = 200;
  int BACKTOFORE = 404;

  @Retention(RetentionPolicy.SOURCE) @IntDef({ FOREGROUND, BACKGROUND,BACKTOFORE }) @interface AppState {
  }

  void onAppStateChanged(@AppState int state);
}
