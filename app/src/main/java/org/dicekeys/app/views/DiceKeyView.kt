package org.dicekeys.app.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import org.dicekeys.app.R
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face

class DiceKeyView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : DiceBaseView(context, attrs, defStyleAttr) {

    companion object {
        val TAG = DiceKeyView::class.java.simpleName
    }

    private var _diceKey: DiceKey<Face>
    var diceKey: DiceKey<Face>
        get() = _diceKey
        set(value) {
            _diceKey = value
            invalidate()
        }
    var centerFace: Face? = null
    var showLidTab: Boolean = true
    var leaveSpaceForTab: Boolean = true
    var showDiceAtIndexes: Set<Int>? = null
    override val sizeModel = DiceSizeModel(1f, leaveSpaceForTab)

    val diceBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val diceBoxDieSlotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val diePenPaint = Paint()
    val faceSurfacePaint = Paint()
    val highlighterPaint = Paint()

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DiceKeyView)
        leaveSpaceForTab = typedArray.getBoolean(R.styleable.DiceKeyView_leaveSpaceForTab, false)
        showLidTab = typedArray.getBoolean(R.styleable.DiceKeyView_showLidTab, false)
        val diceKeyContent = DiceKeyContent.values()[typedArray.getInt(R.styleable.DiceKeyView_dicekey, 0)]
        _diceKey = when(diceKeyContent) {
            DiceKeyContent.RANDOM -> DiceKey.createFromRandom()
            else -> DiceKey.example
        }
        showDiceAtIndexes = when(diceKeyContent) {
            DiceKeyContent.EMPTY -> listOf<Int>().toSet()
            DiceKeyContent.HALF_EMPTY -> (0 until diceKey.faces.size / 2).toSet()
            else -> (0 until diceKey.faces.size).toSet()
        }
        if (diceKeyContent == DiceKeyContent.HALF_EMPTY) {
            highlightedIndexes = listOf(diceKey.faces.size / 2 + 1).toSet()
        }
        diceBoxPaint.color = typedArray.getColor(R.styleable.DiceKeyView_boxColor, ContextCompat.getColor(context, R.color.diceColor))
        diceBoxDieSlotPaint.color = typedArray.getColor(R.styleable.DiceKeyView_slotColor, ContextCompat.getColor(context, R.color.diceBoxDieSlot))
        diePenPaint.color = typedArray.getColor(R.styleable.DiceKeyView_penColor, Color.BLACK)
        faceSurfacePaint.color = typedArray.getColor(R.styleable.DiceKeyView_faceColor, Color.WHITE)
        highlighterPaint.color = typedArray.getColor(R.styleable.DiceKeyView_hightlighColor, ContextCompat.getColor(context, R.color.highlighter))
        typedArray.recycle()
    }

    val computedDiceKeyToRender: DiceKey<Face>
        get() = diceKey

    val computedShowDiceAtIndexes: Set<Int>
        get() = showDiceAtIndexes ?:
            // If the caller did not directly specify which indexes to show,
            // show only the center die if the diceKey is specified via centerFace,
            // and how all 25 dice otherwise
            if  (centerFace != null)
            // Just the center die
            listOf(12).toSet() else
            // all 25 dice
            (0 until 25).toSet()


    override val facePositions: List<DiePosition>
        get() = (0 until sizeModel.columns * sizeModel.rows).map {
            DiePosition(indexInArray =  it,
                    face =  computedDiceKeyToRender.faces[it],
                    column = it % sizeModel.columns,
                    row = it / sizeModel.rows,
                    drawable = DieFace(
                            face = computedDiceKeyToRender.faces[it],
                            dieSize = faceSize,
                            penColor = diePenPaint.color,
                            faceSurfaceColor = faceSurfacePaint.color,
                            highlightSurfaceColor = highlighterPaint.color
                    ))
        }

    val dieLidShape = DieLidShape(sizeModel.lidTabRadius, diceBoxPaint.color)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            canvas.drawRoundRect(0f, 0f, sizeModel.boxHeight, sizeModel.boxWidth,
                    sizeModel.boxCornerRadius, sizeModel.boxCornerRadius, diceBoxPaint)

            if (showLidTab) {
                canvas.save()
                canvas.translate(linearSizeOfBox / 2f - sizeModel.lidTabRadius, sizeModel.bounds.height - sizeModel.lidTabRadius)
                dieLidShape.draw(canvas)
                canvas.restore()
            }

            canvas.save()
            canvas.translate(sizeModel.marginLeft, sizeModel.marginTop)
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
                    canvas.drawRoundRect(0f, 0f, faceSize, faceSize,
                        sizeModel.faceRadius, sizeModel.faceRadius, diceBoxDieSlotPaint)
                }
                canvas.restore()
            }
            canvas.restore()
        }
    }
}