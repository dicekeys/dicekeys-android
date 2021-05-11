package org.dicekeys.app.openssh

import com.google.common.io.BaseEncoding
import com.google.common.io.ByteArrayDataOutput
import com.google.common.io.ByteStreams
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters

object OpenSslHelper {
    private fun marshalData(out: ByteArrayDataOutput, data: ByteArray) {
        out.writeInt(data.size) // 32-bit length
        out.write(data)
    }

    private fun marshalData(out: ByteArrayDataOutput, str: String): ByteArrayDataOutput {
        marshalData(out, str.toByteArray(Charsets.US_ASCII))
        return out
    }

    private fun marshalPublicKeyEd25519(pubKey: ByteArray): ByteArray {
        val out = ByteStreams.newDataOutput()
        marshalData(out, "ssh-ed25519")
        marshalData(out, pubKey)
        return out.toByteArray()
    }

    private fun marshalPrivateKeyEd25519(
        privateKey: ByteArray,
        pubKey: ByteArray,
        comment: String,
        checksum : Int = Math.random().toInt()
    ): ByteArray {
        // Checksum is a random number and is used only to validate that the key when successfully decrypted.
        // This method allow you to provide a checksum in order to validate the unit tests

        val out = ByteStreams.newDataOutput()

        out.writeInt(checksum)
        out.writeInt(checksum)
        out.write(marshalPublicKeyEd25519(pubKey))

        // scalar, point # Private Key part + Public Key part (AGAIN)
        marshalData(out, privateKey + pubKey)

        // Comment
        marshalData(out, comment)

        val blockSize = 8 // for unencrypted is 8

        // Padding
        for (i in 1..(blockSize - out.toByteArray().size % blockSize) % blockSize) {
            out.writeByte(i)
        }

        return out.toByteArray()
    }

    fun createAuthorizedPublicKeyEd25519(pubKey: ByteArray): String {
        val base64 = BaseEncoding.base64().encode(marshalPublicKeyEd25519(pubKey))
        return "ssh-ed25519 $base64 DiceKeys"
    }

    fun createPrivateKeyEd25519(privateKey: ByteArray, comment: String, checksum : Int = Math.random().toInt()): ByteArray {
        val privateKeyEd255119 = Ed25519PrivateKeyParameters(privateKey, 0)
        val publicKey = privateKeyEd255119.generatePublicKey().encoded

        val out = ByteStreams.newDataOutput()
        out.write("openssh-key-v1".toByteArray())
        out.writeByte(0) // null byte

        marshalData(out, "none") // CipherName
        marshalData(out, "none") // KdfName
        marshalData(out, "") // KdfName
        out.writeInt(1) // NumKeys

        val sshPublicKey = marshalPublicKeyEd25519(publicKey)
        marshalData(out, sshPublicKey)

        val sshPrivateKey = marshalPrivateKeyEd25519(privateKey, publicKey, comment, checksum)
        marshalData(out, sshPrivateKey)

        return out.toByteArray()
    }
}