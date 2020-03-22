package org.dicekeys.keys

import android.graphics.Bitmap
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.dicekeys.utilities.QrCodeBitmap
import org.dicekeys.utilities.qrCodeNativeSizeInQrCodeSquarePixels

@JsonClass(generateAdapter = true)
class PublicKey(
        val keyBytes: ByteArray,
        val keyDerivationOptionsJson: String = ""
) {

    companion object {
        internal val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .add(Base64Adapter())
                .build()
        val jsonAdapter: JsonAdapter<PublicKey> =
            moshi.adapter<PublicKey>(PublicKey::class.java)
            .indent("")
        fun fromJson(json: String): PublicKey? {
            return jsonAdapter.fromJson(json)
        }
        fun fromJsonOrThrow(json: String?): PublicKey =
            jsonAdapter.fromJson(json ?: throw Exception("Missing public key json"))
                    ?: throw Exception("Failed to construct public key")
    }

    override fun equals(other: Any?): Boolean =
            (other is PublicKey) &&
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

    private external fun sealJNI(
        publicKeyBytes: ByteArray,
        keyDerivationOptionsJson: String,
        plaintext: ByteArray,
        postDecryptionInstructionsJson: String = ""
    ): ByteArray

    public fun seal(
            message: ByteArray,
            postDecryptionInstructionsJson: String = ""
    ): ByteArray {
        return sealJNI(keyBytes, keyDerivationOptionsJson, message, postDecryptionInstructionsJson)
    }


    fun getJsonQrCode(
        maxEdgeLengthInDevicePixels: Int = qrCodeNativeSizeInQrCodeSquarePixels * 2
    ): Bitmap = QrCodeBitmap(
        "https://dicekeys.org/pk/",
        toJson(),
        maxEdgeLengthInDevicePixels
    )

    fun getJsonQrCode(
            maxWidth: Int,
            maxHeight: Int
    ): Bitmap = getJsonQrCode(kotlin.math.min(maxWidth, maxHeight))

}
