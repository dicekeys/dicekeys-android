package org.dicekeys.app.extensions

fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
