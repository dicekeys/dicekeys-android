package org.dicekeys.app.openpgp;

import com.google.common.io.ByteStreams
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.dicekeys.app.extensions.toHex
import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.MessageDigest

class PublicPacket(private val publicKey: ByteArray, private val timestamp: Int): Packet() {

    override val ctb: Int
        get() = 0x98

    override val body: ByteArray by lazy {
        val body = ByteStreams.newDataOutput()

        val version = 0x4
        val algo = 0x16 // Ed25519
        val curveOid = byteArrayOf(0x2b, 0x06, 0x01, 0x04, 0x01, 0xda.toByte(), 0x47, 0x0f, 0x01) // ed25519 curve oid // 0x2b, 0x06, 0x01, 0x04, 0x01, 0xda, 0x47, 0x0f, 0x01
        val curveLength = curveOid.size // 0x09
        val taggedPublicKey = byteArrayOf(0x40) + publicKey

        body.write(version)
        body.writeInt(timestamp)
        body.write(algo)
        body.write(curveLength)
        body.write(curveOid)

        // TODO MPI
        body.write(byteArrayOf(0x01, 0x07)) // eddsa_public_len , Why two bytes?
        body.write(taggedPublicKey)

        body.toByteArray()
    }

    fun fingerprint(): ByteArray {
        val out = ByteStreams.newDataOutput()
        val digest: MessageDigest = MessageDigest.getInstance("SHA-1")


        out.writeByte(Version)
        out.writeInt(timestamp)
        out.writeByte(Ed25519Algorithm)
        out.writeByte(Ed25519CurveOid.size)
        out.write(Ed25519CurveOid)

        out.write(byteArrayOf(0x01, 0x07)) // eddsa_public_len
        out.write(0x40) // public key tag
        out.write(publicKey) // public key

        digest.update(0x99.toByte())
        digest.update(0x0) // size 2-byte
        digest.update(out.toByteArray().size.toByte()) // size // 0x33
        digest.update(out.toByteArray())

        return digest.digest()
    }

    fun keyId(): ByteArray = fingerprint().takeLast(8).toByteArray()


    override fun hash(digest: MessageDigest) {
        val buffer = ByteStreams.newDataOutput()
        buffer.writeByte(0x99)
        buffer.writeShort(this.body.size) // 2-bytes

        buffer.write(body)

        digest.update(buffer.toByteArray())
    }
}
