package org.dicekeys.app.utils


import com.google.common.io.BaseEncoding
import com.google.common.io.ByteArrayDataOutput
import com.google.common.io.ByteStreams
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.edec.EdECObjectIdentifiers
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.dicekeys.app.extensions.toHex
import org.dicekeys.app.extensions.toHexString
import java.io.StringWriter
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Security
import javax.security.cert.X509Certificate


object OpenSslHelper {

    fun marshalString(out : ByteArrayDataOutput, str: String) : ByteArrayDataOutput {
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

    fun publickey2(): String {
        Security.addProvider(BouncyCastleProvider())

        val privateKeyBytes: ByteArray = byteArrayOf(0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf)

        println(privateKeyBytes.size)
        println(privateKeyBytes.toHexString())

//        val privateKey = Ed25519PrivateKeyParameters(privateKeyBytes, 0)

        val privateKey = PrivateKeyInfo(AlgorithmIdentifier(EdECObjectIdentifiers.id_Ed25519), DEROctetString(privateKeyBytes))


        println(privateKey.encoded.toHexString())
//        println(privateKey.generatePublicKey().encoded.toHexString())

//        val publicKey = privateKey.generatePublicKey()

        val writer = StringWriter()
        val pemWriter = JcaPEMWriter(writer)

        pemWriter.writeObject(privateKey)
        pemWriter.flush()
        pemWriter.close()

        println(writer.toString())


        val out = ByteStreams.newDataOutput()


        val base64 = "TODO"
        return "ssh-ed25519 $base64 DiceKeys"

    }

    fun publicKeyToPem(publicKey: PublicKey): String {
        val base64PubKey = BaseEncoding.base64().encode(publicKey.encoded)

        return "-----BEGIN PUBLIC KEY-----\n" +
                base64PubKey.replace("(.{64})".toRegex(), "$1\n") +
                "\n-----END PUBLIC KEY-----\n"
    }


    fun privateKeyToPem(privateKey: PrivateKey): String {
        val base64PubKey = BaseEncoding.base64().encode(privateKey.encoded)

        return "-----BEGIN PRIVATE KEY-----\n" +
                base64PubKey.replace("(.{64})".toRegex(), "$1\n") +
                "\n-----END PRIVATE KEY-----\n"
    }


    fun certificateToPem(certificate: X509Certificate): String {
        val base64PubKey = BaseEncoding.base64().encode(certificate.encoded)

        return "-----BEGIN CERTIFICATE-----\n" +
                base64PubKey.replace("(.{64})".toRegex(), "$1\n") +
                "\n-----END CERTIFICATE-----\n"
    }

    fun block(type: String, base64Payload: String) : String{
        println(base64Payload)
        return "-----BEGIN $type-----\n" +
                base64Payload.replace("(.{64})".toRegex(), "$1\n") +
                "\n-----END $type-----\n"
    }
}