package org.dicekeys.keys


class SymmetricKey(
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
            symmetricKeyPtr: Long
    ): Long

    private external fun sealJNI(
            symmetricKeyPtr: Long,
            plaintext: ByteArray,
            postDecryptionInstructionsJson: String
    ): ByteArray

    private external fun unsealJNI(
            symmetricKeyPtr: Long,
            ciphertext: ByteArray,
            postDecryptionInstructionsJson: String
    ): ByteArray

    var disposed: Boolean = false
    private val symmetricKeyPtr: Long = constructJNI(
            keySqrInHumanReadableFormWithOrientations,
            keyDerivationOptionsJson,
            clientsApplicationId,
            false // FIXME
    )

    fun seal(
            plaintext: ByteArray,
            postDecryptionInstructionsJson: String? = ""
    ): ByteArray {
        throwIfDisposed()
        return sealJNI(symmetricKeyPtr, plaintext, postDecryptionInstructionsJson ?: "")
    }


    fun unseal(
        ciphertext: ByteArray,
        postDecryptionInstructionsJson: String? = ""
    ): ByteArray {
        throwIfDisposed()
        return unsealJNI(symmetricKeyPtr, ciphertext, postDecryptionInstructionsJson ?: "")
    }

    private fun throwIfDisposed() {
        if (disposed) {
            throw java.lang.IllegalAccessException("Attempt to use a key after its disposal")
        }
    }

    fun erase() {
        // Immediately dispose of the private key
        if (symmetricKeyPtr != 0L && !disposed) {
            destroyJNI(symmetricKeyPtr)
            disposed = true
        }
    }

    protected fun finalize() {
        // Ensure private keys are destroyed when this object is no longer needed
        erase()
    }


}
