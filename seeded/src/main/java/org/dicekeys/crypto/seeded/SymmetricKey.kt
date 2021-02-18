package org.dicekeys.crypto.seeded

/**
 * A SymmetricKey can be used to seal and unseal messages.
 * This SymmetricKey class can be (re) derived from a seed using
 * set of key-derivation options specified in [Recipe JSON Format](https://dicekeys.github.io/seeded-crypto/recipe_format.html).
 * So, you can use this symmetric-key to seal a message, throw the
 * key away, and re-generate the key when you need to unseal the
 * message so long as you still have the original seed and
 * recipe.
 *  
 * Sealing a message (_plaintext_) creates a _ciphertext which contains
 * the message but from which observers who do not have the PrivateKey
 * cannot discern the contents of the message.
 * Sealing also provides integrity-protection, which will preven the
 * message from being unsealed if it is modified.
 * We use the verbs seal and unseal, rather than encrypt and decrypt,
 * because the encrypting alone does not confer that the message includes
 * an integrity (message authentication) check.
 *
 * Supports authenticated encryption and decryption via the [seal] and [unseal] methods.
 *
 * Can be serialized into JSON format via the [toJson] method and restored from JSON
 * by calling the constructor with a JSON string.
 *
 * This class wraps the native c++ SymmetricKey class from the
 * DiceKeys [Seeded Cryptography Library](https://dicekeys.github.io/seeded-crypto/).

 */
class SymmetricKey private constructor(
  internal val nativeObjectPtr: Long
): BinarySerializable,JsonSerializable {

    companion object {
        init {
            ensureJniLoaded()
        }

        @JvmStatic private external fun constructJNI(
            keyBytes: ByteArray,
            recipe: String
        ) : Long
        @JvmStatic private external fun deriveFromSeedJNI(
            seedString: String,
            recipe: String
        ) : Long

        /**
         * Construct a symmetric key from a secret [seedString], which should have enough
         * entropy to make it hard to guess (e.g. 128+ bits) and a set of public (non-secret)
         * key-derivation options ([recipe]).
         */
        @JvmStatic fun deriveFromSeed(
            seedString: String,
            recipe: String
        ) : SymmetricKey = SymmetricKey(deriveFromSeedJNI(seedString, recipe))


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

        /**
         * Unseal a [PackagedSealedMessage] by first re-deriving the [SymmetricKey]
         * from a [seedString].
         */
        @JvmStatic fun unseal(
            packagedSealedMessage: PackagedSealedMessage,
            seedString: String
        ) : ByteArray {
            return deriveFromSeed(
                seedString, packagedSealedMessage.recipe
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

    private external fun deleteNativeObjectPtrJNI()
    private external fun keyBytesGetterJNI(): ByteArray
    private external fun recipeGetterJNI(): String

    /**
     * Convert this symmetric key to JSON-format string so that it can be later reconstituted
     * by passing the string to [fromJson].
     */
    external override fun toJson(): String

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
    val recipe: String get() = recipeGetterJNI()

    /**
     * Construct this object manually by passing the [keyBytes] and the
     * [recipe] that was used in the derivation of [keyBytes].
     */
    internal constructor(
        keyBytes: ByteArray,
        recipe: String
    ) : this( constructJNI(keyBytes, recipe) )

    /**
     * A copy constructor to prevent copying of the native pointer, which would lead
     * to a use-after-dereference pointer vulnerability
     */
    internal constructor(
        other: SymmetricKey
    ) : this(other.keyBytes, other.recipe)

    protected fun finalize() {
        deleteNativeObjectPtrJNI()
    }

    /**
     * Use authenticated encryption to seal a [plaintext] message for secure storage or
     * transmission, so that it can later be decrypted and authenticated by calling
     * [unseal] with the same [SymmetricKey] (or a copy of the key).
     *
     * If a [unsealingInstructions] string is passed,
     * the exact same string must also be passed as [unsealingInstructions]
     * to [UnsealingKey.unseal] the message with the corresponding [UnsealingKey].
     * This allows the sealer to specify a public-set of instructions that the the party
     * unsealing must be aware of before the message can be unsealed.
     */
    external fun sealToCiphertextOnly(
        plaintext: ByteArray,
        unsealingInstructions: String = ""
    ): ByteArray

    /**
     * Seals a string message by first converting it to UTF8 format.
     */
    fun sealToCiphertextOnly(
        plaintext: String,
        unsealingInstructions: String = ""
    ): ByteArray = sealToCiphertextOnly(plaintext.toByteArray(), unsealingInstructions)

    private external fun sealJNI(
        plaintext: ByteArray,
        unsealingInstructions: String
    ): Long

    /**
     * Use authenticated encryption to seal a [plaintext] message for secure storage or
     * transmission, so that it can later be decrypted and authenticated by calling
     * [unseal] with the same [SymmetricKey] (or a copy of the key).
     *
     * If a [unsealingInstructions] string is passed,
     * the exact same string must also be passed as [unsealingInstructions]
     * to [UnsealingKey.unseal] the message with the corresponding [UnsealingKey].
     * This allows the sealer to specify a public-set of instructions that the the party
     * unsealing must be aware of before the message can be unsealed.
     *
     * Returns a [PackagedSealedMessage] containing not only the ciphertext, but the
     * plaintext [unsealingInstructions] the message was sealed with, which
     * are required for unsealing, as well as the [recipe] used to
     * construct this [SymmetricKey] in case it needs to be re-derived from the
     * original secret seed.
     */
    fun seal(
        plaintext: ByteArray,
        unsealingInstructions: String = ""
    ): PackagedSealedMessage = PackagedSealedMessage(
        sealJNI( plaintext, unsealingInstructions)
    )

    /**
     * Seals a string message by first converting it to UTF8 format.
     */
    fun seal(
        plaintext: String,
        unsealingInstructions: String = ""
    ): PackagedSealedMessage = seal(
        plaintext.toByteArray(),
        unsealingInstructions
    )

    /**
     * Decrypt and authenticate a message which had been sealed by [sealToCiphertextOnly].
     *
     * If a [unsealingInstructions] was passed to the [SealingKey.seal] operation,
     * the exact same string must also be passed as [unsealingInstructions] here.
     * This allows the sealer to specify a public-set of instructions that the party
     * unsealing must be aware of before the message can be unsealed.
     */
    public external fun unseal(
        ciphertext: ByteArray,
        unsealingInstructions: String = ""
    ): ByteArray


    /**
     * Unseal a [PackagedSealedMessage] that was sealed with this [SymmetricKey].
     */
    public fun unseal(
        packagedSealedMessage: PackagedSealedMessage
    ): ByteArray = unseal(
        packagedSealedMessage.ciphertext,
        packagedSealedMessage.unsealingInstructions
    )


}
