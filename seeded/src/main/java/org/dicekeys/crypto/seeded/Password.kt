package org.dicekeys.crypto.seeded

/**
 * This class represents secret , which is  derived from a seed
 * and set of key-derivation specified options in
 * [Key-Derivation Options JSON Format](hhttps://dicekeys.github.io/seeded-crypto/derivation_options_format.html).
 *
 * Because secret derivation uses a one-way function, this secret can be shared without
 * revealing the secret seed used to derive it.
 * It can then be used and, if lost, re-derived from the original seed and
 * [derivationOptionsJson] that were first used to derive it.
 *
 * This class wraps the native c++ Secret class from the
 * DiceKeys [Seeded Cryptography Library](https://dicekeys.github.io/seeded-crypto/).
 */
class Password private constructor(
  internal val nativeObjectPtr: Long
): BinarySerializable,JsonSerializable {

    companion object {
        init {
            ensureJniLoaded()
        }

        @JvmStatic private external fun constructJNI(
                password: String,
                derivationOptionsJson: String
        ) : Long

        @JvmStatic private external fun deriveFromSeedJNI(
                seedString: String,
                derivationOptionsJson: String,
                wordListAsSingleString: String = ""
        ) : Long

        /**
         * Derive a new [Password] from a secret seed string and a
         * set of key-derivation options specified in JSON format.
         * May include a word list as a single string delimited by tabs,
         * spaces, commas, or any non-letter character.
         */
        @JvmStatic
        fun deriveFromSeed(
                seedString: String,
                derivationOptionsJson: String,
                wordListAsSingleString: String = ""
        ) = Password(deriveFromSeedJNI(seedString, derivationOptionsJson, wordListAsSingleString))


        @JvmStatic private external fun fromJsonJNI(
                json: String
        ) : Long

        /**
         * Construct a [Password] from a JSON format string,
         * replicating the [Password] on which [toJson]
         * was called to generate [json]
         */
        @JvmStatic fun fromJson(
            json: String
        ): Password =
            Password(fromJsonJNI(json)
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
        ) : Password = Password(fromSerializedBinaryFormJNI(asSerializedBinaryForm))

    }

    /**
     * Convert this object to serialized binary form so that this object
     * can be replicated/reconstituted via a call to [fromSerializedBinaryForm]
     */
    external override fun toSerializedBinaryForm(): ByteArray

    private external fun deleteNativeObjectPtrJNI()
    private external fun passwordGetterJNI(): String
    private external fun derivationOptionsJsonGetterJNI(): String

    /**
     * Serialize the object to a JSON format that stores both the [password]
     * and the [derivationOptionsJson] used to generate it.
     * (The secret seed string used to generate it is not stored, as it is
     * not kept after the object is constructed.)
     */
    external override fun toJson(): String

    /**
     * The password as a string.
     */
    val password: String get() = passwordGetterJNI()

    /**
     * The options that guided the derivation of this key from the raw seed that was
     * passed to [fromJson].
     */
    val derivationOptionsJson: String get() = derivationOptionsJsonGetterJNI()

    /**
     * A copy constructor to prevent copying of the native pointer, which would lead
     * to a use-after-dereference pointer vulnerability
     */
    constructor(
            other: Password
    ) : this(other.password, other.derivationOptionsJson)

    /**
     * Construct this object from its member values
     */
    internal constructor(
            password: String,
            derivationOptionsJson: String
    ) : this( constructJNI(password, derivationOptionsJson) )

    protected fun finalize() {
        deleteNativeObjectPtrJNI()
    }

}
