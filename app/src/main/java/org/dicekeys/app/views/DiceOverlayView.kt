package org.dicekeys.app.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import org.dicekeys.app.R
import org.dicekeys.dicekey.Face

class DiceOverlayView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    var sourceDiceView: DiceBaseView? = null
    var targetDiceView: DiceBaseView? = null
    var sourceDiceViewIndex: Int? = null
    var targetDiceViewIndex: Int? = null
    var handDieFaceColor: Int = Color.TRANSPARENT

    val linePaint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 3f
    }

    val handWithStickerDrawable = VectorDrawableCompat.create(context.resources, R.drawable.hand_with_sticker, null)!!

    fun calcHandWithStickerBounds(diceBounds: RectF) : Rect {
        val density = resources.displayMetrics.density
        val width = handWithStickerDrawable.intrinsicWidth * (diceBounds.width() / 25f) / density
        val height = handWithStickerDrawable.intrinsicHeight * (diceBounds.height() / 25f) / density
        return Rect(0, 0, width.toInt(), height.toInt())
    }

    fun calcHandWithStickerOffset(diceBounds: RectF) : PointF {
        return PointF(
                diceBounds.left - diceBounds.width() * 0.63f,
                diceBounds.top - diceBounds.height() * 1.1f)
    }

    fun getDieBounds(diceView: DiceBaseView, col: Int, row: Int) : RectF {
        val xOffset = diceView.left
        val yOffset = diceView.top
        val rect = diceView.getDieBounds(col, row)
        return RectF(
                rect.left + xOffset,
                rect.top + yOffset,
                rect.right + xOffset,
                rect.bottom + yOffset
        )
    }

    fun getDieBounds(diceView: DiceBaseView, index: Int): RectF {
        val col = index % diceView.sizeModel.columns
        val row = index / diceView.sizeModel.columns
        return getDieBounds(diceView, col, row)
    }

    fun getDieFace(diceView: DiceBaseView, index: Int): Face {
        return diceView.facePositions[index].face
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            val sourceDiceView = sourceDiceView
            val targetDiceView = targetDiceView
            val sourceDiceViewIndex = sourceDiceViewIndex
            val targetDiceViewIndex = targetDiceViewIndex

            if (targetDiceView != null && targetDiceViewIndex != null) {
                val bounds2 = getDieBounds(targetDiceView, targetDiceViewIndex)

                canvas.save()
                val offset = calcHandWithStickerOffset(bounds2)
                canvas.translate(offset.x, offset.y)
                handWithStickerDrawable.bounds = calcHandWithStickerBounds(bounds2)
                handWithStickerDrawable.draw(canvas)
                canvas.restore()

                val dieFaceUpright = DieFace(
                        getDieFace(targetDiceView, targetDiceViewIndex),
                        dieSize = bounds2.width(),
                        faceSurfaceColor = handDieFaceColor)
                canvas.save()
                canvas.translate(bounds2.left, bounds2.top)
                dieFaceUpright.draw(canvas)
                canvas.restore()

                if (sourceDiceView != null && sourceDiceViewIndex != null) {
                    val bounds1 = getDieBounds(sourceDiceView, sourceDiceViewIndex)
                    canvas.drawLine(bounds1.right, bounds1.centerY(),
                            bounds2.left, bounds2.centerY(), linePaint)
                }
            }
        }
    }
}