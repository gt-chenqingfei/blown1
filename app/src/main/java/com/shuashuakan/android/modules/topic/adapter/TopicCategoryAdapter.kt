package com.shuashuakan.android.modules.topic.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.shuashuakan.android.R
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.EmptyChannel
import com.shuashuakan.android.data.api.model.FeedChannel
import com.shuashuakan.android.data.api.model.TopicCategory
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.event.SubscribeEvent
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.account.activity.LoginActivity
import com.shuashuakan.android.modules.topic.TopicCategoryActivity
import com.shuashuakan.android.modules.widget.SpiderAction
import com.shuashuakan.android.utils.*
import com.shuashuakan.android.utils.extension.showCancelSubscribeDialog

/**
 *@author: zhaoningqiang
 *@time: 2019/5/23
 *@Description:话题分类Item Adapter(include title item and footer)
 */
class TopicCategoryAdapter(val apiService: ApiService, val context: Context, private val mShowType: Int) : BaseAdapter() {
    private val viewTypeTitle = 0
    private val viewTypeItem = 1
    private val viewTypeFooter = 2
    private val viewTypeEmptySubscribe = 3
    private val mDataMap = HashMap<Any, TopicCategory>()
    val mWrapperData = ArrayList<Any>()

    private val topicNameTypeface: Typeface by lazy {
        Typeface.createFromAsset(context.assets, "fonts/NotoSans-CondensedBold.ttf")
    }

    private val emptySubTitle: String by lazy {
        context.getString(R.string.string_empty_subscribe)
    }

    private val subscribeText: String by lazy {
        context.getString(R.string.string_topic_subscribe)
    }


    fun setNewData(data: List<TopicCategory>?) {
        transform(data)
        notifyDataSetChanged()
    }

    /**
     * 将数据转换成方便处理的结构
     */
    private fun transform(data: List<TopicCategory>?) {
        mWrapperData.clear()
        mDataMap.clear()
        if (data != null && data.isNotEmpty()) {
            for (topic in data) {
                mWrapperData.add(topic)
                if (topic.feed_channels.isEmpty() && topic.name.contains(subscribeText, true)) {
                    val emptyChannel = EmptyChannel(emptySubTitle, topic.id)
                    mDataMap[emptyChannel] = topic
                    mWrapperData.add(emptyChannel)
                } else {
                    for (channel in topic.feed_channels) {
                        mDataMap[channel] = topic
                        mWrapperData.add(channel)
                    }
                }
            }
        }
    }

    private var mSelectionItem: FeedChannel? = null

    fun setSelection(feedChannel: FeedChannel) {
        mSelectionItem = feedChannel
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return if (mWrapperData.isEmpty()) {
            0
        } else {
            mWrapperData.size + 1 //topic item size + title size + footer size
        }
    }


    override fun getItem(position: Int): Any? {
        return if (position == mWrapperData.size) {
            null
        } else {
            mWrapperData[position]
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun getViewTypeCount(): Int {
        return 4
    }


    override fun getItemViewType(position: Int): Int {
        TopicCategoryActivity
        return if (position == mWrapperData.size) {
            viewTypeFooter
        } else {
            val item = mWrapperData[position]
            if (item is TopicCategory) {
                viewTypeTitle
            } else if (item is EmptyChannel) {
                viewTypeEmptySubscribe
            } else {
                viewTypeItem
            }
        }
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val itemViewType = getItemViewType(position)
        var itemView = convertView
        when (itemViewType) {
            viewTypeTitle -> {
                val holder: TopicTitleViewHolder
                if (itemView == null || itemView.tag !is TopicTitleViewHolder) {
                    itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_topic_category_inner_title, parent, false)
                    holder = TopicTitleViewHolder(itemView)
                    itemView.tag = holder
                } else {
                    holder = itemView.tag as TopicTitleViewHolder
                }
                val topicCategory = mWrapperData[position] as TopicCategory
                holder.itemView.setTag(R.id.tagTopicCategory, topicCategory)
                holder.topicTitle.text = topicCategory.name

            }
            viewTypeItem -> {
                val holder: TopicItemViewHolder
                if (itemView == null || itemView.tag !is TopicItemViewHolder) {
                    itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_topic_category, parent, false)
                    holder = TopicItemViewHolder(itemView)
                    itemView.tag = holder
                } else {
                    holder = itemView.tag as TopicItemViewHolder
                }
                val feedChannel = mWrapperData[position] as FeedChannel
                val topicCategory = mDataMap[feedChannel]
                feedChannel.categroyId = topicCategory?.id
                holder.itemView.setTag(R.id.tagFeedChannel, feedChannel)
                holder.itemView.setTag(R.id.tagTopicCategory, topicCategory)
                val ctx = holder.itemView.context
                val url = imageUrl2WebP2(feedChannel.cover_url ?: "", ctx.dip(60)
                        , ctx.dip(60))
                holder.topicCover.setImageURI(url)
                holder.topicName.text = feedChannel.name
                holder.topicSubscribeDesc.text = String.format(ctx.getString(R.string.string_participate_sub_format), numFormat(feedChannel.subscribed_count))


                if (mShowType == TopicCategoryActivity.showTypeSelect) {
                    if (mSelectionItem == feedChannel) {
                        holder.topicSubscribeStatus.isSelected = true
                        holder.topicSubscribeStatus.text = ctx.getString(R.string.string_topic_has_select)
                    } else {
                        holder.topicSubscribeStatus.isSelected = false
                        holder.topicSubscribeStatus.text = ctx.getString(R.string.string_topic_select)
                    }
                } else {
                    if (feedChannel.has_subscribe) {
                        holder.topicSubscribeStatus.isSelected = true
                        holder.topicSubscribeStatus.text = ctx.getString(R.string.string_topic_has_subscribe)
                    } else {
                        holder.topicSubscribeStatus.isSelected = false
                        holder.topicSubscribeStatus.text = ctx.getString(R.string.string_topic_subscribe)
                    }
                }

                holder.topicSubscribeStatus.tag = feedChannel
            }
            viewTypeEmptySubscribe -> {
                val holder: EmptyTopicTitleViewHolder
                if (itemView == null || itemView.tag !is EmptyTopicTitleViewHolder) {
                    itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_topic_category_empty_subscribe, parent, false)
                    holder = EmptyTopicTitleViewHolder(itemView)
                    itemView.tag = holder
                } else {
                    holder = itemView.tag as EmptyTopicTitleViewHolder
                }
                val channel = mWrapperData[position] as EmptyChannel
                holder.title.text = channel.title

                val emptyChannel = mWrapperData[position] as EmptyChannel
                val topicCategory = mDataMap[emptyChannel]
                emptyChannel.categroyId = topicCategory?.id
                holder.itemView.setTag(R.id.tagTopicCategory, topicCategory)
            }
            else -> {
                if (itemView == null) {
                    itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_topic_category_footer, parent, false)
                }
            }
        }
        return itemView
    }

    private fun changeState(channel: FeedChannel, isSubscribe: Boolean) {
        mWrapperData.filter {
            if (it is FeedChannel) {
                it.id == channel.id
            } else {
                false
            }
        }.forEach {
            it as FeedChannel
            it.has_subscribe = isSubscribe
            if (isSubscribe) {
                channel.subscribed_count += 1
            } else {
                channel.subscribed_count -= 1
            }

        }
    }

    private fun subscribeClick(context: Context, channel: FeedChannel,
                               onSubscribeSuccess: () -> Unit) {

        if (!context.daggerComponent().accountManager().hasAccount()) {
            waitChannel = channel
            LoginActivity.launch(context)
            return
        }

        if (channel.has_subscribe) {
            val cancelSub = {
                apiService.cancelSubscribe(channel.id).applySchedulers().subscribeApi(onNext = {
                    if (it.result.isSuccess) {
                        onSubscribeSuccess.invoke()
                        changeState(channel, false)
                        notifyDataSetChanged()
                        RxBus.get().post(SubscribeEvent())
                    } else {
                        context.showLongToast(context.getString(R.string.string_un_subscription_error))
                    }
                }, onApiError = {
                    if (it is ApiError.HttpError) {
                        context.showLongToast(it.displayMsg)
                    } else {
                        context.showLongToast(context.getString(R.string.string_un_subscription_error))
                    }
                })
            }
            context.showCancelSubscribeDialog(channel.name, cancelSub)
        } else {
            apiService.subscribeMethod(channel.id).applySchedulers().subscribeApi(onNext = {
                if (it.result.isSuccess) {

                    changeState(channel, true)
                    notifyDataSetChanged()
                    RxBus.get().post(SubscribeEvent())
                    //
                    context.getSpider().subscribeChinnalClickEvent(context, channel.id.toString(),
                            SpiderAction.VideoPlaySource.CHANNEL_SUBSCRIPTION_PAGE.source)

                } else {
                    context.showLongToast(context.getString(R.string.string_subscription_error))
                }
            }, onApiError = {
                if (it is ApiError.HttpError) {
                    context.showLongToast(it.displayMsg)
                } else {
                    context.showLongToast(context.getString(R.string.string_subscription_error))
                }
            })
        }
    }

    inner class TopicItemViewHolder(val itemView: View) {
        val topicCover: SimpleDraweeView = itemView.findViewById(R.id.topicCover)
        val topicName: TextView = itemView.findViewById(R.id.topicName)
        val topicSubscribeDesc: TextView = itemView.findViewById(R.id.topicSubscribeDesc)
        val topicSubscribeStatus: TextView = itemView.findViewById(R.id.topicSubscribeStatus)

        init {
            topicName.typeface = topicNameTypeface
            topicSubscribeDesc.typeface = topicNameTypeface
            itemView.setOnClickListener { view ->
                val tagFeedChannel = view.getTag(R.id.tagFeedChannel) as FeedChannel
                mTopicItemClickListener?.onTopicItemClick(view, tagFeedChannel)
            }

            topicSubscribeStatus.setOnClickListener {
                val channel = it.tag as FeedChannel
                if (mTopicItemClickListener?.onSubscribeStatusClick(it, channel) == true) {
                    //do nothing
                } else {
                    subscribeClick(it.context, channel){}
                }
            }

        }
    }

    class TopicTitleViewHolder(val itemView: View) {
        val topicTitle: TextView = itemView.findViewById(R.id.topicTitle)
    }

    class EmptyTopicTitleViewHolder(val itemView: View) {
        val title: TextView = itemView.findViewById(R.id.title)
    }


    private var mTopicItemClickListener: OnTopicItemClickListener? = null
    fun setTopicItemClickListener(l: OnTopicItemClickListener) {
        this.mTopicItemClickListener = l
    }

    interface OnTopicItemClickListener {
        fun onTopicItemClick(view: View, feedChannel: FeedChannel)

        /**
         * 返回值表示是否已经处理了
         */
        fun onSubscribeStatusClick(view: View, feedChannel: FeedChannel): Boolean
    }


    private var waitChannel: FeedChannel? = null
    fun onLoginSubscribe() {
        waitChannel ?: return
        subscribeClick(context, waitChannel!!) {
            waitChannel = null
        }
    }

}