package org.dicekeys.trustedapp.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.SizeF
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import org.dicekeys.trustedapp.R

class StickerTargetSheetView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : DiceBaseView(context, attrs, defStyleAttr) {

    var diceKey: DiceKey<Face>
    var showDiceAtIndexes: Set<Int>

    override val sizeModel = DiceSizeModel(SizeF(0f, 0f), false, extraVerticalMarginOfBoxEdgeAsFractionOfDieSize = 0.5f)

    val diePenPaint = Paint()
    val faceSurfacePaint = Paint()
    val highlighterPaint = Paint()
    val borderPaint = Paint().apply {
        style = Paint.Style.STROKE
    }

    val stickerTargetDrawable = VectorDrawableCompat.create(context.resources, R.drawable.sticker_target, null)

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.StickerTargetSheetView)
        val diceKeyContent = DiceKeyContent.values()[typedArray.getInt(R.styleable.StickerTargetSheetView_dicekey, DiceKeyContent.HALF_EMPTY.ordinal)]
        diceKey = when(diceKeyContent) {
            DiceKeyContent.RANDOM -> DiceKey.createFromRandom()
            else -> DiceKey.example
        }
        showDiceAtIndexes = when(diceKeyContent) {
            DiceKeyContent.EMPTY -> listOf<Int>().toSet()
            DiceKeyContent.HALF_EMPTY -> (0 until diceKey.faces.size / 2).toSet()
            else -> (0 until diceKey.faces.size).toSet()
        }
        diePenPaint.color = typedArray.getColor(R.styleable.StickerTargetSheetView_penColor, Color.BLACK)
        faceSurfacePaint.color = typedArray.getColor(R.styleable.StickerTargetSheetView_faceColor, Color.WHITE)
        highlighterPaint.color = typedArray.getColor(R.styleable.StickerTargetSheetView_hightlighColor, Colors.highlighter)
        borderPaint.color = typedArray.getColor(R.styleable.StickerTargetSheetView_borderColor, Color.GRAY)
        typedArray.recycle()
    }

    val computedDiceKeyToRender: DiceKey<Face>
        get() = diceKey

    val computedShowDiceAtIndexes: Set<Int>
        get() = showDiceAtIndexes


    override val facePositions: List<DiePosition>
        get() = (0 until sizeModel.columns * sizeModel.rows).map {
            DiePosition(indexInArray =  it,
                    face =  computedDiceKeyToRender.faces[it],
                    column = it % sizeModel.columns,
                    row = it / sizeModel.rows,
                    drawable = DieFaceUpright(
                            face = computedDiceKeyToRender.faces[it],
                            dieSize = faceSize,
                            penColor = diePenPaint.color,
                            faceSurfaceColor = faceSurfacePaint.color,
                            highlightSurfaceColor = highlighterPaint.color,
                            faceBorderColor = borderPaint.color)
            )
        }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null) {
            canvas.drawRect(0f, 0f, (width - 1).toFloat(), (height - 1).toFloat() , borderPaint)
            canvas.save()
            canvas.translate(sizeModel.marginLeft, sizeModel.marginTop)

            stickerTargetDrawable?.setBounds(0, 0, faceSize.toInt(), faceSize.toInt())

            for (facePosition in facePositions) {
                canvas.save()
                canvas.translate(
                        dieStepSize * facePosition.column,
                        dieStepSize * facePosition.row
                )

                if (computedShowDiceAtIndexes.contains(facePosition.id)) {
                    val dieFace = facePosition.drawable
                    dieFace.highlighted = highlightedIndexes.contains(facePosition.id)
                    dieFace.draw(canvas)
                } else {
                    stickerTargetDrawable?.draw(canvas)
                }
                canvas.restore()
            }

            canvas.restore()
        }
    }
}