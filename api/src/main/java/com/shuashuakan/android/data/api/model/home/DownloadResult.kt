package com.shuashuakan.android.data.api.model.home

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.KotshiConstructor


@JsonSerializable
data class DownloadResult @KotshiConstructor constructor(
    @Json(name = "download_url")
    val downloadUrl: String) : Parcelable {
  constructor(source: Parcel) : this(
      source.readString()
  )

  override fun describeContents() = 0

  override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
    writeString(downloadUrl)
  }

  companion object {
    @JvmField
    val CREATOR: Parcelable.Creator<DownloadResult> = object : Parcelable.Creator<DownloadResult> {
      override fun createFromParcel(source: Parcel): DownloadResult = DownloadResult(source)
      override fun newArray(size: Int): Array<DownloadResult?> = arrayOfNulls(size)
    }
  }
}

