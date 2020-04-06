package org.dicekeys.crypto.seeded

class SigningKey internal constructor(internal val nativeObjectPtr: Long) {
    companion object {
        init {
            ensureJniLoaded()
        }
        @JvmStatic external fun constructJNI(
                signingKeyBytes: ByteArray,
                keyDerivationOptionsJson: String
        ) : Long

        @JvmStatic external fun constructJNI(
                signingKeyBytes: ByteArray,
                signatureVerificationKeyBytes: ByteArray,
                keyDerivationOptionsJson: String
        ) : Long

        @JvmStatic external fun constructJNI(
                seedString: String,
                keyDerivationOptionsJson: String
        ) : Long

        @JvmStatic external fun constructFromJsonJNI(signingKeyAsJson: String) : Long
    }

    constructor(
        seedString: String,
        keyDerivationOptionsJson: String
    ) : this(constructJNI(seedString, keyDerivationOptionsJson))

    constructor(
            signingKeyBytes: ByteArray,
            signatureVerificationKeyBytes: ByteArray,
            keyDerivationOptionsJson: String
    ) : this(constructJNI(signingKeyBytes, signatureVerificationKeyBytes, keyDerivationOptionsJson))

    constructor(
            signingKeyBytes: ByteArray,
            keyDerivationOptionsJson: String
    ) : this(constructJNI(signingKeyBytes, keyDerivationOptionsJson))

    constructor(
        signingKeyAsJson: String
    ) : this(constructFromJsonJNI(signingKeyAsJson))

    private external fun deleteNativeObjectPtrJNI()
    private external fun getSignatureVerificationKeyJNI() : Long
    private external fun signatureVerificationKeyBytesGetterJNI(): ByteArray
    private external fun signingKeyBytesGetterJNI(): ByteArray
    private external fun keyDerivationOptionsJsonGetterJNI(): String
    external fun generateSignature(message: ByteArray): ByteArray
    external fun toJson(
        minimizeSizeByRemovingTheSignatureVerificationKeyBytesWhichCanBeRegeneratedLater: Boolean
            = true
    ): String

    fun generateSignature(
        message: String
    ): ByteArray = generateSignature(message.toByteArray())

    fun getSignatureVerificationKey(): SignatureVerificationKey =
        SignatureVerificationKey(getSignatureVerificationKeyJNI())
    val signingKeyBytes get() = signingKeyBytesGetterJNI()
    val signatureVerificationKeyBytes get() = signatureVerificationKeyBytesGetterJNI()
    val keyDerivationOptionsJson get() = keyDerivationOptionsJsonGetterJNI()

    override fun equals(other: Any?): Boolean =
            (other is PrivateKey) &&
                    keyDerivationOptionsJson == other.keyDerivationOptionsJson &&
                    signingKeyBytes.contentEquals(other.privateKeyBytes) &&
                    signatureVerificationKeyBytes.contentEquals(other.publicKeyBytes)

}