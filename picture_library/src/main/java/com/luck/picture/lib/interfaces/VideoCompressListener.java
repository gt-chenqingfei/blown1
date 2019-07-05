package com.luck.picture.lib.interfaces;

/**
 * Author:  lijie
 * Date:   2018/11/24
 * Email:  2607401801@qq.com
 */
public interface VideoCompressListener {
  void onProgress(float progress);

  void onSuccess(String scaleFilePath);

  void onFailed(String errorMsg);
}
