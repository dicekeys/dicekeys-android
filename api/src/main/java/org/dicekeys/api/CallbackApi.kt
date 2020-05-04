package org.dicekeys.api

import android.content.Intent
import org.dicekeys.crypto.seeded.CryptographicVerificationFailureException
import org.dicekeys.crypto.seeded.SealingKey
import org.dicekeys.crypto.seeded.UnsealingKey
import org.dicekeys.crypto.seeded.Secret
import org.dicekeys.crypto.seeded.SignatureVerificationKey
import org.dicekeys.crypto.seeded.SigningKey
import org.dicekeys.crypto.seeded.SymmetricKey
import org.dicekeys.crypto.seeded.PackagedSealedMessage

interface CallbackApi {
  interface Callback<T> {
    fun onComplete(result: T)
    fun onException(e: Throwable?)
  }

  fun getSecret(
    derivationOptionsJson: String,
    callback: Callback<Secret>? = null
  )

  /**
   * Get a [UnsealingKey] derived from the user's DiceKey (the seed) and the key-derivation options
   * specified via [derivationOptionsJson],
   * in [Key-Derivation Options JSON Format](hhttps://dicekeys.github.io/seeded-crypto/derivation_options_format.html),
   * which must specify
   *  `"clientMayRetrieveKey": true`.
   */
  fun getUnsealingKey(
    derivationOptionsJson: String,
    callback: Callback<UnsealingKey>? = null
  )


  /**
   * Get a [SymmetricKey] derived from the user's DiceKey (the seed) and the key-derivation options
   * specified via [derivationOptionsJson],
   * in [Key-Derivation Options JSON Format](hhttps://dicekeys.github.io/seeded-crypto/derivation_options_format.html),
   * which must specify
   *  `"clientMayRetrieveKey": true`.
   */
  fun getSymmetricKey(
    derivationOptionsJson: String,
    callback: Callback<SymmetricKey>? = null
  )

  /**
   * Get a [SigningKey] derived from the user's DiceKey (the seed) and the key-derivation options
   * specified via [derivationOptionsJson],
   * in [Key-Derivation Options JSON Format](hhttps://dicekeys.github.io/seeded-crypto/derivation_options_format.html),
   * which must specify
   *  `"clientMayRetrieveKey": true`.
   */
  fun getSigningKey(
    derivationOptionsJson: String,
    callback: Callback<SigningKey>? = null
  )


  /**
   * Get a [SealingKey] derived from the user's DiceKey and the [ApiDerivationOptions] specified
   * in [Key-Derivation Options JSON Format](hhttps://dicekeys.github.io/seeded-crypto/derivation_options_format.html)
   * as [derivationOptionsJson].
   */
  fun getSealingKey(
    derivationOptionsJson: String,
    callback: Callback<SealingKey>? = null
  )

  /**
   * Unseal (decrypt & authenticate) a message that was previously sealed with a
   * [SealingKey] to construct a [PackagedSealedMessage].
   * The public/private key pair will be re-derived from the user's seed (DiceKey) and the
   * key-derivation options packaged with the message.  It will also ensure that the
   * unsealing_instructions instructions have not changed since the message was packaged.
   *
   * @throws [CryptographicVerificationFailureException]
   */
  fun unsealWithUnsealingKey(
    packagedSealedMessage: PackagedSealedMessage,
    callback: Callback<ByteArray>? = null
  )

  /**
   * Seal (encrypt with a message-authentication code) a message ([plaintext]) with a
   * symmetric key derived from the user's DiceKey, the
   * [derivationOptionsJson]
   * in [Key-Derivation Options JSON Format](hhttps://dicekeys.github.io/seeded-crypto/derivation_options_format.html),
   * and [UnsealingInstructions] specified via a JSON string as
   * [unsealingInstructions] in the
   * in [Post-Decryption Instructions JSON Format](https://dicekeys.github.io/seeded-crypto/unsealing_instructions_format.html).
   */
  fun sealWithSymmetricKey(
    derivationOptionsJson: String,
    plaintext: ByteArray,
    unsealingInstructions: String = "",
    callback: Callback<PackagedSealedMessage>
  )


  /**
   * Unseal (decrypt & authenticate) a [packagedSealedMessage] that was previously sealed with a
   * symmetric key derived from the user's DiceKey, the
   * [ApiDerivationOptions] specified in JSON format via [PackagedSealedMessage.derivationOptionsJson],
   * and any [UnsealingInstructions] optionally specified by [PackagedSealedMessage.unsealingInstructions]
   * in [Post-Decryption Instructions JSON Format](https://dicekeys.github.io/seeded-crypto/unsealing_instructions_format.html).
   *
   * If any of those strings change, the wrong key will be derive and the message will
   * not be successfully unsealed, yielding a [org.dicekeys.crypto.seeded.CryptographicVerificationFailureException] exception.
   */
  fun unsealWithSymmetricKey(
    packagedSealedMessage: PackagedSealedMessage,
    callback: Callback<ByteArray>? = null
  )


  /**
   * Get a public [SignatureVerificationKey] derived from the user's DiceKey and the
   * [ApiDerivationOptions] specified in JSON format via [derivationOptionsJson]
   */
  fun getSignatureVerificationKey(
    derivationOptionsJson: String,
    callback: Callback<SignatureVerificationKey>? = null
  )

  /**
   * Sign a [message] using a public/private signing key pair derived
   * from the user's DiceKey and the [ApiDerivationOptions] specified in JSON format via
   * [derivationOptionsJson].
   */
  fun generateSignature(
    derivationOptionsJson: String,
    message: ByteArray,
    callback: Callback<GenerateSignatureResult>? = null
  )

}