package org.dicekeys.app.openpgp

import com.google.common.io.BaseEncoding
import com.google.common.io.ByteStreams
import org.dicekeys.app.utils.PemHelper

object OpenPgpHelper {

    fun generateOpenPgpKey(privateKey: ByteArray, name: String, email: String, timestamp: UInt): String {
        val out = ByteStreams.newDataOutput()
        // val timestamp = 1577836800u // 2020-01-01 00:00:00
        val secretPacket = SecretPacket(privateKey, timestamp)
        val userPacket = UserIdPacket(name, email)
        val signaturePacket = SignaturePacket(privateKey, timestamp, userPacket)

        out.write(secretPacket.toByteArray())
        out.write(userPacket.toByteArray())
        out.write(signaturePacket.toByteArray())

        val base64 = BaseEncoding.base64().encode(out.toByteArray())

        return PemHelper.block("PGP PRIVATE KEY BLOCK", base64)
    }
}