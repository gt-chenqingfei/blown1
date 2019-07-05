package com.shuashuakan.android.data.api.model.chain

import android.os.Parcel
import android.os.Parcelable

/**
 * Author:  lijie
 * Date:   2019/1/9
 * Email:  2607401801@qq.com
 */
class GifModel (val path:String,
                val width:Int,
                val height:Int):Parcelable{
  constructor(parcel: Parcel) : this(
      parcel.readString()?:"",
      parcel.readInt(),
      parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeString(path)
    parcel.writeInt(width)
    parcel.writeInt(height)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<GifModel> {
    override fun createFromParcel(parcel: Parcel): GifModel {
      return GifModel(parcel)
    }

    override fun newArray(size: Int): Array<GifModel?> {
      return arrayOfNulls(size)
    }
  }

}