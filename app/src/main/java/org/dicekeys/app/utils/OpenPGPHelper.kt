package org.dicekeys.app.utils

import com.google.common.io.BaseEncoding
import com.google.common.io.ByteArrayDataOutput
import com.google.common.io.ByteStreams
import org.dicekeys.app.extensions.toHex
import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.MessageDigest

class Packet(val ctb: Int, val body: ByteArray){

    fun toByteArray(sizeInBytes: Int = 1): ByteArray {
        val out = ByteStreams.newDataOutput()
        out.writeByte(ctb)
        // hardcoded byte ? it should be variable, but it's ok for our usecase
        when(sizeInBytes){
            2 -> out.writeShort(body.size)
            4 -> out.writeInt(body.size)
            else -> out.writeByte(body.size)
        }
        out.write(body)
        return out.toByteArray()
    }
}

object OpenPGPHelper {

    fun createPublicKeyPacket(publicKey: ByteArray): Packet {
        val body = ByteStreams.newDataOutput()
        val ctb = 0x98 // CTB 0x99 for Public

        val version = 0x4
        val timestamp = byteArrayOf(0x60, 0x84.toByte(), 0x45, 0x60) // hardcoded to test the implementation
        val algo = 0x16 // Ed25519
        val curveOid = byteArrayOf(0x2b, 0x06, 0x01, 0x04, 0x01, 0xda.toByte(), 0x47, 0x0f, 0x01) // ed25519 curve oid // 0x2b, 0x06, 0x01, 0x04, 0x01, 0xda, 0x47, 0x0f, 0x01
        val curveLength = curveOid.size // 0x09
        val taggedPublicKey = byteArrayOf(0x40) + publicKey

        body.write(version)
        body.write(timestamp)
        body.write(algo)
        body.write(curveLength)
        body.write(curveOid)

        body.write(byteArrayOf(0x01, 0x07)) // eddsa_public_len , Why two bytes?
        body.write(taggedPublicKey)

        val bodyByteArray = body.toByteArray()

        return Packet(ctb, bodyByteArray)
    }

    fun createSecretKeyPacket(privateKey: ByteArray, publicKey: ByteArray): Packet {
        val body = ByteStreams.newDataOutput()
        val ctb = 0x94 // CTB 0x94 for Secret

        val version = 0x4
        val timestamp = byteArrayOf(0x60, 0x84.toByte(), 0x45, 0x60) // hardcoded to test the implementation
        val algo = 0x16 // Ed25519
        val curveOid = byteArrayOf(0x2b, 0x06, 0x01, 0x04, 0x01, 0xda.toByte(), 0x47, 0x0f, 0x01) // ed25519 curve oid // 0x2b, 0x06, 0x01, 0x04, 0x01, 0xda, 0x47, 0x0f, 0x01
        val curveLength = curveOid.size // 0x09
        val taggedPublicKey = byteArrayOf(0x40) + publicKey
        val s2kUsage = 0x00


        body.write(version)
        body.write(timestamp)
        body.write(algo)
        body.write(curveLength)
        body.write(curveOid)

        body.write(byteArrayOf(0x01, 0x07)) // eddsa_public_len , Why two bytes?

        body.write(taggedPublicKey)
        body.write(s2kUsage)
        body.write(byteArrayOf(0x00, 0xff.toByte())) // eddsa_secret_len , Why two bytes? and why 00 ff and 01 00 in some situations? TODO implement MPI
        body.write(privateKey)


        var total = BigInteger.valueOf(0xff)

        for(i in privateKey){
            total = total.add(BigInteger(1, byteArrayOf(i)))
        }

//        println("mod: " + total.mod(BigInteger.valueOf(65536)))


        val buffer: ByteBuffer = ByteBuffer.allocate(2)
        buffer.putShort(total.toShort())
//        println(buffer.array().toHex())
        body.write(buffer.array())

        // secret len
        // secret
        // checksum

        val bodyByteArray = body.toByteArray()

        return Packet(ctb, bodyByteArray)
    }

    fun createUserPacket(name: String, email: String): Packet {
        val ctb = 0xb4 // CTB 0xb4 for UserID

        val body = ByteStreams.newDataOutput()

        val user = "$name <$email>"
        body.write(user.toByteArray())
        return Packet(ctb, body.toByteArray())
    }

    fun fingerprint(publicKey: ByteArray, timestamp: ByteArray): ByteArray {
        val out = ByteStreams.newDataOutput()
        val digest: MessageDigest = MessageDigest.getInstance("SHA-1")


        out.writeByte(0x04) // version
        out.write(timestamp)
        out.writeByte(0x16) // algo
        out.write(0x09) // curve length
        out.write(byteArrayOf(0x2B, 0x06, 0x01, 0x04, 0x01, 0xDA.toByte(), 0x47, 0x0F, 0x01)) // curve
        out.write(byteArrayOf(0x01, 0x07)) // eddsa_public_len

        out.write(0x40) // public key tag

        out.write(publicKey) // public key

        digest.update(0x99.toByte())
        digest.update(0x0) // size 2-byte
        digest.update(out.toByteArray().size.toByte()) // size // 0x33
        digest.update(out.toByteArray())

        return digest.digest()
    }

    fun createSignaturePacket(privateKey: ByteArray, publicKey: ByteArray, timestamp: ByteArray, userPacket: Packet): Packet {
        val ctb = 0x88 // CTB 0x88 for Signature
        val body = ByteStreams.newDataOutput()
        val subpackets = ByteStreams.newDataOutput()

        val publicPacket = OpenPGPHelper.createPublicKeyPacket(publicKey)

        val fingerprint = fingerprint(publicKey, timestamp)

        body.writeByte(0x04) // version
        body.writeByte(0x13) // type
        body.writeByte(0x16) // algo
        body.writeByte(0x08) // hash algo
        body.write(byteArrayOf(0x00, 0x38)) // hashed_area_len

        // sub packet
        subpackets.writeByte(0x16) // length
        subpackets.writeByte(0x21) // tag
        subpackets.writeByte(0x04) // version
        subpackets.write(fingerprint)

        // sub packet
        subpackets.writeByte(0x05) // length
        subpackets.writeByte(0x02) // tag
        subpackets.write(timestamp)

        // sub packet
        subpackets.writeByte(0x02) // length
        subpackets.writeByte(0x1b) // tag
        subpackets.write(0x01) // key flags

        // sub packet
        subpackets.writeByte(0x05) // length
        subpackets.writeByte(0x0b) // tag
        subpackets.write(byteArrayOf(0x09, 0x08, 0x07, 0x02)) // pref sym algos

        // sub packet
        subpackets.writeByte(0x06) // length
        subpackets.writeByte(0x15) // tag
        subpackets.write(byteArrayOf(0x0a, 0x09, 0x08, 0x0b, 0x02)) // pref hash algos

        // sub packet
        subpackets.writeByte(0x04) // length
        subpackets.writeByte(0x16) // tag
        subpackets.write(byteArrayOf(0x02, 0x03, 0x01)) // pref compression algos

        // sub packet
        subpackets.writeByte(0x02) // length
        subpackets.writeByte(0x1e) // tag
        subpackets.writeByte(0x01) // features

        // sub packet
        subpackets.writeByte(0x02) // length
        subpackets.writeByte(0x17) // tag
        subpackets.writeByte(0x80) // key server pref


        val hasher = ByteStreams.newDataOutput()

        hasher.write(publicPacket.toByteArray())
        hasher.write(userPacket.toByteArray(4))

        hasher.write(body.toByteArray())
        hasher.write(subpackets.toByteArray())

        val size = hasher.toByteArray().size

        hasher.write(byteArrayOf(0x04, 0xff.toByte()))

//                util::narrow_cast < uint32_t > ((sizeof(version()) +
//                        sizeof(type()) +
//                        sizeof(public_key_algorithm()) +
//                        sizeof(hashing_algorithm()) +
//                        _hashed_subpackets.size()))
//        )
//        hasher.write(size)
        println("subpacket: ${subpackets.toByteArray().size}")
        println("size: $size")
        hasher.writeInt(size)

        val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
        digest.update(hasher.toByteArray())

        println(hasher.toByteArray().toHex())

        val hash = digest.digest()
        println("SHA-1: 65 EB")
        println("SHA-1: " + hash.toHex().substring(0 until 4))
        println(hash.toHex())

        // 65 EB


        // Generate signature signature
//        val privateKeyEd255119 = Ed25519PrivateKeyParameters(privateKey, 0)
//
//        val signer: Signer = Ed25519Signer()
//        signer.init(true, privateKeyEd255119)
//        signer.update(userPacket.toByteArray(), 0, 1)
//        val signature: ByteArray = signer.generateSignature()
//        println(signature.toHex())
//        println(signature.toHex().length)
//        body.write(signature)
        //val actualSignature: `var` = Base64.getUrlEncoder().encodeToString(signature).replace("=", "")
//        LOG.info("Expected signature: {}", expectedSig)
//        LOG.info("Actual signature  : {}", actualSignature)
//
//        assertEquals(expectedSig, actualSignature)


        // write subpacket
        body.write(subpackets.toByteArray())

        // sub packet
        body.write(byteArrayOf(0x00, 0x0a)) // unhashed_area_len
        body.writeByte(0x09) // length
        body.writeByte(0x10) // tag

        val fingerprintId = fingerprint.takeLast(8).toByteArray()
        body.write(fingerprintId) // issuer

//        val bodyByteArray = body.toByteArray()
//        out.writeByte(bodyByteArray.size) // hardcoded byte ? it should be variable, but it's ok for our usecase
//        out.write(bodyByteArray)

        return Packet(ctb, body.toByteArray())
//        00000040  00 0a                                              unhashed_area_len
//        00000042        09                                           subpacket length
//        00000043           10                                        subpacket tag
//        00000044              85 b7 a3 57  b0 e9 ff d8               issuer

//        0000004c                                       65            digest_prefix1
//        0000004d                                          eb         digest_prefix2
//        0000004e                                             01 00   eddsa_sig_r_len
//        00000050  c5 a7 7d 28 d9 62 3c 74  b4 93 a7 a5 e7 2a bf 24   eddsa_sig_r
//        00000060  f3 4c 4e 13 3d a8 5e 31  4c 61 05 b0 6a 4e 26 af
//        00000070  01 00                                              eddsa_sig_s_len
//        00000072        a6 ec 13 92 0c 80  23 fc 04 44 70 5d 4f 32   eddsa_sig_s
//        00000080  a5 5b 97 7e c1 47 be 3b  6b 68 f1 12 60 1a 52 b6
//        00000090  73 0a

    }

    fun marshalString(out: ByteArrayDataOutput, str: String) : ByteArrayDataOutput {
        out.writeInt(str.length) // length
        out.write(str.toByteArray(Charsets.US_ASCII))
        return out
    }

    fun marshalPublicKeyEd25519(pubKey: ByteArray): ByteArray {
        val out = ByteStreams.newDataOutput()
        marshalString(out, "ssh-ed25519")
        out.writeInt(pubKey.size)
        out.write(pubKey)
        return out.toByteArray()
    }

    fun marshalPrivateKeyEd25519_with_comment(privateKey: ByteArray, pubKey: ByteArray, comment: String): ByteArray {
        val out = ByteStreams.newDataOutput()

//        val checksum = Math.random().toInt()
        val checksum = 0x103D60C3
        out.writeInt(checksum)
        out.writeInt(checksum)
        out.write(marshalPublicKeyEd25519(pubKey))

        // 32-bit length, scalar, point # Private Key part + Public Key part (AGAIN)
        out.writeInt(privateKey.size + pubKey.size)
        out.write(privateKey)
        out.write(pubKey)

        marshalString(out, comment)

        val blockSize = 8 // for unencrypted is 8

        // Padding
        for (i in 1 .. (blockSize - out.toByteArray().size % blockSize) % blockSize){
            out.writeByte(i)
        }

        return out.toByteArray()
    }

    fun marshalAuthorizedPublicKeyEd25519(pubKey: ByteArray): String {
        val base64 = BaseEncoding.base64().encode(marshalPublicKeyEd25519(pubKey))
        return "ssh-ed25519 $base64 DiceKeys"
    }

    fun marshalPrivateKeyEd25519(privateKey: ByteArray, publicKey: ByteArray, comment: String): ByteArray {
        val out = ByteStreams.newDataOutput()
        out.write("openssh-key-v1".toByteArray())
        out.writeByte(0) // null byte


        marshalString(out, "none") // CipherName
        marshalString(out, "none") // KdfName
        marshalString(out, "") // KdfName
        out.writeInt(1) // NumKeys


        val sshpub = marshalPublicKeyEd25519(publicKey)
        out.writeInt(sshpub.size) // 32-bit length
        out.write(sshpub)

        val sshPrivateKey = marshalPrivateKeyEd25519_with_comment(privateKey, publicKey, comment)
        out.writeInt(sshPrivateKey.size) // 32-bit length
        out.write(sshPrivateKey)

//        println("6F70656E 7373682D 6B65792D 76310000 0000046E 6F6E6500 0000046E 6F6E6500 00000000 00000100 00003300 00000B73 73682D65 64323535 31390000 0020C953 742F5D7A 26111D86 8FBBAD22 8C9C1805 24FD1743 891A2796 D49F4735 FD3D0000 0090103D 60C3103D 60C30000 000B7373 682D6564 32353531 39000000 20C95374 2F5D7A26 111D868F BBAD228C 9C180524 FD174389 1A2796D4 9F4735FD 3D000000 4005AD77 68A6BF76 BACF11CD 6E958685 C2921A2D 0A1F7B33 13CB66FA 71382FCF 41C95374 2F5D7A26 111D868F BBAD228C 9C180524 FD174389 1A2796D4 9F4735FD 3D000000 08446963 654B6579 73010203 0405".replace(" ", ""))
//        println(out.toByteArray().toHex())
//        println(String(out.toByteArray()))

//        return String(out.toByteArray())
        return out.toByteArray()
    }
}