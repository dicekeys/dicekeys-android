package org.dicekeys.keys

import org.dicekeys.api.SignatureVerificationKey

class SigningKey(
        private val keySqrInHumanReadableFormWithOrientations: String,
        val keyDerivationOptionsJson: String,
        val clientsApplicationId: String
) {
    private external fun constructJNI(
            keySqrInHumanReadableFormWithOrientations: String,
            keyDerivationOptionsJson: String,
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


    private var disposed: Boolean = false
    private fun throwIfDisposed() {
        if (disposed) {
            throw IllegalAccessException("Attempt to use a key after its disposal")
        }
    }

    private val signingKeyPtr: Long = constructJNI(
            keySqrInHumanReadableFormWithOrientations,
            keyDerivationOptionsJson,
            clientsApplicationId,
            false // FIXME
    )

    fun getSignatureVerificationKey(): SignatureVerificationKey {
        throwIfDisposed()
        return SignatureVerificationKey(
                getSignatureVerificationKeyBytesJNI(signingKeyPtr),
                keyDerivationOptionsJson
        )
    }

    fun generateSignature(
        message: ByteArray
    ): ByteArray {
        throwIfDisposed()
        return generateSignatureJNI(signingKeyPtr, message)
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
