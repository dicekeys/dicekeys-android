package org.dicekeys.trustedapp.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.SizeF
import android.view.View
import org.dicekeys.dicekey.Face

abstract class DiceKeyBaseView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    data class DiePosition(
            val indexInArray: Int,
            val face: Face,
            val column: Int,
            val row: Int,
            val drawable: Drawable) {
        val id: Int get() = indexInArray
    }

    abstract val sizeModel: DiceKeySizeModel
    val linearSizeOfBox: Float get() = sizeModel.linearSizeOfBox
    val dieStepSize: Float get() = sizeModel.stepSize
    val faceSize: Float get() = sizeModel.faceSize

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
                sizeModel.width.toInt(),
                sizeModel.height.toInt()
         )
    }
}