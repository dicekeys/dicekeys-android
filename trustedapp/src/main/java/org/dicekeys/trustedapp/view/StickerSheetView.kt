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

@Deprecated("Moved to :app")
class StickerSheetView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : DiceBaseView(context, attrs, defStyleAttr) {

    override val sizeModel = DiceSizeModel(SizeF(1f, 1f), false, columns = 5, rows = 6)

    val diePenPaint = Paint()
    val faceSurfacePaint = Paint()
    val highlighterPaint = Paint()
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
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

    val firstLetter: Char
        get() = FaceLetters[pageIndex * sizeModel.columns]

    val lastLetter: Char
        get() = FaceLetters[pageIndex * sizeModel.columns + sizeModel.columns - 1]

    fun setPageIndexForFace(face: Face) {
        pageIndex = FaceLetters.indexOf(face.letter) / sizeModel.columns
    }

    fun getIndexForFace(face: Face): Int {
        val row = FaceDigits.indexOf(face.digit)
        val col = (FaceLetters.indexOf(face.letter) % sizeModel.columns)
        val index = sizeModel.columns * row + col
        return index
    }

    override val facePositions: List<DiePosition>
        get() {
            val posiotions = mutableListOf<DiePosition>()
            for (i in 0 until sizeModel.rows) {
                for (j in 0 until sizeModel.columns) {
                    val face = Face(letter = FaceLetters[pageIndex * sizeModel.columns + j], digit = FaceDigits[i])
                    val position = DiePosition(
                            indexInArray = i * sizeModel.columns + j,
                            face = face,
                            column = j,
                            row = i,
                            drawable = DieFace(
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
            canvas.drawRect(1f, 1f, (width - 1).toFloat(), (height - 1).toFloat() , borderPaint)
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