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

        // RFC4880-bis-10 - Section 13.3 - EdDSA Point Format
        // 0x40 indicate compressed format
        val taggedPublicKey = byteArrayOf(0x40) + publicKey

        body.write(Version)
        body.writeInt(timestamp.toInt())
        body.write(Ed25519Algorithm)
        body.write(Ed25519CurveOid.size)
        body.write(Ed25519CurveOid)

        body.write(Mpi.fromByteArray(taggedPublicKey).toByteArray())

        body.toByteArray()
    }

    // A V4 fingerprint is the 160-bit SHA-1 hash of the octet 0x99,
    // followed by the two-octet packet length, followed by the entire
    // Public-Key packet starting with the version field.  The Key ID is the
    // low-order 64 bits of the fingerprint.
    fun fingerprint(): ByteArray {
        val out = ByteStreams.newDataOutput()

        // SHA-1 must not be used when collisions could lead to security failures
        // It is used here as hash for keyIds where collisions are acceptable, and only
        // because it is required for compatibility with RFC2440 (11.2) where no
        // alternative is available.
        val digest: MessageDigest = MessageDigest.getInstance("SHA-1")

        out.writeByte(0x99)
        out.writeShort(body.size)
        out.write(body)

        digest.update(out.toByteArray())

        return digest.digest()
    }

    fun keyId(): ByteArray = fingerprint().takeLast(8).toByteArray()

    override fun hashPreImage(): ByteArray {
        val buffer = ByteStreams.newDataOutput()
        buffer.writeByte(0x99)
        buffer.writeShort(body.size) // 2-bytes
        buffer.write(body)
        return buffer.toByteArray()
    }

}
