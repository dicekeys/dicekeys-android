package org.dicekeys.app.views

import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import android.util.SizeF
import android.view.View
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import kotlin.properties.Delegates

abstract class DiceBaseView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    var diceKey: DiceKey<Face> by Delegates.observable(DiceKey.example) { _, _, _ ->
        invalidate()
    }

    data class DiePosition(
            val indexInArray: Int,
            val face: Face,
            val column: Int,
            val row: Int,
            val drawable: DieFace) {
        val id: Int get() = indexInArray
    }

    enum class DiceKeyContent {
        EXAMPLE, EMPTY, HALF_EMPTY, RANDOM
    }

    abstract val sizeModel: DiceSizeModel
    abstract val facePositions: List<DiePosition>
    protected val linearSizeOfBox: Float get() = sizeModel.linearSizeOfBox
    protected val dieStepSize: Float get() = sizeModel.stepSize
    protected val faceSize: Float get() = sizeModel.faceSize

    var highlightedIndexes: Set<Int> = HashSet()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        val height = MeasureSpec.getSize(heightMeasureSpec).toFloat()
        val aspectRatio = width / height
        if (aspectRatio <= sizeModel.aspectRatio) {
            sizeModel.bounds = SizeF(width / sizeModel.aspectRatio, width)
        } else {
            sizeModel.bounds = SizeF(height, height / sizeModel.aspectRatio)
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