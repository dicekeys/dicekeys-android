package org.dicekeys.crypto.seeded

/**
 * This class represents secret , which is  derived from a seed
 * and set of key-derivation specified options in
 * [Key-Derivation Options JSON Format](https://dicekeys.github.io/seeded-crypto/key_derivation_options_format.html).
 *
 * Because secret derivation uses a one-way function, this secret can be shared without
 * revealing the secret seed used to derive it.
 * It can then be used and, if lost, re-derived from the original seed and
 * [keyDerivationOptionsJson] that were first used to derive it.
 *
 * This class wraps the native c++ Secret class from the
 * DiceKeys [Seeded Cryptography Library](https://dicekeys.github.io/seeded-crypto/).
 */
class Secret private constructor(internal val nativeObjectPtr: Long) {

    companion object {
        init {
            ensureJniLoaded()
        }

        @JvmStatic private external fun constructJNI(
                seedBytes: ByteArray,
                keyDerivationOptionsJson: String
        ) : Long

        @JvmStatic private external fun deriveFromSeedJNI(
                seedString: String,
                keyDerivationOptionsJson: String
        ) : Long

        /**
         * Derive a new [Secret] from a secret seed string and a
         * set of key-derivation options specified in JSON format.
         */
        @JvmStatic
        fun deriveFromSeed(
                seedString: String,
                keyDerivationOptionsJson: String
        ) = Secret(deriveFromSeedJNI(seedString, keyDerivationOptionsJson))


        @JvmStatic private external fun fromJsonJNI(
                seedJson: String
        ) : Long

        /**
         * Construct a [Secret] from a JSON format string,
         * replicating the [Secret] on which [toJson]
         * was called to generate [seedAsJson]
         */
        @JvmStatic fun fromJson(
            seedAsJson: String
        ): Secret =
            Secret(fromJsonJNI(seedAsJson)
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
        ) : Secret = Secret(fromSerializedBinaryFormJNI(asSerializedBinaryForm))

    }

    /**
     * Convert this object to serialized binary form so that this object
     * can be replicated/reconstituted via a call to [fromSerializedBinaryForm]
     */
    external fun toSerializedBinaryForm(): ByteArray

    private external fun deleteNativeObjectPtrJNI()
    private external fun secretBytesGetterJNI(): ByteArray
    private external fun keyDerivationOptionsJsonGetterJNI(): String

    /**
     * Serialize the object to a JSON format that stores both the [secretBytes]
     * and the [keyDerivationOptionsJson] used to generate it.
     * (The secret seed string used to generate it is not stored, as it is
     * not kept after the object is constructed.)
     */
    external fun toJson(): String

    /**
     * The secret as a byte array.
     *
     * Unlike the raw byte arrays generated for keys (e.g. [PrivateKey]s and [SigningKey]s),
     * which perform operations on internal binary keys and discourage callers from accessing
     * them directly,
     * the purpose of the [Secret] class is to expose this array of [secretBytes] to the
     * creator of this object.
     */
    val secretBytes: ByteArray get() = secretBytesGetterJNI()

    /**
     * The options that guided the derivation of this key from the raw seed that was
     * passed to [fromJson].
     */
    val keyDerivationOptionsJson: String get() = keyDerivationOptionsJsonGetterJNI()

    /**
     * A copy constructor to prevent copying of the native pointer, which would lead
     * to a use-after-dereference pointer vulnerability
     */
    constructor(
            other: Secret
    ) : this(other.secretBytes, other.keyDerivationOptionsJson)

    /**
     * Construct this object from its member values
     */
    internal constructor(
            secretBytes: ByteArray,
            keyDerivationOptionsJson: String
    ) : this( constructJNI(secretBytes, keyDerivationOptionsJson) )

    protected fun finalize() {
        deleteNativeObjectPtrJNI()
    }

}
