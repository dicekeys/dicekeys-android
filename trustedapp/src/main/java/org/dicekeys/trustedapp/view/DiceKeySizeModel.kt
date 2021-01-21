package org.dicekeys.trustedapp.view

import android.util.SizeF

class DiceKeySizeModel(
        var bounds: SizeF,
        val hasTab: Boolean = false,
        val columns: Int = 5,
        val rows: Int = 5) {
    constructor(size1d: Float, hasTab: Boolean) : this(SizeF(size1d, size1d), hasTab) {}

    val fractionOfVerticalSpaceRequiredForTab = 0.1F

    val aspectRatio: Float
        get() = if (hasTab)
            rows.toFloat()/columns.toFloat() + this.fractionOfVerticalSpaceRequiredForTab
            else rows.toFloat()/columns.toFloat()

    val width: Float
        get() = Math.min(bounds.width, bounds.width * aspectRatio)

    val height: Float
        get() = Math.min(bounds.height, bounds.height * aspectRatio)

    val boxWidth: Float
        get() = bounds.width * rows.toFloat()/columns.toFloat()

    val boxHeight: Float
        get() = bounds.height * rows.toFloat()/columns.toFloat()

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