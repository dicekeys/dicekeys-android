package org.dicekeys.crypto.seeded

//import org.dicekeys.crypto.seeded.utilities.QrCodeBitmap
//import org.dicekeys.crypto.seeded.utilities.qrCodeNativeSizeInQrCodeSquarePixels

/**
 * A [UnsealingKey] is used to _unseal_ messages sealed with its
 * corresponding [SealingKey].
 * The [UnsealingKey] and [SealingKey] are generated
 * from a seed and a set of key-derivation specified options in
 * [Key-Derivation Options JSON Format](hhttps://dicekeys.github.io/seeded-crypto/derivation_options_format.html).
 *
 * The [UnsealingKey] includes a copy of the public key in binary format, which can be
 * reconstituted as a [SealingKey] object via the [getSealingkey] method.
 *
 * This class wraps the native c++ PrivateKey class from the
 * DiceKeys [Seeded Cryptography Library](https://dicekeys.github.io/seeded-crypto/).
 */
class UnsealingKey private constructor(
  internal val nativeObjectPtr: Long
): BinarySerializable, JsonSerializable {
    companion object {
        init {
            ensureJniLoaded()
        }

        @JvmStatic private external fun constructJNI(
            privateKeyBytes: ByteArray,
            publicKeyBytes: ByteArray,
            derivationOptionsJson: String
        ) : Long
        @JvmStatic private external fun deriveFromSeedJNI(
            seedString: String,
            derivationOptionsJson: String
        ) : Long

        /**
         * Derive a public/private key pair from a seed and a
         * set of key-derivation options specified in JSON format.
         */
        fun deriveFromSeed(
                seedString: String,
                derivationOptionsJson: String
        ) = UnsealingKey(deriveFromSeedJNI(seedString, derivationOptionsJson))



        @JvmStatic private external fun fromJsonJNI(json: String) : Long

        /**
         * Construct a [UnsealingKey] from a JSON format string,
         * replicating the [UnsealingKey] on which [toJson]
         * was called to generate [privateKeyAsJson]
         */
        @JvmStatic fun fromJson(
                privateKeyAsJson: String
        ): UnsealingKey =
            UnsealingKey(fromJsonJNI(privateKeyAsJson)
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
        ) : UnsealingKey = UnsealingKey(fromSerializedBinaryFormJNI(asSerializedBinaryForm))

        /**
         * Unseal a message by re-deriving the [UnsealingKey] from the secret [seedString]
         * used to originally derive it.  The [PackagedSealedMessage.derivationOptionsJson]
         * needed to derive it is in the [packagedSealedMessage], as are the
         * [PackagedSealedMessage.ciphertext] and
         * [PackagedSealedMessage.unsealingInstructions].
         */
        fun unseal(
                seedString: String,
                packagedSealedMessage: PackagedSealedMessage
        ) : ByteArray {
            return deriveFromSeed(
                seedString, packagedSealedMessage.derivationOptionsJson
            ).unseal(
                packagedSealedMessage.ciphertext,
                packagedSealedMessage.unsealingInstructions
            )
        }

    }

    /**
     * Convert this object to serialized binary form so that this object
     * can be replicated/reconstituted via a call to [fromSerializedBinaryForm]
     */
    external override fun toSerializedBinaryForm(): ByteArray

    private external fun getSealingKeyPtrJNI(): Long
    private external fun deleteNativeObjectPtrJNI()
    private external fun unsealingKeyBytesGetterJNI(): ByteArray
    private external fun sealingKeyBytesGetterJNI(): ByteArray
    private external fun derivationOptionsJsonGetterJNI(): String
    external override fun toJson(): String

    /**
     * This constructor ensures copying does not copy the underlying pointer, which could
     * lead to a use-after-free vulnerability or an exception on the second deletion.
     */
    constructor(
        other: UnsealingKey
    ) : this(other.unsealingKeyBytes, other.sealingKeyBytes, other.derivationOptionsJson)

    internal constructor(
        privateKeyBytes: ByteArray,
        publicKeyBytes: ByteArray,
        derivationOptionsJson: String
    ) : this( constructJNI(privateKeyBytes, publicKeyBytes, derivationOptionsJson))

    protected fun finalize() {
        deleteNativeObjectPtrJNI()
    }

    /**
     * Get the corresponding [SealingKey] that can seal messages such that they can only
     * be unsealed with the [UnsealingKey].
     */
    fun getSealingkey(): SealingKey {
        return SealingKey(getSealingKeyPtrJNI())
    }

    /**
     * The internal binary representation of this private unsealing key.
     *
     * (You should not need to access this directly unless you are
     * need to extend the functionality of this library by operating
     * on keys directly.)
     */
    val unsealingKeyBytes get() = unsealingKeyBytesGetterJNI()
    /**
     * The internal binary representation of this unsealing key's
     * corresponding public SealingKey.
     *
     * (You should not need to access this directly unless you are
     * need to extend the functionality of this library by operating
     * on keys directly.)
     */
    val sealingKeyBytes get() = sealingKeyBytesGetterJNI()

    /**
     * The options that guided the derivation of this key from the seed.
     */
    val derivationOptionsJson get() = derivationOptionsJsonGetterJNI()

    override fun equals(other: Any?): Boolean =
            (other is UnsealingKey) &&
                    derivationOptionsJson == other.derivationOptionsJson &&
                    unsealingKeyBytes.contentEquals(other.unsealingKeyBytes) &&
                    sealingKeyBytes.contentEquals(other.sealingKeyBytes)

    /**
     * Unseal a ciphertext that was sealed by this key's corresponding [SealingKey].
     *
     * If a [unsealingInstructions] was passed to the [SealingKey.seal] operation,
     * the exact same string must also be passed as [unsealingInstructions] here.
     * This allows the sealer to specify a public-set of instructions that the party
     * unsealing must be aware of before the message can be unsealed.
     */
    external fun unseal(
        ciphertext: ByteArray,
        unsealingInstructions: String = ""
    ): ByteArray

    /**
     * Unseal a [PackagedSealedMessage] that was sealed with the [SealingKey]
     * corresponding to this [UnsealingKey].
     */
    fun unseal(
        packagedSealedMessage: PackagedSealedMessage
    ): ByteArray = unseal(
        packagedSealedMessage.ciphertext,
        packagedSealedMessage.unsealingInstructions
    )

}
