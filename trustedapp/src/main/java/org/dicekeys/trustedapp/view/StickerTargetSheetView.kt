package org.dicekeys.trustedapp.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.SizeF
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.trustedapp.R

class StickerTargetSheetView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : DiceKeyBaseView(context, attrs, defStyleAttr) {


    override val sizeModel = DiceKeySizeModel(SizeF(0f, 0f), false, extraVerticalMarginOfBoxEdgeAsFractionOfDieSize = 0.5f)

    val borderPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
    }

    val stickerTargetDrawable = VectorDrawableCompat.create(context.resources, R.drawable.sticker_target, null)

    override val facePositions: MutableList<DiePosition> = ArrayList<DiePosition>().apply {
        val faces = DiceKey.example.faces
        for (index in 0 until 13) {
            add(DiePosition(
                    indexInArray = index,
                    face = faces[index],
                    column = index % sizeModel.columns,
                    row = index / sizeModel.rows,
                    drawable = DieFaceUpright(face = faces[index], dieSize = faceSize, faceBorderColor = Color.GRAY)
            ))
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null) {

            canvas.drawRect(0f, 0f, (width - 1).toFloat(), (height - 1).toFloat() , borderPaint)
            canvas.save()
            canvas.translate(sizeModel.marginLeft, sizeModel.marginTop)

            for (facePosition in facePositions) {
                canvas.save()
                canvas.translate(
                        dieStepSize * facePosition.column,
                        dieStepSize * facePosition.row
                )
                val dieFace = facePosition.drawable
                dieFace.dieSize = faceSize
                dieFace.draw(canvas)
                canvas.restore()
            }

            stickerTargetDrawable?.setBounds(0, 0, faceSize.toInt(), faceSize.toInt())
            for (index in facePositions.size until sizeModel.columns * sizeModel.rows) {
                val column = index % sizeModel.columns
                val row = index / sizeModel.rows
                canvas.save()
                canvas.translate(
                        dieStepSize * column,
                        dieStepSize * row
                )
                stickerTargetDrawable?.draw(canvas)
                canvas.restore()
            }

            canvas.restore()
        }
    }
}