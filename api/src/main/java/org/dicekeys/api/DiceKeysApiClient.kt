package org.dicekeys.api
//
//import android.app.Activity
//import android.content.ActivityNotFoundException
//import android.content.ComponentName
//import android.content.Intent
//import android.os.Bundle
//import androidx.core.os.bundleOf
//import androidx.fragment.app.Fragment
//import org.dicekeys.crypto.seeded.CryptographicVerificationFailureException
//import org.dicekeys.crypto.seeded.SealingKey
//import org.dicekeys.crypto.seeded.UnsealingKey
//import org.dicekeys.crypto.seeded.Secret
//import org.dicekeys.crypto.seeded.SignatureVerificationKey
//import org.dicekeys.crypto.seeded.SigningKey
//import org.dicekeys.crypto.seeded.SymmetricKey
//import org.dicekeys.crypto.seeded.PackagedSealedMessage
//import java.security.InvalidParameterException
//import kotlin.coroutines.resume
//import kotlin.coroutines.resumeWithException
//import kotlin.coroutines.suspendCoroutine
//
//typealias Callback<T> = CallbackApi.Callback<T>
//
///**
// * The API client used to ask the DiceKeys app to generate cryptographic keys seeded by a user's
// * DiceKey and to perform operations on the application's behalf.
// *
// *
// * **IMPORTANT**
// *
// * **To use this API, you must:**
// * 1. Call [create] to instantiate an API for your activity or fragment, which you can use
// * to make API calls/
// * 2. Implement _onActivityResult_ for your activity or fragment, and pass the
// * intent you receive to your API object's [handleOnActivityResult] function. This allows the
// * API to receive responses to API requests, process them, and return them to you.
// *
// * If you forget the second step, your API calls will never return—a class of bug first
// * documented in ([Steiner and Hawes, 1949](https://en.wikipedia.org/wiki/M.T.A._(song))).
// *
// */
//abstract class DiceKeysIntentApiClient : CallbackApi, SuspendApi {
//  companion object {
//    /**
//     * Instantiate an API client for a use within a [Activity].
//     *
//     * The [Activity] using the [DiceKeysIntentApiClient] must pass a reference
//     * to itself via the [activity] parameter.
//     *
//     * This client will send API requests to the DiceKeys app by creating intents and
//     * calling [Activity.startActivityForResult], but it needs your help
//     * to relay the results. You must have your activity override
//     * [Activity.onActivityResult] and pass the received intent to
//     * your [DiceKeysIntentApiClient]'s [handleOnActivityResult] method.
//     */
//    @JvmStatic
//    fun create(activity: Activity): DiceKeysIntentApiClient = object : DiceKeysIntentApiClient() {
//      override fun call(command: String, parameters: Bundle, requestCode: Int): Unit {
//        createIntentForCall(command, parameters).also { intent ->
//          try {
//            activity.startActivityForResult(intent, requestCode)
//          } catch (e: ActivityNotFoundException) {
//            throw DiceKeysAppNotPresentException()
//          }
//        }
//      }
//    }
//
//    /**
//     * Instantiate an API client for a use within a [Fragment].
//     *
//     * The [Fragment] using the [DiceKeysIntentApiClient] must pass a reference
//     * to itself via the [fragment] parameter.
//     *
//     * This client will send API requests to the DiceKeys app by creating intents and
//     * calling [Fragment.startActivityForResult], but it needs your help
//     * to relay the results. You must have your activity override
//     * [Fragment.onActivityResult] and pass the received intent to
//     * your [DiceKeysIntentApiClient]'s [handleOnActivityResult] method.
//     */
//    @JvmStatic
//    fun create(fragment: Fragment): DiceKeysIntentApiClient = object : DiceKeysIntentApiClient() {
//      override fun call(command: String, parameters: Bundle, requestCode: Int): Unit {
//        createIntentForCall(command, parameters).also { intent ->
//          try {
//            fragment.startActivityForResult(intent, requestCode)
//          } catch (e: ActivityNotFoundException) {
//            throw DiceKeysAppNotPresentException()
//          }
//        }
//      }
//    }
//
//    /**
//     * Generate unique request IDs fro each command by appending a random UUID to the command name.
//     */
//    private fun uniqueRequestIdForCommand(command: String) = "$command:${java.util.UUID.randomUUID()}"
//
//    /**
//     * Pull out the command name from the unique ID
//     */
////    private fun requestIdToCommand(requestId: String) =
////      requestId.substringBefore(':', "")
//  }
//
//  /**
//   * Creates an intent used to call the DiceKeys API by referencing the DiceKeys API server
//   * class, which responds to API requests ("org.dicekeys.trustedapp.activities.ExecuteApiCommandActivity").
//   * The intent's action is set to the command name and the rest of the parameters
//   * are passed as a bundle, with a unique requestId added the parameter list.
//   */
//  internal fun createIntentForCall(
//    command: String,
//    parameters: Bundle = Bundle()
//  ): Intent =
////            Intent(callingContext, ExecuteApiCommandActivity::class.java).apply {
//    Intent(command).apply {
//      // Component name set with (package name in manifest, fully qualified name)
//      component = ComponentName("org.dicekeys.trustedapp", "org.dicekeys.trustedapp.activities.ExecuteApiCommandActivity")
//      // FIXME -- will break when we separate out API from the app
//      //component = android.content.ComponentName(callingContext.packageName ?: "", "org.dicekeys.trustedapp.activities.ExecuteApiCommandActivity")
//      action = command
//      val parametersWithRequestId = Bundle(parameters)
//      parametersWithRequestId.putString(ApiStrings::requestId.name, uniqueRequestIdForCommand(command))
//      putExtras(parametersWithRequestId)
//    }
//
//  /**
//   * Since call is handle differently for fragments and for activities, the method that actually
//   * triggers calling is abstracted and implemented in the [create] call.
//   */
//  internal abstract fun call(
//    command: String,
//    parameters: Bundle = Bundle(),
//    requestCode: Int = 0
//  ): Unit
//
//
//  /**
//   * The generic wrapper for all callback classes used to get asynchronous
//   * responses. (Kotlin users can avoid using callbacks by using the suspendable
//   * APi calls.)
//   */
//
//
//  /***********************************************************************************
//   * API construction helper functions
//   */
//
//
//  /**
//   * For each API call, we'll track a map of requestId to the intent used to make the
//   * request and to the callbacks the caller registered for the response.
//   */
//
//  /**
//   * This helper function adds the intent and callbacks for a request
//   * to a map that can retrieve them based on the requestId.
//   */
//  private fun <T>addRequestAndCallback(
//    map: MutableMap<String, CallbackApi.Callback<T>>,
//    callback: CallbackApi.Callback<T>?
//  ): String = java.util.UUID.randomUUID().toString().also { requestId ->
//    if (callback != null) {
//      map[requestId] = callback
//    }
//  }
//
//  private suspend fun <T> awaitCallback(block: (CallbackApi.Callback<T>) -> Unit): T =
//    suspendCoroutine { cont ->
//      block(object : CallbackApi.Callback<T> {
//        override fun onComplete(result: T) = cont.resume(result)
//        override fun onException(e: Exception?) {
//          e?.let { cont.resumeWithException(it) }
//        }
//      })
//    }
//
//  private val getSecretCallbacks = mutableMapOf<String, CallbackApi.Callback<Secret>>()
//
//  /*****************************************************************************
//   * The API itself
//   */
//
//
//  /**
//   * Derive a pseudo-random cryptographic [Secret] from the user's DiceKey and
//   * the key-derivation options passed as [derivationOptionsJson]
//   * in [Key-Derivation Options JSON Format](hhttps://dicekeys.github.io/seeded-crypto/derivation_options_format.html).
//   */
//  override fun getSecret(
//    derivationOptionsJson: String,
//    callback: CallbackApi.Callback<Secret>?
//  ) =
//    call(SuspendApi::getSecret.name,
//      bundleOf(
//        ApiStrings::requestId.name to addRequestAndCallback(getSecretCallbacks, callback),
//        ApiStrings.Inputs.getSecret::derivationOptionsJson.name to derivationOptionsJson
//      )
//    )
//  /**
//   * getSeed (same as above) implemented as a Kotlin suspend function
//   * in place of callbacks.
//   */
//  override suspend fun getSecret(
//    derivationOptionsJson: String
//  ): Secret = awaitCallback{ getSecret(
//    derivationOptionsJson, it
//  ) }
//
//
//  private val getPrivateKeyCallbacks = mutableMapOf<String, Callback<UnsealingKey>>()
//  /**
//   * Get a [UnsealingKey] derived from the user's DiceKey (the seed) and the key-derivation options
//   * specified via [derivationOptionsJson],
//   * in [Key-Derivation Options JSON Format](hhttps://dicekeys.github.io/seeded-crypto/derivation_options_format.html),
//   * which must specify
//   *  `"clientMayRetrieveKey": true`.
//   */
//  override fun getUnsealingKey(
//    derivationOptionsJson: String,
//    callback: CallbackApi.Callback<UnsealingKey>?
//  ) =
//    call(SuspendApi::getUnsealingKey.name,
//      bundleOf(
//        ApiStrings::requestId.name to addRequestAndCallback(getPrivateKeyCallbacks, callback),
//        ApiStrings.Inputs.getUnsealingKey::derivationOptionsJson.name to derivationOptionsJson
//      )
//    )
//  /**
//   * getUnsealingKey (same as above) implemented as a Kotlin suspend function
//   * in place of callbacks.
//   */
//  override suspend fun getUnsealingKey(
//    derivationOptionsJson: String
//  ): UnsealingKey = awaitCallback{ getUnsealingKey(
//    derivationOptionsJson, it
//  ) }
//
//  private val getSymmetricKeyCallbacks = mutableMapOf<String, Callback<SymmetricKey>>()
//  /**
//   * Get a [SymmetricKey] derived from the user's DiceKey (the seed) and the key-derivation options
//   * specified via [derivationOptionsJson],
//   * in [Key-Derivation Options JSON Format](hhttps://dicekeys.github.io/seeded-crypto/derivation_options_format.html),
//   * which must specify
//   *  `"clientMayRetrieveKey": true`.
//   */
//  override fun getSymmetricKey(
//    derivationOptionsJson: String,
//    callback: CallbackApi.Callback<SymmetricKey>?
//  ) =
//    call(SuspendApi::getSymmetricKey.name,
//      bundleOf(
//        ApiStrings::requestId.name to addRequestAndCallback(getSymmetricKeyCallbacks, callback),
//        ApiStrings.Inputs.getSymmetricKey::derivationOptionsJson.name to derivationOptionsJson
//      )
//    )
//  /**
//   * getSymmetricKey (same as above) implemented as a Kotlin suspend function
//   * in place of callbacks.
//   */
//  override suspend fun getSymmetricKey(
//    derivationOptionsJson: String
//  ): SymmetricKey = awaitCallback{ getSymmetricKey(
//    derivationOptionsJson, it
//  ) }
//
//  private val getSigningKeyCallbacks = mutableMapOf<String, Callback<SigningKey>>()
//  /**
//   * Get a [SigningKey] derived from the user's DiceKey (the seed) and the key-derivation options
//   * specified via [derivationOptionsJson],
//   * in [Key-Derivation Options JSON Format](hhttps://dicekeys.github.io/seeded-crypto/derivation_options_format.html),
//   * which must specify
//   *  `"clientMayRetrieveKey": true`.
//   */
//  override fun getSigningKey(
//    derivationOptionsJson: String,
//    callback: CallbackApi.Callback<SigningKey>?
//  ) =
//    call(SuspendApi::getSigningKey.name,
//      bundleOf(
//        ApiStrings::requestId.name to addRequestAndCallback(getSigningKeyCallbacks, callback),
//        ApiStrings.Inputs.getSigningKey::derivationOptionsJson.name to derivationOptionsJson
//      )
//    )
//  /**
//   * getSigningKey (same as above) implemented as a Kotlin suspend function
//   * in place of callbacks.
//   */
//  override suspend fun getSigningKey(
//    derivationOptionsJson: String
//  ): SigningKey = awaitCallback{ getSigningKey(
//    derivationOptionsJson, it
//  ) }
//
//
//  private val getSealingKeyCallbacks = mutableMapOf<String, Callback<SealingKey>>()
//  /**
//   * Get a [SealingKey] derived from the user's DiceKey and the [ApiDerivationOptions] specified
//   * in [Key-Derivation Options JSON Format](hhttps://dicekeys.github.io/seeded-crypto/derivation_options_format.html)
//   * as [derivationOptionsJson].
//   */
//  override fun getSealingKey(
//    derivationOptionsJson: String,
//    callback: CallbackApi.Callback<SealingKey>?
//  ) =
//    call(SuspendApi::getSealingKey.name,
//      bundleOf(
//        ApiStrings::requestId.name to addRequestAndCallback(getSealingKeyCallbacks, callback),
//        ApiStrings.Inputs.getSealingKey::derivationOptionsJson.name to derivationOptionsJson
//      )
//    )
//  /**
//   * getPublicKey (same as above) implemented as a Kotlin suspend function
//   * in place of callbacks.
//   */
//  override suspend fun getSealingKey(
//    derivationOptionsJson: String
//  ): SealingKey = awaitCallback{ getSealingKey(
//    derivationOptionsJson, it
//  ) }
//
//  private val unsealWithUnsealingKeyCallbacks = mutableMapOf<String, Callback<ByteArray>>()
//  /**
//   * Unseal (decrypt & authenticate) a message that was previously sealed with a
//   * [SealingKey] to construct a [PackagedSealedMessage].
//   * The public/private key pair will be re-derived from the user's seed (DiceKey) and the
//   * key-derivation options packaged with the message.  It will also ensure that the
//   * unsealing_instructions instructions have not changed since the message was packaged.
//   *
//   * @throws [CryptographicVerificationFailureException]
//   */
//  override fun unsealWithUnsealingKey(
//    packagedSealedMessage: PackagedSealedMessage,
//    callback: CallbackApi.Callback<ByteArray>?
//  ) =
//    call(SuspendApi::unsealWithUnsealingKey.name,
//      bundleOf(
//        ApiStrings::requestId.name to addRequestAndCallback(unsealWithUnsealingKeyCallbacks, callback),
//        ApiStrings.Inputs.unsealWithUnsealingKey::packagedSealedMessage.name to packagedSealedMessage.toJson()
//      )
//    )
//  /**
//   * unsealWithPrivateKey (same as above) implemented as a Kotlin suspend function
//   * in place of callbacks.
//   */
//  override suspend fun unsealWithUnsealingKey(
//    packagedSealedMessage: PackagedSealedMessage
//  ): ByteArray = awaitCallback{ unsealWithUnsealingKey(
//    packagedSealedMessage, it
//  ) }
//
//
//  private val sealWithSymmetricKeyCallbacks = mutableMapOf<String, Callback<PackagedSealedMessage>>()
//  /**
//   * Seal (encrypt with a message-authentication code) a message ([plaintext]) with a
//   * symmetric key derived from the user's DiceKey, the
//   * [derivationOptionsJson]
//   * in [Key-Derivation Options JSON Format](hhttps://dicekeys.github.io/seeded-crypto/derivation_options_format.html),
//   * and [UnsealingInstructions] specified via a JSON string as
//   * [unsealingInstructions] in the
//   * in [Post-Decryption Instructions JSON Format](https://dicekeys.github.io/seeded-crypto/unsealing_instructions_format.html).
//   */
//  override fun sealWithSymmetricKey(
//    derivationOptionsJson: String,
//    plaintext: ByteArray,
//    unsealingInstructions: String,
//    callback: CallbackApi.Callback<PackagedSealedMessage>
//  ) =
//    call(SuspendApi::sealWithSymmetricKey.name,
//      bundleOf(
//        ApiStrings::requestId.name to addRequestAndCallback(sealWithSymmetricKeyCallbacks, callback),
//        ApiStrings.Inputs.sealWithSymmetricKey::derivationOptionsJson.name to derivationOptionsJson,
//        ApiStrings.Inputs.sealWithSymmetricKey::plaintext.name to plaintext,
//        ApiStrings.Inputs.sealWithSymmetricKey::unsealingInstructions.name to unsealingInstructions
//      )
//    )
//
//  /**
//   * Seal (same as above) implemented as a Kotlin suspend function in place of callbacks.
//   */
//  override suspend fun sealWithSymmetricKey(
//    derivationOptionsJson: String,
//    plaintext: ByteArray,
//    unsealingInstructions: String
//  ): PackagedSealedMessage = awaitCallback{ sealWithSymmetricKey(
//    derivationOptionsJson, plaintext, unsealingInstructions, it
//  ) }
//
//
//  private val unsealWithSymmetricKeyCallbacks = mutableMapOf<String, Callback<ByteArray>>()
//  /**
//   * Unseal (decrypt & authenticate) a [packagedSealedMessage] that was previously sealed with a
//   * symmetric key derived from the user's DiceKey, the
//   * [ApiDerivationOptions] specified in JSON format via [PackagedSealedMessage.derivationOptionsJson],
//   * and any [UnsealingInstructions] optionally specified by [PackagedSealedMessage.unsealingInstructions]
//   * in [Post-Decryption Instructions JSON Format](https://dicekeys.github.io/seeded-crypto/unsealing_instructions_format.html).
//   *
//   * If any of those strings change, the wrong key will be derive and the message will
//   * not be successfully unsealed, yielding a [org.dicekeys.crypto.seeded.CryptographicVerificationFailureException] exception.
//   */
//  override fun unsealWithSymmetricKey(
//    packagedSealedMessage: PackagedSealedMessage,
//    callback: CallbackApi.Callback<ByteArray>?
//  ) =
//
//    call(SuspendApi::unsealWithSymmetricKey.name,
//      bundleOf(
//        ApiStrings::requestId.name to addRequestAndCallback(unsealWithSymmetricKeyCallbacks, callback),
//        ApiStrings.Inputs.unsealWithSymmetricKey::packagedSealedMessage.name to packagedSealedMessage.toJson()
//      )
//    )
//
//  /**
//   * unsealWithSymmetricKey (same as above) implemented as a Kotlin suspend function
//   * in place of callbacks.
//   */
//  override suspend fun unsealWithSymmetricKey(
//    packagedSealedMessage: PackagedSealedMessage
//  ): ByteArray = awaitCallback{ unsealWithSymmetricKey(
//    packagedSealedMessage, it
//  ) }
//
//  private val getSignatureVerificationKeyCallbacks = mutableMapOf<String, Callback<SignatureVerificationKey>>()
//  /**
//   * Get a public [SignatureVerificationKey] derived from the user's DiceKey and the
//   * [ApiDerivationOptions] specified in JSON format via [derivationOptionsJson]
//   */
//  override fun getSignatureVerificationKey(
//    derivationOptionsJson: String,
//    callback: CallbackApi.Callback<SignatureVerificationKey>?
//  ) =
//    call(SuspendApi::getSignatureVerificationKey.name,
//      bundleOf(
//        ApiStrings::requestId.name to addRequestAndCallback(getSignatureVerificationKeyCallbacks, callback),
//        ApiStrings.Inputs.getSignatureVerificationKey::derivationOptionsJson.name to derivationOptionsJson
//      )
//    )
//  /**
//   * getSignatureVerificationKey (same as above) implemented as a Kotlin suspend function
//   * in place of callbacks.
//   */
//  override suspend fun getSignatureVerificationKey(
//    derivationOptionsJson: String
//  ): SignatureVerificationKey = awaitCallback{ getSignatureVerificationKey(
//    derivationOptionsJson, it
//  ) }
//
//  private val generateSignatureCallbacks = mutableMapOf<String, Callback<GenerateSignatureResult>>()
//  /**
//   * Sign a [message] using a public/private signing key pair derived
//   * from the user's DiceKey and the [ApiDerivationOptions] specified in JSON format via
//   * [derivationOptionsJson].
//   */
//  override fun generateSignature(
//    derivationOptionsJson: String,
//    message: ByteArray,
//    callback: CallbackApi.Callback<GenerateSignatureResult>?
//  ) =
//    call(SuspendApi::generateSignature.name,
//      bundleOf(
//        ApiStrings::requestId.name to addRequestAndCallback(generateSignatureCallbacks, callback),
//        ApiStrings.Inputs.generateSignature::derivationOptionsJson.name to derivationOptionsJson,
//        ApiStrings.Inputs.generateSignature::message.name to message
//      )
//    )
//  /**
//   * generateSignature (same as above) implemented as a Kotlin suspend function
//   * in place of callbacks.
//   */
//  override suspend fun generateSignature(
//    derivationOptionsJson: String,
//    message: ByteArray
//  ): GenerateSignatureResult = awaitCallback{ generateSignature(
//    derivationOptionsJson, message, it
//  ) }
//
//  private fun <T>handleResult(
//    intentAndCallbackMap: MutableMap<String, Callback<T>>,
//    resultIntent: Intent,
//    successHandler: () -> T
//  ) {
//    resultIntent.getStringExtra(ApiStrings::requestId.name)?.let{ requestId ->
//      intentAndCallbackMap[requestId]?.let { callback ->
//        if (resultIntent.hasExtra(ApiStrings.Outputs::exception.name)) {
//          callback.onException(resultIntent.getSerializableExtra(ApiStrings.Outputs::exception.name) as Exception)
//        } else {
//          try {
//            callback.onComplete(successHandler())
//          } catch (e: Exception) {
//            callback.onException(e)
//          }
//        }
//      }
//    }
//  }
//
//  fun <T>requireResult(t: T?): T = t ?:
//      throw InvalidParameterException("A DiceKeys API method failed to return an expected result parameter.")
//
//  /**
//   * Activities and Fragments that use this API should implement onActivityResult and
//   * and call handleOnActivityResult with the data/intent (third parameter) received.
//   * Doing so allows this class to process results returned to the activity/fragment
//   * and then call the appropriate callback functions when an API call has either
//   * succeeded or failed.
//   */
//  fun handleOnActivityResult(resultIntent: Intent?) {
//    resultIntent?.getStringExtra(ApiStrings::requestId.name)?.let{ requestId ->
//      when(requestIdToCommand(requestId)) {
//        SuspendApi::getSecret.name -> handleResult(getSecretCallbacks, resultIntent){
//          Secret.fromJson(requireResult(resultIntent.getStringExtra(ApiStrings.Outputs.getSecret::secret.name)))
//        }
//
//        SuspendApi::getSealingKey.name -> handleResult(getSealingKeyCallbacks, resultIntent){
//          SealingKey.fromJson(
//            requireResult(resultIntent.getStringExtra(ApiStrings.Outputs.getSealingKey::sealingKey.name))
//          )
//        }
//
//        SuspendApi::unsealWithSymmetricKey.name -> handleResult(unsealWithSymmetricKeyCallbacks, resultIntent){
//          requireResult(resultIntent.getByteArrayExtra(ApiStrings.Outputs.unsealWithSymmetricKey::plaintext.name))
//        }
//
//        SuspendApi::unsealWithUnsealingKey.name -> handleResult(unsealWithUnsealingKeyCallbacks, resultIntent){
//            requireResult(resultIntent.getByteArrayExtra(ApiStrings.Outputs.unsealWithUnsealingKey::plaintext.name))
//        }
//
//        SuspendApi::sealWithSymmetricKey.name -> handleResult(sealWithSymmetricKeyCallbacks, resultIntent){
//          PackagedSealedMessage.fromJson(requireResult(
//            resultIntent.getStringExtra(ApiStrings.Outputs.sealWithSymmetricKey::packagedSealedMessage.name))
//          )
//        }
//
//        SuspendApi::generateSignature.name -> handleResult(generateSignatureCallbacks, resultIntent){
//          object: GenerateSignatureResult {
//            override val signature = requireResult(
//              resultIntent.getByteArrayExtra(ApiStrings.Outputs.generateSignature::signature.name)
//            )
//            override val signatureVerificationKey =
//              SignatureVerificationKey.fromJson(requireResult(
//                resultIntent.getStringExtra(ApiStrings.Outputs.generateSignature::signatureVerificationKey.name)
//            ))
//          }
//        }
//
//        SuspendApi::getSignatureVerificationKey.name -> handleResult(getSignatureVerificationKeyCallbacks, resultIntent){
//          SignatureVerificationKey.fromJson(
//              requireResult(resultIntent.getStringExtra(
//                ApiStrings.Outputs.getSignatureVerificationKey::signatureVerificationKey.name
//              ))
//            )
//          }
//
//        SuspendApi::getSigningKey.name -> handleResult(getSigningKeyCallbacks, resultIntent){
//          SigningKey.fromJson(requireResult(resultIntent.getStringExtra(
//                  ApiStrings.Outputs.getSigningKey::signingKey.name
//          )))
//        }
//
//        SuspendApi::getUnsealingKey.name -> handleResult(getPrivateKeyCallbacks, resultIntent){
//          UnsealingKey.fromJson(requireResult(
//              resultIntent.getStringExtra(
//                ApiStrings.Outputs.getUnsealingKey::unsealingKey.name
//              )
//            ))
//        }
//
//        SuspendApi::getSymmetricKey.name -> handleResult(getSymmetricKeyCallbacks, resultIntent){
//          SymmetricKey.fromJson(requireResult(
//            resultIntent.getStringExtra(
//              ApiStrings.Outputs.getSymmetricKey::symmetricKey.name
//            )))
//        }
//
//        else -> {}
//      }
//    }
//  }
//}
//
