package org.dicekeys.keys


class PublicPrivateKeyPair(
        public val keySqrInHumanReadableFormWithOrientations: String,
        public val keyDerivationOptionsJson: String,
        public val clientsApplicationId: String
) {
    private external fun constructJNI(
            keySqrInHumanReadableFormWithOrientations: String,
            keyDerivationOptionsJson: String,
            clientsApplicationId: String,
            validateClientId: Boolean
    ): Long

    private external fun destroyJNI(
            publicPrivateKeyPairPtr: Long
    ): Void

    private external fun getPublicKeyBytesJNI(
            publicPrivateKeyPairPtr: Long
    ): ByteArray

    private external fun unsealJNI(
        publicPrivateKeyPairPtr: Long,
        ciphertext: ByteArray,
        postDecryptionInstructionsJson: String
    ): ByteArray


    var disposed: Boolean = false
    private fun throwIfDisposed() {
        if (disposed) {
            throw java.lang.IllegalAccessException("Attempt to use a key after its disposal")
        }
    }

    private val publicPrivateKeyPairPtr: Long = constructJNI(
            keySqrInHumanReadableFormWithOrientations,
            keyDerivationOptionsJson,
            clientsApplicationId,
            false // FIXME
    )

    fun getPublicKey(): PublicKey {
        throwIfDisposed()
        return PublicKey(
                getPublicKeyBytesJNI(publicPrivateKeyPairPtr),
                keyDerivationOptionsJson
        )
    }

    fun unseal(
            ciphertext: ByteArray,
            postDecryptionInstructionsJson: String? = ""
    ): ByteArray {
        throwIfDisposed()
        return unsealJNI(publicPrivateKeyPairPtr, ciphertext, postDecryptionInstructionsJson ?: "")
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
