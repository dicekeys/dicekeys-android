package com.keysqr.readkeysqr

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import java.lang.Exception


class KeySqrRenderer(private val typeface: Typeface?) {

    // Typeface inconsolata = Typeface.createFromAsset(assetManager, pathToFont) / ResourcesCompat.getFont(context, R.font.Inconsolata700)
    private fun textPaintForFaceSize(faceSize: Float): Paint {
        val paint = Paint()
        if (typeface != null) {
            paint.typeface = typeface
        }
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = faceSize * FaceDimensionsFractional.fontSize
        paint.color = Color.BLACK
        return paint
    }

    fun <T : Face<T>> renderFace(
            face: Face<T>,
            canvas: Canvas,
            faceSize: Float = minOf(canvas.width, canvas.height).toFloat(),
            textPaint: Paint = textPaintForFaceSize(faceSize),
            x: Float = 0f,
            y: Float = 0f
    ) {
        val whitePaint = Paint()
        whitePaint.color = Color.WHITE
        val blackPaint = Paint()
        blackPaint.color = Color.BLACK
        val undoverlineDotWidth = FaceDimensionsFractional.undoverlineDotWidth * faceSize
        val undoverlineDotHeight = FaceDimensionsFractional.undoverlineDotHeight * faceSize
        fun renderUndoverline(isOverline: Boolean) {
            val undoverlineDotTop = y + (
                    if (isOverline)
                        FaceDimensionsFractional.overlineDotTop
                    else
                        FaceDimensionsFractional.underlineDotTop
                    ) * faceSize
            val undoverlineDotBottom = undoverlineDotTop + undoverlineDotHeight
            fun renderUndoverlineBit(pos: Int) {
                val undoverlineDotLeft = x +
                        FaceDimensionsFractional.undoverlineFirstDotLeftEdge * faceSize +
                        undoverlineDotWidth * pos
                val undoverlineDotRight = undoverlineDotLeft + undoverlineDotWidth
                canvas.drawRect(undoverlineDotLeft, undoverlineDotTop, undoverlineDotRight, undoverlineDotBottom, whitePaint)
            }

            val left = x + FaceDimensionsFractional.undoverlineLeftEdge * faceSize
            val top = y + (
                    if (isOverline)
                        FaceDimensionsFractional.overlineTop
                    else
                        FaceDimensionsFractional.underlineTop
                    ) * faceSize
            val right = left + FaceDimensionsFractional.undoverlineLength * faceSize
            val bottom = top + FaceDimensionsFractional.undoverlineThickness * faceSize
            canvas.drawRect(left, top, right, bottom, blackPaint)

            val code: Short? = if (isOverline) face.overlineCode else face.underlineCode
            if (code != null) {
                val fullCode = 1024 + (if (isOverline) 512 else 0) + (code.toInt() shl 1)
                for (pos in 0..10) {
                    if (((fullCode shr (10 - pos)) and 1) != 0) {
                        renderUndoverlineBit(pos)
                    }
                }
            }
        }

        val fractionalXDistFromFaceCenterToCharCenter = (FaceDimensionsFractional.charWidth + FaceDimensionsFractional.spaceBetweenLetterAndDigit) / 2
        val letterX = x + (0.5f - fractionalXDistFromFaceCenterToCharCenter) * faceSize
        val digitX = x + (0.5f + fractionalXDistFromFaceCenterToCharCenter) * faceSize
        val textY = y + FaceDimensionsFractional.textBaselineY * faceSize
        canvas.drawText(face.letter.toString(), letterX, textY, textPaint)
        canvas.drawText(face.digit.toString(), digitX, textY, textPaint)
        renderUndoverline(false)
        renderUndoverline(true)
    }

    fun <T : Face<T>> renderKeySqr(
            keySqr: KeySqr<T>,
            canvas: Canvas,
            size: Float = minOf(canvas.width, canvas.height).toFloat(),
            x: Float = (canvas.width - size) / 2,
            y: Float = (canvas.height - size) / 2
    ) {
        val faceDist = size / 5f
        val faceSize = size / 8f
        val left = x + (faceDist - faceSize) / 2f
        val top = y + (faceDist - faceSize) / 2f
        val textPaint: Paint = textPaintForFaceSize(faceSize)

        keySqr.faces.forEachIndexed { index, face ->
            run {
                renderFace(
                        face,
                        canvas,
                        faceSize,
                        textPaint,
                        left + faceDist * (index % 5).toFloat(),
                        top + faceDist * (index / 5).toFloat()
                )
            }
        }
    }
}

class KeySqrDrawable(
    private val context: Context,
    private val keySqr: KeySqr<FaceRead>
) : Drawable() {

    private val inconsolataBold: Typeface? = ResourcesCompat.getFont(context, R.font.inconsolata_bold)
    private val renderer = KeySqrRenderer(inconsolataBold)

    override fun draw(canvas: Canvas) {
        renderer.renderKeySqr(keySqr, canvas)
    }

    override fun setAlpha(alpha: Int) {
        // This method is required
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        // This method is required
    }

    override fun getOpacity(): Int =
            // Must be PixelFormat.UNKNOWN, TRANSLUCENT, TRANSPARENT, or OPAQUE
            PixelFormat.OPAQUE
}