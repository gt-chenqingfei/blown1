package com.shuashuakan.android.tools

import com.shuashuakan.android.tools.RunnableCommand.InvalidCommandException
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import java.io.File
import java.nio.file.Path


@Suppress("MemberVisibilityCanPrivate")
@Command(name = "apk-upload",
    header = ["上传 apk 文件到七牛"])
class ApkUploadCommand : RunnableCommand {
  @Option(names = ["-h", "--help"], usageHelp = true,
      description = ["display this help message"])
  var usageHelpRequested: Boolean = false

  @Parameters(paramLabel = "<files>", description = ["the files to upload"])
  var files: ArrayList<File> = ArrayList()

  @Option(names = ["--folder"], description = ["upload all apk files in given folder"])
  val apkPath: Path? = null

  override fun validate(): Boolean {
    if (!usageHelpRequested) {
      if (files.isEmpty() && apkPath == null) {
        throw InvalidCommandException(
            "Please input apk file path or folder, use --help for more information.")
      }
      files.map { it.toPath() }.forEach {
        validateArgs(it.isApkFile()) { "$it not exists or not an apk file" }
      }
      apkPath?.let {
        validateArgs(it.existsAndIsDir()) { "$it exists or not a directory" }
      }
    }
    return true
  }

  override fun run() {
    if (usageHelpRequested) {
      CommandLine.usage(this, System.out)
    } else {
      if (files.isEmpty() && apkPath == null) {
        CommandLine.usage(this, System.out)
      } else {
        ApkUploader(this).start()
      }
    }
  }
}