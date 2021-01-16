package org.dicekeys.trustedapp.view

import android.graphics.*
import android.graphics.drawable.Drawable

class DieLidShape(val radius: Float, val color: Int): Drawable() {
    var _alpha: Int = 255
    val paint = Paint().apply {
        color = this@DieLidShape.color
    }

    override fun draw(canvas: Canvas) {
        val path = Path()
        path.addArc(0F, -radius, radius * 2F,radius, 0F, 180F)
        canvas.drawPath(path, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int = PixelFormat.OPAQUE
}