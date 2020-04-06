package org.dicekeys.crypto.seeded

import android.graphics.Bitmap
import org.dicekeys.crypto.seeded.utilities.QrCodeBitmap
import org.dicekeys.crypto.seeded.utilities.qrCodeNativeSizeInQrCodeSquarePixels

class PrivateKey private constructor(internal val nativeObjectPtr: Long) {
    companion object {
        init {
            ensureJniLoaded()
        }

        @JvmStatic external fun constructJNI(
            privateKeyBytes: ByteArray,
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
    private external fun privateKeyBytesGetterJNI(): ByteArray
    private external fun publicKeyBytesGetterJNI(): ByteArray
    private external fun keyDerivationOptionsJsonGetterJNI(): String
    external fun toJson(): String

    // Create copy constructor to prevent copying of the native pointer, which would lead
    // to a use-after-dereference pointer vulnerability
    constructor(
        other: PrivateKey
    ) : this(other.privateKeyBytes, other.publicKeyBytes, other.keyDerivationOptionsJson)

    constructor(
        privateKeyBytes: ByteArray,
        publicKeyBytes: ByteArray,
        keyDerivationOptionsJson: String
    ) : this( constructJNI(privateKeyBytes, publicKeyBytes, keyDerivationOptionsJson))

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


    val privateKeyBytes get() = privateKeyBytesGetterJNI()
    val publicKeyBytes get() = publicKeyBytesGetterJNI()
    val keyDerivationOptionsJson get() = keyDerivationOptionsJsonGetterJNI()

    override fun equals(other: Any?): Boolean =
            (other is PrivateKey) &&
                    keyDerivationOptionsJson == other.keyDerivationOptionsJson &&
                    privateKeyBytes.contentEquals(other.privateKeyBytes) &&
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
