package com.shuashuakan.android.data.api.model.home

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.KotshiConstructor

@JsonSerializable
data class EditVideoResult @KotshiConstructor constructor(
    @Json(name = "cover_url")
    val coverUrl: String?,
    @Json(name = "is_success")
    val isSuccess: Boolean?

) : Parcelable {
  constructor(source: Parcel) : this(
      source.readString(),
      source.readValue(Boolean::class.java.classLoader) as Boolean?
  )

  override fun describeContents() = 0

  override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
    writeString(coverUrl)
    writeValue(isSuccess)
  }

  companion object {
    @JvmField
    val CREATOR: Parcelable.Creator<EditVideoResult> = object : Parcelable.Creator<EditVideoResult> {
      override fun createFromParcel(source: Parcel): EditVideoResult = EditVideoResult(source)
      override fun newArray(size: Int): Array<EditVideoResult?> = arrayOfNulls(size)
    }
  }
}


