package org.dicekeys.keys

class SigningKey(
        private val keySqrInHumanReadableFormWithOrientations: String,
        val jsonKeyDerivationOptions: String,
        public val clientsApplicationId: String
) {
    private external fun constructJNI(
            keySqrInHumanReadableFormWithOrientations: String,
            jsonKeyDerivationOptions: String,
            clientsApplicationId: String,
            validateClientId: Boolean
    ): Long

    private external fun destroyJNI(
        signingKeyPtr: Long
    ): Void

    private external fun getSignatureVerificationKeyBytesJNI(
        signingKeyPtr: Long
    ): ByteArray

    private external fun generateSignatureJNI(
        signingKeyPtr: Long,
        message: ByteArray
    ): ByteArray


    var disposed: Boolean = false
    private fun throwIfDisposed() {
        if (disposed) {
            throw IllegalAccessException("Attempt to use a key after its disposal")
        }
    }

    private val signingKeyPtr: Long = constructJNI(
            keySqrInHumanReadableFormWithOrientations,
            jsonKeyDerivationOptions,
            clientsApplicationId,
            false // FIXME
    )

    fun getSignatureVerificationKey(): SignatureVerificationKey {
        throwIfDisposed()
        return SignatureVerificationKey(
                getSignatureVerificationKeyBytesJNI(signingKeyPtr),
                jsonKeyDerivationOptions
        )
    }

    fun generateSignature(
        message: ByteArray
    ): ByteArray {
        throwIfDisposed()
        return generateSignatureJNI(signingKeyPtr, message);
    }

    fun erase() {
        // Immediately dispose of the private key
        if (signingKeyPtr != 0L && !disposed) {
            destroyJNI(signingKeyPtr)
            disposed = true
        }
    }

    protected fun finalize() {
        // Ensure private keys are destroyed when this object is no longer needed
        erase()
    }


}
