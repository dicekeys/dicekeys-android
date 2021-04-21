package org.dicekeys.app.openpgp

import com.google.common.io.ByteStreams
import java.security.MessageDigest

abstract class Packet {
    abstract val ctb : Int
    abstract val body : ByteArray

    abstract fun hash(digest: MessageDigest)

    fun toByteArray(sizeInBytes: Int = 1): ByteArray {
        val out = ByteStreams.newDataOutput()
        out.writeByte(ctb)
        // hardcoded byte ? it should be variable, but it's ok for our usecase
        out.writeByte(body.size)
        out.write(body)
        return out.toByteArray()
    }

    companion object{
        const val Version : Int = 0x04
        const val Sha256Algorithm = 0x08
        const val Ed25519Algorithm = 0x16
        val Ed25519CurveOid = byteArrayOf(0x2b, 0x06, 0x01, 0x04, 0x01, 0xda.toByte(), 0x47, 0x0f, 0x01) // 0x2b, 0x06, 0x01, 0x04, 0x01, 0xda, 0x47, 0x0f, 0x01
    }
}

