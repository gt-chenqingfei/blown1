package com.shuashuakan.android.utils.download

interface DownloadListener {
  fun downloadStart()
  fun downloadProgress(percent: Int)
  fun downloadSucceeded(path: String)
  fun downloadCanceled()
  fun downloadFailed()
}