package com.shuashuakan.android.data.api.services

import com.shuashuakan.android.data.api.model.UploadToken
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface UploadService {

  /*
  * 获取上传token
  */
  @GET("mhw/v1/upload/qiniu_token.json")
  fun getUploadToken(@Query("type") type:String,@Query("cover_timestamp")cover_timestamp:Long?=3000): Observable<UploadToken>
}