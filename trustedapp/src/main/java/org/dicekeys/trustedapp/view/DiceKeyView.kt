package org.dicekeys.trustedapp

import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.Shape
import android.util.SizeF
import java.lang.Math.min

class DiceKeySizeModel(val bounds: SizeF, val hasTab: Boolean = false) {
    constructor(size1d: Float, hasTab: Boolean) : this(SizeF(size1d, size1d), hasTab) {}

    val fractionOfVerticalSpaceRequiredForTab = 0.1F

    val aspectRatio: Float
        get() = if (hasTab) 1 - this.fractionOfVerticalSpaceRequiredForTab else 1F

    val width: Float
        get() = min(bounds.width, bounds.width * aspectRatio)

    val height: Float
        get() = min(bounds.height, bounds.height * aspectRatio)

    val size: SizeF
        get() = SizeF(width, height)

    val fractionOfVerticalSpaceUsedByTab: Float
        get() = if (hasTab) fractionOfVerticalSpaceRequiredForTab else 0F

    val fractionOfVerticalSpaceUsedByBox: Float
        get() = 1 - fractionOfVerticalSpaceUsedByTab

    val linearSizeOfBox: Float
        get() = width

    val lidTabRadius: Float
        get() = height * fractionOfVerticalSpaceUsedByTab

    val boxCornerRadius: Float
        get() =  linearSizeOfBox / 50F

    val offsetToBoxCenterY: Float
        get() = -lidTabRadius / 2

    val centerY: Float
        get() = height / 2

    val boxCenterY: Float
        get() = centerY + offsetToBoxCenterY

    val centerX: Float
        get() = bounds.width / 2

    val marginOfBoxEdgeAsFractionOfDieSize = 0.25F
    val distanceBetweenFacesAsFractionOfFaceSize = 0.15F

    val faceSize: Float
        get() = (linearSizeOfBox / (5 +
                4 * distanceBetweenFacesAsFractionOfFaceSize +
                2 * marginOfBoxEdgeAsFractionOfDieSize))

    val faceRadiusAsFractionOfSize = 1/8.0F

    val faceRadius: Float
        get() = faceSize * faceRadiusAsFractionOfSize

    val stepSize: Float
        get() =  (1 + distanceBetweenFacesAsFractionOfFaceSize) * faceSize
}

class DieLidShape(val radius: Float, val color: Int): Drawable() {
    var _alpha: Int = 255
    val paint: Paint
        get() {
            val p = Paint()
            p.alpha = _alpha
            p.color = color
            return p
        }

    override fun draw(canvas: Canvas) {
        val path = Path()
        path.addArc(0F, -radius, radius * 2F,radius, 0F, 180F)
        canvas.drawPath(path, paint)
    }

    override fun setAlpha(alpha: Int) {
        _alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }

    override fun getOpacity(): Int = PixelFormat.OPAQUE
}

class DiceKeyView {
}