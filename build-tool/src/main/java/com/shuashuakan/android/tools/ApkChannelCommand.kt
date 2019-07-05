package com.shuashuakan.android.tools

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.nio.file.Path

@Suppress("MemberVisibilityCanPrivate")
@Command(name = "apk-channel", header = ["多渠道打包"])
class ApkChannelCommand : RunnableCommand {
  @Option(names = ["--apk"], description = ["base apk used by walle to build multi channel files"])
  var apkPath: Path? = null

  @Option(names = ["--channel"], description = ["apk channel file"])
  var channelPath: Path? = null

  @Option(names = ["--output"], description = ["output path to store generated apk files"])
  var outputPath: Path? = null

  @Option(names = ["-h", "--help"], usageHelp = true,
      description = ["display this help message"])
  var usageHelpRequested: Boolean = false

  override fun validate(): Boolean {
    if (usageHelpRequested) return true
    validateArgs(apkPath != null) { "Please set apk file by --apk" }
    validateArgs(channelPath != null) { "Please set channel file by --channel" }
    validateArgs(outputPath != null) { "Please set output directory by --outputPath" }
    validateArgs(apkPath!!.isApkFile()) { "$apkPath not exists or not an apk file" }
    validateArgs(channelPath!!.existsAndIsFile()) { "$channelPath not exists or not a file" }
    validateArgs(
        outputPath!!.existsAndIsDir()) { "$outputPath not exists or not a file" }

    return true
  }

  override fun run() {
    if (usageHelpRequested) {
      CommandLine.usage(this, System.out)
    } else {
      ApkChannelBuilder(this).build()
    }
  }
}