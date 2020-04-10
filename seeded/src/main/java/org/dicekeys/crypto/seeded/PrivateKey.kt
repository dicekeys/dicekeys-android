package org.dicekeys.crypto.seeded

//import org.dicekeys.crypto.seeded.utilities.QrCodeBitmap
//import org.dicekeys.crypto.seeded.utilities.qrCodeNativeSizeInQrCodeSquarePixels

/**
 * A wrapper for the native c++ PrivateKey class from the DiceKeys seeded cryptography library.
 *
 * A [PrivateKey] is used to _unseal_ messages sealed with its
 * corresponding [PublicKey].
 * The [PrivateKey] and [PublicKey] are generated
 * from a seed and a set of key-derivation specified options in
 * @ref key_derivation_options_format.
 *
 * The [PrivateKey] includes a copy of the public key in binary format, which can be
 * reconstituted as a [PublicKey] object via the [getPublicKey] method.
 */
class PrivateKey private constructor(internal val nativeObjectPtr: Long) {
    companion object {
        init {
            ensureJniLoaded()
        }

        @JvmStatic private external fun constructJNI(
            privateKeyBytes: ByteArray,
            publicKeyBytes: ByteArray,
            keyDerivationOptionsJson: String
        ) : Long
        @JvmStatic private external fun deriveFromSeedJNI(
            seedString: String,
            keyDerivationOptionsJson: String
        ) : Long

        /**
         * Derive a public/private key pair from a seed and a
         * set of key-derivation options specified in JSON format.
         */
        fun deriveFromSeed(
                seedString: String,
                keyDerivationOptionsJson: String
        ) = PrivateKey(deriveFromSeedJNI(seedString, keyDerivationOptionsJson))



        @JvmStatic private external fun fromJsonJNI(json: String) : Long

        /**
         * Construct a [PrivateKey] from a JSON format string,
         * replicating the [PrivateKey] on which [toJson]
         * was called to generate [privateKeyAsJson]
         */
        @JvmStatic fun fromJson(
                privateKeyAsJson: String
        ): PrivateKey =
            PrivateKey(fromJsonJNI(privateKeyAsJson)
        )


        @JvmStatic private external fun fromSerializedBinaryFormJNI(
                asSerializedBinaryForm: ByteArray
        ) : Long

        /**
         * Reconstruct this object from serialized binary form using a
         * ByteArray that was constructed via [toSerializedBinaryForm].
         */
        fun fromSerializedBinaryForm(
                asSerializedBinaryForm: ByteArray
        ) : PrivateKey = PrivateKey(fromSerializedBinaryFormJNI(asSerializedBinaryForm))

        fun unseal(
                seedString: String,
                packagedSealedMessage: PackagedSealedMessage
        ) : ByteArray {
            return PrivateKey.deriveFromSeed(
                seedString, packagedSealedMessage.keyDerivationOptionsJson
            ).unseal(
                packagedSealedMessage.ciphertext,
                packagedSealedMessage.postDecryptionInstructionsJson
            )
        }

    }

    /**
     * Convert this object to serialized binary form so that this object
     * can be replicated/reconstituted via a call to [fromSerializedBinaryForm]
     */
    external fun toSerializedBinaryForm(): ByteArray

    private external fun getPublicKeyPtrJNI(): Long
    private external fun deleteNativeObjectPtrJNI()
    private external fun privateKeyBytesGetterJNI(): ByteArray
    private external fun publicKeyBytesGetterJNI(): ByteArray
    private external fun keyDerivationOptionsJsonGetterJNI(): String
    external fun toJson(): String

    /**
     * This constructor ensures copying does not copy the underlying pointer, which could
     * lead to a use-after-free vulnerability or an exception on the second deletion.
     */
    constructor(
        other: PrivateKey
    ) : this(other.privateKeyBytes, other.publicKeyBytes, other.keyDerivationOptionsJson)

    internal constructor(
        privateKeyBytes: ByteArray,
        publicKeyBytes: ByteArray,
        keyDerivationOptionsJson: String
    ) : this( constructJNI(privateKeyBytes, publicKeyBytes, keyDerivationOptionsJson))

    protected fun finalize() {
        deleteNativeObjectPtrJNI()
    }

    /**
     * Get the corresponding [PublicKey] that can seal messages such that they can only
     * be unsealed with the [PrivateKey].
     */
    fun getPublicKey(): PublicKey {
        return PublicKey(getPublicKeyPtrJNI())
    }

    /**
     * The internal binary representation of this private key.
     *
     * (You should not need to access this directly unless you are
     * need to extend the functionality of this library by operating
     * on keys directly.)
     */
    val privateKeyBytes get() = privateKeyBytesGetterJNI()
    /**
     * The internal binary representation of this private key's
     * corresponding public key.
     *
     * (You should not need to access this directly unless you are
     * need to extend the functionality of this library by operating
     * on keys directly.)
     */
    val publicKeyBytes get() = publicKeyBytesGetterJNI()

    /**
     * The options that guided the derivation of this key from the seed.
     */
    val keyDerivationOptionsJson get() = keyDerivationOptionsJsonGetterJNI()

    override fun equals(other: Any?): Boolean =
            (other is PrivateKey) &&
                    keyDerivationOptionsJson == other.keyDerivationOptionsJson &&
                    privateKeyBytes.contentEquals(other.privateKeyBytes) &&
                    publicKeyBytes.contentEquals(other.publicKeyBytes)

    /**
     * Unseal a ciphertext that was sealed by this key's corresponding [PublicKey].
     *
     * If a [postDecryptionInstructionsJson] was passed to the [PublicKey.seal] operation,
     * the exact same string must also be passed as [postDecryptionInstructionsJson] here.
     * This allows the sealer to specify a public-set of instructions that the party
     * unsealing must be aware of before the message can be unsealed.
     */
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
