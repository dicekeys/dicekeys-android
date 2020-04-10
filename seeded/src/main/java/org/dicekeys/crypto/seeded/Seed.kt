package org.dicekeys.crypto.seeded

/**
 * This class represents seed/secret, which is itself (re)derived from another
 * secret seed and set of key-derivation specified options in
 * @ref key_derivation_options_format.
 *
 * Because seed derivation uses a one-way function, this seed can be shared without revealing the
 * secret used to derive it.
 * It can then be used and, if lost, re-derived from the original seed and
 * [keyDerivationOptionsJson] that were first used to derive it.
 *
 * This class wraps the native c++ Seed class from the
 * DiceKeys seeded cryptography library.
 */
class Seed private constructor(internal val nativeObjectPtr: Long) {

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
         * Derive a new [Seed] from a secret seed string and a
         * set of key-derivation options specified in JSON format.
         */
        fun deriveFromSeed(
                seedString: String,
                keyDerivationOptionsJson: String
        ) = Seed(deriveFromSeedJNI(seedString, keyDerivationOptionsJson))



        @JvmStatic private external fun fromJsonJNI(
                seedJson: String
        ) : Long

        /**
         * Construct a [Seed] from a JSON format string,
         * replicating the [Seed] on which [toJson]
         * was called to generate [seedAsJson]
         */
        @JvmStatic fun fromJson(
            seedAsJson: String
        ): Seed =
            Seed(fromJsonJNI(seedAsJson)
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
        ) : Seed = Seed(fromSerializedBinaryFormJNI(asSerializedBinaryForm))

    }

    /**
     * Convert this object to serialized binary form so that this object
     * can be replicated/reconstituted via a call to [fromSerializedBinaryForm]
     */
    external fun toSerializedBinaryForm(): ByteArray

    private external fun deleteNativeObjectPtrJNI()
    private external fun seedBytesGetterJNI(): ByteArray
    private external fun keyDerivationOptionsJsonGetterJNI(): String

    /**
     * Serialize the object to a JSON format that stores both the [seedBytes]
     * and the [keyDerivationOptionsJson] used to generate it.
     * (The secret seed string used to generate it is not stored, as it is
     * not kept after the object is constructed.)
     */
    external fun toJson(): String

    /**
     * The seed as a byte array.
     *
     * Unlike the raw byte arrays generated for keys (e.g. [PrivateKey]s and [SigningKey]s),
     * which perform operations on internal binary keys and discourage callers from accessing
     * them directly,
     * the purpose of the [Seed] class is to expose this array of [seedBytes] to the
     * creator of this object.
     */
    val seedBytes: ByteArray get() = seedBytesGetterJNI()

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
            other: Seed
    ) : this(other.seedBytes, other.keyDerivationOptionsJson)

    /**
     * Construct this object from its member values
     */
    internal constructor(
            seedBytes: ByteArray,
            keyDerivationOptionsJson: String
    ) : this( constructJNI(seedBytes, keyDerivationOptionsJson) )

    protected fun finalize() {
        deleteNativeObjectPtrJNI()
    }

}
