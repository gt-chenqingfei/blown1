package com.shuashuakan.android.tools

import okio.Okio
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.ProcessBuilder.Redirect
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

// colorful console output
//  https://stackoverflow.com/questions/5762491/how-to-print-color-in-console-using-system-out-println
private const val ANSI_RESET = "\u001B[0m"
private const val ANSI_BLACK = "\u001B[30m"
private const val ANSI_RED = "\u001B[31m"
private const val ANSI_GREEN = "\u001B[32m"
private const val ANSI_YELLOW = "\u001B[33m"
private const val ANSI_BLUE = "\u001B[34m"
private const val ANSI_PURPLE = "\u001B[35m"
private const val ANSI_CYAN = "\u001B[36m"
private const val ANSI_WHITE = "\u001B[37m"

fun String.red() = "$ANSI_RED$this$ANSI_RESET"
fun String.green() = "$ANSI_GREEN$this$ANSI_RESET"
fun String.yellow() = "$ANSI_YELLOW$this$ANSI_RESET"

fun Path.absPath(): Path {
  return if (this.toString().startsWith("~" + File.separator)) {
    val homePath = System.getProperty("user.home")
    return Paths.get(this.toString().replace("~", homePath))
  } else {
    this.toAbsolutePath()
  }
}

fun Throwable.stackTrackAsString(): String {
  val stringWriter = StringWriter()
  printStackTrace(PrintWriter(stringWriter))
  return stringWriter.toString()
}

fun String.fixLength(length: Int = 15): String {
  return if (length > 0) {
    this.padEnd(length, ' ')
  } else {
    this
  }
}

fun Path.isApkFile(): Boolean {
  val file = this.absPath().toFile()
  if (file.exists() && file.isFile) {
    return file.extension == "apk"
  }
  return false
}

fun Path.existsAndIsFile(): Boolean {
  val file = this.toFile()
  return file.exists() && file.isFile
}

fun Path.existsAndIsDir(): Boolean {
  val file = this.toFile()
  return file.exists() && file.isDirectory
}

fun String?.valueOfDefault(d: String): String {
  return this ?: d
}

fun List<String>.run(): Boolean {
  val process = ProcessBuilder()
      .redirectError(Redirect.INHERIT)
      .command(toList())
      .start()
  return process.waitFor() == 0
}

fun List<String>.runThenGet(timeout: Long = 3000): String {
  val process = ProcessBuilder()
      .command(this)
      .redirectOutput(Redirect.PIPE)
      .start().apply {
    waitFor(timeout, TimeUnit.MILLISECONDS)
  }
  val output = Okio.buffer(Okio.source(process.inputStream))
  return output.readByteString().utf8()
}