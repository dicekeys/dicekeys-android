package org.dicekeys.api

import kotlinx.coroutines.*
import org.dicekeys.crypto.seeded.*

abstract class Api: AsyncApi, SuspendApi, CallbackApi {
  protected abstract fun call(
    command: String,
    marshallParameters: ApiMarshaller.ParameterMarshaller.() -> Unit
  ): Unit

  private val apiMarshallers = ApiMarshallers(::call)

  /**
   * Activities and Fragments that use this API should implement onActivityResult and
   * and call handleOnActivityResult with the data/intent (third parameter) received.
   * Doing so allows this class to process results returned to the activity/fragment
   * and then call the appropriate callback functions when an API call has either
   * succeeded or failed.
   */
  fun handleResult(result: ApiMarshaller.ParameterUnmarshaller) : Boolean =
    apiMarshallers.handleResponse(
      result.unmarshallString(ApiStrings::requestId.name),
      result
    )



  /**
   * Deferrable API
   */


  /**
   * Derive a pseudo-random cryptographic [Secret] from the user's DiceKey and
   * the key-derivation options passed as [derivationOptionsJson]
   * in [Key-Derivation Options JSON Format](hhttps://dicekeys.github.io/seeded-crypto/derivation_options_format.html).
   */
  override fun getSecretAsync(
    derivationOptionsJson: String
  ): Deferred<Secret> =
    getSecretMarshaller.callAsync {
      marshallParameter(ApiStrings.Inputs.getSecret::derivationOptionsJson.name, derivationOptionsJson)
    }

  private val getSecretMarshaller =
    apiMarshallers.add<Secret>(SuspendApi::getSecret.name) {
      Secret.fromJson(unmarshallString(ApiStrings.Outputs.getSecret::secret.name))
    }


  /**
   * Get a [UnsealingKey] derived from the user's DiceKey (the seed) and the key-derivation options
   * specified via [derivationOptionsJson],
   * in [Key-Derivation Options JSON Format](hhttps://dicekeys.github.io/seeded-crypto/derivation_options_format.html),
   * which must specify
   *  `"clientMayRetrieveKey": true`.
   */
  override fun getUnsealingKeyAsync(
    derivationOptionsJson: String
  ): Deferred<UnsealingKey> =
    getUnsealingKeyMarshaller.callAsync {
      marshallParameter(ApiStrings.Inputs.getUnsealingKey::derivationOptionsJson.name, derivationOptionsJson)
    }

  private val getUnsealingKeyMarshaller =
    apiMarshallers.add<UnsealingKey>(SuspendApi::getUnsealingKey.name) {
      UnsealingKey.fromJson(unmarshallString(ApiStrings.Outputs.getUnsealingKey::unsealingKey.name))
    }


  /**
   * Get a [SymmetricKey] derived from the user's DiceKey (the seed) and the key-derivation options
   * specified via [derivationOptionsJson],
   * in [Key-Derivation Options JSON Format](hhttps://dicekeys.github.io/seeded-crypto/derivation_options_format.html),
   * which must specify
   *  `"clientMayRetrieveKey": true`.
   */
  override fun getSymmetricKeyAsync(
    derivationOptionsJson: String
  ): Deferred<SymmetricKey> =
    getSymmetricKeyMarshaller.callAsync {
      marshallParameter(ApiStrings.Inputs.getSymmetricKey::derivationOptionsJson.name, derivationOptionsJson)
    }

  private val getSymmetricKeyMarshaller =
    apiMarshallers.add(SuspendApi::getSymmetricKey.name) {
      SymmetricKey.fromJson(unmarshallString(ApiStrings.Outputs.getSymmetricKey::symmetricKey.name))
    }

  /**
   * Get a [SigningKey] derived from the user's DiceKey (the seed) and the key-derivation options
   * specified via [derivationOptionsJson],
   * in [Key-Derivation Options JSON Format](hhttps://dicekeys.github.io/seeded-crypto/derivation_options_format.html),
   * which must specify
   *  `"clientMayRetrieveKey": true`.
   */
  override fun getSigningKeyAsync(
    derivationOptionsJson: String
  ): Deferred<SigningKey> =
    getSigningKeyMarshaller.callAsync {
      marshallParameter(ApiStrings.Inputs.getSigningKey::derivationOptionsJson.name, derivationOptionsJson)
    }

  private val getSigningKeyMarshaller =
    apiMarshallers.add<SigningKey>(SuspendApi::getSigningKey.name) {
      SigningKey.fromJson(unmarshallString(ApiStrings.Outputs.getSigningKey::signingKey.name))
    }


  /**
   * Get a [SealingKey] derived from the user's DiceKey and the [ApiDerivationOptions] specified
   * in [Key-Derivation Options JSON Format](hhttps://dicekeys.github.io/seeded-crypto/derivation_options_format.html)
   * as [derivationOptionsJson].
   */
  override fun getSealingKeyAsync(
    derivationOptionsJson: String
  ): Deferred<SealingKey> =
    getSealingKeyMarshaller.callAsync {
      marshallParameter(ApiStrings.Inputs.getSealingKey::derivationOptionsJson.name, derivationOptionsJson)
    }

  private val getSealingKeyMarshaller =
    apiMarshallers.add<SealingKey>(SuspendApi::getSealingKey.name) {
      SealingKey.fromJson(unmarshallString(ApiStrings.Outputs.getSealingKey::sealingKey.name))
  }


  /**
   * Unseal (decrypt & authenticate) a message that was previously sealed with a
   * [SealingKey] to construct a [PackagedSealedMessage].
   * The public/private key pair will be re-derived from the user's seed (DiceKey) and the
   * key-derivation options packaged with the message.  It will also ensure that the
   * unsealing_instructions instructions have not changed since the message was packaged.
   *
   * @throws [CryptographicVerificationFailureException]
   */
  override fun unsealWithUnsealingKeyAsync(
    packagedSealedMessage: PackagedSealedMessage
  ): Deferred<ByteArray> =
    unsealWithUnsealingKeyMarshaller.callAsync {
      marshallParameter(ApiStrings.Inputs.unsealWithUnsealingKey::packagedSealedMessage.name, packagedSealedMessage)
    }

  private val unsealWithUnsealingKeyMarshaller =
    apiMarshallers.add<ByteArray>(SuspendApi::unsealWithUnsealingKey.name) {
      unmarshallByteArray(ApiStrings.Outputs.unsealWithUnsealingKey::plaintext.name)
   }


  /**
   * Seal (encrypt with a message-authentication code) a message ([plaintext]) with a
   * symmetric key derived from the user's DiceKey, the
   * [derivationOptionsJson]
   * in [Key-Derivation Options JSON Format](hhttps://dicekeys.github.io/seeded-crypto/derivation_options_format.html),
   * and [UnsealingInstructions] specified via a JSON string as
   * [unsealingInstructions] in the
   * in [Post-Decryption Instructions JSON Format](https://dicekeys.github.io/seeded-crypto/unsealing_instructions_format.html).
   */
  override fun sealWithSymmetricKeyAsync(
    derivationOptionsJson: String,
    plaintext: ByteArray,
    unsealingInstructions: String
  ): Deferred<PackagedSealedMessage> =
    sealWithSymmetricKeyMarshaller.callAsync {
      marshallParameter(ApiStrings.Inputs.sealWithSymmetricKey::derivationOptionsJson.name, derivationOptionsJson)
      marshallParameter(ApiStrings.Inputs.sealWithSymmetricKey::plaintext.name, plaintext)
      marshallParameter(ApiStrings.Inputs.sealWithSymmetricKey::unsealingInstructions.name, unsealingInstructions)
    }

  private val sealWithSymmetricKeyMarshaller =
    apiMarshallers.add<PackagedSealedMessage>(SuspendApi::sealWithSymmetricKey.name) {
      PackagedSealedMessage.fromJson(unmarshallString(ApiStrings.Outputs.sealWithSymmetricKey::packagedSealedMessage.name))
    }


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
  override fun unsealWithSymmetricKeyAsync(
    packagedSealedMessage: PackagedSealedMessage
  ): Deferred<ByteArray> =
    unsealWithSymmetricKeyMarshaller.callAsync {
      marshallParameter(ApiStrings.Inputs.unsealWithSymmetricKey::packagedSealedMessage.name, packagedSealedMessage)
    }

  private val unsealWithSymmetricKeyMarshaller =
    apiMarshallers.add<ByteArray>(SuspendApi::unsealWithSymmetricKey.name) {
      unmarshallByteArray(ApiStrings.Outputs.unsealWithSymmetricKey::plaintext.name)
    }


  /**
   * Get a public [SignatureVerificationKey] derived from the user's DiceKey and the
   * [ApiDerivationOptions] specified in JSON format via [derivationOptionsJson]
   */
  override fun getSignatureVerificationKeyAsync(
    derivationOptionsJson: String
  ): Deferred<SignatureVerificationKey> =
    getSignatureVerificationKeyMarshaller.callAsync {
      marshallParameter(ApiStrings.Inputs.getSignatureVerificationKey::derivationOptionsJson.name, derivationOptionsJson)
    }

  private val getSignatureVerificationKeyMarshaller =
    apiMarshallers.add<SignatureVerificationKey>(SuspendApi::getSignatureVerificationKey.name) {
      SignatureVerificationKey.fromJson(unmarshallString(ApiStrings.Outputs.getSignatureVerificationKey::signatureVerificationKey.name))
    }


  /**
   * Sign a [message] using a public/private signing key pair derived
   * from the user's DiceKey and the [ApiDerivationOptions] specified in JSON format via
   * [derivationOptionsJson].
   */
  override fun generateSignatureAsync(
    derivationOptionsJson: String,
    message: ByteArray
  ): Deferred<GenerateSignatureResult> =
    generateSignatureMarshaller.callAsync {
      marshallParameter(ApiStrings.Inputs.generateSignature::derivationOptionsJson.name, derivationOptionsJson)
      marshallParameter(ApiStrings.Inputs.generateSignature::message.name, message)
    }

  private val generateSignatureMarshaller =
    apiMarshallers.add<GenerateSignatureResult>(SuspendApi::generateSignature.name) {
      object : GenerateSignatureResult {
        override val signature = unmarshallByteArray(ApiStrings.Outputs.generateSignature::signature.name)
        override val signatureVerificationKey =
          SignatureVerificationKey.fromJson(unmarshallString(ApiStrings.Outputs.generateSignature::signatureVerificationKey.name))
      }
   }



  /**
   * Suspendable API
   */

  override suspend fun generateSignature(
    derivationOptionsJson: String,
    message: ByteArray
  ): GenerateSignatureResult = generateSignatureAsync(derivationOptionsJson, message).await()

  override suspend fun getSealingKey(
    derivationOptionsJson: String
  ): SealingKey = getSealingKeyAsync(derivationOptionsJson).await()

  override suspend fun getSecret(
    derivationOptionsJson: String
  ): Secret = getSecretAsync(derivationOptionsJson).await()

  override suspend fun getSignatureVerificationKey(
    derivationOptionsJson: String
  ): SignatureVerificationKey = getSignatureVerificationKeyAsync(derivationOptionsJson).await()

  override suspend fun getSigningKey(
    derivationOptionsJson: String
  ): SigningKey = getSigningKeyAsync(derivationOptionsJson).await()

  override suspend fun getSymmetricKey(
    derivationOptionsJson: String
  ): SymmetricKey = getSymmetricKeyAsync(derivationOptionsJson).await()

  override suspend fun getUnsealingKey(
    derivationOptionsJson: String
  ): UnsealingKey = getUnsealingKeyAsync(derivationOptionsJson).await()

  override suspend fun sealWithSymmetricKey(
    derivationOptionsJson: String,
    plaintext: ByteArray,
    unsealingInstructions: String
  ): PackagedSealedMessage = sealWithSymmetricKeyAsync(
    derivationOptionsJson, plaintext, unsealingInstructions
  ).await()

  override suspend fun unsealWithSymmetricKey(
    packagedSealedMessage: PackagedSealedMessage
  ): ByteArray = unsealWithSymmetricKeyAsync(packagedSealedMessage).await()

  override suspend fun unsealWithUnsealingKey(
    packagedSealedMessage: PackagedSealedMessage
  ): ByteArray = unsealWithUnsealingKeyAsync(packagedSealedMessage).await()

  /**
   * Callback Api
   */
  private fun <T>toCallback(callback: CallbackApi.Callback<T>?, deferred: Deferred<T>) {
    CoroutineScope(Dispatchers.Default).launch {
      try {
        callback?.onComplete(deferred.await())
      } catch (e: Throwable) {
        callback?.onException(e)
      }
    }
  }

  override fun generateSignature(
    derivationOptionsJson: String,
    message: ByteArray,
    callback: CallbackApi.Callback<GenerateSignatureResult>?
  ) = toCallback(callback, generateSignatureAsync(
    derivationOptionsJson, message
  ))

  override fun getSealingKey(
    derivationOptionsJson: String,
    callback: CallbackApi.Callback<SealingKey>?
  ) = toCallback(callback, getSealingKeyAsync(derivationOptionsJson))

  override fun getSecret(
    derivationOptionsJson: String,
    callback: CallbackApi.Callback<Secret>?
  ) = toCallback(callback, getSecretAsync(derivationOptionsJson))

  override fun getSignatureVerificationKey(
    derivationOptionsJson: String,
    callback: CallbackApi.Callback<SignatureVerificationKey>?
  ) = toCallback(callback, getSignatureVerificationKeyAsync(derivationOptionsJson))

  override fun getSigningKey(
    derivationOptionsJson: String,
    callback: CallbackApi.Callback<SigningKey>?
  ) = toCallback(callback, getSigningKeyAsync(derivationOptionsJson))

  override fun getSymmetricKey(
    derivationOptionsJson: String,
    callback: CallbackApi.Callback<SymmetricKey>?
  ) = toCallback(callback, getSymmetricKeyAsync(derivationOptionsJson))

  override fun getUnsealingKey(
    derivationOptionsJson: String,
    callback: CallbackApi.Callback<UnsealingKey>?
  ) = toCallback(callback, getUnsealingKeyAsync(derivationOptionsJson))

  override fun sealWithSymmetricKey(
    derivationOptionsJson: String,
    plaintext: ByteArray,
    unsealingInstructions: String,
    callback: CallbackApi.Callback<PackagedSealedMessage>
  ) = toCallback(callback, sealWithSymmetricKeyAsync(
    derivationOptionsJson, plaintext, unsealingInstructions
  ))

  override fun unsealWithSymmetricKey(
    packagedSealedMessage: PackagedSealedMessage,
    callback: CallbackApi.Callback<ByteArray>?
  )= toCallback(callback, unsealWithSymmetricKeyAsync(packagedSealedMessage))

  override fun unsealWithUnsealingKey(
    packagedSealedMessage: PackagedSealedMessage,
    callback: CallbackApi.Callback<ByteArray>?
  ) = toCallback(callback, unsealWithUnsealingKeyAsync(packagedSealedMessage))


}