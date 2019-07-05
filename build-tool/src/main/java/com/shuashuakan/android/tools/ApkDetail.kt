package com.shuashuakan.android.tools

import com.meituan.android.walle.ChannelReader
import net.dongliu.apk.parser.ApkFile
import java.nio.file.Path

@Suppress("MemberVisibilityCanPrivate")
class ApkDetail(
    val apkFile: ApkFile,
    val manifestMetadata: Map<String, String>,
    val zipExtraInfo: Map<String, String>
) {
  companion object {
    const val TINKER_ID = "TINKER_ID"
    fun parse(path: Path): ApkDetail {
      val apkFile = ApkFile(path.toFile())
      val metadata = ManifestParser().apply { parse(apkFile.manifestXml) }.metaData
      val extraMap = ChannelReader.getMap(path.toFile()) ?: mapOf()
      return ApkDetail(apkFile, metadata, extraMap)
    }
  }


  fun hasTinkerId(): Boolean {
    return manifestMetadata.containsKey(TINKER_ID)
  }

  fun dump() {
    val metadata = apkFile.apkMeta
    println("APK Summary")
    println("==================================================")
    printKeyAndValue("package", metadata.packageName)
    printKeyAndValue("version name", metadata.versionName)
    printKeyAndValue("version code", metadata.versionCode)
    printKeyAndValue("min version code", metadata.minSdkVersion)
    printKeyAndValue("target version code", metadata.targetSdkVersion)
    printKeyAndValue("tinker id", manifestMetadata[TINKER_ID]!!)
    zipExtraInfo.forEach { key, value ->
      printKeyAndValue(key, value)
    }
    println("==================================================")
  }

  private fun printKeyAndValue(key: String, value: Any) {
    println("${key.fixLength(20).green()}: $value")
  }
}