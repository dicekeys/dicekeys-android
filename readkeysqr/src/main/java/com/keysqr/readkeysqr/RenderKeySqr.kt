package com.keysqr.readkeysqr

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

// Typeface inconsolata = Typeface.createFromAsset(assetManager, pathToFont) / ResourcesCompat.getFont(context, R.font.Inconsolata700)
fun defaultPaint(faceSize: Float): Paint {
    var paint = Paint()
    // paint.setTypeface(inconsolata)
    paint.textAlign = Paint.Align.CENTER
    paint.textSize = faceSize * FaceDimensionsFractional.fontSize
    return paint
}



fun <T: Face<T>>renderFace(
        face: Face<T>,
        canvas: Canvas,
        x: Float = 0f,
        y: Float = 0f,
        faceSize: Float = maxOf(canvas.width, canvas.height).toFloat(),
        paint: Paint = defaultPaint(faceSize)
) {
    var whitePaint = Paint();
    whitePaint.setColor(Color.WHITE)
    val undoverlineDotWidth = FaceDimensionsFractional.undoverlineDotWidth * faceSize;
    val undoverlineDotHeight = FaceDimensionsFractional.undoverlineDotHeight * faceSize;
    fun renderUndoverline(isOverline: Boolean) {
        val undoverlineDotTop = y + (
                if (isOverline)
                    FaceDimensionsFractional.overlineDotTop
                else
                    FaceDimensionsFractional.underlineDotTop
                ) * faceSize
        val undoverlineDotBottom = undoverlineDotTop + undoverlineDotHeight
        fun renderUndoverlineBit(pos: Int) {
            val undoverlineDotLeft = x + undoverlineDotWidth * pos
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
        paint.color = Color.BLACK
        canvas.drawRect(top, top, right, bottom, paint)

        val code: Short? = if (isOverline) face.overlineCode else face.underlineCode
        if (code == null)
            return
        val fullCode = 1024 + (if (isOverline) 512 else 0) + (code.toInt() shl 1)
        for (var pos in 0..10) {
            if (((fullCode shr (10 - pos)) and 1) != 0) {
                renderUndoverlineBit(pos)
            }
        }

    }
    val fractionalXDistFromFaceCenterToCharCenter = (FaceDimensionsFractional.charWidth + FaceDimensionsFractional.spaceBetweenLetterAndDigit) / 2
    val letterX = x + (0.5f - fractionalXDistFromFaceCenterToCharCenter) * faceSize
    val digitX = x + (0.5f + fractionalXDistFromFaceCenterToCharCenter) * faceSize
    val textY = y + FaceDimensionsFractional.textBaselineY * faceSize
    paint.color = Color.BLACK
    canvas.drawText( face.letter.toString(), letterX, textY, paint )
    canvas.drawText( face.digit.toString(), digitX, textY, paint )
    renderUndoverline(false)
    renderUndoverline(true)
}

fun <T: Face<T>>renderKeySqr(
        keySqr: KeySqr<T>,
        canvas: Canvas,
        x: Float = canvas.width.toFloat() / 2,
        y: Float = canvas.height.toFloat() / 2,
        size: Float = minOf(canvas.width, canvas.height).toFloat(),
        paint: Paint = defaultPaint(size)
) {
    val faceDist = size / 5f
    val faceSize = size / 6f
    val left = x + (faceDist - faceSize) / 2f
    val top = y + (faceDist - faceSize) / 2f


    keySqr.faces.forEachIndexed{ index, face -> run {
        renderFace(
                face,
                canvas,
                left + faceDist * (index % 5).toFloat(),
                top + faceDist * (index / 5).toFloat(),
                faceSize,
                paint
        )
    }}
}