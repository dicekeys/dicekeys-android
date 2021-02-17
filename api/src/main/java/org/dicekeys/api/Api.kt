@file:Suppress("RemoveExplicitTypeArguments")

package org.dicekeys.api

import kotlinx.coroutines.*
import org.dicekeys.crypto.seeded.*
import org.dicekeys.api.ApiStrings.Inputs
import org.dicekeys.api.ApiStrings.Outputs


abstract class Api(
  val protocolMayRequireHandshakes: Boolean
): AsyncApi, SuspendApi, CallbackApi {

  protected abstract fun call(
    command: String,
    marshallParameters: ApiMarshaller.ParameterMarshaller.() -> Unit
  )

  private val apiMarshallers = ApiMarshallers(::call)

  private suspend fun authTokenIfRequired(
    authenticationRequirements: AuthenticationRequirements
  ): String? =
    if (protocolMayRequireHandshakes && authenticationRequirements.requireAuthenticationHandshake == true) {
      getAuthToken()
    } else null


  /**
     * Activities and Fragments that use this API should implement onActivityResult and
     * and call handleOnActivityResult with the data/intent (third parameter) received.
     * Doing so allows this class to process results returned to the activity/fragment
     * and then call the appropriate callback functions when an API call has either
     * succeeded or failed.
     */
    fun handleResult(result: ApiMarshaller.ParameterUnmarshaller) : Boolean =
      apiMarshallers.handleResponse(
        result.unmarshallString(ApiStrings.MetaInputs.requestId),
        result
      )

    /**
     * Deferrable API (the one we actually implement, rather than wrap)
     */
    var authToken: String? = null
    private suspend fun getAuthToken(forceReload: Boolean = false): String {
      if (forceReload || authToken == null) {
        authToken = getAuthTokenMarshaller.call {}
      }
      return authToken!!
    }

    private val getAuthTokenMarshaller =
      apiMarshallers.add<String>(::getAuthToken.name) {
        unmarshallString(ApiStrings.UrlMetaInputs.authToken)
      }

    /**
     * Sign a [message] using a public/private signing key pair derived
     * from the user's DiceKey and the [ApiDerivationOptions] specified in JSON format via
     * [derivationOptionsJson].
     */
    override suspend fun generateSignature(
      derivationOptionsJson: String,
      message: ByteArray
    ): GenerateSignatureResult =
      generateSignatureMarshaller.call(authTokenIfRequired(ApiDerivationOptions(derivationOptionsJson))) {
        marshallParameter(Inputs.generateSignature::derivationOptionsJson.name, derivationOptionsJson )
        marshallParameter(Inputs.generateSignature::message.name, message)
      }

    private val generateSignatureMarshaller =
      apiMarshallers.add<GenerateSignatureResult>(SuspendApi::generateSignature.name) {
        object : GenerateSignatureResult {
          override val signature = unmarshallByteArray(Outputs.generateSignature::signature.name)
          override val signatureVerificationKey =
            SignatureVerificationKey.fromJson(unmarshallString(Outputs.generateSignature.signatureVerificationKeyJson))
        }
      }


    /**
     * Derive a pseudo-random cryptographic [Secret] from the user's DiceKey and
     * the key-derivation options passed as [derivationOptionsJson]
     * in [Recipe JSON Format](https://dicekeys.github.io/seeded-crypto/recipe_format.html).
     */
    override suspend fun getSecret(
      derivationOptionsJson: String
    ): Secret =
      getSecretMarshaller.call(authTokenIfRequired(ApiDerivationOptions(derivationOptionsJson))) {
        marshallParameter(Inputs.getSecret::derivationOptionsJson.name, derivationOptionsJson)
      }

    private val getSecretMarshaller =
      apiMarshallers.add<Secret>(SuspendApi::getSecret.name) {
        Secret.fromJson(unmarshallString(Outputs.getSecret.secretJson))
      }


    /**
     * Get a [UnsealingKey] derived from the user's DiceKey (the seed) and the key-derivation options
     * specified via [derivationOptionsJson],
     * in [Recipe JSON Format](https://dicekeys.github.io/seeded-crypto/recipe_format.html),
     * which must specify
     *  `"clientMayRetrieveKey": true`.
     */
    override suspend fun getUnsealingKey(
      derivationOptionsJson: String
    ): UnsealingKey =
      getUnsealingKeyMarshaller.call(authTokenIfRequired(ApiDerivationOptions(derivationOptionsJson))) {
        marshallParameter(Inputs.getUnsealingKey::derivationOptionsJson.name, derivationOptionsJson)
      }

    private val getUnsealingKeyMarshaller =
      apiMarshallers.add<UnsealingKey>(SuspendApi::getUnsealingKey.name) {
        UnsealingKey.fromJson(unmarshallString(Outputs.getUnsealingKey.unsealingKeyJson))
      }


    /**
     * Get a [SymmetricKey] derived from the user's DiceKey (the seed) and the key-derivation options
     * specified via [derivationOptionsJson],
     * in [Recipe JSON Format](https://dicekeys.github.io/seeded-crypto/recipe_format.html),
     * which must specify
     *  `"clientMayRetrieveKey": true`.
     */
    override suspend fun getSymmetricKey(
      derivationOptionsJson: String
    ): SymmetricKey =
      getSymmetricKeyMarshaller.call(authTokenIfRequired(ApiDerivationOptions(derivationOptionsJson))) {
        marshallParameter(Inputs.getSymmetricKey::derivationOptionsJson.name, derivationOptionsJson)
      }

    private val getSymmetricKeyMarshaller =
      apiMarshallers.add(SuspendApi::getSymmetricKey.name) {
        SymmetricKey.fromJson(unmarshallString(Outputs.getSymmetricKey.symmetricKeyJson))
      }

    /**
     * Get a [SigningKey] derived from the user's DiceKey (the seed) and the key-derivation options
     * specified via [derivationOptionsJson],
     * in [Recipe JSON Format](https://dicekeys.github.io/seeded-crypto/recipe_format.html),
     * which must specify
     *  `"clientMayRetrieveKey": true`.
     */
    override suspend fun getSigningKey(
      derivationOptionsJson: String
    ): SigningKey =
      getSigningKeyMarshaller.call(authTokenIfRequired(ApiDerivationOptions(derivationOptionsJson))) {
        marshallParameter(Inputs.getSigningKey::derivationOptionsJson.name, derivationOptionsJson)
      }

    private val getSigningKeyMarshaller =
      apiMarshallers.add<SigningKey>(SuspendApi::getSigningKey.name) {
        SigningKey.fromJson(unmarshallString(Outputs.getSigningKey.signingKeyJson))
      }


    /**
     * Get a [SealingKey] derived from the user's DiceKey and the [ApiDerivationOptions] specified
     * in [Recipe JSON Format](https://dicekeys.github.io/seeded-crypto/recipe_format.html)
     * as [derivationOptionsJson].
     */
    override suspend fun getSealingKey(
      derivationOptionsJson: String
    ): SealingKey =
      getSealingKeyMarshaller.call(authTokenIfRequired(ApiDerivationOptions(derivationOptionsJson))) {
        marshallParameter(Inputs.getSealingKey::derivationOptionsJson.name, derivationOptionsJson)
      }

    private val getSealingKeyMarshaller =
      apiMarshallers.add<SealingKey>(SuspendApi::getSealingKey.name) {
        SealingKey.fromJson(unmarshallString(Outputs.getSealingKey.sealingKeyJson))
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
    override suspend fun unsealWithUnsealingKey(
      packagedSealedMessage: PackagedSealedMessage
    ): ByteArray =
      unsealWithUnsealingKeyMarshaller.call(
        authTokenIfRequired(ApiDerivationOptions(packagedSealedMessage.recipe)) ?:
        authTokenIfRequired(UnsealingInstructions(packagedSealedMessage.unsealingInstructions))
      ) {
        marshallParameter(Inputs.unsealWithUnsealingKey.packagedSealedMessageJson, packagedSealedMessage)
      }

    private val unsealWithUnsealingKeyMarshaller =
      apiMarshallers.add<ByteArray>(SuspendApi::unsealWithUnsealingKey.name) {
        unmarshallByteArray(Outputs.unsealWithUnsealingKey::plaintext.name)
      }


    /**
     * Seal (encrypt with a message-authentication code) a message ([plaintext]) with a
     * symmetric key derived from the user's DiceKey, the
     * [derivationOptionsJson]
     * in [Recipe JSON Format](https://dicekeys.github.io/seeded-crypto/recipe_format.html),
     * and [UnsealingInstructions] specified via a JSON string as
     * [unsealingInstructions] in the
     * in [Post-Decryption Instructions JSON Format](https://dicekeys.github.io/seeded-crypto/unsealing_instructions_format.html).
     */
    override suspend fun sealWithSymmetricKey(
      derivationOptionsJson: String,
      plaintext: ByteArray,
      unsealingInstructions: String
    ): PackagedSealedMessage =
      sealWithSymmetricKeyMarshaller.call(authTokenIfRequired(ApiDerivationOptions(derivationOptionsJson))) {
        marshallParameter(Inputs.sealWithSymmetricKey::derivationOptionsJson.name, derivationOptionsJson)
        marshallParameter(Inputs.sealWithSymmetricKey::plaintext.name, plaintext)
        marshallParameter(Inputs.sealWithSymmetricKey::unsealingInstructions.name, unsealingInstructions)
      }

    private val sealWithSymmetricKeyMarshaller =
      apiMarshallers.add<PackagedSealedMessage>(SuspendApi::sealWithSymmetricKey.name) {
        PackagedSealedMessage.fromJson(unmarshallString(Outputs.sealWithSymmetricKey.packagedSealedMessageJson))
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
    override suspend fun unsealWithSymmetricKey(
      packagedSealedMessage: PackagedSealedMessage
    ): ByteArray =
      unsealWithSymmetricKeyMarshaller.call(
        authTokenIfRequired(ApiDerivationOptions(packagedSealedMessage.recipe)) ?:
        authTokenIfRequired(UnsealingInstructions(packagedSealedMessage.unsealingInstructions))
      ) {
        marshallParameter(Inputs.unsealWithSymmetricKey.packagedSealedMessageJson, packagedSealedMessage)
      }

    private val unsealWithSymmetricKeyMarshaller =
      apiMarshallers.add<ByteArray>(SuspendApi::unsealWithSymmetricKey.name) {
        unmarshallByteArray(Outputs.unsealWithSymmetricKey::plaintext.name)
      }


    /**
     * Get a public [SignatureVerificationKey] derived from the user's DiceKey and the
     * [ApiDerivationOptions] specified in JSON format via [derivationOptionsJson]
     */
    override suspend fun getSignatureVerificationKey(
      derivationOptionsJson: String
    ): SignatureVerificationKey =
      getSignatureVerificationKeyMarshaller.call(authTokenIfRequired(ApiDerivationOptions(derivationOptionsJson))) {
        marshallParameter(Inputs.getSignatureVerificationKey::derivationOptionsJson.name, derivationOptionsJson)
      }

    private val getSignatureVerificationKeyMarshaller =
      apiMarshallers.add<SignatureVerificationKey>(SuspendApi::getSignatureVerificationKey.name) {
        SignatureVerificationKey.fromJson(unmarshallString(Outputs.getSignatureVerificationKey.signatureVerificationKeyJson))
      }

    /**
     * Async API
     */


    override fun generateSignatureAsync(
      derivationOptionsJson: String,
      message: ByteArray
    ): Deferred<GenerateSignatureResult> = CoroutineScope(Dispatchers.Default).async {
      generateSignature(derivationOptionsJson, message) }

    override fun getSealingKeyAsync(
      derivationOptionsJson: String
    ): Deferred<SealingKey> = CoroutineScope(Dispatchers.Default).async {
      getSealingKey(derivationOptionsJson) }

    override fun getSecretAsync(
      derivationOptionsJson: String
    ): Deferred<Secret> = CoroutineScope(Dispatchers.Default).async {
      getSecret(derivationOptionsJson) }

    override fun getSignatureVerificationKeyAsync(
      derivationOptionsJson: String
    ): Deferred<SignatureVerificationKey> = CoroutineScope(Dispatchers.Default).async {
      getSignatureVerificationKey(derivationOptionsJson) }

    override fun getSigningKeyAsync(
      derivationOptionsJson: String
    ): Deferred<SigningKey> = CoroutineScope(Dispatchers.Default).async {
      getSigningKey(derivationOptionsJson) }

    override fun getSymmetricKeyAsync(
      derivationOptionsJson: String
    ): Deferred<SymmetricKey> = CoroutineScope(Dispatchers.Default).async {
      getSymmetricKey(derivationOptionsJson) }

    override fun getUnsealingKeyAsync(
      derivationOptionsJson: String
    ): Deferred<UnsealingKey> = CoroutineScope(Dispatchers.Default).async {
      getUnsealingKey(derivationOptionsJson) }

    override fun sealWithSymmetricKeyAsync(
      derivationOptionsJson: String,
      plaintext: ByteArray,
      unsealingInstructions: String
    ): Deferred<PackagedSealedMessage> = CoroutineScope(Dispatchers.Default).async {
      sealWithSymmetricKey(
      derivationOptionsJson, plaintext, unsealingInstructions
    ) }


    override fun unsealWithSymmetricKeyAsync(
      packagedSealedMessage: PackagedSealedMessage
    ): Deferred<ByteArray> = CoroutineScope(Dispatchers.Default).async {
      unsealWithSymmetricKey(packagedSealedMessage) }

    override fun unsealWithUnsealingKeyAsync(
      packagedSealedMessage: PackagedSealedMessage
    ): Deferred<ByteArray> = CoroutineScope(Dispatchers.Default).async {
      unsealWithUnsealingKey(packagedSealedMessage) }

    /**
     * Callback Api
     */
    private fun <T>toCallback(callback: CallbackApi.Callback<T>?, suspendFn: suspend () -> T) {
      CoroutineScope(Dispatchers.Default).launch {
        try {
          val result = suspendFn()
          callback?.onComplete(result)
        } catch (e: Throwable) {
          callback?.onException(e)
        }
      }
    }

    override fun generateSignature(
      derivationOptionsJson: String,
      message: ByteArray,
      callback: CallbackApi.Callback<GenerateSignatureResult>?
    ) = toCallback(callback) { generateSignature(
      derivationOptionsJson, message
    )}

    override fun getSealingKey(
      derivationOptionsJson: String,
      callback: CallbackApi.Callback<SealingKey>?
    ) = toCallback(callback) { getSealingKey(derivationOptionsJson) }

    override fun getSecret(
      derivationOptionsJson: String,
      callback: CallbackApi.Callback<Secret>?
    ) = toCallback(callback) { getSecret(derivationOptionsJson) }

    override fun getSignatureVerificationKey(
      derivationOptionsJson: String,
      callback: CallbackApi.Callback<SignatureVerificationKey>?
    ) = toCallback(callback) { getSignatureVerificationKey(derivationOptionsJson) }

    override fun getSigningKey(
      derivationOptionsJson: String,
      callback: CallbackApi.Callback<SigningKey>?
    ) = toCallback(callback) { getSigningKey(derivationOptionsJson) }

    override fun getSymmetricKey(
      derivationOptionsJson: String,
      callback: CallbackApi.Callback<SymmetricKey>?
    ) = toCallback(callback) { getSymmetricKey(derivationOptionsJson) }

    override fun getUnsealingKey(
      derivationOptionsJson: String,
      callback: CallbackApi.Callback<UnsealingKey>?
    ) = toCallback(callback) { getUnsealingKey(derivationOptionsJson) }

    override fun sealWithSymmetricKey(
      derivationOptionsJson: String,
      plaintext: ByteArray,
      unsealingInstructions: String,
      callback: CallbackApi.Callback<PackagedSealedMessage>
    ) = toCallback(callback) { sealWithSymmetricKey(
      derivationOptionsJson, plaintext, unsealingInstructions
    ) }

    override fun unsealWithSymmetricKey(
      packagedSealedMessage: PackagedSealedMessage,
      callback: CallbackApi.Callback<ByteArray>?
    )= toCallback(callback) { unsealWithSymmetricKey(packagedSealedMessage) }

    override fun unsealWithUnsealingKey(
      packagedSealedMessage: PackagedSealedMessage,
      callback: CallbackApi.Callback<ByteArray>?
    ) = toCallback(callback) { unsealWithUnsealingKey(packagedSealedMessage) }


}