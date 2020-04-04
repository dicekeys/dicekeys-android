package org.dicekeys.crypto.seeded

import android.graphics.Bitmap
import org.dicekeys.crypto.seeded.utilities.QrCodeBitmap
import org.dicekeys.crypto.seeded.utilities.qrCodeNativeSizeInQrCodeSquarePixels

class PublicPrivateKeyPair private constructor(internal val nativeObjectPtr: Long) {
    companion object {
        init {
            ensureJniLoaded()
        }

        @JvmStatic external fun constructJNI(
            secretKeyBytes: ByteArray,
            publicKeyBytes: ByteArray,
            keyDerivationOptionsJson: String
        ) : Long
        @JvmStatic external fun constructJNI(
            seedString: String,
            keyDerivationOptionsJson: String
        ) : Long

        @JvmStatic external fun constructFromJsonJNI(json: String) : Long
    }
    private external fun getPublicKeyPtrJNI(): Long
    private external fun deleteNativeObjectPtrJNI()
    private external fun secretKeyBytesGetterJNI(): ByteArray
    private external fun publicKeyBytesGetterJNI(): ByteArray
    private external fun keyDerivationOptionsJsonGetterJNI(): String
    external fun toJson(): String

    // Create copy constructor to prevent copying of the native pointer, which would lead
    // to a use-after-dereference pointer vulnerability
    constructor(
        other: PublicPrivateKeyPair
    ) : this(other.secretKeyBytes, other.publicKeyBytes, other.keyDerivationOptionsJson)

    constructor(
        secretKeyBytes: ByteArray,
        publicKeyBytes: ByteArray,
        keyDerivationOptionsJson: String
    ) : this( constructJNI(secretKeyBytes, publicKeyBytes, keyDerivationOptionsJson))

    constructor(
        seedString: String,
        keyDerivationOptionsJson: String = ""
    ) : this ( constructJNI( seedString, keyDerivationOptionsJson ) )

    protected fun finalize() {
        deleteNativeObjectPtrJNI()
    }

    fun getPublicKey(): PublicKey {
        return PublicKey(getPublicKeyPtrJNI())
    }


    val secretKeyBytes get() = secretKeyBytesGetterJNI()
    val publicKeyBytes get() = publicKeyBytesGetterJNI()
    val keyDerivationOptionsJson get() = keyDerivationOptionsJsonGetterJNI()

    override fun equals(other: Any?): Boolean =
            (other is PublicPrivateKeyPair) &&
                    keyDerivationOptionsJson == other.keyDerivationOptionsJson &&
                    secretKeyBytes.contentEquals(other.secretKeyBytes) &&
                    publicKeyBytes.contentEquals(other.publicKeyBytes)

    external fun unseal(
        ciphertext: ByteArray,
        postDecryptionInstructionsJson: String = ""
    ): ByteArray

//    fun getJsonQrCode(
//        maxEdgeLengthInDevicePixels: Int = qrCodeNativeSizeInQrCodeSquarePixels * 2
//    ): Bitmap = QrCodeBitmap(
//        "https://dicekeys.org/pk/",
//        toJson(),
//        maxEdgeLengthInDevicePixels
//    )
//
//    fun getJsonQrCode(
//            maxWidth: Int,
//            maxHeight: Int
//    ): Bitmap = getJsonQrCode(kotlin.math.min(maxWidth, maxHeight))

}
