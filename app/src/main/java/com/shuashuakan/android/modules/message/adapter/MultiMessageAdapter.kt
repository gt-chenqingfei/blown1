package com.shuashuakan.android.modules.message.adapter

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.facebook.drawee.view.SimpleDraweeView
import com.shuashuakan.android.R
import com.shuashuakan.android.data.api.model.message.ActionUserInfoListItem
import com.shuashuakan.android.data.api.model.message.NormalMsgItemData
import com.shuashuakan.android.modules.comment.CommentImageShowActivity
import com.shuashuakan.android.modules.message.MessagePersonListActivity
import com.shuashuakan.android.modules.widget.FollowButton
import com.shuashuakan.android.utils.TimeUtil
import com.shuashuakan.android.utils.getColor1
import com.shuashuakan.android.utils.startActivity
import timber.log.Timber
import java.util.*


/**
 * Author:  lijie
 * Date:   2018/12/10
 * Email:  2607401801@qq.com
 */
class MultiMessageAdapter constructor(
        private val context: Context,
        dataList: List<MultiItemEntity>?
) : BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(dataList) {

    private var centerListener: OnMessageCenterListener? = null

    companion object {
        const val COMMENT = 0
        const val SIMPLE_LAYOUT = 1
        const val MULTIPLE_LAYOUT = 2
    }

    init {
        addItemType(COMMENT, R.layout.item_notice_comment)//评论
        addItemType(SIMPLE_LAYOUT, R.layout.item_notice_follow)//喜欢视频   喜欢评论  关注
        addItemType(MULTIPLE_LAYOUT, R.layout.item_notice_multi_follow)//多人喜欢   多人喜欢评论  多人关注
    }

    fun setCenterListener(centerListener: OnMessageCenterListener) {
        this.centerListener = centerListener
    }

    override fun convert(helper: BaseViewHolder, item: MultiItemEntity) {
        val dataPair = item as com.shuashuakan.android.modules.discovery.ItemDataPair
        val itemModel = dataPair.data as NormalMsgItemData

        Timber.e(itemModel.type)

        when (item.itemType) {
            COMMENT -> {
                setCommentLayout(helper, itemModel)
            }
            SIMPLE_LAYOUT -> {
                setSimpleLayout(helper, itemModel)
            }
            MULTIPLE_LAYOUT -> {
                setMultiFollowLayout(helper, itemModel)
            }
        }
        helper.itemView.setOnClickListener {
            if (itemModel.type == "COMMENT")
                centerListener?.onItemClick(itemModel, helper.adapterPosition)
        }
    }

    private fun setMultiFollowLayout(helper: BaseViewHolder, data: NormalMsgItemData) {
        var userList = data.actionUserInfoList
        val action = data.type
        val title = helper.getView<TextView>(R.id.user_name)
        if (userList.size >= 2) {
            helper.getView<TextView>(R.id.time).text = TimeUtil.getTimeFormatText(Date(data.createAt))
            helper.getView<SimpleDraweeView>(R.id.avatar_one).setImageURI(userList[0].avatar)
            helper.getView<SimpleDraweeView>(R.id.avatar_two).setImageURI(userList[1].avatar)


            val nameStr = SpannableString("${userList[0].nickName}、${userList[1].nickName}")
            nameStr.setSpan(StyleSpan(Typeface.BOLD), 0, nameStr.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            title.text = nameStr
            var str: SpannableString? = null
            if (action == "FOLLOW") {

                str = SpannableString(String.format(context.getString(R.string.string_follow_you_format), data.actionUserCount))
            } else if (action == "LIKE_FEED") {
                str = SpannableString(String.format(context.getString(R.string.string_up_video_you_format), data.actionUserCount))
            } else if (action == "LIKE_COMMENT") {
                str = SpannableString(String.format(context.getString(R.string.string_like_comment_you_format), data.actionUserCount))
            }
            str?.setSpan(ForegroundColorSpan(context.getColor1(R.color.enjoy_color_2)), 0, str.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
            title.append(str)
        }
        if (action == "LIKE_COMMENT") {
            title.append("「${data.referenceItem?.content}」")
        }
        val cover = helper.getView<SimpleDraweeView>(R.id.cover)
        if (action == "FOLLOW") {
            cover.visibility = View.GONE
        } else {
            helper.setGone(R.id.cover_holder, data.notificationLink?.targetStatus == "OFFLINE")
            helper.setGone(R.id.cover_holder_label, data.notificationLink?.targetStatus == "OFFLINE")
            cover.visibility = View.VISIBLE
        }
        cover.setImageURI(data.notificationLink?.coverUrl)
        cover.setOnClickListener {
            context.startActivity(data.notificationLink?.redirectUrl)
        }

        title.setOnClickListener {
            context.startActivity(MessagePersonListActivity.create(context, data.id!!, data.type))
        }
    }

    private fun setCommentLayout(helper: BaseViewHolder, data: NormalMsgItemData) {
        val coverView = helper.getView<SimpleDraweeView>(R.id.cover)
        val tempImage = helper.getView<ImageView>(R.id.image)
        if (data.actionUserInfoList.isNotEmpty()) {
            helper.getView<SimpleDraweeView>(R.id.avatar).setImageURI(data.actionUserInfoList[0].avatar)
            helper.getView<TextView>(R.id.user_name).text = data.actionUserInfoList[0].nickName
        }
        coverView.setImageURI(data.notificationLink?.coverUrl)

        helper.setGone(R.id.cover_holder, data.notificationLink?.targetStatus == "OFFLINE")
        helper.setGone(R.id.cover_holder_label, data.notificationLink?.targetStatus == "OFFLINE")


        val bottomTv = helper.getView<TextView>(R.id.comment_image_btn)
        coverView.setOnClickListener {
            context.startActivity(data.notificationLink?.redirectUrl)
        }
        val content = helper.getView<TextView>(R.id.home_container)
        if (!data.referenceItem?.content.isNullOrEmpty()) {
            content.text = data.referenceItem?.content
            content.visibility = View.VISIBLE
        } else {
            content.visibility = View.GONE
        }
        helper.getView<SimpleDraweeView>(R.id.avatar).setOnClickListener {
            if (data.actionUserInfoList.isNotEmpty())
                centerListener?.onAvatarClick(data.actionUserInfoList[0].userId)
        }
        helper.getView<TextView>(R.id.time).text = TimeUtil.getTimeFormatText(Date(data.createAt))
        val media = data.referenceItem?.media
        bottomTv.visibility = View.VISIBLE
        if (media != null && media.isNotEmpty()) {
            val mediaType = data.referenceItem?.media!![0].mediaType

            if (mediaType != null && mediaType == "IMAGE" || mediaType == "LONG_IMAGE" || mediaType == "ANIMATION") {
                bottomTv.text = context.getString(R.string.string_look_picture)
                setDrawableLeft(R.drawable.ic_picture_message, bottomTv)
            } else {
                bottomTv.visibility = View.GONE
            }

        } else {
            bottomTv.visibility = View.GONE
        }
        bottomTv.setOnClickListener {
            if (data.referenceItem?.media != null && data.referenceItem?.media!!.isNotEmpty()) {
                val type = data.referenceItem?.media!![0].mediaType
                if (type == "IMAGE" || type == "LONG_IMAGE") {
                    CommentImageShowActivity.create(context, data.referenceItem?.media!![0].thumbUrl, tempImage, type, data.referenceItem?.media!![0].thumbUrl, null)
                } else if (type == "ANIMATION") {
                    CommentImageShowActivity.create(context, data.referenceItem?.media!![0].thumbUrl, type, data.referenceItem?.media!![0].thumbUrl, null)
                }
            }
        }
    }

    private fun setSimpleLayout(helper: BaseViewHolder, data: NormalMsgItemData) {
        val followButton = helper.getView<FollowButton>(R.id.follow_btn)
        val cover = helper.getView<SimpleDraweeView>(R.id.cover)

        if (data.actionUserInfoList.isNotEmpty()) {
            helper.getView<SimpleDraweeView>(R.id.avatar).setImageURI(data.actionUserInfoList[0].avatar)
            helper.getView<TextView>(R.id.user_name).text = data.actionUserInfoList[0].nickName
            val isFollow = data.actionUserInfoList[0].isFollow
            val isFans = data.actionUserInfoList[0].isFans
            if (isFollow != null) {
                if (isFollow) {
                    followButton.setFollowStatus(true)
                } else {
                    followButton.setFollowStatus(false, isFans)
                }
            } else {
                followButton.setFollowStatus(false, isFans)
            }
        }
        if (data.notificationLink?.coverUrl != null) {
            cover.setImageURI(data.notificationLink?.coverUrl)
            cover.setOnClickListener {
                context.startActivity(data.notificationLink?.redirectUrl)
            }
        }
        val action = data.type
        val titleStr = helper.getView<TextView>(R.id.text)
        when (action) {
            "LIKE_FEED" -> titleStr.text = context.getString(R.string.string_liked_you_video)
            "FOLLOW" -> titleStr.text = context.getString(R.string.string_follow_to_you)
            "LIKE_COMMENT" -> titleStr.text = context.getString(R.string.string_like_you_comment)
        }
        helper.getView<SimpleDraweeView>(R.id.avatar).setOnClickListener {
            if (data.actionUserInfoList.isNotEmpty())
                centerListener?.onAvatarClick(data.actionUserInfoList[0].userId.toString())
        }
        helper.getView<TextView>(R.id.time).text = TimeUtil.getTimeFormatText(Date(data.createAt))


        followButton.setOnClickListener {
            if (data.actionUserInfoList.isNotEmpty())
                centerListener?.onFollowClick(followButton, data.actionUserInfoList[0])
        }
        if (action == "FOLLOW") {
            followButton.visibility = View.VISIBLE
            cover.visibility = View.GONE
            helper.setGone(R.id.cover_holder, false)
            helper.setGone(R.id.cover_holder_label, false)
        } else {
            followButton.visibility = View.GONE
            cover.visibility = View.VISIBLE
            helper.setGone(R.id.cover_holder, data.notificationLink?.targetStatus == "OFFLINE")
            helper.setGone(R.id.cover_holder_label, data.notificationLink?.targetStatus == "OFFLINE")
        }
    }

    interface OnMessageCenterListener {
        fun onFollowClick(button: FollowButton, user: ActionUserInfoListItem)
        fun onAvatarClick(user_id: String)
        fun onItemClick(data: NormalMsgItemData, position: Int)
    }

    private fun setDrawableLeft(drawable: Int, tv: TextView) {
        val theDrawable = context.resources.getDrawable(drawable)
        theDrawable.setBounds(0, 0, theDrawable.minimumWidth, theDrawable.minimumHeight)
        tv.setCompoundDrawables(theDrawable, null, null, null)
    }
}