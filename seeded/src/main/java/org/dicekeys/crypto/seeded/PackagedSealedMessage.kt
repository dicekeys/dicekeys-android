package org.dicekeys.crypto.seeded

/**
 * This class stores everything needed to unseal a message
 * sealed with a [SymmetricKey] or [SealingKey], so long as you have either the seed from which
 * that key was derived from _or_ the key itself:
 *
 * This class wraps the native c++ PackagedSealedMessage class from the
 * DiceKeys [Seeded Cryptography Library](https://dicekeys.github.io/seeded-crypto/).

 */
class PackagedSealedMessage internal constructor(
  internal val nativeObjectPtr: Long
): BinarySerializable,JsonSerializable {

    companion object {
        init {
            ensureJniLoaded()
        }

        @JvmStatic private external fun constructJNI(
            ciphertext: ByteArray,
            recipe: String,
            unsealingInstructions: String
        ) : Long

        @JvmStatic private external fun fromJsonJNI(
                packagedSealedMessageInJsonFormat: String
        ) : Long

        @JvmStatic private external fun fromSerializedBinaryFormJNI(
                packagedSealedMessageInSerializedBinaryForm: ByteArray
        ) : Long

        /**
         * Construct a [PackagedSealedMessage] from a JSON format string,
         * replicating the [PackagedSealedMessage] on which [toJson]
         * was called to generate [packagedSealedMessageAsJson]
         */
        fun fromJson(
                packagedSealedMessageAsJson: String
        ) : PackagedSealedMessage = PackagedSealedMessage(
                fromJsonJNI(packagedSealedMessageAsJson)
        )

        /**
         * Reconstruct this object from serialized binary form using a
         * ByteArray that was constructed via [toSerializedBinaryForm].
         */
        fun fromSerializedBinaryForm(
                asSerializedBinaryForm: ByteArray
        ) : PackagedSealedMessage = PackagedSealedMessage(fromSerializedBinaryFormJNI(asSerializedBinaryForm))

    }

    /**
     * Convert this object to serialized binary form so that this object
     * can be replicated/reconstituted via a call to [fromSerializedBinaryForm]
     */
    external override fun toSerializedBinaryForm(): ByteArray

    /**
     * Serialize the object to a JSON format that stores the [ciphertext],
     * [recipeJson], and [unsealingInstructions].
     * It can then be reconstructed via a call to [fromJson].
     */
    external override fun toJson(): String

    private external fun deleteNativeObjectPtrJNI()

    private external fun ciphertextGetterJNI(): ByteArray
    private external fun recipeGetterJNI(): String
    private external fun unsealingInstructionsGetterJNI(): String

    /**
     * The encrypted message in binary format
     */
    val ciphertext: ByteArray get() = ciphertextGetterJNI()

    /**
     * The options that guided the derivation of the key used to seal/unseal the message.
     */
    val recipe: String get() = recipeGetterJNI()

    /**
     * An optional string that provides instructions the party unsealing the message should
     * be aware of (or is asked to follow).  If this is changed between when the message
     * is sealed and when it is unsealed the unseal operation will fail.
     */
    val unsealingInstructions: String get() = unsealingInstructionsGetterJNI()

    /**
     * A copy constructor to prevent copying of the native pointer, which would lead
     * to a use-after-dereference pointer vulnerability
     */
    constructor(
        other: PackagedSealedMessage
    ) : this(other.ciphertext, other.recipe, other.unsealingInstructions)

    /**
     * Construct this object from its member values
     */
    constructor(
            ciphertext: ByteArray,
            recipe: String,
            unsealingInstructions: String
    ) : this( constructJNI(ciphertext, recipe, unsealingInstructions) )

    protected fun finalize() {
        deleteNativeObjectPtrJNI()
    }

}
