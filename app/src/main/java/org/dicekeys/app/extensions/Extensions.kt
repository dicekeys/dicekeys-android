package org.dicekeys.app.extensions

import android.content.Context

fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

fun Context.toPixels(size: Float): Float {
    return resources.displayMetrics.density * size
}