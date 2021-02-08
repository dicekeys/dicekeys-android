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
            return (0 until numberOfDots.toInt()).filter {
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
        canvas.drawRect(0F, 0F, width, height, penPaint)
        for (i in bitPositionsSet) {
            val offsetX = marginAtStartAndEnd + i * dotStep
            val offsetY = dotTop
            canvas.drawRect(offsetX, offsetY, offsetX + dotWidth, offsetY + dotHeight, holePaint)
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

class DieFace(val face: Face,
              var dieSize: Float,
              val linearFractionOfFaceRenderedToDieSize: Float = 5f/8f,
              val font: Typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD),
              penColor: Int = Color.BLACK,
              faceSurfaceColor: Int = Color.WHITE,
              highlightSurfaceColor: Int = Color.YELLOW,
              faceBorderColor: Int? = null) : Drawable() {

    val borderPaint: Paint?

    init {
        borderPaint = if (faceBorderColor != null) Paint().apply {
            color = faceBorderColor
            style = Paint.Style.STROKE
        } else null
    }

    val penPaint = Paint().apply {
        color = penColor
    }

    val faceSurfacePaint = Paint().apply {
        color = faceSurfaceColor
    }

    val highlightSurfacePaint = Paint().apply {
        color = highlightSurfaceColor
    }

    val textPaint = Paint().apply {
        typeface = font
        textAlign = Paint.Align.CENTER
        letterSpacing = FaceDimensionsFractional.spaceBetweenLetterAndDigit
    }

    val sizeOfRenderedFace: Float
        get() = dieSize * linearFractionOfFaceRenderedToDieSize

    val left: Float
        get() = (dieSize - sizeOfRenderedFace) / 2

    val top: Float
        get() = (dieSize - sizeOfRenderedFace) / 2

    val underlineTop: Float
        get() = top + FaceDimensionsFractional.underlineTop * sizeOfRenderedFace

    val overlineTop: Float
        get() = top + FaceDimensionsFractional.overlineTop * sizeOfRenderedFace

    val fontSize: Float
        get() = FaceDimensionsFractional.fontSize * sizeOfRenderedFace

    val textCenterY: Float
        get() = (
                (dieSize / 2) - ((textPaint.descent() + textPaint.ascent()) / 2))

    val text: String
        get() = String.format("%c%c", face.letter, face.digit)

    var highlighted: Boolean = false

    override fun draw(canvas: Canvas) {
        canvas.save()
        canvas.rotate(face.orientationAsDegrees, dieSize / 2, dieSize / 2)

        if (highlighted) {
            canvas.drawRoundRect(0F, 0F, dieSize, dieSize, dieSize / 8, dieSize / 8, highlightSurfacePaint)
        } else {
            canvas.drawRoundRect(0F, 0F, dieSize, dieSize, dieSize / 8, dieSize / 8, faceSurfacePaint)
        }
        if (borderPaint != null) {
            canvas.drawRoundRect(1F, 1F, dieSize - 1, dieSize - 1, dieSize / 8, dieSize / 8, borderPaint)
        }
        textPaint.textSize = fontSize
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