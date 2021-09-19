package org.dicekeys.crypto.seeded

/**
 * [SigningKey]s generate _signatures_ of messages which can then be
 * used by the corresponding [SignatureVerificationKey] to verify that a message
 * was signed by  can confirm that the message was indeed signed by the
 * SigningKey and has not since been tampered with.
 *
 * The corresponding SignatureVerificationKey can be obtained by calling
 * [getSignatureVerificationKey].
 * 
 * The key pair of the [SigningKey] and [SignatureVerificationKey] is generated
 * from a seed and a set of key-derivation specified options in [recipe]
 *
 * This class wraps the native c++ SigningKey class from the
 * DiceKeys [Seeded Cryptography Library](https://dicekeys.github.io/seeded-crypto/).

 */
class SigningKey internal constructor(
  internal val nativeObjectPtr: Long
): BinarySerializable,JsonSerializable  {
    companion object {
        init {
            ensureJniLoaded()
        }
        @JvmStatic private external fun constructJNI(
                signingKeyBytes: ByteArray,
                recipe: String
        ) : Long

        @JvmStatic private external fun constructJNI(
                signingKeyBytes: ByteArray,
                signatureVerificationKeyBytes: ByteArray,
                recipe: String
        ) : Long

        @JvmStatic private external fun deriveFromSeedJNI(
                seedString: String,
                recipe: String
        ) : Long

        /**
         * Derive a signing/signature-verification key pair from a seed and a
         * set of key-derivation options specified in JSON format.
         */
        fun deriveFromSeed(
                seedString: String,
                recipe: String
        ) = SigningKey (deriveFromSeedJNI(seedString, recipe))


        @JvmStatic private external fun fromJsonJNI(signingKeyAsJson: String) : Long

        /**
         * Construct a [SigningKey] from a JSON format string,
         * replicating the [SigningKey] on which [toJson]
         * was called to generate [signingKeyAsJson]
         */
        @JvmStatic fun fromJson(
            signingKeyAsJson: String
        ): SigningKey =
            SigningKey(fromJsonJNI(signingKeyAsJson)
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
        ) : SigningKey = SigningKey(fromSerializedBinaryFormJNI(asSerializedBinaryForm))

    }

    /**
     * Convert this object to serialized binary form so that this object
     * can be replicated/reconstituted via a call to [fromSerializedBinaryForm]
     */
    external override fun toSerializedBinaryForm(): ByteArray

    /**
     * This constructor ensures copying does not copy the underlying pointer, which could
     * lead to a use-after-free vulnerability or an exception on the second deletion.
     */
    constructor(other: SigningKey) : this(
            other.signingKeyBytes,
            other.signatureVerificationKeyBytes,
            other.recipe)

    /**
     * Construct by reconstituting its members (including [signatureVerificationKeyBytes])
     */
    internal constructor(
            signingKeyBytes: ByteArray,
            signatureVerificationKeyBytes: ByteArray,
            recipe: String
    ) : this(constructJNI(signingKeyBytes, signatureVerificationKeyBytes, recipe))

    /**
     * Construct by reconstituting its members (excluding [signatureVerificationKeyBytes], which
     * can be re-generated if needed.
     */
    internal constructor(
            signingKeyBytes: ByteArray,
            recipe: String
    ) : this(constructJNI(signingKeyBytes, recipe))

    private external fun deleteNativeObjectPtrJNI()
    private external fun getSignatureVerificationKeyJNI() : Long
    private external fun signatureVerificationKeyBytesGetterJNI(): ByteArray
    private external fun signingKeyBytesGetterJNI(): ByteArray
    private external fun recipeGetterJNI(): String
    private external fun openPgpPemFormatSecretKeyJNI(userIdPacketContent: String, timestamp: Long): String
    private external fun openSshPemPrivateKeyJNI(comment: String): String
    private external fun openSshPublicKeyJNI(): String
    external fun generateSignature(message: ByteArray): ByteArray

    /**
      * Serialize this object to a JSON-formatted string
      * 
      * It can be reconstituted by calling the constructor with this string.
      * 
      * The JSON-encoding will always include the binary signing key bytes (in hex format)
      * and the keyDerviationOptionsJson used to derive the key, but the
      * signature-verification key bytes will not be included unless you set
      * the first parameter,
      * [minimizeSizeByRemovingTheSignatureVerificationKeyBytesWhichCanBeRegeneratedLater],
      * to false (it defaults to true). As the excessively-long name implies,
      * when the signature-generation key is not included in the JSON format,
      * if can reconstituted from the signing-key shoudld it be needed again.
      * So, the default saves space at the cost of the computation to recalculate
      * the signature-verification key if it is needed later.
      */
    external fun toJson(
        minimizeSizeByRemovingTheSignatureVerificationKeyBytesWhichCanBeRegeneratedLater: Boolean
    ): String

    override fun toJson(): String = toJson(true)

    /**
    * Generate a signature for a message, which can be used
    * by the corresponding public SignatureVerificationKey to verify that
    * this message was, in fact, signed by this key.
    */
    fun generateSignature(
        message: String
    ): ByteArray = generateSignature(message.toByteArray())

    /**
     * Return the corresponding public signature-verification key that
     * others can use to verify messages signed with this key.
     */
    fun getSignatureVerificationKey(): SignatureVerificationKey =
        SignatureVerificationKey(getSignatureVerificationKeyJNI())

    /**
     * The binary representation of the signing-key.
     *
     * (You should not need to access this directly unless you are
     * need to extend the functionality of this library by operating
     * on keys directly.)

     */
    val signingKeyBytes get() = signingKeyBytesGetterJNI()

    /**
     * The binary representation of the signature-verification key.
     *
     * If this key was reconstituted from a JSON format where the signature-verification key bytes
     * were not stored, accessing this getter will cause them to be regenerated.
     *
     * (You should not need to access this directly unless you are
     * need to extend the functionality of this library by operating
     * on keys directly.)

     */
    val signatureVerificationKeyBytes get() = signatureVerificationKeyBytesGetterJNI()

    /**
     * Get the key-derivation options used to generate this [SigningKey]
     */
    val recipe get() = recipeGetterJNI()


    /**
     * Get the OpenPGP secret key in PEM format
     */
    val openPgpPemFormatSecretKey get() = openPgpPemFormatSecretKeyJNI("", 0)

    /**
     * Get the OpenSSH private key in PEM format
     */
    val openSshPemPrivateKey get() = openSshPemPrivateKeyJNI("")

    /**
     * Get the OpenSSH public key
     */
    val openSshPublicKey get() = openSshPublicKeyJNI()

    override fun equals(other: Any?): Boolean =
            (other is SigningKey) &&
                    recipe == other.recipe &&
                    signingKeyBytes.contentEquals(other.signingKeyBytes) &&
                    signatureVerificationKeyBytes.contentEquals(other.signatureVerificationKeyBytes)

}