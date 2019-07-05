package com.shuashuakan.android.modules.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout

import com.shuashuakan.android.R
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.event.DanmakaControlEvent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo

/**
 * @author: zhaoningqiang
 * @time: 2019/5/9
 * @Description: 弹幕控制按钮的容器
 */
class DanmakuContainer : RelativeLayout {

    private var mDanmakaEvent: DanmakaControlEvent? = null
    private var barrageToggle: ImageView? = null
    private var divider: View? = null
//    private var sendBarrage: TextView? = null

    private val compositeDisposable = CompositeDisposable()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    fun setDanmakaEvent(danmakaEvent: DanmakaControlEvent?) {
        this.mDanmakaEvent = danmakaEvent
    }


    override fun onFinishInflate() {
        super.onFinishInflate()
        barrageToggle = findViewById(R.id.barrageToggle)
        divider = findViewById(R.id.divider)
//        sendBarrage = findViewById(R.id.sendBarrage)

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        this.translationY = 0f
        setBackgroundColor(Color.TRANSPARENT)
        divider?.visibility = View.VISIBLE
        if (mDanmakaEvent != null) {
            barrageToggle?.isSelected = mDanmakaEvent?.isShow ?: false
        }

        RxBus.get().toFlowable().subscribe { event ->
            when (event) {
                is DanmakaControlEvent ->{
                    barrageToggle?.isSelected = event.isShow
                    mDanmakaEvent = event
                }

            }
        }.addTo(compositeDisposable)



    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        compositeDisposable.clear()
    }

}
