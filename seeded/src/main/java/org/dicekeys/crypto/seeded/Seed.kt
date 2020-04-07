package org.dicekeys.crypto.seeded

/**
 * A wrapper for the native c++ Seed class from the DiceKeys seeded cryptography library.
 *
 * This class represents seed/secret, which is itself (re)derived from another
 * secret seed and set of key-derivation specified options in
 * @ref key_derivation_options_format.
 *
 * Because seed derivation uses a one-way function, this seed can be shared without revealing the
 * secret used to derive it.
 * It can then be used and, if lost, re-derived from the original seed and
 * [keyDerivationOptionsJson] that were first used to derive it.
 *
 */
class Seed private constructor(internal val nativeObjectPtr: Long) {

    companion object {
        init {
            ensureJniLoaded()
        }

        @JvmStatic internal external fun constructJNI(
                seedBytes: ByteArray,
                keyDerivationOptionsJson: String
        ) : Long

        @JvmStatic internal external fun constructJNI(
                seedString: String,
                keyDerivationOptionsJson: String
        ) : Long

        @JvmStatic internal external fun constructFromJsonJNI(
                seedJson: String
        ) : Long

    }

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
     * passed to the constructor of this object.
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
     * Derive this seed from [seedBytes] and key-derivation options specified as
     * [keyDerivationOptionsJson].
     */
    constructor(
            seedBytes: ByteArray,
            keyDerivationOptionsJson: String
    ) : this( constructJNI(seedBytes, keyDerivationOptionsJson) )

    /**
     * Derive this seed from [seedBytes] and key-derivation options specified as
     * [keyDerivationOptionsJson].
     */
    constructor(
            seedString: String,
            keyDerivationOptionsJson: String
    ) : this(constructJNI(seedString, keyDerivationOptionsJson))

    /**
     * Reconstitute this [Seed] from JSON format
     */
    internal constructor(
            seedJson: String
    ) : this(constructFromJsonJNI(seedJson))

    protected fun finalize() {
        deleteNativeObjectPtrJNI()
    }

}
