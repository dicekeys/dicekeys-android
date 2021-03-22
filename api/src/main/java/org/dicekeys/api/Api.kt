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
     * from the user's DiceKey and the [ApiRecipe] specified in JSON format via
     * [recipeJson].
     */
    override suspend fun generateSignature(
      recipeJson: String,
      message: ByteArray
    ): GenerateSignatureResult =
      generateSignatureMarshaller.call(authTokenIfRequired(ApiRecipe(recipeJson))) {
        marshallParameter(Inputs.generateSignature::recipeJson.name, recipeJson )
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
     * Derive a pseudo-random cryptographic [Password] from the user's DiceKey and
     * the key-derivation options passed as [recipeJson]
     * in [Recipe JSON Format](https://dicekeys.github.io/seeded-crypto/recipe_format.html).
     */
    override suspend fun getPassword(
            recipeJson: String
    ): Password =
            getPasswordMarshaller.call(authTokenIfRequired(ApiRecipe(recipeJson))) {
            marshallParameter(Inputs.getPassword::recipeJson.name, recipeJson)
          }

    private val getPasswordMarshaller =
          apiMarshallers.add<Password>(SuspendApi::getPassword.name) {
            Password.fromJson(unmarshallString(Outputs.getSecret.secretJson))
          }

    /**
     * Derive a pseudo-random cryptographic [Secret] from the user's DiceKey and
     * the key-derivation options passed as [recipeJson]
     * in [Recipe JSON Format](https://dicekeys.github.io/seeded-crypto/recipe_format.html).
     */
    override suspend fun getSecret(
      recipeJson: String
    ): Secret =
      getSecretMarshaller.call(authTokenIfRequired(ApiRecipe(recipeJson))) {
        marshallParameter(Inputs.getSecret::recipeJson.name, recipeJson)
      }

    private val getSecretMarshaller =
      apiMarshallers.add<Secret>(SuspendApi::getSecret.name) {
        Secret.fromJson(unmarshallString(Outputs.getSecret.secretJson))
      }


    /**
     * Get a [UnsealingKey] derived from the user's DiceKey (the seed) and the key-derivation options
     * specified via [recipeJson],
     * in [Recipe JSON Format](https://dicekeys.github.io/seeded-crypto/recipe_format.html),
     * which must specify
     *  `"clientMayRetrieveKey": true`.
     */
    override suspend fun getUnsealingKey(
      recipeJson: String
    ): UnsealingKey =
      getUnsealingKeyMarshaller.call(authTokenIfRequired(ApiRecipe(recipeJson))) {
        marshallParameter(Inputs.getUnsealingKey::recipeJson.name, recipeJson)
      }

    private val getUnsealingKeyMarshaller =
      apiMarshallers.add<UnsealingKey>(SuspendApi::getUnsealingKey.name) {
        UnsealingKey.fromJson(unmarshallString(Outputs.getUnsealingKey.unsealingKeyJson))
      }


    /**
     * Get a [SymmetricKey] derived from the user's DiceKey (the seed) and the key-derivation options
     * specified via [recipeJson],
     * in [Recipe JSON Format](https://dicekeys.github.io/seeded-crypto/recipe_format.html),
     * which must specify
     *  `"clientMayRetrieveKey": true`.
     */
    override suspend fun getSymmetricKey(
      recipeJson: String
    ): SymmetricKey =
      getSymmetricKeyMarshaller.call(authTokenIfRequired(ApiRecipe(recipeJson))) {
        marshallParameter(Inputs.getSymmetricKey::recipeJson.name, recipeJson)
      }

    private val getSymmetricKeyMarshaller =
      apiMarshallers.add(SuspendApi::getSymmetricKey.name) {
        SymmetricKey.fromJson(unmarshallString(Outputs.getSymmetricKey.symmetricKeyJson))
      }

    /**
     * Get a [SigningKey] derived from the user's DiceKey (the seed) and the key-derivation options
     * specified via [recipeJson],
     * in [Recipe JSON Format](https://dicekeys.github.io/seeded-crypto/recipe_format.html),
     * which must specify
     *  `"clientMayRetrieveKey": true`.
     */
    override suspend fun getSigningKey(
      recipeJson: String
    ): SigningKey =
      getSigningKeyMarshaller.call(authTokenIfRequired(ApiRecipe(recipeJson))) {
        marshallParameter(Inputs.getSigningKey::recipeJson.name, recipeJson)
      }

    private val getSigningKeyMarshaller =
      apiMarshallers.add<SigningKey>(SuspendApi::getSigningKey.name) {
        SigningKey.fromJson(unmarshallString(Outputs.getSigningKey.signingKeyJson))
      }


    /**
     * Get a [SealingKey] derived from the user's DiceKey and the [ApiRecipe] specified
     * in [Recipe JSON Format](https://dicekeys.github.io/seeded-crypto/recipe_format.html)
     * as [recipeJson].
     */
    override suspend fun getSealingKey(
      recipeJson: String
    ): SealingKey =
      getSealingKeyMarshaller.call(authTokenIfRequired(ApiRecipe(recipeJson))) {
        marshallParameter(Inputs.getSealingKey::recipeJson.name, recipeJson)
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
        authTokenIfRequired(ApiRecipe(packagedSealedMessage.recipe)) ?:
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
     * [recipeJson]
     * in [Recipe JSON Format](https://dicekeys.github.io/seeded-crypto/recipe_format.html),
     * and [UnsealingInstructions] specified via a JSON string as
     * [unsealingInstructions] in the
     * in [Post-Decryption Instructions JSON Format](https://dicekeys.github.io/seeded-crypto/unsealing_instructions_format.html).
     */
    override suspend fun sealWithSymmetricKey(
      recipeJson: String,
      plaintext: ByteArray,
      unsealingInstructions: String
    ): PackagedSealedMessage =
      sealWithSymmetricKeyMarshaller.call(authTokenIfRequired(ApiRecipe(recipeJson))) {
        marshallParameter(Inputs.sealWithSymmetricKey::recipeJson.name, recipeJson)
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
     * [ApiRecipe] specified in JSON format via [PackagedSealedMessage.recipeJson],
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
        authTokenIfRequired(ApiRecipe(packagedSealedMessage.recipe)) ?:
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
     * [ApiRecipe] specified in JSON format via [recipeJson]
     */
    override suspend fun getSignatureVerificationKey(
      recipeJson: String
    ): SignatureVerificationKey =
      getSignatureVerificationKeyMarshaller.call(authTokenIfRequired(ApiRecipe(recipeJson))) {
        marshallParameter(Inputs.getSignatureVerificationKey::recipeJson.name, recipeJson)
      }

    private val getSignatureVerificationKeyMarshaller =
      apiMarshallers.add<SignatureVerificationKey>(SuspendApi::getSignatureVerificationKey.name) {
        SignatureVerificationKey.fromJson(unmarshallString(Outputs.getSignatureVerificationKey.signatureVerificationKeyJson))
      }

    /**
     * Async API
     */


    override fun generateSignatureAsync(
      recipeJson: String,
      message: ByteArray
    ): Deferred<GenerateSignatureResult> = CoroutineScope(Dispatchers.Default).async {
      generateSignature(recipeJson, message) }

    override fun getSealingKeyAsync(
      recipeJson: String
    ): Deferred<SealingKey> = CoroutineScope(Dispatchers.Default).async {
      getSealingKey(recipeJson) }

    override fun getPasswordAsync(
            recipeJson: String
    ): Deferred<Password> = CoroutineScope(Dispatchers.Default).async {
      getPassword(recipeJson) }

    override fun getSecretAsync(
      recipeJson: String
    ): Deferred<Secret> = CoroutineScope(Dispatchers.Default).async {
      getSecret(recipeJson) }

    override fun getSignatureVerificationKeyAsync(
      recipeJson: String
    ): Deferred<SignatureVerificationKey> = CoroutineScope(Dispatchers.Default).async {
      getSignatureVerificationKey(recipeJson) }

    override fun getSigningKeyAsync(
      recipeJson: String
    ): Deferred<SigningKey> = CoroutineScope(Dispatchers.Default).async {
      getSigningKey(recipeJson) }

    override fun getSymmetricKeyAsync(
      recipeJson: String
    ): Deferred<SymmetricKey> = CoroutineScope(Dispatchers.Default).async {
      getSymmetricKey(recipeJson) }

    override fun getUnsealingKeyAsync(
      recipeJson: String
    ): Deferred<UnsealingKey> = CoroutineScope(Dispatchers.Default).async {
      getUnsealingKey(recipeJson) }

    override fun sealWithSymmetricKeyAsync(
      recipeJson: String,
      plaintext: ByteArray,
      unsealingInstructions: String
    ): Deferred<PackagedSealedMessage> = CoroutineScope(Dispatchers.Default).async {
      sealWithSymmetricKey(
      recipeJson, plaintext, unsealingInstructions
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
      recipeJson: String,
      message: ByteArray,
      callback: CallbackApi.Callback<GenerateSignatureResult>?
    ) = toCallback(callback) { generateSignature(
      recipeJson, message
    )}

    override fun getSealingKey(
      recipeJson: String,
      callback: CallbackApi.Callback<SealingKey>?
    ) = toCallback(callback) { getSealingKey(recipeJson) }

    override fun getPassword(
            recipeJson: String,
            callback: CallbackApi.Callback<Password>?
    ) = toCallback(callback) { getPassword(recipeJson) }

    override fun getSecret(
      recipeJson: String,
      callback: CallbackApi.Callback<Secret>?
    ) = toCallback(callback) { getSecret(recipeJson) }

    override fun getSignatureVerificationKey(
      recipeJson: String,
      callback: CallbackApi.Callback<SignatureVerificationKey>?
    ) = toCallback(callback) { getSignatureVerificationKey(recipeJson) }

    override fun getSigningKey(
      recipeJson: String,
      callback: CallbackApi.Callback<SigningKey>?
    ) = toCallback(callback) { getSigningKey(recipeJson) }

    override fun getSymmetricKey(
      recipeJson: String,
      callback: CallbackApi.Callback<SymmetricKey>?
    ) = toCallback(callback) { getSymmetricKey(recipeJson) }

    override fun getUnsealingKey(
      recipeJson: String,
      callback: CallbackApi.Callback<UnsealingKey>?
    ) = toCallback(callback) { getUnsealingKey(recipeJson) }

    override fun sealWithSymmetricKey(
      recipeJson: String,
      plaintext: ByteArray,
      unsealingInstructions: String,
      callback: CallbackApi.Callback<PackagedSealedMessage>
    ) = toCallback(callback) { sealWithSymmetricKey(
      recipeJson, plaintext, unsealingInstructions
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