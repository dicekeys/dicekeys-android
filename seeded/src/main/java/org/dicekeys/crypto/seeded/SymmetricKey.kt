package org.dicekeys.crypto.seeded

class SymmetricKey private constructor(internal val nativeObjectPtr: Long) {

    companion object {
        init {
            ensureJniLoaded()
        }

        @JvmStatic external fun constructJNI(
            keyBytes: ByteArray,
            keyDerivationOptionsJson: String
        ) : Long
        @JvmStatic external fun constructJNI(
            seedString: String,
            keyDerivationOptionsJson: String
        ) : Long
        @JvmStatic external fun constructFromJsonJNI(
            symmetricKeyJson: String
        ) : Long
    }

    private external fun deleteNativeObjectPtrJNI()
    private external fun keyBytesGetterJNI(): ByteArray
    private external fun keyDerivationOptionsJsonGetterJNI(): String
    external fun toJson(): String

    val keyBytes: ByteArray get() = keyBytesGetterJNI()
    val keyDerivationOptionsJson: String get() = keyDerivationOptionsJsonGetterJNI()

    constructor(
        keyBytes: ByteArray,
        keyDerivationOptionsJson: String
    ) : this( constructJNI(keyBytes, keyDerivationOptionsJson) )

    // Create copy constructor to prevent copying of the native pointer, which would lead
    // to a use-after-dereference pointer vulnerability
    constructor(
        other: SymmetricKey
    ) : this(other.keyBytes, other.keyDerivationOptionsJson)

    constructor(
        seedString: String,
        keyDerivationOptionsJson: String
    ) : this(constructJNI(seedString, keyDerivationOptionsJson))

    constructor(
        symmetricKeyJson: String
    ) : this(constructFromJsonJNI(symmetricKeyJson))
    
    protected fun finalize() {
        deleteNativeObjectPtrJNI()
    }

    public external fun seal(
        plaintext: ByteArray,
        postDecryptionInstructionsJson: String = ""
    ): ByteArray

    public external fun unseal(
        ciphertext: ByteArray,
        postDecryptionInstructionsJson: String = ""
    ): ByteArray

    fun seal(
        plaintext: String,
        postDecryptionInstructionsJson: String = ""
    ): ByteArray = seal( plaintext.toByteArray(), postDecryptionInstructionsJson)

}
