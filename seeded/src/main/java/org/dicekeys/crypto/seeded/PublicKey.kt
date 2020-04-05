package org.dicekeys.crypto.seeded

import android.graphics.Bitmap
import org.dicekeys.crypto.seeded.utilities.QrCodeBitmap
import org.dicekeys.crypto.seeded.utilities.qrCodeNativeSizeInQrCodeSquarePixels
import java.nio.charset.StandardCharsets

class PublicKey internal constructor(internal val nativeObjectPtr: Long) {
    companion object {
        init {
            ensureJniLoaded()
        }

        @JvmStatic external fun constructFromJsonJNI(json: String) : Long
        @JvmStatic external fun constructJNI(
                keyBytes: ByteArray,
                keyDerivationOptionsJson: String = ""
        ) : Long
    }

    // Create copy constructor to prevent copying of the native pointer, which would lead
    // to a use-after-dereference pointer vulnerability
    constructor(
        other: PublicKey
    ) : this(other.keyBytes, other.keyDerivationOptionsJson)

    constructor(
            publicKeyInJsonFormat: String
    ) : this ( constructFromJsonJNI(
            publicKeyInJsonFormat
    ) )

    constructor(
            keyBytes: ByteArray,
            keyDerivationOptionsJson: String
    ) : this ( constructJNI(
            keyBytes,
            keyDerivationOptionsJson
    ) )

    protected fun finalize() {
        deleteNativeObjectPtrJNI()
    }
    private external fun deleteNativeObjectPtrJNI()
    private external fun keyBytesGetterJNI(): ByteArray
    private external fun keyDerivationOptionsJsonGetterJNI(): String
    external fun toJson(): String

    val keyBytes get() = keyBytesGetterJNI()
    val keyDerivationOptionsJson get() = keyDerivationOptionsJsonGetterJNI()

    external fun seal(
            message: ByteArray,
            postDecryptionInstructionsJson: String = ""
    ): ByteArray

    fun seal(
            message: String,
            postDecryptionInstructionsJson: String = ""
    ): ByteArray = seal( message.toByteArray(StandardCharsets.UTF_8), postDecryptionInstructionsJson )

    override fun equals(other: Any?): Boolean =
            (other is PublicKey) &&
            keyDerivationOptionsJson == other.keyDerivationOptionsJson &&
            keyBytes.contentEquals(other.keyBytes)

//    @ExperimentalUnsignedTypes
//    public val asHexDigits: String get() =
//        keyBytes.asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }

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
