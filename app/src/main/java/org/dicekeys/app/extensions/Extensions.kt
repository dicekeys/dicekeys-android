package org.dicekeys.app.extensions

import android.content.Context

fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

fun Context.toPixels(size: Float): Float {
    return resources.displayMetrics.density * size
}

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