package com.shuashuakan.android.modules.comment

import android.content.Context
import com.shuashuakan.android.commons.di.AppContext
import com.shuashuakan.android.data.api.model.comment.CommentListResp
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.exts.mvp.ApiError.HttpError
import com.shuashuakan.android.exts.mvp.ApiPresenter
import io.reactivex.Observable
import javax.inject.Inject

class VideoCommentPresenter @Inject constructor(
    @AppContext val context: Context,
    private val apiService: ApiService
) : ApiPresenter<CommentApiView<CommentListResp>, CommentListResp>(context) {

  private var count: Int = 0
  private var commentCursor: CommentListResp.CommentCursor? = null
  private var isWithHotComments: Boolean = false

  private var targetId: String = ""

  private var targetType: String = ""

  override fun onSuccess(data: CommentListResp) {
    view.showData(data, commentCursor)
  }

  override fun getObservable(): Observable<CommentListResp> {
    return apiService.getCommentList(targetId, targetType, commentCursor?.maxId, commentCursor?.sinceId, count, isWithHotComments)
  }

  fun requestApi(targetId: String,
                 targetType: String,
                 count: Int = 20,
                 commentCursor: CommentListResp.CommentCursor?,
                 isWithHotComments: Boolean = false) {
    this.targetId = targetId
    this.targetType = targetType
    this.count = count
    this.commentCursor = commentCursor
    this.isWithHotComments = isWithHotComments
    subscribe()
  }

  override fun onHttpError(httpError: HttpError) {
    super.onHttpError(httpError)
    view.showError()
  }

  override fun onNetworkError(throwable: Throwable) {
    super.onNetworkError(throwable)
    view.showError()
  }

  override fun onUnExpectedError(throwable: Throwable) {
    super.onUnExpectedError(throwable)
    view.showError()
  }
}

interface CommentApiView<in DATA> : com.shuashuakan.android.exts.mvp.ApiView {
  fun showData(data: DATA, commentCursor: CommentListResp.CommentCursor?)
  fun showError()
}