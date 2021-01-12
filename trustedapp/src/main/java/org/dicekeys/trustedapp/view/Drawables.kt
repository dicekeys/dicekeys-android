package org.dicekeys.trustedapp.view

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
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
        val width = width
        val height = height
        val dotStep = dotStep
        canvas.drawRect(0F, 0F, width, height, penPaint)
        for (i in bitPositionsSet) {
            val offsetX = marginAtStartAndEnd + i * dotStep
            val offsetY = dotTop
            canvas.drawRect(offsetX, offsetY, offsetX + dotWidth, offsetY + dotHeight, holePaint)
            canvas.drawText(i.toString(), offsetX, offsetY, penPaint)
        }
    }

    override fun setAlpha(alpha: Int) {
        penPaint.alpha = alpha
        holePaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        penPaint.colorFilter = colorFilter
        holePaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int = PixelFormat.OPAQUE
}

class DieFaceUpright(val face: Face,
                     val dieSize: Float,
                     val linearFractionOfFaceRenderedToDieSize: Float,
                     val font: Typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD),
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
        textAlign = Paint.Align.CENTER
        letterSpacing = FaceDimensionsFractional.spaceBetweenLetterAndDigit
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
        get() = dieSize / 2f

    val textBaseline: Float
        get() = top + FaceDimensionsFractional.textBaselineY * sizeOfRenderedFace

    val letterLeft: Float
        get() = left + (1 - FaceDimensionsFractional.textRegionWidth) * sizeOfRenderedFace / 2

    val digitLeft: Float
        get() = left + (1 + FaceDimensionsFractional.spaceBetweenLetterAndDigit) * sizeOfRenderedFace / 2

    val underlineTop: Float
        get() = top + FaceDimensionsFractional.underlineTop * sizeOfRenderedFace

    val overlineTop: Float
        get() = top + FaceDimensionsFractional.overlineTop * sizeOfRenderedFace

    val fontSize: Float
        get() = FaceDimensionsFractional.fontSize * sizeOfRenderedFace

    val halfTextRegionWidth: Float
        get() = FaceDimensionsFractional.textRegionWidth * sizeOfRenderedFace / 2

    val textCenterY: Float
        get() = (
                (dieSize / 2) - ((textPaint.descent() + textPaint.ascent()) / 2))

    val text: String
        get() = String.format("%c%c", face.letter, face.digit)

    override fun draw(canvas: Canvas) {
        canvas.drawRoundRect(0F, 0F, dieSize, dieSize, dieSize / 8, dieSize / 8, faceSurfacePaint)
        canvas.drawText(text, dieSize / 2, textCenterY, textPaint)

        canvas.save()
        canvas.translate(left, underlineTop)
        Undoverline(face, sizeOfRenderedFace, false, penColor = penPaint.color, holeColor = faceSurfacePaint.color)
                .draw(canvas)
        canvas.restore()

        canvas.save()
        canvas.translate(left, overlineTop)
        Undoverline(face, sizeOfRenderedFace, true, penColor = penPaint.color, holeColor = faceSurfacePaint.color)
                .draw(canvas)
        canvas.restore()
    }

    override fun setAlpha(alpha: Int) {
        penPaint.alpha = alpha
        faceSurfacePaint.alpha = alpha
        textPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        penPaint.colorFilter = colorFilter
        faceSurfacePaint.colorFilter = colorFilter
        textPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int = PixelFormat.OPAQUE
}