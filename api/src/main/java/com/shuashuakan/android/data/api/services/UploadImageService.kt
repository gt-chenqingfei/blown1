package com.shuashuakan.android.data.api.services

import com.shuashuakan.android.data.api.model.UploadResult
import io.reactivex.Observable
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Author:  lijie
 * Date:   2018/12/25
 * Email:  2607401801@qq.com
 */
interface UploadImageService {
  @POST("v1/upload/upload.json") @Multipart
  fun updateProfilePic(@Part imagePart: MultipartBody.Part, @Part typePart: MultipartBody.Part)
      : Observable<UploadResult>
}