package org.dicekeys.crypto.seeded

import android.graphics.Bitmap
import org.dicekeys.crypto.seeded.utilities.QrCodeBitmap
import org.dicekeys.crypto.seeded.utilities.qrCodeNativeSizeInQrCodeSquarePixels

/**
 * A [SealingKey] is a pubic used to _seal_ messages, in combination with a
 * private [UnsealingKey] which can _unseal_ them.
 * The key pair of this [SealingKey] and the matching [UnsealingKey] are generated
 * from a seed and a set of key-derivation specified options in JSON format
 * [Recipe JSON Format](https://dicekeys.github.io/seeded-crypto/recipe_format.html).
 *
 * To derive a public key from a seed, first derive the corresponding
 * [UnsealingKey] and then call [UnsealingKey.getSealingkey].
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
 *
 * This class wraps the native c++ PublicKey class from the
 * DiceKeys [Seeded Cryptography Library](https://dicekeys.github.io/seeded-crypto/).

 */
class SealingKey internal constructor(
  internal val nativeObjectPtr: Long
): BinarySerializable,JsonSerializable {
    companion object {
        init {
            ensureJniLoaded()
        }

        @JvmStatic private external fun fromJsonJNI(_publicKeyAsJson: String) : Long

        /**
         * Construct a [SealingKey] from a JSON format string,
         * replicating the [SealingKey] on which [toJson]
         * was called to generate [publicKeyAsJson]
         */
        @JvmStatic fun fromJson(
                publicKeyAsJson: String
        ): SealingKey =
            SealingKey(fromJsonJNI(publicKeyAsJson)
        )

        @JvmStatic private external fun constructJNI(
                keyBytes: ByteArray,
                recipe: String = ""
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
        ) : SealingKey = SealingKey(fromSerializedBinaryFormJNI(asSerializedBinaryForm))

    }

    /**
     * Convert this object to serialized binary form so that this object
     * can be replicated/reconstituted via a call to [fromSerializedBinaryForm]
     */
    external override fun toSerializedBinaryForm(): ByteArray

    /**
     * This constructor ensures copying does not copy the underlying pointer, which could
     * lead to a use-after-free vulnerability or an exception on the second deletion.
     */
    constructor(
        other: SealingKey
    ) : this(other.keyBytes, other.recipe)

    /**
     * Construct by specifying the value of each member
     */
    internal constructor(
            keyBytes: ByteArray,
            recipe: String
    ) : this ( constructJNI(
            keyBytes,
            recipe
    ) )

    protected fun finalize() {
        deleteNativeObjectPtrJNI()
    }
    private external fun deleteNativeObjectPtrJNI()
    private external fun keyBytesGetterJNI(): ByteArray
    private external fun recipeGetterJNI(): String

    /**
     * Serialize the object to JSON format so that it can later be
     * reconstituted via a call to [fromJson].
     */
    external override fun toJson(): String

    /**
     * The binary representation of the public key.
     *
     * (You should not need to access this directly unless you are
     * need to extend the functionality of this library by operating
     * on keys directly.)
     */
    val keyBytes get() = keyBytesGetterJNI()

    /**
     * The key-derivation options used to derive the [SealingKey] and its corresponding
     * [UnsealingKey]
     */
    val recipe get() = recipeGetterJNI()

    /**
     * Seal a plaintext message to create a ciphertext which can only be unsealed
     * using the corresponding [UnsealingKey]. The [message] string will be converted
     * to UTF8 binary format before it is sealed.
     *
     * If a [unsealingInstructions] string is passed,
     * the exact same string must also be passed as [unsealingInstructions]
     * to [UnsealingKey.unseal] the message with the corresponding [UnsealingKey].
     * This allows the sealer to specify a public-set of instructions that the party
     * unsealing must be aware of before the message can be unsealed.
     */
    external fun sealJNI(
            message: ByteArray,
            unsealingInstructions: String = ""
    ): Long

    fun seal(
        message: ByteArray,
        unsealingInstructions: String = ""
    ) : PackagedSealedMessage = PackagedSealedMessage(
        sealJNI(message, unsealingInstructions)
    )

    /**
     * Seal a plaintext message to create a ciphertext which can only be unsealed
     * using the corresponding [UnsealingKey]. The [message] string will be converted
     * to UTF8 binary format before it is sealed.
     *
     * If a [unsealingInstructions] string is passed,
     * the exact same string must also be passed as [unsealingInstructions]
     * to [UnsealingKey.unseal] the message with the corresponding [UnsealingKey].
     * This allows the sealer to specify a public-set of instructions that the party
     * unsealing must be aware of before the message can be unsealed.
     */
    fun seal(
            message: String,
            unsealingInstructions: String = ""
    ): PackagedSealedMessage = seal( message.toByteArray(), unsealingInstructions )

    override fun equals(other: Any?): Boolean =
            (other is SealingKey) &&
            recipe == other.recipe &&
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
