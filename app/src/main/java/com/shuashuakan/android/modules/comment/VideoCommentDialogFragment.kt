package com.shuashuakan.android.modules.comment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Typeface
import android.media.ThumbnailUtils
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.*
import android.view.View.OVER_SCROLL_NEVER
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.net.toUri
import com.facebook.drawee.view.SimpleDraweeView
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.shuashuakan.android.FishInjection
import com.shuashuakan.android.R
import com.shuashuakan.android.commons.cache.Storage
import com.shuashuakan.android.commons.util.ACache
import com.shuashuakan.android.config.AppConfig
import com.shuashuakan.android.constant.Constants
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.account.UserAccount
import com.shuashuakan.android.data.api.model.chain.GifModel
import com.shuashuakan.android.data.api.model.comment.*
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.event.FeedCommentDismissEvent
import com.shuashuakan.android.event.FeedRefreshCountEvent
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.service.PullService
import com.shuashuakan.android.spider.Spider
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.modules.ACCOUNT_PAGE
import com.shuashuakan.android.modules.profile.fragment.ProfileFragment
import com.shuashuakan.android.modules.profile.UserProfileActivity
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.modules.share.ShareConfig
import com.shuashuakan.android.modules.share.ShareHelper
import com.shuashuakan.android.modules.widget.SpiderAction
import com.shuashuakan.android.modules.widget.dialogs.BindPhoneDialog
import com.shuashuakan.android.utils.*
import io.reactivex.disposables.CompositeDisposable
import java.util.*
import javax.inject.Inject


/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/08/08
 * Description:
 */
class VideoCommentDialogFragment : BottomSheetDialogFragment(), CommentApiView<CommentListResp> {

    //  private val closeView by bindView<ImageView>(R.id.comment_close)
//  private val commentTitle by bindView<TextView>(R.id.comment_title)
    //  private val commentHUser by bindView<SimpleDraweeView>(R.id.comment_header_user)
//  private val commentHTotal by bindView<TextView>(R.id.comment_header_total)
    private val commentList by bindView<RecyclerView>(R.id.comment_list)
    private val commentAt by bindView<View>(R.id.comment_at_iv)
    val addCommentTv by bindView<TextView>(R.id.add_comment_tv)
    private val addCommentL by bindView<LinearLayout>(R.id.add_comment_l)
    private val addCommentIv by bindView<ImageView>(R.id.add_comment_iv)
    private val selMediaL by bindView<FrameLayout>(R.id.video_media_layout)
    private val sendCommentTv by bindView<TextView>(R.id.send_comment_tv)
    private val selMediaIv by bindView<SimpleDraweeView>(R.id.sel_media_iv)
    private val addCommentEmjLayout by bindView<LinearLayout>(R.id.comment_emoji_layout)
    private val errorView by bindView<View>(R.id.comment_error)
    private val emptyView by bindView<View>(R.id.comment_empty)
    private val loadingView by bindView<View>(R.id.comment_loading)

    @Inject
    lateinit var presenter: VideoCommentPresenter
    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var accountManager: AccountManager
    @Inject
    lateinit var shareHelper: ShareHelper

    lateinit var listAdapter: CommentListAdapter

    private lateinit var addCommentPopup: AddCommentPopup

    private var countListener: OnCommentCountListener? = null
    private var gifModel: GifModel? = null

    fun setCountListener(countListener: OnCommentCountListener) {
        this.countListener = countListener
    }

    @Inject
    lateinit var spider: Spider

    private var emojiList = arrayOf(0x1F602, 0x1F61A, 0x1F64C, 0x1F525, 0x26FD, 0x1F60D, 0x1F630, 0x1F621)

    private var shareDialog: CommentShareDialog? = null
    private val compositeDisposable = CompositeDisposable()
    private var userAccount: UserAccount? = null
    @Inject
    lateinit var storage: Storage

    private lateinit var defaultHint: String

    private lateinit var commentListHeader: CommentListHeader

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return com.shuashuakan.android.modules.widget.FixHeightBottomSheetDialog(requireContext(), theme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        defaultHint = getString(R.string.string_luck_to_speak)
        val view = inflater.inflate(R.layout.dialog_video_comment_fragment, container, false)
        view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                requireContext().getScreenSize().y - requireContext().dip(75))
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        spider.pageTracer().reportPageCreated(this)
        if (gifModel != null) {
            addCommentPopup.showSelImage(gifModel?.path, gifModel!!.width, gifModel!!.height, CommentListAdapter.TYPE_GIF, "typeimage/gif")
        }
    }

    override fun onResume() {
        super.onResume()
        if (feedId != null)
            spider.pageTracer().reportPageShown(this, "ssr://popup/feed_comment?feed_id=" + feedId, "")
    }

    override fun onDetach() {
        super.onDetach()
        compositeDisposable.clear()
        presenter.detachView(false)
        countListener = null
        addCommentPopup.destroy()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        FishInjection.inject(this)
        presenter.attachView(this)
    }

    override fun getTheme(): Int {
        return R.style.CommentDialogTheme
    }

    companion object {
        private const val EXTRA_FEED_ID = "feedId"
        private const val EXTRA_FEED_COUNT = "count"
        private const val EXTRA_USER_ID = "count"
        private const val EXTRA_POSITION = "position"
        private const val EXTRA_GIF = "gif"

        private const val REQUEST_TYPE_REFRESH = 1
        private const val REQUEST_TYPE_LOAD_MORE = 2
        private const val REQUEST_TYPE_LOAD_REPLY = 3

        fun create(feedId: String, count: Int?, userId: String, position: Int?, gifModel: GifModel? = null): VideoCommentDialogFragment {
            val fragment = VideoCommentDialogFragment()
            val args = Bundle()
            args.putString(EXTRA_FEED_ID, feedId)
            args.putInt(EXTRA_FEED_COUNT, count ?: 0)
            args.putString(EXTRA_USER_ID, userId)
            args.putInt(EXTRA_POSITION, position ?: 0)
            args.putParcelable(EXTRA_GIF, gifModel)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var feedId: String
    private lateinit var userId: String
    private var count: Int = 0
    private var position: Int = 0

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        feedId = arguments?.getString(EXTRA_FEED_ID) ?: ""
        userId = arguments?.getString(EXTRA_USER_ID) ?: ""

        count = arguments?.getInt(EXTRA_FEED_COUNT) ?: 0

        position = arguments?.getInt(
                EXTRA_POSITION
        ) ?: 0
        gifModel = arguments?.getParcelable<GifModel>(EXTRA_GIF)


        val defaultCommentList: ArrayList<*>? = ACache.get(requireContext()).getAsObject(ACache.KEY_DEFAULT_COMMENT) as ArrayList<*>?

        if (defaultCommentList != null && !defaultCommentList.isEmpty()) {
            defaultHint = defaultCommentList[Random().nextInt(defaultCommentList.size)] as String
            addCommentTv.hint = defaultHint
        }

        listAdapter = CommentListAdapter(requireActivity(), commentList, accountManager)
        commentListHeader = CommentListHeader(this@VideoCommentDialogFragment.requireContext())
        listAdapter.setNoMore(R.layout.view_no_more)
        listAdapter.setMore(R.layout.view_more, object : RecyclerArrayAdapter.OnMoreListener {
            override fun onMoreClick() {

            }

            override fun onMoreShow() {
                loadMoreComment()
            }
        })
        listAdapter.setOperationListener(object : CommentListAdapter.OnCommentListOperationListener {
            override fun onPraise(data: ApiComment, position: Int, isSonPraise: Boolean, sonPosition: Int, onListener: ReplyListAdapter.OnListener) {
                if (accountManager.hasAccount()) {
                    praise(data, onListener)
                } else {
                    requireActivity().startActivityForResultByLink(ACCOUNT_PAGE, Constants.REQUEST_LOGIN_CODE)
                }
            }

            override fun onReplyClick(data: ApiComment, position: Int, isBtn: Boolean) {
                if (isMineSendComment(data)) {
                    //删除
                    if (!isBtn) {// 点击自己的恢复按钮无操作
                        deleteComment(data, position)
                    }
                } else {
                    //回复
                    showAddCommentPopup(addCommentTv, data, position)
                }
            }

            override fun onLoadReply(data: ApiComment) {
                loadReplyComment(data)
            }

            override fun onUserAvatarClick(data: ApiComment) {
                startActivity(Intent(context, UserProfileActivity::class.java)
                        .putExtra("id", data.author.userId.toString())
                        .putExtra("source", SpiderAction.PersonSource.COMMENT.source))
            }

            override fun onShareClick(id: Long, media: List<ApiMedia>?) {
                if (shareDialog == null) {
                    shareDialog = CommentShareDialog(requireContext(), CommentShareDialog.ShareDialogListener {
                        shareHelper.shareType = ShareConfig.SHARE_TYPE_COMMENT
                        if (media != null && media.isNotEmpty()) {
                            shareHelper.doShare(requireActivity(), null, exchangeId(it), feedId, id, media[0].mediaType)
                        } else {
                            shareHelper.doShare(requireActivity(), null, exchangeId(it), feedId, id, "txt")
                        }
                    })
                }
                if (shareDialog != null)
                    shareDialog!!.show()

            }
        })

        errorView.setOnClickListener {
            showLoadingBar()
            refreshComment()
        }

        commentList.layoutManager = LinearLayoutManager(requireContext())
        commentList.adapter = listAdapter
        commentList.overScrollMode = OVER_SCROLL_NEVER
        addCommentPopup = AddCommentPopup(requireActivity(), addCommentTv.hint.toString(), true, object : AddCommentPopup.OnCommentListener {
            override fun onReply(commentId: Long, content: String, position: Int) {
                addReply(commentId, content, position)
            }

            override fun onComment(content: String?, mediaPath: String?) {
                addComment(content, feedId, mediaPath)
                selMediaL.visibility = View.GONE
            }

            override fun onShowAlbum() {
                showAlbum()
            }

            override fun onRefreshTextView(text: String?, path: String?, replyName: String?) {
                if (TextUtils.isEmpty(text) && path == null) {
                    sendCommentTv.visibility = View.GONE
                } else {
                    sendCommentTv.visibility = View.VISIBLE
                }
                addCommentTv.text = text
                if (path != null) {
                    selMediaL.visibility = View.VISIBLE
                    selMediaIv.setImagePath(requireContext(), path, 4)
                } else {
                    selMediaL.visibility = View.GONE
                }
                if (addCommentPopup.isLastIsReply) {
                    addCommentTv.hint = "@$replyName"
                } else {
                    addCommentTv.hint = defaultHint
                }
            }
        })
        addCommentL.noDoubleClick {
            if (addCommentPopup.isLastIsReply) {
                showAddCommentPopup(addCommentTv, addCommentPopup.replyCommentId, addCommentPopup.replyAuthorName, addCommentPopup.replyPosition)
            } else {
                showAddCommentPopup(addCommentTv, null, null)
            }
        }
        addCommentIv.noDoubleClick {
            if (accountManager.hasAccount()) {
                showAlbum()
            } else {
                requireActivity().startActivityForResultByLink(ACCOUNT_PAGE, Constants.REQUEST_LOGIN_CODE)
            }
        }
        sendCommentTv.noDoubleClick {
            if (addCommentPopup.isLastIsReply) {
                addReply(addCommentPopup.replyCommentId, addCommentTv.text.toString(), addCommentPopup.replyPosition)
            } else {
                addComment(addCommentTv.text.toString(), feedId, addCommentPopup.mediaPath)
            }
        }

        listAdapter.setOnItemClickListener {
            if (it >= 0) {
                val apiComment = listAdapter.getItem(it)
                if (apiComment.id != null) {
                    if (!listAdapter.isDeleted(apiComment)) {
                        if (isMineSendComment(apiComment)) {
                            //删除
                            deleteComment(apiComment, it)
                        } else {
                            //回复
                            showAddCommentPopup(addCommentTv, apiComment, it)
                        }
                    }
                }
            }
        }

        refreshComment()

        apiService.counter(feedId).applySchedulers().subscribeApi(onNext = {
            count = it.commentNum
            refreshCommentCount()
        })
        initEmojiLayout()
    }

    private fun exchangeId(viewId: Int): String {
        when (viewId) {
            R.id.share_wechat -> return "wechat_session"
            R.id.share_moments -> return "wechat_timeline"
            R.id.share_qq -> return "QQ"
            R.id.share_qzone -> return "QZONE"
            R.id.share_copy_url -> return "copy_url"
            R.id.share_open_browser -> return "open_with_browser"
        }
        return ""
    }

    private fun initEmojiLayout() {
        for (i in 0..7) {
            val textView = TextView(context)
            textView.text = String(Character.toChars(emojiList[i]))
            textView.setTextColor(resources.getColor(R.color.comment_content_color))
            textView.gravity = Gravity.CENTER
            textView.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f)
            textView.setOnClickListener {
                if (addCommentPopup.isLastIsReply) {
                    showAddCommentPopup(addCommentTv, addCommentPopup.replyCommentId, addCommentPopup.replyAuthorName, addCommentPopup.replyPosition)
                } else {
                    showAddCommentPopup(addCommentTv, null, null)
                }
                if (accountManager.hasAccount()) {
                    addCommentPopup.addEmoji(textView.text.toString())
                }
            }
            addCommentEmjLayout.addView(textView)
        }
    }

    private fun showAlbum() {
        PictureSelector.create(this)
                .openGallery(PictureMimeType.ofImage())
                .theme(R.style.customPictureStyle)
                .imageSpanCount(3)
                .selectionMode(PictureConfig.SINGLE)
                .previewImage(true)
                .previewVideo(false)
                .isCamera(false)
                .videoQuality(1)
                .compress(true)
                .isGif(true)
                .videoMinSecond(0)
                .videoMaxSecond(15)
                .recordVideoSecond(15)
                .forResult(PictureMimeType.ofAll())
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        mDismissListener?.onDismiss()
        spider.manuallyEvent(SpiderEventNames.COMMENT_POPOVER_CLOSE_CLICK).track()
        RxBus.get().post(FeedCommentDismissEvent(feedId, position))
    }

    private fun refreshCommentCount() {

//    commentTitle.text = "评论 $count"
        RxBus.get().post(FeedRefreshCountEvent(feedId, count, position))

        if (countListener != null) {
            countListener!!.showCount(count)
        }
    }

    private fun showAddCommentPopup(view: View, apiComment: ApiComment?, position: Int?) {
        if (accountManager.hasAccount()) {
            if (apiComment != null && position != null) {
                addCommentPopup.reply(view, apiComment.id, apiComment.author.nickName, position + listAdapter.headerCount)
            } else {
                addCommentPopup.comment(view)
            }
        } else {
            requireActivity().startActivityForResultByLink(ACCOUNT_PAGE, Constants.REQUEST_LOGIN_CODE)
        }
    }

    private fun showAddCommentPopup(view: View, replyId: Long, replyName: String?, position: Int?) {
        if (accountManager.hasAccount()) {
            if (SpUtil.find(AppConfig.LOGIN_TYPE) == "WeChat" && SpUtil.find(AppConfig.PHONE_NUM).isNullOrEmpty()
                    && ACache.get(requireActivity()).getAsString(AppConfig.SHOE_BIND_PHONE) != "show") {
                BindPhoneDialog.create(BindPhoneDialog.COMMENT).show(requireActivity().supportFragmentManager, "all_chain")
                ACache.get(requireActivity()).put(AppConfig.SHOE_BIND_PHONE, "show", AppConfig.BIND_DIALOG_SAVE_TIME)
            } else {
                if (replyName != null && position != null) {
                    addCommentPopup.reply(view, replyId, replyName, position + listAdapter.headerCount)
                }
            }
        } else {
            requireActivity().startActivityForResultByLink(ACCOUNT_PAGE, Constants.REQUEST_LOGIN_CODE)
        }
    }

    private fun isMineSendComment(apiComment: ApiComment): Boolean {
        val account = accountManager.account()
        return account != null && account.userId == apiComment.author.userId
    }


    private var requestType = REQUEST_TYPE_REFRESH

    fun refreshComment() {
        requestType = REQUEST_TYPE_REFRESH
        presenter.requestApi(feedId, "FEED", 10, null, true)


    }

    fun loadMoreComment() {
        if (commentCursor != null) {
            requestType = REQUEST_TYPE_LOAD_MORE
            presenter.requestApi(feedId, "FEED", 10, commentCursor, false)
        }
    }

    private var loadReplyItemData: ApiComment? = null
    fun loadReplyComment(apiComment: ApiComment) {
        var commentCursor: CommentListResp.CommentCursor? = null
        if (apiComment.newestComments != null) {
            commentCursor = apiComment.newestComments!!.nextCursor
        }
        loadReplyItemData = apiComment
        requestType = REQUEST_TYPE_LOAD_REPLY
        presenter.requestApi(apiComment.id.toString(), "COMMENT", 10, commentCursor, false)
    }

    override fun showError() {
        if (requestType == REQUEST_TYPE_REFRESH) {
            //刷新逻辑
            ViewHelper.crossfade(errorView, emptyView, commentList, loadingView)
        } else if (requestType == REQUEST_TYPE_LOAD_MORE) {
            //加载更多逻辑
            listAdapter.pauseMore()
        }
    }

    private var commentCursor: CommentListResp.CommentCursor? = null

    @SuppressLint("CheckResult")
    override fun showData(data: CommentListResp, commentCursor: CommentListResp.CommentCursor?) {

        if (requestType == REQUEST_TYPE_REFRESH) {
            //刷新逻辑
            listAdapter.clear()
            listAdapter.addAll(data.result.hotComments)
            processData(data)

            if (listAdapter.count > 0) {
                showContentView()
            } else {
                showEmptyView()
            }
            if (!(data.result.hotComments?.size == 0 && data.result.comments.size == 0)) {
                setCommentHeaderView(data.result.summary)
                listAdapter.addHeader(commentListHeader)
            }
        } else if (requestType == REQUEST_TYPE_LOAD_MORE) {
            //加载更多逻辑
            processData(data)
        } else if (requestType == REQUEST_TYPE_LOAD_REPLY) {
            val loadReplyPosition = listAdapter.getPosition(loadReplyItemData)

            if (loadReplyPosition > -1) {
                if (listAdapter.getItem(loadReplyPosition).newestComments!!.nextCursor == null) {
                    listAdapter.getItem(loadReplyPosition).newestComments!!.comments = data.result.comments
                } else {
                    listAdapter.getItem(loadReplyPosition).newestComments!!.comments.addAll(data.result.comments)
                }
                listAdapter.getItem(loadReplyPosition).newestComments!!.hasMore = data.result.hasMore
                listAdapter.getItem(loadReplyPosition).newestComments!!.nextCursor = data.result.nextCursor

                val viewHolder = commentList.findViewHolderForAdapterPosition(loadReplyPosition + listAdapter.headerCount)
                if (viewHolder != null)
                    when (viewHolder) {
                        is CommentListAdapter.CommentVideoListVH -> viewHolder.addAllReply(data.result.comments, commentCursor)
                    }
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun setCommentHeaderView(summary: List<ApiSummary>?) {
        if (summary != null) {
            for (data in summary) {
                if (data.type == "FEED_INFO") {
                    commentListHeader.title.text = data.title
                    val topic = SpannableString(" #" + data.channelName)
                    topic.setSpan(object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            requireContext().startActivity(data.redirectUrl)
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            super.updateDrawState(ds)
                            ds.color = commentListHeader.title.context.getColor1(R.color.comment_send_color)
                            ds.flags = 0
                        }
                    }, 0, topic.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    commentListHeader.title.append(topic)
                    commentListHeader.title.append(" ")
                    commentListHeader.title.movementMethod = LinkMovementMethod.getInstance()
                } else if (data.type == "COMMENT") {
                    if (data.count ?: 0 > 1 && data.userList?.isNotEmpty() == true) {
                        commentListHeader.commentTv.visibility = View.VISIBLE
                        commentListHeader.commentIv.visibility = View.VISIBLE
                        commentListHeader.commentIv.setImageURI(data.userList!![0].avatar.toUri())
                        val spannableString = SpannableString(String.format(getString(R.string.string_more_comment_format), data.userList!![0].nickName, data.count.toString()))
                        spannableString.setSpan(StyleSpan(Typeface.BOLD), 0, data.userList!![0].nickName.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        spannableString.setSpan(StyleSpan(Typeface.BOLD), data.userList!![0].nickName.length + 1,
                                data.userList!![0].nickName.length + data.count.toString().length + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        commentListHeader.commentTv.text = spannableString
                    }
                } else if (data.type == "LIKE") {
                    if (data.count ?: 0 > 1 && data.userList?.isNotEmpty() == true) {
                        commentListHeader.likeIv.visibility = View.VISIBLE
                        commentListHeader.likeTv.visibility = View.VISIBLE
                        commentListHeader.likeIv.setImageURI(data.userList!![0].avatar.toUri())
                        val spannableString = SpannableString(String.format(getString(R.string.string_more_up_format), data.userList!![0].nickName, data.count.toString()))
                        spannableString.setSpan(StyleSpan(Typeface.BOLD), 0, data.userList!![0].nickName.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        spannableString.setSpan(StyleSpan(Typeface.BOLD), data.userList!![0].nickName.length + 1,
                                data.userList!![0].nickName.length + data.count.toString().length + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        commentListHeader.likeTv.text = spannableString
                    }
                }
            }
        }
    }

    private fun processData(data: CommentListResp) {
        listAdapter.addAll(data.result.comments)
        if (!data.result.hasMore) {
            commentCursor = null
            listAdapter.stopMore()
        } else {
            commentCursor = data.result.nextCursor
        }
    }

    private fun addComment(content: String?, targetId: String, mediaPath: String?) {
        if (TextUtils.isEmpty(mediaPath)) {
            apiService.createComment(targetId, "FEED", content
                    ?: "h").applySchedulers().subscribeApi(onNext = {
                addCommentPopup.clearText()
                addCommentPopup.clearMedia()
                clearTextAndMedia()
                count++
                refreshCommentCount()
                listAdapter.insert(it, 0)
                showContentView()
                commentList.smoothScrollToPosition(0)
                setCommentSpider("FEED", targetId, content, "txt", true, it.id)
            }, onApiError = {
                if (it is ApiError.HttpError) {
                    requireContext().showLongToast(it.displayMsg)
                } else if (it is ApiError.NetworkError) {
                    requireContext().showLongToast(getString(R.string.string_net_work_is_unenable))
                }
                setCommentSpider("FEED", targetId, content, "txt", false, null)
            })
        } else {
            val uploadComment = PullService.uploadComment(requireContext(), mediaPath!!, addCommentPopup.type,
                    content.let { if (it.isNullOrEmpty()) null else it }, targetId)
            if (uploadComment) {
                userAccount.let {
                    if (userAccount == null)
                        userAccount = storage.userCache.cacheOf<UserAccount>().get(ProfileFragment.ACCOUNT_CACHE_KEY).orNull()
                    else
                        it
                }
                clearTextAndMedia()
                count++
                refreshCommentCount()
                //添加空占位
                if (addCommentPopup.pictureType == "VIDEO") {
                    val bitmap = ThumbnailUtils.createVideoThumbnail("file://" + mediaPath, MediaStore.Images.Thumbnails.MINI_KIND)
                    if (bitmap != null) {
                        addCommentPopup.mediaWidth = bitmap.width
                        addCommentPopup.mediaHeight = bitmap.height
                    }
                }
                showContentView()
                listAdapter.insert(ApiComment(null, ApiUserInfo(accountManager.account()?.userId
                        ?: 0, userAccount?.avatar
                        ?: "",
                        "http://t.image.ricebook.com/avatar/101", null, userAccount?.nickName
                        ?: "", 0, 0,
                        "", 0, null, false, null), content ?: mediaPath
                ?: "", System.currentTimeMillis(), 0,
                        false, "NORMAL", null, 0, targetId, "FEED", null, null,
                        arrayListOf(ApiMedia(12, addCommentPopup.mediaHeight, addCommentPopup.mediaWidth, mediaPath, addCommentPopup.pictureType
                                ?: "", arrayListOf()))), 0)
                commentList.smoothScrollToPosition(0)
                addCommentPopup.clearText()
                addCommentPopup.clearMedia()
            } else {
                context?.showLongToast(getString(R.string.string_comment_wait_edit))
            }
        }
    }

    private fun setCommentSpider(targetType: String, targetId: String, content: String?, commentType: String, isSuccess: Boolean, commentId: Long?) {
        val event = spider.manuallyEvent(SpiderEventNames.COMMENT)
                .put("commentTarget", targetId)
                .put("TargetType", targetType)
                .put("feedID", feedId)
                .put("content", content ?: "")
                .put("userID", accountManager.account()?.userId ?: "")
                .put("commentType", commentType)
                .put("isSuccess", isSuccess)

        if (commentId != null) {
            event.put("commentID", commentId.toString())
        }
        event.track()
    }

    private fun clearTextAndMedia() {
        sendCommentTv.visibility = View.GONE
        addCommentTv.text = ""
        selMediaL.visibility = View.GONE
    }

    private fun addReply(commentId: Long, content: String, position: Int) {
        apiService.createComment(commentId.toString(), "COMMENT", content).applySchedulers().subscribeApi(onNext = {
            addCommentPopup.clearText()
            addCommentPopup.clearMedia()
            clearTextAndMedia()
            if (position > -1) {
                val viewHolder = commentList.findViewHolderForAdapterPosition(position)
                if (viewHolder != null) {
                    when (viewHolder) {
                        is CommentListAdapter.CommentVideoListVH -> {
                            viewHolder.addReply(it)
                        }
                    }
                    commentList.scrollToPosition(position)
                }
            }
            setCommentSpider("USER", it.replyTo?.userId?.toString()
                    ?: "", content, "txt", true, it.id)
        }, onApiError = {
            if (it is ApiError.HttpError) {
                requireContext().showLongToast(it.displayMsg)
            } else if (it is ApiError.NetworkError) {
                requireContext().showLongToast(requireContext().getString(R.string.string_net_work_is_unenable))
            }
            setCommentSpider("USER", commentId.toString(), content, "txt", false, null)
        })
    }

    private fun deleteComment(apiComment: ApiComment, position: Int) {
        val items = arrayOf(getString(R.string.string_delete))
        AlertDialog.Builder(requireContext())
                .setItems(items) { _, which ->
                    var commentId: String = ""
                    if (which == 0) {
                        if (apiComment.id == null) {
                            if (apiComment.targetType != null) {
                                if (apiComment.targetType.equals("FEED")) {
                                    count--
                                    refreshCommentCount()
                                    listAdapter.remove(apiComment)
                                } else {
                                    val viewHolder = commentList.findViewHolderForAdapterPosition(position + listAdapter.headerCount)
                                    if (viewHolder != null) {
                                        when (viewHolder) {

                                            is CommentListAdapter.CommentVideoListVH -> viewHolder.deleteReply(apiComment)
                                        }
                                    }
                                }
                            }

                        } else {
                            commentId = apiComment.id.toString()
                            apiService.destoryComment(apiComment.id.toString()).applySchedulers().subscribeApi(onNext = {
                                if (it.result.isSuccess) {
                                    if (apiComment.targetType != null) {
                                        if (apiComment.targetType.equals("FEED")) {
                                            count--
                                            refreshCommentCount()
                                            listAdapter.remove(apiComment)
                                        } else {
                                            val viewHolder = commentList.findViewHolderForAdapterPosition(position + listAdapter.headerCount)
                                            if (viewHolder != null) {
                                                when (viewHolder) {
                                                    is CommentListAdapter.CommentVideoListVH -> viewHolder.deleteReply(apiComment)
                                                }
                                            }
                                        }
                                    }
                                }
                            }, onApiError = {
                                if (it is ApiError.HttpError) {
                                    requireContext().showLongToast(it.displayMsg)
                                }
                            })
                        }
                    }
                    spider.manuallyEvent(SpiderEventNames.COMMENT_DELETE_CLICK).put("commentID", commentId).track()
                }
                .show()
    }

    /**
     * 点赞
     */
    private fun praise(data: ApiComment, onListener: ReplyListAdapter.OnListener) {
        if (data.liked) {
            apiService.cancelPraise(data.id.toString(), "COMMENT").applySchedulers().subscribeApi(onNext = {
                if (it.result.isSuccess) {
                    onListener.onCancelPraise()
                    spider.commentLikeEvent(requireContext(), feedId, data.id?.toString(), "unlike")
                }
            }, onApiError = {
                if (it is ApiError.HttpError) {
                    requireContext().showLongToast(it.displayMsg)
                }
            })
        } else {
            apiService.praise(data.id.toString(), "COMMENT").applySchedulers().subscribeApi(onNext = {
                if (it.result.isSuccess) {
                    onListener.onPraise()
                    spider.commentLikeEvent(requireContext(), feedId, data.id?.toString(), "like")
                }
            }, onApiError = {
                if (it is ApiError.HttpError) {
                    requireContext().showLongToast(it.displayMsg)
                }
            })
        }
    }

    override fun showMessage(message: String) {
        requireContext().showLongToast(message)
    }

    interface OnCommentCountListener {
        fun showCount(count: Int)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PictureMimeType.ofAll()) {
            val result = PictureSelector.obtainMultipleResult(data)
            if (result.isNotEmpty()) {
                val localMedia: LocalMedia = result[0]
                val pictureType = when {
                    localMedia.pictureType.contains("video") -> CommentListAdapter.TYPE_VIDEO
                    localMedia.pictureType.contains("image/gif") -> CommentListAdapter.TYPE_GIF
                    localMedia.height / localMedia.width > 2 -> CommentListAdapter.TYPE_LONG
                    else -> CommentListAdapter.TYPE_IMAGE
                }
                showAddCommentPopup(addCommentTv, null, null)

                addCommentPopup.showSelImage(localMedia.compressPath
                        ?: localMedia.path, localMedia.width, localMedia.height, pictureType, localMedia.pictureType)
            }
        }
    }

    private fun showLoadingBar() {
        ViewHelper.crossfade(loadingView, commentList, emptyView, errorView)
    }

    private fun showContentView() {
        ViewHelper.crossfade(commentList, loadingView, emptyView, errorView)
    }

    private fun showEmptyView() {
        ViewHelper.crossfade(emptyView, commentList, loadingView, errorView)
    }

    override fun show(manager: FragmentManager, tag: String?) {
        //super.show(manager, tag)
        val ft = manager.beginTransaction()
        ft.add(this, tag)
        ft.commitAllowingStateLoss()
    }

    private var mDismissListener: OnDismissListener? = null
    fun setOnDismissListener(listener: OnDismissListener) {
        mDismissListener = listener
    }

    interface OnDismissListener {
        fun onDismiss()
    }
}