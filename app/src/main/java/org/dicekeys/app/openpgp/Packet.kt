package org.dicekeys.app.openpgp

import com.google.common.io.ByteStreams
import java.security.MessageDigest

abstract class Packet {
    // RFC4880 - Section 4
    // Explanation: https://under-the-hood.sequoia-pgp.org/packet-structure/
    abstract val pTag : Int // Also known as CTB (Cipher Type Byte)
    abstract val body : ByteArray

    abstract fun hash(digest: MessageDigest)

    fun toByteArray(): ByteArray {
        val out = ByteStreams.newDataOutput()
        out.writeByte(pTag)
        // RFC2440 Section 4.2.2
        // Hardcoded as a byte, it should be variable based on PTag but it's ok for our use case
        out.writeByte(body.size)
        out.write(body)
        return out.toByteArray()
    }

    companion object{
        const val Version : Int = 0x04
        const val Sha256Algorithm = 0x08 // RFC4880-bis-10 - Section 9.5 - 08 - SHA2-256 [FIPS180]
        const val Ed25519Algorithm = 0x16 // RFC4880-bis-10 - Section 9.1 - 22 (0x16) - EdDSA [RFC8032]
        val Ed25519CurveOid = byteArrayOf(0x2b, 0x06, 0x01, 0x04, 0x01, 0xda.toByte(), 0x47, 0x0f, 0x01) // RFC4880-bis-10 - Section 9.2.  ECC Curve OID
    }
}

