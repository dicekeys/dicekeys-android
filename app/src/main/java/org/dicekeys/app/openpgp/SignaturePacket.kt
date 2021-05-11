package org.dicekeys.app.openpgp;

import com.google.common.io.ByteArrayDataOutput
import com.google.common.io.ByteStreams
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.math.ec.rfc8032.Ed25519
import java.security.MessageDigest

class SignaturePacket(privateKey: ByteArray, private val timestamp: UInt, userIdPacket: UserIdPacket): Packet() {

    private val secretPacket = SecretPacket(privateKey, timestamp)

    private val privateKeyEd255119 = Ed25519PrivateKeyParameters(privateKey, 0)

    override val pTag: Int
        get() = 0x88

    // RFC4880-bis-10 - Section 5.2.3.1 - Signature Subpacket Specification
    // RFC4880-bis-10 - Section 5.2.3.21 - Key Flags
    private val hashedSubPackets : ByteArray by lazy {
        val out = ByteStreams.newDataOutput()

        // Issuer Fingerprint
        Subpacket(0x21, out).apply {
            writeByte(Version) // Version
            write(secretPacket.publicPacket.fingerprint())
            write()
        }

        // Signature Creation Time (0x2)
        Subpacket(0x02, out).apply {
            writeInt(timestamp.toInt())
            write()
        }

        // Key Flags (0x1b)
        Subpacket(0x1b, out).apply {
            writeByte(0x01) // Certify (0x1)
            write()
        }

        // Preferred Symmetric Algorithms (0xb)
        Subpacket(0x0b, out).apply {
            // AES with 256-bit key (0x9),AES with 192-bit key (0x8),AES with 128-bit key (0x7),TripleDES (DES-EDE, 168 bit key derived from 192) (0x2)
            write(byteArrayOf(0x09, 0x08, 0x07, 0x02))
            write()
        }

        // Preferred Hash Algorithms (0x15)
        Subpacket(0x15, out).apply {
            // SHA512 (0xa),SHA384 (0x9),SHA256 (0x8),SHA224 (0xb),SHA1 (0x2)
            write(byteArrayOf(0x0a, 0x09, 0x08, 0x0b, 0x02))
            write()
        }

        // Preferred Compression Algorithms (0x16)
        Subpacket(0x16, out).apply {
            // ZLIB (0x2),BZip2 (0x3),ZIP (0x1)
            write(byteArrayOf(0x02, 0x03, 0x01))
            write()
        }

        // Features (0x1e)
        Subpacket(0x1e, out).apply {
            writeByte(0x01) // Modification detection (0x1)
            write()
        }

        // Key Server Preferences (0x17)
        Subpacket(0x17, out ).apply {
            writeByte(0x80) // No-modify (0x80)
            write()
        }

        out.toByteArray()
    }

    private val unhashedSubPackets : ByteArray by lazy {
        val out = ByteStreams.newDataOutput()

        // Issuer (0x10)
        Subpacket(0x10, out).apply {
            write(secretPacket.publicPacket.keyId())
            write()
        }

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
