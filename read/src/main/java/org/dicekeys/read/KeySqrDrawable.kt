package org.dicekeys.read

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import org.dicekeys.keysqr.Face
import org.dicekeys.keysqr.KeySqr


class KeySqrDrawable(
        private val context: Context,
        private val keySqr: KeySqr<Face>
) : Drawable() {

    private val inconsolataBold: Typeface? = ResourcesCompat.getFont(context, R.font.inconsolata_bold)
    private val renderer = KeySqrRenderer(inconsolataBold)

    override fun draw(canvas: Canvas) {
        // https://github.com/dicekeys/read-keysqr-android/issues/18
        renderer.renderKeySqr(keySqr.rotate(1), canvas)
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