package org.dicekeys.trustedapp.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.SizeF
import android.view.View
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import org.dicekeys.dicekey.FaceDigits
import org.dicekeys.dicekey.FaceLetters

class StickerSheetView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : DiceKeyBaseView(context, attrs, defStyleAttr) {

    override val sizeModel = DiceKeySizeModel(SizeF(1f, 1f), false, columns = 5, rows = 6)

    val borderPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
    }

    val facePositions: List<DiePosition>
        get() {
            val posiotions = mutableListOf<DiePosition>()
            for (i in 0 until sizeModel.columns) {
                for (j in 0 until sizeModel.rows) {
                    val face = Face(letter = FaceLetters[i], digit = FaceDigits[j])
                    val position = DiePosition(
                            indexInArray = i * sizeModel.columns + j,
                            face = face,
                            column = i,
                            row = j,
                            drawable = DieFaceUpright(face, faceSize, faceBorderColor = Color.GRAY)
                    )
                    posiotions.add(position)
                }
            }
            return posiotions
        }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null) {
            canvas.drawRect(0f, 0f, (width - 1).toFloat(), (height - 1).toFloat() , borderPaint)
            canvas.save()
            canvas.translate(
                    sizeModel.marginOfBoxEdgeAsFractionOfDieSize * faceSize,
                    sizeModel.marginOfBoxEdgeAsFractionOfDieSize * faceSize
            )

            for (facePosition in facePositions) {
                canvas.save()
                canvas.translate(
                        dieStepSize * facePosition.column,
                        dieStepSize * facePosition.row
                )
                val dieFace = facePosition.drawable
                dieFace.draw(canvas)
                canvas.restore()
            }

            canvas.restore()
        }
    }
}