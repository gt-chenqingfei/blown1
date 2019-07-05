package com.shuashuakan.android.event

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/12/01
 * Description:
 */
data class UploadQiniuProgressEvent(val progress: Int,var source:Int,val videoPath:String?=null)
