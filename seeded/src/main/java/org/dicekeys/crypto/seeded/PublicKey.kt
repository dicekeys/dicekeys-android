package org.dicekeys.crypto.seeded

import android.graphics.Bitmap
import org.dicekeys.crypto.seeded.utilities.QrCodeBitmap
import org.dicekeys.crypto.seeded.utilities.qrCodeNativeSizeInQrCodeSquarePixels
import java.nio.charset.StandardCharsets

/**
 * A wrapper for the native c++ PublicKey class from the DiceKeys seeded cryptography library.
 *
 * A [PublicKey] is used to _seal_ messages, in combination with a
 * [PrivateKey] which can _unseal_ them.
 * The key pair of this [PublicKey] and the matching [PrivateKey] are generated
 * from a seed and a set of key-derivation specified options in JSON format
 * @ref key_derivation_options_format.
 *
 * To derive a public key from a seed, first derive the corresponding
 * [PrivateKey] and then call [PrivateKey.getPublicKey].
 *
 * Sealing a message (_plaintext_) creates a _ciphertext which contains
 * the message but from which observers who do not have the PrivateKey
 * cannot discern the contents of the message.
 * Sealing also provides integrity-protection, which will prevent the
 * message from being unsealed if it is modified.
 * We use the verbs seal and unseal, rather than encrypt and decrypt,
 * because the encrypting alone does not confer that the message includes
 * an integrity (message authentication) code to prove that the ciphertext
 * has not been tampered with.
 *
 * Note that sealing data does not prevent attackers who capture a sealed message
 * (ciphertext) in transit with another validly-sealed message. A SigningKey
 * can be used to sign messages that another party can verify that the
 * message has not been forged or modified since the signer approved it.
 */
class PublicKey internal constructor(internal val nativeObjectPtr: Long) {
    companion object {
        init {
            ensureJniLoaded()
        }

        @JvmStatic internal external fun constructFromJsonJNI(json: String) : Long
        @JvmStatic internal external fun constructJNI(
                keyBytes: ByteArray,
                keyDerivationOptionsJson: String = ""
        ) : Long
    }

    /**
     * This constructor ensures copying does not copy the underlying pointer, which could
     * lead to a use-after-free vulnerability or an exception on the second deletion.
     */
    constructor(
        other: PublicKey
    ) : this(other.keyBytes, other.keyDerivationOptionsJson)

    /**
     * Reconstitute a [PublicKey] from its JSON-serialized format.
     */
    constructor(
            publicKeyInJsonFormat: String
    ) : this ( constructFromJsonJNI(
            publicKeyInJsonFormat
    ) )

    /**
     * Construct by specifying the value of each member
     */
    internal constructor(
            keyBytes: ByteArray,
            keyDerivationOptionsJson: String
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
     * Convert this object into a JSON format, From which a copy of this [PublicKey]
     * can be constructed.
     */
    external fun toJson(): String

    /**
     * The binary representation of the public key.
     *
     * (You should not need to access this directly unless you are
     * need to extend the functionality of this library by operating
     * on keys directly.)
     */
    val keyBytes get() = keyBytesGetterJNI()

    /**
     * The key-derivation options used to derive the [PublicKey] and its corresponding
     * [PrivateKey]
     */
    val keyDerivationOptionsJson get() = keyDerivationOptionsJsonGetterJNI()

    /**
     * Seal a plaintext message to create a ciphertext which can only be unsealed
     * using the corresponding [PrivateKey].
     */
    external fun seal(
            message: ByteArray,
            postDecryptionInstructionsJson: String = ""
    ): ByteArray


    /**
     * Seal a plaintext message to create a ciphertext which can only be unsealed
     * using the corresponding [PrivateKey]. The [message] string will be converted
     * to UTF8 binary format before it is sealed.
     *
     * If a [postDecryptionInstructionsJson] string is passed,
     * the exact same string must also be passed as [postDecryptionInstructionsJson]
     * to [PrivateKey.unseal] the message with the corresponding [PrivateKey].
     * This allows the sealer to specify a public-set of instructions that the party
     * unsealing must be aware of before the message can be unsealed.
     */
    fun seal(
            message: String,
            postDecryptionInstructionsJson: String = ""
    ): ByteArray = seal( message.toByteArray(StandardCharsets.UTF_8), postDecryptionInstructionsJson )

    override fun equals(other: Any?): Boolean =
            (other is PublicKey) &&
            keyDerivationOptionsJson == other.keyDerivationOptionsJson &&
            keyBytes.contentEquals(other.keyBytes)

    /**
     * Get a QR code that encodes this public key in JSON format.
     */
    fun getJsonQrCode(
        maxEdgeLengthInDevicePixels: Int = qrCodeNativeSizeInQrCodeSquarePixels * 2
    ): Bitmap = QrCodeBitmap(
        "https://dicekeys.org/pk/",
        toJson(),
        maxEdgeLengthInDevicePixels
    )

    /**
     * Get a QR code that encodes this public key in JSON format.
     */
    fun getJsonQrCode(
            maxWidth: Int,
            maxHeight: Int
    ): Bitmap = getJsonQrCode(kotlin.math.min(maxWidth, maxHeight))

}
