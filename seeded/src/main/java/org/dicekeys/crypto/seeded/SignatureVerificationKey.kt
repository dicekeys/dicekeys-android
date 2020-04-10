package org.dicekeys.crypto.seeded

import android.graphics.Bitmap
import org.dicekeys.crypto.seeded.utilities.QrCodeBitmap
import org.dicekeys.crypto.seeded.utilities.qrCodeNativeSizeInQrCodeSquarePixels

/**
 * A wrapper for the native c++ SignatureVerificationKey class from the DiceKeys seeded cryptography library.
 *
 * A [SignatureVerificationKey] is used to verify that messages were
 * signed by its corresponding [SigningKey].
 * [SigningKey]s generate _signatures_, and by verifying a message/signature
 * pair the SignatureVerificationKey can confirm that the message was
 * indeed signed using the [SigningKey].
 * The key pair of the [SigningKey] and SignatureVerificationKey is generated
 * from a seed and a set of key-derivation specified options in
 *  @ref key_derivation_options_format.
 * 
 * To derive a [SignatureVerificationKey] from a seed, first derive the
 * corresponding SigningKey and then call [SigningKey.getSignatureVerificationKey].
 */
 class SignatureVerificationKey internal constructor(internal val nativeObjectPtr: Long) {
    companion object {
        init {
            ensureJniLoaded()
        }

        @JvmStatic private external fun fromJsonJNI(json: String) : Long

        /**
         * Construct a [SignatureVerificationKey] from a JSON format string,
         * replicating the [SignatureVerificationKey] on which [toJson]
         * was called to generate [signatureVerificationKeyAsJson]
         */
        @JvmStatic fun fromJson(
            signatureVerificationKeyAsJson: String
        ): SignatureVerificationKey =
            SignatureVerificationKey(fromJsonJNI(signatureVerificationKeyAsJson))

        @JvmStatic private external fun constructJNI(
                keyBytes: ByteArray,
                keyDerivationOptionsJson: String
        ) : Long

       @JvmStatic private external fun fromSerializedBinaryFormJNI(
               asSerializedBinaryForm: ByteArray
       ) : Long

       /**
        * Reconstruct this object from serialized binary form using a
        * ByteArray that was constructed via [toSerializedBinaryForm].
        */
       @JvmStatic fun fromSerializedBinaryForm(
               asSerializedBinaryForm: ByteArray
       ) : SignatureVerificationKey = SignatureVerificationKey(fromSerializedBinaryFormJNI(asSerializedBinaryForm))

    }

   /**
    * Convert this object to serialized binary form so that this object
    * can be replicated/reconstituted via a call to [fromSerializedBinaryForm]
    */
   external fun toSerializedBinaryForm(): ByteArray


   /**
     * This constructor ensures copying does not copy the underlying pointer, which could
     * lead to a use-after-free vulnerability or an exception on the second deletion.
     */
    constructor(
            other: PublicKey
    ) : this(other.keyBytes, other.keyDerivationOptionsJson)


    /**
     * Construct by passing the classes' members
     */
    constructor(
            keyBytes: ByteArray,
            keyDerivationOptionsJson: String = ""
    ) : this ( constructJNI(
            keyBytes,
            keyDerivationOptionsJson
    ) )

    protected fun finalize() {
        deleteNativeObjectPtrJNI()
    }
    private external fun deleteNativeObjectPtrJNI()
    private external fun keyBytesGetterJNI(): ByteArray
    private external fun keyDerivationOptionsJsonGetterJNI(): String

    /**
     * Serialize the object to JSON format
     */
    external fun toJson(): String

    /**
     * The binary representation of the signature-verification key.
     *
     * (You should not need to access this directly unless you are
     * need to extend the functionality of this library by operating
     * on keys directly.)
     */
    val keyBytes get() = keyBytesGetterJNI()

    /**
     * The key-derivation options used to derive this [SigningKey] and its corresponding
     * [SignatureVerificationKey]
     */
    val keyDerivationOptionsJson get() = keyDerivationOptionsJsonGetterJNI()

    override fun equals(other: Any?): Boolean =
        (other is SignatureVerificationKey) &&
        keyDerivationOptionsJson == other.keyDerivationOptionsJson &&
        keyBytes.contentEquals(other.keyBytes)

    /**
     * Verify that [message] was signed by this key's corresponding [SigningKey] to generate
     * [signature].
     */
    external fun verifySignature(
        message: ByteArray,
        signature: ByteArray
    ): Boolean

    /**
     * Verify that [message] was signed by this key's corresponding [SigningKey] to generate
     * [signature].
     */
    fun verifySignature(
        message: String,
        signature: ByteArray
    ) : Boolean = verifySignature( message.toByteArray(), signature)

    /**
     * Get a QR code that encodes this signature-verification key in JSON format.
     */
    fun getJsonQrCode(
            maxEdgeLengthInDevicePixels: Int = qrCodeNativeSizeInQrCodeSquarePixels * 2
    ): Bitmap = QrCodeBitmap(
            "https://dicekeys.org/svk/",
            toJson(),
            maxEdgeLengthInDevicePixels
    )

    /**
     * Get a QR code that encodes this signature-verification key in JSON format.
     */
    fun getJsonQrCode(
            maxWidth: Int,
            maxHeight: Int
    ): Bitmap = getJsonQrCode(kotlin.math.min(maxWidth, maxHeight))


}
