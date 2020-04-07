package org.dicekeys.crypto.seeded

/**
 * A wrapper for the native c++ SigningKey class from the DiceKeys seeded cryptography library.
 *
  *SigningKeys generate _signatures_ of messages which can then be
 * used by the corresponding SignatureVerificationKey to verify that a message
 * was signed by  can confirm that the message was indeed signed by the
 * SigningKey and has not since been tampered with.
 *
 * The corresponding SignatureVerificationKey can be obtained by calling
 * [getSignatureVerificationKey].
 * 
 * The key pair of the SigningKey and SignatureVerificationKey is generated
 * from a seed and a set of key-derivation specified options in
 *  @ref key_derivation_options_format.
 */
class SigningKey internal constructor(internal val nativeObjectPtr: Long) {
    companion object {
        init {
            ensureJniLoaded()
        }
        @JvmStatic internal external fun constructJNI(
                signingKeyBytes: ByteArray,
                keyDerivationOptionsJson: String
        ) : Long

        @JvmStatic internal external fun constructJNI(
                signingKeyBytes: ByteArray,
                signatureVerificationKeyBytes: ByteArray,
                keyDerivationOptionsJson: String
        ) : Long

        @JvmStatic internal external fun constructJNI(
                seedString: String,
                keyDerivationOptionsJson: String
        ) : Long

        @JvmStatic internal external fun constructFromJsonJNI(signingKeyAsJson: String) : Long
    }

    /**
     * This constructor ensures copying does not copy the underlying pointer, which could
     * lead to a use-after-free vulnerability or an exception on the second deletion.
     */
    constructor(other: SigningKey) : this(
            other.signingKeyBytes,
            other.signatureVerificationKeyBytes,
            other.keyDerivationOptionsJson)

    /**
     * Derive a signing/signature-verification key pair from a seed and a
     * set of key-derivation options specified in JSON format.
     */
    constructor(
        seedString: String,
        keyDerivationOptionsJson: String
    ) : this(constructJNI(seedString, keyDerivationOptionsJson))

    /**
     * Construct by reconstituting its members (including [signatureVerificationKeyBytes])
     */
    internal constructor(
            signingKeyBytes: ByteArray,
            signatureVerificationKeyBytes: ByteArray,
            keyDerivationOptionsJson: String
    ) : this(constructJNI(signingKeyBytes, signatureVerificationKeyBytes, keyDerivationOptionsJson))

    /**
     * Construct by reconstituting its members (excluding [signatureVerificationKeyBytes], which
     * can be re-generated if needed.
     */
    internal constructor(
            signingKeyBytes: ByteArray,
            keyDerivationOptionsJson: String
    ) : this(constructJNI(signingKeyBytes, keyDerivationOptionsJson))

    /**
     * Construct from JSON format
     */
    constructor(
        signingKeyAsJson: String
    ) : this(constructFromJsonJNI(signingKeyAsJson))

    private external fun deleteNativeObjectPtrJNI()
    private external fun getSignatureVerificationKeyJNI() : Long
    private external fun signatureVerificationKeyBytesGetterJNI(): ByteArray
    private external fun signingKeyBytesGetterJNI(): ByteArray
    private external fun keyDerivationOptionsJsonGetterJNI(): String
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
            = true
    ): String

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
    val keyDerivationOptionsJson get() = keyDerivationOptionsJsonGetterJNI()

    override fun equals(other: Any?): Boolean =
            (other is SigningKey) &&
                    keyDerivationOptionsJson == other.keyDerivationOptionsJson &&
                    signingKeyBytes.contentEquals(other.signingKeyBytes) &&
                    signatureVerificationKeyBytes.contentEquals(other.signatureVerificationKeyBytes)

}