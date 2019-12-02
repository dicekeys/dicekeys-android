package com.keysqr.readkeysqr

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
//import com.keysqr.readkeysqr.Face
//import com.keysqr.readkeysqr.KeySqr

// Typeface inconsolata = Typeface.createFromAsset(assetManager, pathToFont) / ResourcesCompat.getFont(context, R.font.Inconsolata700)
fun defaultPaint(faceSize: Float): Paint {
    var paint = Paint()
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
    fun renderUndoverline(isOverline: Boolean) {
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
    }
    // paint.setTypeface(inconsolata)
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
        centerX: Float = canvas.width.toFloat() / 2,
        centerY: Float = canvas.height.toFloat() / 2,
        size: Float = maxOf(canvas.width, canvas.height).toFloat(),
        paint: Paint = defaultPaint(size)
) {

    // keySqr.faces.forEach( (face, index) => {})

    for (face in keySqr.faces) {
        renderFace(face, canvas)
    }
}