package com.goldenowl.ecommerce.ui.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.Button
import com.goldenowl.ecommerce.R

@SuppressLint("AppCompatCustomView")
class RedButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : Button(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        // Paint styles used for rendering are initialized here. This
        // is a performance optimization, since onDraw() is called
        // for every screen refresh.
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null) {
            canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), 5F, paint)
        }
    }

    override fun setBackgroundResource(resid: Int) {
        super.setBackgroundResource(R.drawable.bg_rounded_button)
    }

    override fun setBackgroundColor(color: Int) {
        super.setBackgroundColor(getResources().getColor(R.color.red_dark))
    }
}