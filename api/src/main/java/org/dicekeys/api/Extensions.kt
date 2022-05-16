package org.dicekeys.api

fun ByteArray.toHexString() = joinToString("") { byte -> "%02x".format(byte) }