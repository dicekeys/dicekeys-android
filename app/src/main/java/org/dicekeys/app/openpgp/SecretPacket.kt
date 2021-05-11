package org.dicekeys.app.openpgp;

import com.google.common.io.ByteStreams
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.MessageDigest

class SecretPacket(private val privateKey: ByteArray, private val timestamp: UInt) : Packet() {

    private val privateKeyEd255119 = Ed25519PrivateKeyParameters(privateKey, 0)

    override val pTag: Int
        get() = 0x94

    val publicPacket: PublicPacket by lazy {
        PublicPacket(privateKeyEd255119.generatePublicKey().encoded, timestamp)
    }

    override val body: ByteArray by lazy {
        val body = ByteStreams.newDataOutput()

        val taggedPublicKey = byteArrayOf(0x40) + privateKeyEd255119.generatePublicKey().encoded
        val s2kUsage = 0x00

        body.writeByte(Version)
        body.writeInt(timestamp.toInt())
        body.writeByte(Ed25519Algorithm)
        body.writeByte(Ed25519CurveOid.size)
        body.write(Ed25519CurveOid)

        body.write(Mpi.fromByteArray(taggedPublicKey).toByteArray())

        body.write(s2kUsage)

        val privateKeyMpi = Mpi.fromByteArray(privateKey)
        body.write(privateKeyMpi.toByteArray())

        val buffer: ByteBuffer = ByteBuffer.allocate(2)

        // Private Key Checksum
        var total = BigInteger.valueOf(0)

        for (i in privateKeyMpi.toByteArray()) {
            total = total.add(BigInteger(1, byteArrayOf(i)))
        }

        buffer.putShort(total.toShort())
        body.write(buffer.array())

        body.toByteArray()
    }

    override fun hash(digest: MessageDigest) {
        publicPacket.hash(digest)
    }
}
