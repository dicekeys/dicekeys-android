package org.dicekeys.trustedapp.view

import android.graphics.*
import android.graphics.drawable.Drawable
import org.dicekeys.dicekey.Face
import org.dicekeys.dicekey.FaceDimensionsFractional

class Undoverline(val face: Face,
                  val faceSize: Float,
                  val isOverline: Boolean,
                  val numberOfDots: Float = 11F,
                  penColor: Int = Color.BLACK,
                  holeColor: Int = Color.WHITE) : Drawable() {

    val width: Float
        get() = faceSize * FaceDimensionsFractional.undoverlineLength

    val height: Float
        get() = faceSize * FaceDimensionsFractional.undoverlineThickness

    val code: Int
        get() = (if (isOverline) face.overlineCode11Bits else face.underlineCode11Bits)?.toInt() ?: 0

    val dotTop: Float
        get() = faceSize * FaceDimensionsFractional.undoverlineMarginAlongLength

    val marginAtStartAndEnd: Float
        get() = faceSize * FaceDimensionsFractional.undoverlineMarginAtLineStartAndEnd

    val dotStep: Float
        get() = (width - 2 * marginAtStartAndEnd) / numberOfDots

    val dotWidth: Float
        get() = faceSize * (FaceDimensionsFractional.undoverlineDotWidth + 0.001F)

    val dotHeight: Float
        get() = faceSize * FaceDimensionsFractional.undoverlineDotHeight

    val bitPositionsSet: List<Int>
        get() {
            val code = this.code
            return (0..10).filter {
                (code and (1 shl (10 - it))) != 0
            }
        }

    private val penPaint = Paint().apply {
        color = penColor
    }

    private val holePaint = Paint().apply {
        color = holeColor
    }

    override fun draw(canvas: Canvas) {
        val colors = listOf(
                Color.WHITE,
                Color.BLUE,
                Color.CYAN,
                Color.GREEN,
                Color.MAGENTA,
                Color.LTGRAY,
                Color.RED,
                Color.YELLOW,
                Color.GRAY,
                Color.BLUE,
                Color.CYAN)
        val width = width
        val height = height
        val dotStep = dotStep
        val dotWidth = dotWidth
        val dotHeight = dotHeight
        val bitSet = bitPositionsSet
        canvas.drawRect(0F, 0F, width, height, penPaint)
        for (i in bitSet) {
            val offsetX = marginAtStartAndEnd + i * dotStep
            val offsetY = dotTop
            holePaint.color = colors[i]
            canvas.drawRect(offsetX, offsetY, offsetX + dotWidth, offsetY + dotHeight, holePaint)
            canvas.drawText(i.toString(), offsetX, offsetY, penPaint)
        }
    }

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    override fun getOpacity(): Int = PixelFormat.OPAQUE
}

class DieFaceUpright(val face: Face,
                     val dieSize: Float,
                     val linearFractionOfFaceRenderedToDieSize: Float,
                     penColor: Int = Color.BLACK,
                     faceSurfaceColor: Int = Color.WHITE,
                     faceBorderColor: Int? = null) : Drawable() {

    val penPaint = Paint().apply {
        color = penColor
    }

    val faceSurfacePaint = Paint().apply {
        color = faceSurfaceColor
    }

    val textPaint = Paint().apply {
        typeface = font
        textSize = fontSize
    }

    val sizeOfRenderedFace: Float
        get() = dieSize * linearFractionOfFaceRenderedToDieSize

    val left: Float
        get() = (dieSize - sizeOfRenderedFace) / 2

    val top: Float
        get() = (dieSize - sizeOfRenderedFace) / 2

    val hCenter: Float
        get() = dieSize / 2f

    val vCenter: Float
        get() = dieSize / 2

    val textBaseline: Float
        get() = top + FaceDimensionsFractional.textBaselineY * sizeOfRenderedFace

    val letterLeft: Float
        get() = left + (1 - FaceDimensionsFractional.textRegionWidth) * sizeOfRenderedFace / 2

    val digitLeft: Float
        get() = left + (1 + FaceDimensionsFractional.spaceBetweenLetterAndDigit) * sizeOfRenderedFace / 2

    val underlineVCenter: Float
        get() = top + ( FaceDimensionsFractional.underlineTop + FaceDimensionsFractional.undoverlineThickness / 2) * sizeOfRenderedFace

    val overlineVCenter: Float
        get() = top + ( FaceDimensionsFractional.overlineTop + FaceDimensionsFractional.undoverlineThickness / 2) * sizeOfRenderedFace

    val overlineTop: Float
        get() = top + FaceDimensionsFractional.overlineTop * sizeOfRenderedFace

    val fontSize: Float
        get() = FaceDimensionsFractional.fontSize * sizeOfRenderedFace

    val font = Typeface.SANS_SERIF

    val halfTextRegionWidth: Float
        get() = FaceDimensionsFractional.textRegionWidth * sizeOfRenderedFace / 2

    val textCenterY: Float
        get() = (
                dieSize
                        // Move down to remove region above capital letter
                        + textPaint.fontMetrics.ascent
                        // Move up to remove region below capital letter
                        - textPaint.fontMetrics.descent
                ) / 2 // take center

    override fun draw(canvas: Canvas) {
//        canvas.drawRoundRect(0F, 0F, dieSize, dieSize, dieSize / 8, dieSize / 8, penPaint)
//        canvas.drawText(face.letter.toString(), (dieSize - halfTextRegionWidth) / 2, textCenterY, faceSurfacePaint)
//        canvas.drawText(face.digit.toString(), (dieSize + halfTextRegionWidth) / 2, textCenterY, faceSurfacePaint)

        canvas.save()
        canvas.translate(hCenter, underlineVCenter)
        Undoverline(face, sizeOfRenderedFace, false, penColor = penPaint.color, holeColor = faceSurfacePaint.color)
                .draw(canvas)
        canvas.restore()

        canvas.save()
        canvas.translate(hCenter, overlineVCenter)
        Undoverline(face, sizeOfRenderedFace, true, penColor = penPaint.color, holeColor = faceSurfacePaint.color)
                .draw(canvas)
        canvas.restore()
    }

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    override fun getOpacity(): Int = PixelFormat.OPAQUE
}