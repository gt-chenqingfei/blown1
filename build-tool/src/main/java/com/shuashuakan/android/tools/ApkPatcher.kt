package com.shuashuakan.android.tools

import com.shuashuakan.android.tools.ApkDetail.Companion.TINKER_ID
import java.nio.file.Path

class ApkPatcher(private val cmd: ApkPatchCommand) {

  fun patch() {
    val apkDetail = ApkDetail.parse(cmd.apkPath!!)
    if (!apkDetail.hasTinkerId()) {
      println("Can't find $TINKER_ID in AndroidManifest.xml".red())
    } else {
      apkDetail.dump()
      doPatch(cmd.apkPath!!, cmd.symbolPath!!, cmd.proguardMappingPath!!)
    }
  }

  private fun doPatch(apk: Path, rTxt: Path, proguard: Path) {
    val commands = mutableListOf<String>()
    with(commands) {
      add("./gradlew")
      add("-PenableTinker=true")
      add("-PpatchMode=true")
      add("-PbaseApk=$apk")
      add("-PbaseApkResourceMapping=$rTxt")
      add("-PbaseApkProguardMapping=$proguard")
      add("-PtinkerId=${buildTinkerId()}")
      add("clean")
      add("buildTinkerPatchRelease")
    }
    println("starting run command:\n${commands.joinToString(separator = " \\\n").green()}")
    if (commands.run()) {
      println("Patch done, see app/build/output/patch".green())
    } else {
      println("Patch failed, please make sure you are at root project of fish".red())
    }
  }

  private fun buildTinkerId(): String {
    val gitSha = listOf("git", "rev-parse", "--short", "HEAD").runThenGet().trim()
    return "fish_${gitSha}_${System.currentTimeMillis()}_patch"
  }
}