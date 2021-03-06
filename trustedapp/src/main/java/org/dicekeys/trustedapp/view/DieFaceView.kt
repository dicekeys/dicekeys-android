package org.dicekeys.trustedapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import org.dicekeys.dicekey.Face
import org.dicekeys.trustedapp.view.DieFace


class DieFaceView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    var face = Face('A', '0')
    var dieSize = 100F
    var linearFractionOfFaceRenderedToDieSize = 5F/8

    private val penPaint = Paint().apply {
        color = Color.BLACK
    }

    private val faceSurfacePaint = Paint().apply {
        color = Color.WHITE
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        if (canvas != null) {
            val drawable = DieFace(face, dieSize, linearFractionOfFaceRenderedToDieSize, penColor = penPaint.color, faceSurfaceColor = faceSurfacePaint.color)
            drawable.draw(canvas)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(dieSize.toInt(), dieSize.toInt())
    }
}