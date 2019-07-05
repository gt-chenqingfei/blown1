package com.shuashuakan.android.tools

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.nio.file.Path

@Suppress("MemberVisibilityCanPrivate")
@Command(name = "apk-patch",
    header = ["打补丁包 by Tinker"])
class ApkPatchCommand : RunnableCommand {

  @Option(names = ["--apk"], description = ["base apk used by tinker to build path files"])
  var apkPath: Path? = null

  @Option(names = ["--symbol"], description = ["path to R.txt file"])
  var symbolPath: Path? = null

  @Option(names = ["--proguard"], description = ["path to proguard mapping file, eg: mapping.txt"])
  var proguardMappingPath: Path? = null

  @Option(names = ["-h", "--help"], usageHelp = true,
      description = ["display this help message"])
  var usageHelpRequested: Boolean = false

  override fun validate(): Boolean {
    if (usageHelpRequested) return true
    validateArgs(apkPath != null) { "Please set apk file by --apk" }
    validateArgs(symbolPath != null) { "Please set symbol file by --symbol" }
    validateArgs(proguardMappingPath != null) { "Please set proguard mapping file by --proguard" }
    validateArgs(apkPath!!.isApkFile()) { "$apkPath not exists or not an apk file" }
    validateArgs(symbolPath!!.existsAndIsFile()) { "$symbolPath not exists or not a file" }
    validateArgs(
        proguardMappingPath!!.existsAndIsFile()) { "$proguardMappingPath not exists or not a file" }

    return true
  }

  override fun run() {
    if (usageHelpRequested) {
      CommandLine.usage(this, System.out)
    } else {
      ApkPatcher(this).patch()
    }
  }
}