package com.shuashuakan.android.modules.partition.adapter.viewhoder

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.luck.picture.lib.tools.ScreenUtils
import com.shuashuakan.android.R
import com.shuashuakan.android.data.api.model.partition.PartitionBannerItemModel
import com.shuashuakan.android.data.api.model.partition.PartitionBannerModel
import com.shuashuakan.android.modules.discovery.ItemDataPair
import com.shuashuakan.android.ui.banner.BannerView
import com.shuashuakan.android.utils.*

/**
 * @author hushiguang
 * @since 2019-06-17.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
class PartitionBannerViewHolder(
        val categoryId: Int,
        val mContext: Context,
        val helper: BaseViewHolder?,
        val item: ItemDataPair?) {

    init {
        formatContent()
    }

    private fun formatContent() {
        val bannerModel = item?.data as PartitionBannerModel
        val bannerView = helper?.getView<BannerView<PartitionBannerItemModel>>(R.id.banner_list)
        val list = bannerModel.dataList

        bannerView?.let { bannerView ->
            bannerView.setViewFactory(BannerViewFactory())
            bannerView.setDataList(list)
            bannerView.start()

            // 第一个的点
            if (list.isNotEmpty()) {
                put(list[0].id?.toString()!!, categoryId, bannerView)
            }

            bannerView.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                }

                override fun onPageSelected(position: Int) {
                    // 滚动的时候的点
                    put(list[position].id?.toString()!!, categoryId, bannerView)
                }
            })
        }

    }

    internal inner class BannerViewFactory : BannerView.ViewFactory<PartitionBannerItemModel> {
        @SuppressLint("InflateParams")
        override fun create(item: PartitionBannerItemModel, position: Int, container: ViewGroup): View {
            val inflate = LayoutInflater.from(container.context!!).inflate(R.layout.item_channel_banner, null)
            val pictureImageView = inflate.findViewById<SimpleDraweeView>(R.id.banner_image)
            if (item.image != null) {
                pictureImageView.setImageUrl2Webp(item.image!!,
                        ScreenUtils.getScreenWidth(mContext) - ScreenUtils.dip2px(mContext, 30f), mContext.dip(140f))
            }
            pictureImageView.setOnClickListener {
                mContext.getSpider().categoryBannerDidSelectedEvent(categoryId.toString(), item.id!!)
                mContext.startActivity(item.url)
            }
            return inflate
        }
    }

    companion object {
        var array: ArrayList<String> = ArrayList()
        fun put(id: String, categoryId: Int, view: View) {
            if (array.contains(id)) {
                return
            }
            if (!array.isEmpty()) {
                array.clear()
            }
            array.add(id)
            view.context.getSpider().categoryBannerExposureEvent(categoryId.toString(), id)
        }
    }
}