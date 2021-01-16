package org.dicekeys.trustedapp.view

import android.content.Context
import android.util.AttributeSet
import android.util.SizeF
import android.view.View

abstract class DiceKeyBaseView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    val sizeModel = DiceKeySizeModel(0f, false)
    val linearSizeOfBox: Float get() = sizeModel.linearSizeOfBox
    val dieStepSize: Float get() = sizeModel.stepSize
    val faceSize: Float get() = sizeModel.faceSize

    open fun hasSpaceForTab() = false

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val aspectRatio = width.toFloat() / height.toFloat()
        if (aspectRatio < sizeModel.aspectRatio) {
            sizeModel.bounds = SizeF(width.toFloat() / sizeModel.aspectRatio, width.toFloat())
        } else {
            sizeModel.bounds = SizeF(height.toFloat(), height.toFloat() / sizeModel.aspectRatio)
        }

        setMeasuredDimension(
                linearSizeOfBox.toInt(),
                if (hasSpaceForTab()) (linearSizeOfBox / sizeModel.aspectRatio).toInt() else linearSizeOfBox.toInt()
        )
    }
}