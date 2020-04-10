package org.dicekeys.crypto.seeded

/**
 * A wrapper for the native c++ SymmetricKey class from the DiceKeys seeded cryptography library.
 *
 * A SymmetricKey can be used to seal and unseal messages.
 * This SymmetricKey class can be (re) derived from a seed using
 * set of key-derivation options specified in @ref key_derivation_options_format.
 * So, you can use this symmetric-key to seal a message, throw the
 * key away, and re-generate the key when you need to unseal the
 * message so long as you still have the original seed and
 * keyDerivationOptionsJson.
 *  
 * Sealing a message (_plaintext_) creates a _ciphertext which contains
 * the message but from which observers who do not have the PrivateKey
 * cannot discern the contents of the message.
 * Sealing also provides integrity-protection, which will preven the
 * message from being unsealed if it is modified.
 * We use the verbs seal and unseal, rather than encrypt and decrypt,
 * because the encrypting alone does not confer that the message includes
 * an integrity (message authentica
 * Supports authenticated encryption and decryption via the [seal] and [unseal] methods.
 *
 * Can be serialized into JSON format via the [toJson] method and restored from JSON
 * by calling the constructor with a JSON string.
 *
 * This class wraps the native c++ class SymmetricKey class from DiceKeys seeded crypto library.
 */
class SymmetricKey private constructor(internal val nativeObjectPtr: Long) {

    companion object {
        init {
            ensureJniLoaded()
        }

        @JvmStatic private external fun constructJNI(
            keyBytes: ByteArray,
            keyDerivationOptionsJson: String
        ) : Long
        @JvmStatic private external fun constructJNI(
            seedString: String,
            keyDerivationOptionsJson: String
        ) : Long
        @JvmStatic private external fun fromJsonJNI(
            symmetricKeyAsJson: String
        ) : Long

        /**
         * Construct a [SymmetricKey] from a JSON format string,
         * replicating the [SymmetricKey] on which [toJson]
         * was called to generate [symmetricKeyAsJson]
         */
        @JvmStatic fun fromJson(
            symmetricKeyAsJson: String
        ): SymmetricKey =
            SymmetricKey(fromJsonJNI(symmetricKeyAsJson)
        )


        @JvmStatic private external fun fromSerializedBinaryFormJNI(
            asSerializedBinaryForm: ByteArray
        ) : Long

        /**
         * Reconstruct this object from serialized binary form using a
         * ByteArray that was constructed via [toSerializedBinaryForm].
         */
        @JvmStatic fun fromSerializedBinaryForm(
            asSerializedBinaryForm: ByteArray
        ) : SymmetricKey = SymmetricKey(fromSerializedBinaryFormJNI(asSerializedBinaryForm))

        @JvmStatic fun unseal(
            seedString: String,
            packagedSealedMessage: PackagedSealedMessage
        ) : ByteArray {
            return SymmetricKey(
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

    private external fun deleteNativeObjectPtrJNI()
    private external fun keyBytesGetterJNI(): ByteArray
    private external fun keyDerivationOptionsJsonGetterJNI(): String

    /**
     * Convert this symmetric key to JSON-format string so that it can be later reconstituted
     * by passing the string to the constructor.
     */
    external fun toJson(): String

    /**
     * The binary representation of the symmetric key.
     *
     * (You should not need to access this directly unless you are
     * need to extend the functionality of this library by operating
     * on keys directly.)
     */
    val keyBytes: ByteArray get() = keyBytesGetterJNI()

    /**
     * The options that guided the derivation of this key from the seed.
     */
    val keyDerivationOptionsJson: String get() = keyDerivationOptionsJsonGetterJNI()

    /**
     * Construct this object manually by passing the [keyBytes] and the
     * [keyDerivationOptionsJson] that was used in the derivation of [keyBytes].
     */
    internal constructor(
        keyBytes: ByteArray,
        keyDerivationOptionsJson: String
    ) : this( constructJNI(keyBytes, keyDerivationOptionsJson) )

    /**
     * A copy constructor to prevent copying of the native pointer, which would lead
     * to a use-after-dereference pointer vulnerability
     */
    internal constructor(
        other: SymmetricKey
    ) : this(other.keyBytes, other.keyDerivationOptionsJson)

    /**
     * Construct a symmetric key from a secret [seedString], which should have enough
     * entropy to make it hard to guess (e.g. 128+ bits) and a set of public (non-secret)
     * key-derivation options ([keyDerivationOptionsJson]).
     */
    constructor(
        seedString: String,
        keyDerivationOptionsJson: String
    ) : this(constructJNI(seedString, keyDerivationOptionsJson))

    protected fun finalize() {
        deleteNativeObjectPtrJNI()
    }

    /**
     * Use authenticated encryption to seal a [plaintext] message for secure storage or
     * transmission, so that it can later be decrypted and authenticated by calling
     * [unseal] with the same [SymmetricKey] (or a copy of the key).
     *
     * If a [postDecryptionInstructionsJson] string is passed,
     * the exact same string must also be passed as [postDecryptionInstructionsJson]
     * to [PrivateKey.unseal] the message with the corresponding [PrivateKey].
     * This allows the sealer to specify a public-set of instructions that the the party
     * unsealing must be aware of before the message can be unsealed.
     */
    public external fun seal(
        plaintext: ByteArray,
        postDecryptionInstructionsJson: String = ""
    ): ByteArray

    private external fun sealAndPackageJNI(
        plaintext: ByteArray,
        postDecryptionInstructionsJson: String
    ): Long

    fun sealAndPackage(
        plaintext: ByteArray,
        postDecryptionInstructionsJson: String = ""
    ): PackagedSealedMessage = PackagedSealedMessage(
        sealAndPackageJNI( plaintext, postDecryptionInstructionsJson)
    )

    fun sealAndPackage(
        plaintext: String,
        postDecryptionInstructionsJson: String = ""
    ): PackagedSealedMessage = sealAndPackage(
        plaintext.toByteArray(),
        postDecryptionInstructionsJson
    )

    /**
     * Decrypt and authenticate a message which had been sealed by [seal].
     *
     * If a [postDecryptionInstructionsJson] was passed to the [PublicKey.seal] operation,
     * the exact same string must also be passed as [postDecryptionInstructionsJson] here.
     * This allows the sealer to specify a public-set of instructions that the party
     * unsealing must be aware of before the message can be unsealed.

     */
    public external fun unseal(
        ciphertext: ByteArray,
        postDecryptionInstructionsJson: String = ""
    ): ByteArray


}
