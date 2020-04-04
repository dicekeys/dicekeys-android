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

    private external fun sealJNI(
        plaintext: ByteArray,
        postDecryptionInstructionsJson: String
    ): ByteArray

    private external fun unsealJNI(
        ciphertext: ByteArray,
        postDecryptionInstructionsJson: String
    ): ByteArray

}
