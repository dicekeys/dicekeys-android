package org.dicekeys.app.extensions

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import org.dicekeys.crypto.seeded.DerivationOptions
import java.util.*


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

fun View.pulse(scale: Float = 1.05f, duration: Long = 400L) {
    AnimatorSet().also {
        it.playTogether(
            ObjectAnimator.ofFloat(this, "scaleY", 1f, scale, 1f),
            ObjectAnimator.ofFloat(this, "scaleX", 1f, scale, 1f),
        )
        it.duration = duration
        it.start()
    }
}

fun View.flash(duration: Long = 1000L) {
    AnimatorSet().also {
        it.playTogether(
            ObjectAnimator.ofFloat(this, "alpha", 1f, 0.1f, 1f, 0.1f, 1f, 0.1f, 1f),
        )
        it.duration = duration
        it.start()
    }
}


fun DerivationOptions.Type.description(capitalize: Boolean = false) = when(this){
        DerivationOptions.Type.Password ->"password"
        DerivationOptions.Type.Secret -> "seed or other secret"
        DerivationOptions.Type.SymmetricKey -> "symmetric cryptographic key"
        DerivationOptions.Type.UnsealingKey -> "public/private key pair"
        DerivationOptions.Type.SigningKey -> "signing/authentication key"
    }.let { if(capitalize) it.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } else it }