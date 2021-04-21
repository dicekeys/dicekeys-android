package org.dicekeys.app.openpgp;

import com.google.common.io.ByteStreams
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.math.ec.rfc8032.Ed25519
import java.security.MessageDigest

class SignaturePacket(privateKey: ByteArray, private val timestamp: UInt, userIdPacket: UserIdPacket): Packet() {

    private val secretPacket = SecretPacket(privateKey, timestamp)

    private val privateKeyEd255119 = Ed25519PrivateKeyParameters(privateKey, 0)

    override val ctb: Int
        get() = 0x88

    val hashedSubPackets : ByteArray by lazy {
        val subpackets = ByteStreams.newDataOutput()

        subpackets.writeByte(0x16) // length
        subpackets.writeByte(0x21) // tag
        subpackets.writeByte(Version) // version
        subpackets.write(secretPacket.publicPacket.fingerprint())

        subpackets.writeByte(0x05) // length
        subpackets.writeByte(0x02) // Signature Creation Time (0x2)
        subpackets.writeInt(timestamp.toInt())

        subpackets.writeByte(0x02) // length
        subpackets.writeByte(0x1b) // Key Flags (0x1b)
        subpackets.writeByte(0x01) // Certify (0x1)

        subpackets.writeByte(0x05) // length
        subpackets.writeByte(0x0b) // Preferred Symmetric Algorithms (0xb)
        // AES with 256-bit key (0x9),AES with 192-bit key (0x8),AES with 128-bit key (0x7),TripleDES (DES-EDE, 168 bit key derived from 192) (0x2)
        subpackets.write(byteArrayOf(0x09, 0x08, 0x07, 0x02))

        subpackets.writeByte(0x06) // length
        subpackets.writeByte(0x15) // Preferred Hash Algorithms (0x15)
        // SHA512 (0xa),SHA384 (0x9),SHA256 (0x8),SHA224 (0xb),SHA1 (0x2)
        subpackets.write(byteArrayOf(0x0a, 0x09, 0x08, 0x0b, 0x02))

        subpackets.writeByte(0x04) // length
        subpackets.writeByte(0x16) // Preferred Compression Algorithms (0x16)
        // ZLIB (0x2),BZip2 (0x3),ZIP (0x1)
        subpackets.write(byteArrayOf(0x02, 0x03, 0x01))

        subpackets.writeByte(0x02) // length
        subpackets.writeByte(0x1e) // Features (0x1e)
        subpackets.writeByte(0x01) // Modification detection (0x1)

        subpackets.writeByte(0x02) // length
        subpackets.writeByte(0x17) // Key Server Preferences (0x17)
        subpackets.writeByte(0x80) // No-modify (0x80)

        subpackets.toByteArray()
    }

    val unhashedSubPackets : ByteArray by lazy {
        val out = ByteStreams.newDataOutput()
        val data = ByteStreams.newDataOutput().let {
            it.writeByte(0x10) // Issuer (0x10)
            it.write(secretPacket.publicPacket.keyId())
            it.toByteArray()
        }

        out.writeByte(data.size) // length
        out.write(data)

        out.toByteArray()
    }

    override val body: ByteArray by lazy {
        val buffer = ByteStreams.newDataOutput()

        val publicPacket = secretPacket.publicPacket

        buffer.writeByte(Version)
        buffer.writeByte(0x13) //   signatureType: "Positive certification of a User ID and Public-Key packet. (0x13)"
        buffer.writeByte(Ed25519Algorithm)
        buffer.writeByte(Sha256Algorithm)

        buffer.writeShort(hashedSubPackets.size) // hashed_area_len
        buffer.write(hashedSubPackets)

        buffer.writeShort(unhashedSubPackets.size) // unhashed_area_len
        buffer.write(unhashedSubPackets)

        val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
        publicPacket.hash(digest)
        userIdPacket.hash(digest)
        hash(digest)

        val hash = digest.digest()

        buffer.write(hash.take(2).toByteArray())

        val signature = ByteArray(Ed25519PrivateKeyParameters.SIGNATURE_SIZE)
        privateKeyEd255119.sign(Ed25519.Algorithm.Ed25519, null, hash, 0, hash.size, signature, 0)

        // split signature into 2 parts of 32 bytes
        // r & s
        buffer.write(Mpi.fromByteArray(signature.take(32).toByteArray()).toByteArray())
        buffer.write(Mpi.fromByteArray(signature.takeLast(32).toByteArray()).toByteArray())

        buffer.toByteArray()
    }


    override fun hash(digest: MessageDigest) {
        val buffer = ByteStreams.newDataOutput()

        buffer.writeByte(Version)
        buffer.writeByte(0x13) //   signatureType: "Positive certification of a User ID and Public-Key packet. (0x13)"
        buffer.writeByte(Ed25519Algorithm)
        buffer.writeByte(Sha256Algorithm)

        buffer.writeShort(hashedSubPackets.size) // hashed_area_len
        buffer.write(hashedSubPackets)

        val signatureHashedSize = buffer.toByteArray().size

        buffer.write(byteArrayOf(Version.toByte(), 0xff.toByte()))
        buffer.writeInt(signatureHashedSize)

        digest.update(buffer.toByteArray())
    }

}
