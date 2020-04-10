package org.dicekeys.crypto.seeded

/**
 * A wrapper for the native c++ PackagedSealedMessage class from the DiceKeys
 * seeded cryptography library.
 *
 * This class stores everything needed to instruct the DiceKeys app to unseal a message
 * sealed with a [SymmetricKey] or [PublicKey] using only the seed (DiceKey):
 *   * the [ciphertext] that encodes the sealed message,
 *   * the [keyDerivationOptionsJson] that specifies how to re-generate the key, and
 *   * the [postDecryptionInstructionsJson] that provides any public information that the
 *     sealer might want the app unsealing the message to be aware of before unsealing
 *     or releasing unsealed data.
 *
 */
class PackagedSealedMessage internal constructor(internal val nativeObjectPtr: Long) {

    companion object {
        init {
            ensureJniLoaded()
        }

        @JvmStatic private external fun constructJNI(
            ciphertext: ByteArray,
            keyDerivationOptionsJson: String,
            postDecryptionInstructionsJson: String
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
                packagedSealedMessageAsJson: ByteArray
        ) : PackagedSealedMessage = PackagedSealedMessage(
                fromSerializedBinaryFormJNI(packagedSealedMessageAsJson)
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
    external fun toSerializedBinaryForm(): ByteArray

    /**
     * Serialize the object to a JSON format that stores both the [seedBytes]
     * and the [keyDerivationOptionsJson] used to generate it.
     * (The secret seed string used to generate it is not stored, as it is
     * not kept after the object is constructed.)
     */
    external fun toJson(): String

    private external fun deleteNativeObjectPtrJNI()

    private external fun ciphertextGetterJNI(): ByteArray
    private external fun keyDerivationOptionsJsonGetterJNI(): String
    private external fun postDecryptionInstructionsGetterJNI(): String

    /**
     * The encrypted message in binary format
     */
    val ciphertext: ByteArray get() = ciphertextGetterJNI()

    /**
     * The options that guided the derivation of the key used to seal/unseal the message.
     */
    val keyDerivationOptionsJson: String get() = keyDerivationOptionsJsonGetterJNI()

    /**
     * An optional string that provides instructions the party unsealing the message should
     * be aware of (or is asked to follow).  If this is changed between when the message
     * is sealed and when it is unsealed the unseal operation will fail.
     */
    val postDecryptionInstructionsJson: String get() = postDecryptionInstructionsGetterJNI()

    /**
     * A copy constructor to prevent copying of the native pointer, which would lead
     * to a use-after-dereference pointer vulnerability
     */
    constructor(
        other: PackagedSealedMessage
    ) : this(other.ciphertext, other.keyDerivationOptionsJson, other.postDecryptionInstructionsJson)

    /**
     * Construct this object from its member values
     */
    constructor(
            ciphertext: ByteArray,
            keyDerivationOptionsJson: String,
            postDecryptionInstructionsJson: String
    ) : this( constructJNI(ciphertext, keyDerivationOptionsJson, postDecryptionInstructionsJson) )

    protected fun finalize() {
        deleteNativeObjectPtrJNI()
    }

}
