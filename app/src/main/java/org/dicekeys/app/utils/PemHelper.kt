package org.dicekeys.app.utils


import com.google.common.io.BaseEncoding
import java.security.PrivateKey
import java.security.PublicKey
import javax.security.cert.X509Certificate


object PemHelper {
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
        return "-----BEGIN $type-----\n" +
                base64Payload.replace("(.{64})".toRegex(), "$1\n") +
                "\n-----END $type-----\n"
    }
}