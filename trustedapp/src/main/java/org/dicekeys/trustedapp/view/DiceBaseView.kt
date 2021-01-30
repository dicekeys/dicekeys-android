package org.dicekeys.trustedapp.view

import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import android.util.SizeF
import android.view.View
import org.dicekeys.dicekey.Face

abstract class DiceBaseView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    data class DiePosition(
            val indexInArray: Int,
            val face: Face,
            val column: Int,
            val row: Int,
            val drawable: DieFaceUpright) {
        val id: Int get() = indexInArray
    }

    abstract val sizeModel: DiceSizeModel
    abstract val facePositions: List<DiePosition>
    protected val linearSizeOfBox: Float get() = sizeModel.linearSizeOfBox
    protected val dieStepSize: Float get() = sizeModel.stepSize
    protected val faceSize: Float get() = sizeModel.faceSize

    open fun highlightedIndex(): Int? = null
    open fun highlightedFace(): Face? = null

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

    fun getDieBounds(col: Int, row: Int): RectF {
        val left = sizeModel.marginLeft + col * sizeModel.stepSize
        val top = sizeModel.marginTop + row * sizeModel.stepSize
        return RectF(left, top, left + sizeModel.faceSize, top + sizeModel.faceSize)
    }
}