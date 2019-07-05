package com.shuashuakan.android.utils

import android.util.Log
import com.shuashuakan.android.BuildConfig

val defaultTag = "SSKLogger"
fun e(tag:String,msg:String){
    if (BuildConfig.DEBUG)
    Log.e(tag,msg)
}

fun e(msg:String){
    if (BuildConfig.DEBUG)
    Log.e(defaultTag,msg)
}
