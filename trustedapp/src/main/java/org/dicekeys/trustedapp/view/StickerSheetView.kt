package org.dicekeys.trustedapp.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.SizeF
import org.dicekeys.dicekey.Face
import org.dicekeys.dicekey.FaceDigits
import org.dicekeys.dicekey.FaceLetters
import org.dicekeys.trustedapp.R

class StickerSheetView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : DiceBaseView(context, attrs, defStyleAttr) {

    override val sizeModel = DiceSizeModel(SizeF(1f, 1f), false, columns = 5, rows = 6)

    val diePenPaint = Paint()
    val faceSurfacePaint = Paint()
    val highlighterPaint = Paint()
    val borderPaint = Paint().apply {
        style = Paint.Style.STROKE
    }

    var pageIndex: Int = 0

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.StickerSheetView)
        pageIndex = typedArray.getInteger(R.styleable.StickerSheetView_pageIndex, 0)
        diePenPaint.color = typedArray.getColor(R.styleable.StickerSheetView_penColor, Color.BLACK)
        faceSurfacePaint.color = typedArray.getColor(R.styleable.StickerSheetView_faceColor, Color.WHITE)
        highlighterPaint.color = typedArray.getColor(R.styleable.StickerSheetView_hightlighColor, Colors.highlighter)
        borderPaint.color = typedArray.getColor(R.styleable.StickerSheetView_borderColor, Color.GRAY)
        typedArray.recycle()
    }

    fun setPageIndexForFace(face: Face) {
        pageIndex = FaceLetters.indexOf(face.letter) / sizeModel.columns
    }

    fun getIndexForFace(face: Face): Int {
        val row = sizeModel.rows * FaceDigits.indexOf(face.digit)
        val col = (FaceLetters.indexOf(face.letter) % sizeModel.columns) * sizeModel.columns
        return sizeModel.columns * row + col
    }

    override val facePositions: List<DiePosition>
        get() {
            val posiotions = mutableListOf<DiePosition>()
            for (i in 0 until sizeModel.columns) {
                for (j in 0 until sizeModel.rows) {
                    val face = Face(letter = FaceLetters[pageIndex * sizeModel.columns + i], digit = FaceDigits[j])
                    val position = DiePosition(
                            indexInArray = i * sizeModel.columns + j,
                            face = face,
                            column = i,
                            row = j,
                            drawable = DieFaceUpright(
                                    face = face,
                                    dieSize = faceSize,
                                    penColor = diePenPaint.color,
                                    faceSurfaceColor = faceSurfacePaint.color,
                                    highlightSurfaceColor = highlighterPaint.color,
                                    faceBorderColor = borderPaint.color)
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
                dieFace.highlighted = highlightedIndexes.contains(facePosition.id)
                dieFace.draw(canvas)
                canvas.restore()
            }

            canvas.restore()
        }
    }
}