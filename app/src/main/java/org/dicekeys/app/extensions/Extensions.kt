package org.dicekeys.app.extensions

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.view.View

fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

fun String.fromHex(): ByteArray {
    val len = length
    val data = ByteArray(len / 2)
    var i = 0
    while (i < len) {
        data[i / 2] =
            ((Character.digit(this[i], 16) shl 4) + Character.digit(this[i + 1], 16)).toByte()
        i += 2
    }
    return data
}

fun Context.toPixels(size: Float): Float {
    return resources.displayMetrics.density * size
}

fun View.pulse() {
    AnimatorSet().also {
        it.playTogether(
            ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.05f, 1f),
            ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.05f, 1f),
        )
        it.duration = 400
        it.start()
    }
}