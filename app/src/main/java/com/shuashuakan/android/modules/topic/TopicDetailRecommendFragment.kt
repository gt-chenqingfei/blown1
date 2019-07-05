package com.shuashuakan.android.modules.topic


import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.shuashuakan.android.R
import com.shuashuakan.android.config.AppConfig
import com.shuashuakan.android.data.api.model.channel.ChannelItemModel
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.FeedTransportManager
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.player.activity.VideoPlayActivity
import com.shuashuakan.android.modules.widget.EmptyView
import com.shuashuakan.android.modules.widget.horizontalslideLayout.DZStickyNavLayouts
import com.shuashuakan.android.modules.widget.loadmoreview.SskLoadMoreView
import com.shuashuakan.android.ui.base.FishFragment
import com.shuashuakan.android.utils.*
import java.util.*
import javax.inject.Inject

/**
 * 话题详情中推荐页面
 */
class TopicDetailRecommendFragment : FishFragment() {

    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var accountManager: AccountManager

    @Inject
    lateinit var appConfig: AppConfig

    private var channelId: Long? = null
    private val recyclerView by bindView<RecyclerView>(R.id.channel_recommend_recycler)
    private val errorView by bindView<LinearLayout>(R.id.error_layout)
    private val emptyView by bindView<View>(R.id.empty_layout)
    private val loadingBar by bindView<View>(R.id.loading_view)
    private val emptyTv by bindView<EmptyView>(R.id.empty_view)

    private lateinit var adapter: BaseQuickAdapter<ChannelItemModel, BaseViewHolder>
    private var notifyId: String? = null
    private var totalNum = 0
    private var isRefresh = true
    val parentList: MutableList<Feed> = mutableListOf()
    var hasMore: Boolean = true

    companion object {
        private const val EXTRA_CHANNEL_ID = "extra_channel_id"

        fun create(channel: Long): TopicDetailRecommendFragment {
            val recommendFragment = TopicDetailRecommendFragment()
            val bundle = Bundle()
            bundle.putLong(EXTRA_CHANNEL_ID, channel)
            recommendFragment.arguments = bundle
            return recommendFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_channel_detail_recommend, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        channelId = arguments?.getLong(EXTRA_CHANNEL_ID)
        if (channelId != null) getData()
        initView()
    }

    private fun initView() {
        errorView.setOnClickListener {
            notifyId = null
            isRefresh = true
            getData()
        }

        val linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        adapter = object : BaseQuickAdapter<ChannelItemModel, BaseViewHolder>(R.layout.layout_channel_recommend_item) {
            override fun convert(helper: BaseViewHolder, item: ChannelItemModel) {
                val slidingLayout = helper.getView<DZStickyNavLayouts>(R.id.slide_layout)
                val betterRecyclerView = helper.getView<com.shuashuakan.android.modules.widget.BetterRecyclerView>(R.id.channel_recommend_item_recycler)

                val title = helper.getView<TextView>(R.id.title)
                if (item.data.solitaireFeeds.isNotEmpty())
                    title.text = item.data.solitaireFeeds[0].title
                val linearLayoutManager = LinearLayoutManager(context, HORIZONTAL, false)
                betterRecyclerView?.layoutManager = linearLayoutManager
                val randomColor = getRandomColor()
                val subAdapter = object : BaseQuickAdapter<Feed, BaseViewHolder>(R.layout.layout_sub_item_channel_recommend) {
                    override fun convert(subHelper: BaseViewHolder, subItem: Feed) {
                        val itemWidth: Int = (mContext.getScreenSize().x - mContext.dip(40f)) / 2
                        subHelper.itemView.layoutParams.width = itemWidth

                        val title = subHelper.getView<TextView>(R.id.channel_recommend_title)
                        val avatar = subHelper.getView<SimpleDraweeView>(R.id.rank_avatar)
                        val nickName = subHelper.getView<TextView>(R.id.nick_name)
                        val cover = subHelper.getView<SimpleDraweeView>(R.id.channel_image_view)
                        val num = subHelper.getView<TextView>(R.id.channel_recommend_num)
                        val numLayout = subHelper.getView<LinearLayout>(R.id.num_layout)

                        if (subHelper.adapterPosition <= 8) {
                            cover.setImageUrl2Webp(subItem.cover!!, requireActivity().dip(150), requireActivity().dip(200))
                            avatar.setImageUrl2Webp(subItem.avatar!!, requireActivity().dip(18), requireActivity().dip(18))
                            title.text = subItem.title
                            nickName.text = subItem.userName
                            num.text = (subHelper.adapterPosition + 1).toString()
                            val drawable = numLayout.background
                            (drawable as GradientDrawable).setColor(Color.parseColor(randomColor))
                            subHelper.itemView.setOnClickListener {

                                if (notifyId == null && !hasMore) {
                                    notifyId = ""
                                }
                                val intentParam = FeedTransportManager.jumpToVideoFromTopicDetailRecommend(channelId, notifyId, parentList.toList(), item.data.solitaireFeeds, item.data.solitaireFeeds[0], subHelper.adapterPosition)
                                requireActivity().startActivity(VideoPlayActivity.create(requireContext(), intentParam))
                            }
                        }
                    }
                }


                slidingLayout.setFootLayoutViewShow()
                slidingLayout.setOnStartActivity {
                    if (item.data.solitaireFeeds.size >= 10) {
                        if (subAdapter.data.size != 0) {
                            if (notifyId == null && !hasMore) {
                                notifyId = ""
                            }
                            val intentParam = FeedTransportManager.jumpToVideoFromTopicDetailRecommend(channelId, notifyId, parentList.toList(), item.data.solitaireFeeds, item.data.solitaireFeeds[0], 9)
                            requireActivity().startActivity(VideoPlayActivity.create(requireContext(), intentParam))

                        }
                    }
                }

                if (item.data.solitaireFeeds.size >= 9) {
                    subAdapter.setNewData(item.data.solitaireFeeds.subList(0, 9))
                } else {
                    subAdapter.setNewData(item.data.solitaireFeeds)
                }
                betterRecyclerView?.adapter = subAdapter
                if (item.data.solitaireFeeds.size >= 10) {
                    slidingLayout.setFootLayoutViewShow()
                } else {
                    slidingLayout.setFootLayoutViewHide()
                }
            }
        }
        recyclerView.adapter = adapter
        adapter.setOnLoadMoreListener({
            isRefresh = false
            getData()
        }, recyclerView)
        adapter.setLoadMoreView(SskLoadMoreView())
        emptyTv.setTitle(getString(R.string.string_go_solitaire_tips))
    }

    private fun getData() {
        if (isRefresh) {
            ViewHelper.crossfade(loadingBar, errorView)
            parentList.clear()
        }

        apiService.channelRecommendData(channelId, null, notifyId)
                .applySchedulers()
                .subscribeApi(onNext = {
                    totalNum += it.feedList.size
                    hasMore = it.hasMore
                    //if(it.cursor?.nextId!=null)
                    notifyId = it.cursor?.nextId
                    if (!it.hasMore) {
                        adapter.loadMoreEnd(false)
                    } else {
                        adapter.loadMoreComplete()
                    }
                    adapter.addData(it.feedList)
                    if (totalNum == 0) {
                        ViewHelper.crossfade(emptyView, loadingBar, recyclerView, errorView)
                    } else {
                        ViewHelper.crossfade(recyclerView, emptyView, loadingBar, errorView)
                    }
                    changeData(it.feedList)
                }, onApiError = {
                    ViewHelper.crossfade(errorView, loadingBar, emptyView, recyclerView)
                })
    }

    private fun changeData(feedList: List<ChannelItemModel>) {
        for (item in feedList) {
            parentList.add(item.data.solitaireFeeds[0])
        }
    }

    fun getRandomColor(): String {
        val colors = arrayOf("#3E389E", "#992551", "#296890", "#37755C", "#652D8E", "#1E5AA6", "#297782", "#9E5722")
        return colors[Random().nextInt(colors.size - 1)]
    }

}
