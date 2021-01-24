package org.dicekeys.trustedapp.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import org.dicekeys.dicekey.Face
import org.dicekeys.trustedapp.R

class DiceOverlayView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    var diceView1: DiceKeyBaseView? = null
    var diceView2: DiceKeyBaseView? = null

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

    fun getDieBounds(diceView: DiceKeyBaseView, col: Int, row: Int) : RectF {
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

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            val diceView1 = diceView1
            val diceView2 = diceView2

            if (diceView2 != null) {
                val bounds2 = getDieBounds(diceView2, 2, 2)

                canvas.save()
                val offset = calcHandWithStickerOffset(bounds2)
                canvas.translate(offset.x, offset.y)
                handWithStickerDrawable.bounds = calcHandWithStickerBounds(bounds2)
                handWithStickerDrawable.draw(canvas)
                canvas.restore()

                val dieFaceUpright = DieFaceUpright(Face('H', '1'), dieSize = bounds2.width())
                canvas.save()
                canvas.translate(bounds2.left, bounds2.top)
                dieFaceUpright.draw(canvas)
                canvas.restore()

                if (diceView1 != null) {
                    val bounds1 = getDieBounds(diceView1, 2, 2)
                    canvas.drawLine(bounds1.right, bounds1.centerY(),
                            bounds2.left, bounds2.centerY(), linePaint)
                }
            }
        }
    }
}