package org.dicekeys.trustedapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.util.SizeF
import android.view.View
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import org.dicekeys.dicekey.SimpleDiceKey
import org.dicekeys.trustedapp.view.*

class DiceKeyView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : DiceKeyBaseView(context, attrs, defStyleAttr) {

    companion object {
        val TAG = DiceKeyView::class.java.simpleName
    }

    var diceKey: SimpleDiceKey? = null
    var centerFace: Face? = null
    var showLidTab: Boolean = false
    var leaveSpaceForTab: Boolean = false
    var showDiceAtIndexes: Set<Int>? = null

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DiceKeyView)
        leaveSpaceForTab = typedArray.getBoolean(R.styleable.DiceKeyView_leaveSpaceForTab, false)
        showLidTab = typedArray.getBoolean(R.styleable.DiceKeyView_showLidTab, false)
        typedArray.recycle()
    }

    val computedDiceKeyToRender: DiceKey<Face>
        get() = diceKey ?: // If the caller specified a diceKey, use that
            if (centerFace != null)
            // If the caller specified a center face, create a
            // diceKey with just that face for all dice
            DiceKey(faces = (0 until 25).map { centerFace!! })
            // If no diceKey was specified, we'll render the example diceKey
            else DiceKey.example

    val computedShowDiceAtIndexes: Set<Int>
        get() = showDiceAtIndexes ?:
            // If the caller did not directly specify which indexes to show,
            // show only the center die if the diceKey is specified via centerFace,
            // and how all 25 dice otherwise
            if  (diceKey == null && centerFace != null)
            // Just the center die
            listOf(12).toSet() else
            // all 25 dice
            (0 until 25).toSet()

    data class DiePosition(val indexInArray: Int, val face: Face) {
        val id: Int get() = indexInArray
        val column: Int get() = indexInArray % 5
        val row: Int get() = indexInArray / 5
    }

    val facePositions: List<DiePosition>
        get() = (0 until 25).map { DiePosition(it, computedDiceKeyToRender.faces[it]) }

    val diceBoxPaint = Paint().apply {
        color = Colors.diceColor
    }

    val diceBoxDieSlotPaint = Paint().apply {
        color = Colors.diceBoxDieSlot
    }

    val diePenPaint = Paint().apply {
        color = Color.BLACK
    }

    val faceSurfacePaint = Paint().apply {
        color = Color.WHITE
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null) {
            canvas.drawRoundRect(0f, 0f, linearSizeOfBox, linearSizeOfBox,
                    sizeModel.boxCornerRadius, sizeModel.boxCornerRadius, diceBoxPaint)

            if (showLidTab) {
                canvas.save()
                canvas.translate(linearSizeOfBox / 2f - sizeModel.lidTabRadius, linearSizeOfBox)
                val dieLid = DieLidShape(sizeModel.lidTabRadius, diceBoxPaint.color)
                dieLid.draw(canvas)
                canvas.restore()
            }
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
                if (computedShowDiceAtIndexes.contains(facePosition.id)) {
                    val dieFace = DieFaceUpright(facePosition.face, faceSize)
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