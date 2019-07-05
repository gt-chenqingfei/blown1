package com.shuashuakan.android.tools

import com.meituan.android.walle.ChannelWriter
import okio.Okio
import java.io.File
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.function.Supplier

class ApkChannelBuilder(private val cmd: ApkChannelCommand) {
  private val cpuCount = Runtime.getRuntime().availableProcessors()
  private val executor = Executors.newFixedThreadPool(cpuCount * 2 + 1)

  fun build() {
    val apk = cmd.apkPath!!
    ApkDetail.parse(apk).dump()

    // starting
    val extras = mapOf("build_time" to System.currentTimeMillis().toString())
    val futures = parseChannels().map {
      CompletableFuture.supplyAsync(WriteApkInfoSupplier(
          outputDir = cmd.outputPath!!.toFile(),
          originFile = apk.toFile(),
          channel = it,
          extra = extras
      ), executor)
    }
    CompletableFuture.allOf(*futures.toTypedArray())
        .thenAccept {
          println("DONE ^_^".green())
          executor.shutdown()
        }
        .exceptionally { throw RuntimeException(it) }
  }

  private fun parseChannels(): List<String> {
    val buffer = Okio.buffer(Okio.source(cmd.channelPath!!))
    val channels = mutableListOf<String>()
    while (!buffer.exhausted()) {
      val line = buffer.readUtf8Line()
      if (line != null && !line.isNullOrBlank()) {
        channels.add(line)
      }
    }
    return channels.
        filter { it.matches(Regex("^[a-zA-Z0-9_.-]*\$")) }
        .toList()
  }

  private class WriteApkInfoSupplier(private val outputDir: File, private val originFile: File,
      private val channel: String,
      val extra: Map<String, String>) : Supplier<Path> {
    override fun get(): Path {
      outputDir.mkdirs()
      val nameWithoutExtension = originFile.nameWithoutExtension
      val extension = originFile.extension
      val newFile = File(outputDir, "$nameWithoutExtension-$channel.$extension")
      if (!newFile.exists()) {
        originFile.copyTo(newFile, overwrite = false)
        ChannelWriter.put(newFile, channel, extra)
        println("$newFile generated".green())
      } else {
        println("$newFile already exists, skipped".yellow())
      }
      return newFile.toPath()
    }
  }
}