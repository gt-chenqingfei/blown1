package com.shuashuakan.android.tools

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import java.nio.file.Path

@Suppress("MemberVisibilityCanPrivate")
@Command(name = "apk-dump", header = ["Dump apk 信息"])
class ApkDumpCommand : RunnableCommand {
  @Parameters(paramLabel = "<files>", description = ["the files to upload"])
  var files: ArrayList<Path> = ArrayList()

  @Option(names = ["-h", "--help"], usageHelp = true,
      description = ["display this help message"])
  var usageHelpRequested: Boolean = false

  override fun validate(): Boolean {
    if (usageHelpRequested) return true
    files.forEach {
      validateArgs(it.isApkFile()) { "$it not exists or not an apk file" }
    }
    return true
  }

  override fun run() {
    if (usageHelpRequested) {
      CommandLine.usage(this, System.out)
    } else {
      files.map { ApkDetail.parse(it) }
          .forEach { it.dump() }
    }
  }
}

