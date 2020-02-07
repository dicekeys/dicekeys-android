package com.keysqr.keys


class PublicPrivateKeyPair(
        public val keySqrInHumanReadableFormWithOrientations: String,
        public val jsonKeyDerivationOptions: String,
        public val clientsApplicationId: String
) {
    external fun constructJNI(
            keySqrInHumanReadableFormWithOrientations: String,
            jsonKeyDerivationOptions: String,
            clientsApplicationId: String
    ): Long

    external fun destroyJNI(
            publicPrivateKeyPairPtr: Long
    ): Long

    external fun getPublicKeyBytesJNI(
            publicPrivateKeyPairPtr: Long
    ): ByteArray

    external fun  unsealJNI(
        publicPrivateKeyPairPtr: Long,
        ciphertext: ByteArray,
        postDecryptionInstructionJson: String
    ): ByteArray // FIXME -- result should be message

    var disposed: Boolean = false
    private val publicPrivateKeyPairPtr: Long = constructJNI(
            keySqrInHumanReadableFormWithOrientations,
            jsonKeyDerivationOptions,
            clientsApplicationId
    )

    fun getPublicKey(): PublicKey {
        return PublicKey(
                jsonKeyDerivationOptions,
                getPublicKeyBytesJNI(publicPrivateKeyPairPtr)
        )
    }

    fun decrypt() {
        // FIXME
    }

    fun erase() {
        // Immediately dispose of the private key
        if (publicPrivateKeyPairPtr != 0L && !disposed) {
            destroyJNI(publicPrivateKeyPairPtr)
            disposed = true
        }
    }

    protected fun finalize() {
        // Ensure private keys are destroyed when this object is no longer needed
        erase()
    }


}
