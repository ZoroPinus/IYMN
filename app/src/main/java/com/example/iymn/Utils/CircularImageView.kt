package com.example.iymn.Utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class CircularImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {

    private val path = Path()
    private val rect = RectF()

    override fun onDraw(canvas: Canvas) {
        val halfWidth = width / 2f
        val halfHeight = height / 2f
        val radius = Math.min(halfWidth, halfHeight)

        path.reset()
        path.addCircle(halfWidth, halfHeight, radius, Path.Direction.CW)

        canvas.clipPath(path)

        super.onDraw(canvas)
    }
}