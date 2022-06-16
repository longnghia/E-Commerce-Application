package com.goldenowl.ecommerce.ui.customview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.goldenowl.ecommerce.R
import kotlinx.android.synthetic.main.layout_my_button.view.*

@SuppressLint("AppCompatCustomView")
class MyButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        val l = LayoutInflater.from(context).inflate(R.layout.layout_my_button, this, true)
        val typedArray = context.obtainStyledAttributes(attrs,R.styleable.MyButton)
        val text = typedArray.getText(R.styleable.MyButton_mbText)
        l.button.text = text
    }
}