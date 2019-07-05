package com.shuashuakan.android.modules.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.shuashuakan.android.R

/**
 * Author:  lijie
 * Date:   2018/11/20
 * Email:  2607401801@qq.com
 */
class EmptyView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private var iv: ImageView
    private var title: TextView
    private var content: TextView
    private var button: TextView

    init {
        View.inflate(context, R.layout.cus_empty_layout, this)
        iv = findViewById(R.id.empty_iv)
        title = findViewById(R.id.empty_title)
        content = findViewById(R.id.empty_content)
        button = findViewById(R.id.empty_button)

        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.EmptyView)
        val ivValue = typeArray.getResourceId(R.styleable.EmptyView_iv, R.drawable.empty_states)
        val titleValue = typeArray.getString(R.styleable.EmptyView_title)
        val contentValue = typeArray.getString(R.styleable.EmptyView_contentEmpty)
        val btnValue = typeArray.getString(R.styleable.EmptyView_btnText)
        typeArray.recycle()
        title.text = titleValue
        content.text = contentValue
        button.text = btnValue
        iv.setImageResource(ivValue)

        invalidate()
    }

    fun setIv(ivValue: Int): EmptyView {
        iv.setImageResource(ivValue)
        return this
    }

    fun hideTitle(): EmptyView {
        title.visibility = View.GONE
        return this
    }

    fun setClickBtn(text: String, clickListener: () -> Unit) {
        button.visibility = View.VISIBLE
        button.text = text
        button.setOnClickListener {
            clickListener.invoke()
        }
    }

    fun setTitle(titleValue: String): EmptyView {
        title.text = titleValue
        return this
    }

    fun setContent(contentValue: String): EmptyView {
        content.text = contentValue
        return this
    }
}