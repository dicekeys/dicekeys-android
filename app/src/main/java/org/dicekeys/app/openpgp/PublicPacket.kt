package org.dicekeys.app.openpgp;

import com.google.common.io.ByteStreams
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.dicekeys.app.extensions.toHex
import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.MessageDigest

class PublicPacket(private val publicKey: ByteArray, private val timestamp: UInt): Packet() {

    override val pTag: Int
        get() = 0x98

    override val body: ByteArray by lazy {
        val body = ByteStreams.newDataOutput()

        val taggedPublicKey = byteArrayOf(0x40) + publicKey

        body.write(Version)
        body.writeInt(timestamp.toInt())
        body.write(Ed25519Algorithm)
        body.write(Ed25519CurveOid.size)
        body.write(Ed25519CurveOid)

        body.write(Mpi.fromByteArray(taggedPublicKey).toByteArray())

        body.toByteArray()
    }

    fun fingerprint(): ByteArray {
        val out = ByteStreams.newDataOutput()
        val digest: MessageDigest = MessageDigest.getInstance("SHA-1")

        out.writeByte(0x99)
        out.writeShort(body.size)
        out.write(body)

        digest.update(out.toByteArray())

        return digest.digest()
    }

    fun keyId(): ByteArray = fingerprint().takeLast(8).toByteArray()


    override fun hash(digest: MessageDigest) {
        val buffer = ByteStreams.newDataOutput()
        buffer.writeByte(0x99)
        buffer.writeShort(body.size) // 2-bytes
        buffer.write(body)

        digest.update(buffer.toByteArray())
    }
}
