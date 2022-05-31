package com.goldenowl.ecommerce.ui.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Region
import android.util.AttributeSet
import android.widget.FrameLayout

class RoundedCornerLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    var cornerRadius = 0.0
    private fun init(context: Context, attrs: AttributeSet?, defStyle: Int) {
        val metrics = context.resources.displayMetrics
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun draw(canvas: Canvas) {
        val count = canvas.save()
        val path = Path()
        path.addRoundRect(
            RectF(0F, 0F, canvas.width.toFloat(), canvas.height.toFloat()),
            cornerRadius.toFloat(),
            cornerRadius.toFloat(),
            Path.Direction.CW
        )
        canvas.clipPath(path, Region.Op.REPLACE)
        canvas.clipPath(path)
        super.draw(canvas)
        canvas.restoreToCount(count)
    }

    init {
        init(context, attrs, defStyle)
    }
}