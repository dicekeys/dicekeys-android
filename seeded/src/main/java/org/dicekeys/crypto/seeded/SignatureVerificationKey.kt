package org.dicekeys.crypto.seeded

import android.graphics.Bitmap
import org.dicekeys.crypto.seeded.utilities.QrCodeBitmap
import org.dicekeys.crypto.seeded.utilities.qrCodeNativeSizeInQrCodeSquarePixels

class SignatureVerificationKey internal constructor(internal val nativeObjectPtr: Long) {
    companion object {
        init {
            ensureJniLoaded()
        }

        @JvmStatic external fun constructFromJsonJNI(json: String) : Long
        @JvmStatic external fun constructJNI(
                keyBytes: ByteArray,
                keyDerivationOptionsJson: String
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
            keyDerivationOptionsJson: String = ""
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

    override fun equals(other: Any?): Boolean =
        (other is SignatureVerificationKey) &&
        keyDerivationOptionsJson == other.keyDerivationOptionsJson &&
        keyBytes.contentEquals(other.keyBytes)

    @ExperimentalUnsignedTypes
    public val asHexDigits: String get() =
        keyBytes.asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }

    external fun verifySignature(
        message: ByteArray,
        signature: ByteArray
    ): Boolean

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
