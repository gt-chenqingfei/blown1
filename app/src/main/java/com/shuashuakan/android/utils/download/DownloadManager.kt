package com.shuashuakan.android.utils.download

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Message
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.download.DownloadTask
import com.shuashuakan.android.R
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.event.SaveVideoEvent
import com.shuashuakan.android.modules.widget.dialogs.DownloadProgressDialog
import com.shuashuakan.android.utils.toastCustomText
import timber.log.Timber
import java.io.File

/**
 * 下载管理类
 * https://github.com/AriaLyy/Aria
 *
 * Author: ZhaiDongyang
 * Date: 2019/2/16
 */
open class DownloadManager(
        val context: Context,
        val path: String,
        val url: String,
        val downloadProgressDialog: DownloadProgressDialog,
        val feedId: String?
) :Handler.Callback{
    override fun handleMessage(msg: Message?): Boolean {
        downloadProgressDialog.dismiss()
        toastCustomText(context, context.getString(R.string.string_download_failed))
        cancelTask()
        return true
    }

    companion object {
        // 一个常量表示一个页面需要显式提供下载功能
        const val DOWNLOAD_TAG_FRAGMENT_CHAINSFEED = "download_tag_fragment_chainsfeed"
        const val DOWNLOAD_TAG_FRAGMENT_PROFILETIMELINE = "download_tag_fragment_profiletimeline"
        const val DOWNLOAD_TAG_FRAGMENT_MULTITYPE = "download_tag_fragment_multitype"
        const val DOWNLOAD_TAG_FRAGMENT_SPECIAL_VIDEO = "download_tag_fragment_special_video"
        const val DOWNLOAD_TIMEOUT_WHAT = 1000
    }


    //  private val downloadProgressDialog: DownloadProgressDialog = DownloadProgressDialog.progressDialog(
//      context, "下载中...", false)
    private var mPath: String = ""
    private lateinit var listener: DownloadListener
    val mainPath = Environment.getExternalStorageDirectory().path + "/DCIM/Camera/"
    val handler: Handler = Handler(this)
    fun setDownloadListener(_listener: DownloadListener) {
        listener = _listener
    }

    fun startTask() {
        handler.sendEmptyMessageDelayed(DOWNLOAD_TIMEOUT_WHAT,20*1000)
        if (url.isEmpty()) {
            toastCustomText(context, context.getString(R.string.string_video_path_error))
            return
        }
        if (mPath.isEmpty()) {
            mPath = path
        }
        val file = File(mPath)
        if (!file.exists()) {
            Aria.download(context).load(url)
                    .setDownloadPath(mPath)
                    .start()
            addDownloadSchedulerListener()
        } else {

            if (!::listener.isInitialized) {
                downloadProgressDialog.dismiss()
                handler.removeMessages(DOWNLOAD_TIMEOUT_WHAT)
                Timber.e("VideoChainAdapter -dm 1- 视频已保存至DCIM/Camera文件中  dig = $downloadProgressDialog")
                toastCustomText(context, String.format(context.getString(R.string.string_video_save_format), "DCIM/Camera"))
            }
        }
    }

    private fun getVideoName(url: String): String {
        val urlSplit = url.split("/")
        val lastUrlSplit = urlSplit[urlSplit.size - 1]
        return "$mainPath$lastUrlSplit.mp4"
    }

    fun cancelTask() {
        handler.removeMessages(DOWNLOAD_TIMEOUT_WHAT)
        Aria.download(context).load(url).cancel()
        Aria.download(context).removeSchedulerListener()
        //          Aria.download(context).removeAllTask()
    }

    private fun addDownloadSchedulerListener() {

        val listener = object : Aria.DownloadSchedulerListener() {

            override fun onTaskPre(task: DownloadTask) {
                super.onTaskPre(task)
                if (task.key == url) {
                    if (::listener.isInitialized) {
                        listener.downloadStart()
                    } else {
//            downloadProgressDialog.show()
                    }
                }
            }

            override fun onTaskRunning(task: DownloadTask) {
                super.onTaskRunning(task)
                Timber.e("onTaskRunning")
                if (task.key == url) {
                    if (::listener.isInitialized) {
                        listener.downloadProgress(task.percent)
                    } else {
                        downloadProgressDialog.setCircleProgressBarPercent(task.percent)
                    }
                }
            }

            override fun onTaskComplete(task: DownloadTask) {
                super.onTaskComplete(task)
                Timber.e("onTaskComplete")

                if (task.key == url) {
                    // 通知图库更新
                    val file = File(mPath)
                    context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            Uri.fromFile(File(file.path))))
                    if (::listener.isInitialized) {
                        downloadProgressDialog.dismiss()
                        handler.removeMessages(DOWNLOAD_TIMEOUT_WHAT)
                        listener.downloadSucceeded(mPath)
                    } else {
                        downloadProgressDialog.dismiss()
                        handler.removeMessages(DOWNLOAD_TIMEOUT_WHAT)
                        Timber.e("VideoChainAdapter -dm 222- 视频已保存至DCIM/Camera文件中 dig = $downloadProgressDialog")
                        toastCustomText(context, String.format(context.getString(R.string.string_video_save_format), "DCIM/Camera"))
                        RxBus.get().post(SaveVideoEvent(true, feedId))
                    }
                    cancelTask()
                }
            }

            override fun onTaskCancel(task: DownloadTask) {
                super.onTaskCancel(task)
                Timber.e("onTaskCancel")
                if (task.key == url) {
                    cancelTask()
                    if (::listener.isInitialized) {
                        listener.downloadCanceled()
                    } else {
                        downloadProgressDialog.dismiss()
                        handler.removeMessages(DOWNLOAD_TIMEOUT_WHAT)
                        RxBus.get().post(SaveVideoEvent(false, feedId))
                    }
                    cancelTask()
                }
            }

            override fun onTaskFail(task: DownloadTask) {
                super.onTaskFail(task)
                Timber.e("onTaskFail")
                if (task.key == url) {
                    if (::listener.isInitialized) {
                        listener.downloadFailed()
                    } else {
                        downloadProgressDialog.dismiss()
                        handler.removeMessages(DOWNLOAD_TIMEOUT_WHAT)
                        RxBus.get().post(SaveVideoEvent(false, feedId))
                    }
                    cancelTask()
                }
            }
        }
        Aria.download(context).addSchedulerListener(listener)
    }


}