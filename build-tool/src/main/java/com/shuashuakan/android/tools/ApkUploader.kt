package com.shuashuakan.android.tools

import com.qiniu.common.Zone
import com.qiniu.storage.Configuration
import com.qiniu.storage.UploadManager
import com.qiniu.storage.persistent.FileRecorder
import com.qiniu.util.Auth
import com.qiniu.util.StringMap
import okio.Okio
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.function.Supplier

class ApkUploader(private val cmd: ApkUploadCommand) {
  private val executor = Executors.newFixedThreadPool(16)
  private val failedList = Collections.synchronizedList(mutableListOf<Failure>())
  private val successList = Collections.synchronizedList(mutableListOf<Success>())
  private val localTempDir = File(Paths.get("").toAbsolutePath().toFile(),
      "qiniu_tmp").apply { mkdirs() }

  private val uploadManager by lazy {
    val cfg = Configuration(Zone.autoZone())
    val fileRecorder = FileRecorder(localTempDir)
    UploadManager(cfg, fileRecorder)
  }
  private val auth by lazy { Auth.create(ACCESS_KEY, SECRET_KEY) }

  fun start() {
    val files = prepareApkFiles()
    val totalSize = files.size
    if (files.isEmpty()) {
      println("Can't find any apk files".red())
    } else {
      println("---> $totalSize apk file(s) will be uploaded <---".green())
      val tasks = files.map {
        UploadWorker(uploadManager, it) { key ->
          // insertOnly == 1 表示不允许覆盖上传
          val policy = StringMap()
//              .put("insertOnly", 1)
          auth.uploadToken(BUCKET, key, 3600L, policy)
        }
      }.map {
        CompletableFuture.supplyAsync(it, executor)
            .thenAccept { handleResult(totalSize, it) }
            .exceptionally { throw RuntimeException("Unexpected error", it) }
      }

      CompletableFuture.allOf(*tasks.toTypedArray()).whenComplete { _, _ ->
        // print log
        println("\nSUMMARY".green())
        println("===================================================")
        println("Total    : $totalSize")
        println("Success  : ${successList.size}")
        println("Failed   : ${failedList.size}")
        println("===================================================")
        localTempDir.deleteRecursively()
        executor.shutdown()
        dumpLogging()
      }
    }
  }

  private fun handleResult(size: Int, result: Result) {
    when (result) {
      is Success -> {
        successList.add(result)
        val msg = "SUCCESS(${successList.size}/$size): ".fixLength(18).green()
        println("$msg ${result.path}")
      }
      is Failure -> {
        failedList.add(result)
        val msg = "FAILURE: ".fixLength(18).red()
        println("$msg\n${result.msg}")
        result.ex?.printStackTrace()
      }
    }
  }

  private fun dumpLogging() {
    successList.sortBy { it.key }
    if (successList.isNotEmpty()) {
      val successLog = File("success_${System.currentTimeMillis()}.txt")
      Okio.buffer(Okio.sink(successLog)).use {
        successList.forEach { s ->
          it.writeUtf8(s.path.fileName.toString())
          it.writeUtf8("\n")
          it.writeUtf8("$BASE_URL/${s.key}")
          it.writeUtf8("\n\n")
        }
      }
      println("successful log: ${successLog.absolutePath}")
    }

    if (failedList.isNotEmpty()) {
      val failedLogging = File("failed_${System.currentTimeMillis()}.txt")
      Okio.buffer(Okio.sink(failedLogging)).use {
        failedList.forEach { f ->
          it.writeUtf8(f.path.toString())
          it.writeUtf8("\n")
          val stackTrace = f.ex?.stackTrackAsString().orEmpty()
          it.writeUtf8(stackTrace)
          it.writeUtf8("\n\n")
        }
      }
      println("failure log: ${failedLogging.absolutePath}")
    }
  }

  private fun prepareApkFiles(): List<Path> {
    println("Scanning files...".green())
    val apkFiles = cmd.apkPath?.toFile()?.walk()?.filter { it.isFile && it.extension == "apk" }
        ?.map { it.toPath().absPath() }?.toMutableList() ?: mutableListOf()
    return apkFiles.apply {
      addAll(cmd.files.map { it.toPath().absPath() })
    }.toList()
  }

  companion object {
    private const val ACCESS_KEY = "UaogL1rgdbLDkKN60_j_3YTC7WVOxlCj0g9T9wMl"
    private const val SECRET_KEY = "YCywVyNyP1xgHPGPDyc2PT1pPDdRsHC8pr2rDsAn"
    private const val BUCKET = "ricebook-apk"
    private const val BASE_URL = "http://apk.ricebook.com"

    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
    // We want at least 2 threads and at most 4 threads in the core pool,
    // preferring to have 1 less than the CPU count to avoid saturating
    // the CPU with background work
    private val CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4))
    private val MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1
    private val KEEP_ALIVE_SECONDS = 30L
  }

  private class UploadWorker(private val uploadManager: UploadManager,
                             private val path: Path,
                             private val tokenProvider: (String) -> String) : Supplier<Result> {

    override fun get(): Result {
      val key = path.fileName.toString()
      return try {
        val uploading = "UPLOADING: ".fixLength(18).green()
        println("$uploading$path @${Thread.currentThread().name}")
        val response = uploadManager.put(path.toFile(), key, tokenProvider(key))
        if (response.isOK) {
          Success(key, path)
        } else {
          Failure(response.toString(), null, path)
        }
      } catch (e: Exception) {
        Failure(e.message ?: "upload error", e, path)
      }
    }
  }
}

sealed class Result
data class Success(val key: String, val path: Path) : Result()
data class Failure(val msg: String, val ex: Exception? = null, val path: Path) : Result()

