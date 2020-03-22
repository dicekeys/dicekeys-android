package org.dicekeys.keys

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Base64
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.dicekeys.utilities.QrCodeBitmap
import org.dicekeys.utilities.qrCodeNativeSizeInQrCodeSquarePixels

@JsonClass(generateAdapter = true)
class SignatureVerificationKey(
        val keyBytes: ByteArray,
        val keyDerivationOptionsJson: String = ""
) {
    companion object {
        internal val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .add(Base64Adapter())
                .build()
        private val jsonAdapter: JsonAdapter<SignatureVerificationKey> =
                moshi.adapter<SignatureVerificationKey>(SignatureVerificationKey::class.java)
                        .indent("")

        fun fromJson(json: String): SignatureVerificationKey? {
            return jsonAdapter.fromJson(json)
        }

        fun fromJsonOrThrow(json: String?): SignatureVerificationKey =
                jsonAdapter.fromJson(json
                        ?: throw Exception("Missing signature verification key json"))
                        ?: throw Exception("Failed to construct signature verification key")
    }

    override fun equals(other: Any?): Boolean =
        (other is SignatureVerificationKey) &&
        keyDerivationOptionsJson == other.keyDerivationOptionsJson &&
        keyBytes.contentEquals(other.keyBytes)

    fun toJson(): String { return jsonAdapter.toJson(this)
        // If the key derivation options are empty, remove them
        .replace("\"keyDerivationOptionsJson\":\"\",","", false)
        .replace(",\"keyDerivationOptionsJson\":\"\"","", false)
    }
    // public val asJson: String get() = jsonAdapter.toJson(this)

    @ExperimentalUnsignedTypes
    public val asHexDigits: String get() =
        keyBytes.asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }

    private external fun verifySignatureJNI(
        message: ByteArray,
        signature: ByteArray,
        signatureVerificationKeyBytes: ByteArray
//        keyDerivationOptionsJson: String,
    ): Boolean

    public fun verifySignature(
            message: ByteArray,
            signature: ByteArray
//            postDecryptionInstructionsJson: String = ""
    ): Boolean {
        return verifySignatureJNI(message, signature, keyBytes)
    }

    fun getJsonQrCode(
            maxEdgeLengthInDevicePixels: Int = qrCodeNativeSizeInQrCodeSquarePixels * 2
    ): Bitmap = QrCodeBitmap(
            "https://dicekeys.org/svk/",
            toJson(),
            maxEdgeLengthInDevicePixels
    )

    fun getJsonQrCode(
            maxWidth: Int,
            maxHeight: Int
    ): Bitmap = getJsonQrCode(kotlin.math.min(maxWidth, maxHeight))


}
