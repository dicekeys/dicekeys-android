package org.dicekeys.api

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import org.dicekeys.crypto.seeded.SealingKey
import org.dicekeys.crypto.seeded.UnsealingKey
import org.dicekeys.crypto.seeded.Secret
import org.dicekeys.crypto.seeded.SignatureVerificationKey
import org.dicekeys.crypto.seeded.SigningKey
import org.dicekeys.crypto.seeded.SymmetricKey
import org.dicekeys.crypto.seeded.PackagedSealedMessage
import java.security.InvalidParameterException
import kotlin.collections.HashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * The API client used to ask the DiceKeys app to generate cryptographic keys seeded by a user's
 * DiceKey and to perform operations on the application's behalf.
 *
 *
 * **IMPORTANT**
 *
 * **To use this API, you must:**
 * 1. Call [create] to instantiate an API for your activity or fragment, which you can use
 * to make API calls/
 * 2. Implement _onActivityResult_ for your activity or fragment, and pass the
 * intent you receive to your API object's [handleOnActivityResult] function. This allows the
 * API to receive responses to API requests, process them, and return them to you.
 *
 * If you forget the second step, your API calls will never return—a class of bug first
 * documented in ([Steiner and Hawes, 1949](https://en.wikipedia.org/wiki/M.T.A._(song))).
 *
 */
abstract class DiceKeysApiClient(
  private val callingContext: Context
) {
  companion object {
    /**
     * Instantiate an API client for a use within a [Activity].
     *
     * The [Activity] using the [DiceKeysApiClient] must pass a reference
     * to itself via the [activity] parameter.
     *
     * This client will send API requests to the DiceKeys app by creating intents and
     * calling [Activity.startActivityForResult], but it needs your help
     * to relay the results. You must have your activity override
     * [Activity.onActivityResult] and pass the received intent to
     * your [DiceKeysApiClient]'s [handleOnActivityResult] method.
     */
    @JvmStatic
    fun create(activity: Activity): DiceKeysApiClient = object: DiceKeysApiClient(activity) {
      override fun call(command: String, parameters: Bundle, requestCode: Int): Intent =
        createIntentForCall(command, parameters).also { intent->
          try {
            activity.startActivityForResult(intent, requestCode)
          } catch (e: ActivityNotFoundException) {
            throw DiceKeysAppNotPresentException()
          }
        }
    }

    /**
     * Instantiate an API client for a use within a [Fragment].
     *
     * The [Fragment] using the [DiceKeysApiClient] must pass a reference
     * to itself via the [fragment] parameter.
     *
     * This client will send API requests to the DiceKeys app by creating intents and
     * calling [Fragment.startActivityForResult], but it needs your help
     * to relay the results. You must have your activity override
     * [Fragment.onActivityResult] and pass the received intent to
     * your [DiceKeysApiClient]'s [handleOnActivityResult] method.
     */
    @JvmStatic
    fun create(fragment: Fragment): DiceKeysApiClient = object: DiceKeysApiClient(fragment.context ?: throw InvalidParameterException("Fragment must have context")) {
      override fun call(command: String, parameters: Bundle, requestCode: Int): Intent =
        createIntentForCall(command, parameters).also { intent->
          try {
            fragment.startActivityForResult(intent, requestCode)
          } catch (e: ActivityNotFoundException) {
            throw DiceKeysAppNotPresentException()
          }
        }
    }
  }

  /**
   * Generate unique request IDs fro each command by appending a random UUID to the command name.
   */
  private fun uniqueRequestIdForCommand(command: String) = "$command:${java.util.UUID.randomUUID()}"

  /**
   * Pull out the command name from the unique ID
   */
  private fun requestIdToCommand(requestId: String) =
    requestId.substringBefore(':', "")

  /**
   * Creates an intent used to call the DiceKeys API by referencing the DiceKeys API server
   * class, which responds to API requests ([ExecuteApiCommandActivity]).
   * The intent's action is set to the command name and the rest of the parameters
   * are passed as a bundle, with a unique requestId added the parameter list.
   */
  internal fun createIntentForCall(
    command: String,
    parameters: Bundle = Bundle()
  ): Intent =
//            Intent(callingContext, ExecuteApiCommandActivity::class.java).apply {
    Intent(command).apply {
      // Component name set with (package name in manifest, fully qualified name)
      component = ComponentName("org.dicekeys.trustedapp","org.dicekeys.trustedapp.activities.ExecuteApiCommandActivity")
      // FIXME -- will break when we separate out API from the app
      //component = android.content.ComponentName(callingContext.packageName ?: "", "org.dicekeys.trustedapp.activities.ExecuteApiCommandActivity")
      action = command
      val parametersWithRequestId = Bundle(parameters)
      parametersWithRequestId.putString(ParameterNames.Common.requestId, uniqueRequestIdForCommand(command))
      putExtras(parametersWithRequestId)
    }

  /**
   * Since call is handle differently for fragments and for activities, the method that actually
   * triggers calling is abstracted and implemented in the [create] call.
   */
  internal abstract fun call(
    command: String,
    parameters: Bundle = Bundle(),
    requestCode: Int = 0
  ): Intent


  /**
   * The names used to marshall parameters into bundles for requests and response.
   */
  object ParameterNames {
    internal object Reused {
      const val postDecryptionInstructions = "postDecryptionInstructions"
      const val plaintext = "plaintext"
      const val packagedSealedMessageSerializedToBinary = "packagedSealedMessageSerializedToBinary"
      const val signatureVerificationKeySerializedToBinary = "signatureVerificationKeySerializedToBinary"
      const val signingKeySerializedToBinary = "signingKeySerializedToBinary"
      const val unsealingKeySerializedToBinary = "privateKeySerializedToBinary"
      const val sealingKeySerializedToBinary = "publicKeySerializedToBinary"
      const val symmetricKeySerializedToBinary = "symmetricKeySerializedToBinary"

    }

    object Common {
      const val requestId  = "requestId"
      const val derivationOptionsJson = "derivationOptionsJson "
      const val exception = "exception"
    }

    object Secret {
      object Get {
        const val secretSerializedToBinary = "secretSerializedToBinary"
      }
    }

    object SymmetricKey {
      object GetKey {
        const val symmetricKeySerializedToBinary = Reused.symmetricKeySerializedToBinary
      }

      object Seal {
        const val plaintext = Reused.plaintext
        const val postDecryptionInstructions = Reused.postDecryptionInstructions
        const val packagedSealedMessageSerializedToBinary = Reused.packagedSealedMessageSerializedToBinary
      }
      object Unseal {
        const val packagedSealedMessageSerializedToBinary = Reused.packagedSealedMessageSerializedToBinary
        const val plaintext = Reused.plaintext
      }
    }

    object UnsealingKey {
      object GetUnsealingKey {
        const val unsealingKeySerializedToBinary = Reused.unsealingKeySerializedToBinary
      }

      object GetSealingKey {
        const val sealingKeySerializedToBinary = Reused.sealingKeySerializedToBinary
      }

      object Unseal {
        const val packagedSealedMessageSerializedToBinary = Reused.packagedSealedMessageSerializedToBinary
        const val plaintext = Reused.plaintext
      }
    }

    object SigningKey {
      object GetSigningKey {
        const val signingKeySerializedToBinary = Reused.signingKeySerializedToBinary
      }

      object GetSignatureVerificationKey {
        const val signatureVerificationKeySerializedToBinary = Reused.signatureVerificationKeySerializedToBinary
      }

      object GenerateSignature {
        const val message = "message"
        const val signature = "signature"
        const val signatureVerificationKeySerializedToBinary = Reused.signatureVerificationKeySerializedToBinary
      }
    }

  }

  /**
   * When an API call is made, an intent is sent with an actual equal to one of these
   * operation names, each of which represents a different API function (operation).
   */
  object OperationNames {

    const val getSecret = "getSecret"
    const val getSymmetricKey = "getSymmetricKey"
    const val sealWithSymmetricKey = "sealWithSymmetricKey"
    const val unsealWithSymmetricKey = "unsealWithSymmetricKey"
    const val getUnsealingKey = "getUnsealingKey"
    const val getSealingKey = "getSealingKey"
    const val unsealWithUnsealingKey = "unsealWithUnsealingKey"
    const val getSigningKey = "getSigningKey"
    const val getSignatureVerificationKey = "getSignatureVerificationKey"
    const val generateSignature = "generateSignature"

    val All = setOf(
      getSecret,
      sealWithSymmetricKey,
      unsealWithSymmetricKey,
      getSymmetricKey,
      getSealingKey,
      getUnsealingKey,
      unsealWithUnsealingKey,
      generateSignature,
      getSigningKey,
      getSignatureVerificationKey
    )
  }

  /**
   * The generic wrapper for all callback classes used to get asynchronous
   * responses. (Kotlin users can avoid using callbacks by using the suspendable
   * APi calls.)
   */
  interface Callback<T> {
    fun onComplete(result: T)
    fun onException(e: Exception?)
  }

  /***********************************************************************************
   * API construction helper functions
   */


  /**
   * For each API call, we'll track a map of requestId to the intent used to make the
   * request and to the callbacks the caller registered for the response.
   */
  internal data class RequestIntentAndCallback<T> (
    val requestIntent: Intent,
    val callback: Callback<T>
  )

  /**
   * This helper function adds the intent and callbacks for a request
   * to a map that can retrieve them based on the requestId.
   */
  private fun <T>addRequestAndCallback(
    map: HashMap<String, RequestIntentAndCallback<T>>,
    intent: Intent,
    callback: Callback<T>
  ): Unit {
    intent.getStringExtra(ParameterNames.Common.requestId)?.let { requestId ->
      map[requestId] = RequestIntentAndCallback<T>(intent, callback)
    }
  }

  private suspend fun <T> awaitCallback(block: (Callback<T>) -> Unit) : T =
    suspendCoroutine { cont ->
      block(object : Callback<T> {
        override fun onComplete(result: T) = cont.resume(result)
        override fun onException(e: Exception?) {
          e?.let { cont.resumeWithException(it) }
        }
      })
    }

  private val getSecretCallbacks = HashMap<String, RequestIntentAndCallback<Secret>>()

  /*****************************************************************************
   * The API itself
   */


  /**
   * Derive a pseudo-random cryptographic [Secret] from the user's DiceKey and
   * the key-derivation options passed as [derivationOptionsJson]
   * in [Key-Derivation Options JSON Format](hhttps://dicekeys.github.io/seeded-crypto/derivation_options_format.html).
   */
  fun getSecret(
    derivationOptionsJson: String,
    callback: Callback<Secret>? = null
  ): Intent =
    call(OperationNames.getSecret,
      bundleOf(
        ParameterNames.Common.derivationOptionsJson to derivationOptionsJson
      )
    ).also { intent -> callback?.let{
      addRequestAndCallback(getSecretCallbacks, intent, it)
    } }
  /**
   * getSeed (same as above) implemented as a Kotlin suspend function
   * in place of callbacks.
   */
  suspend fun getSecret(
    derivationOptionsJson: String
  ): Secret = awaitCallback{ getSecret(
    derivationOptionsJson, it
  ) }


  private val getPrivateKeyCallbacks = HashMap<String, RequestIntentAndCallback<UnsealingKey>>()
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
  ): Intent =
    call(OperationNames.getUnsealingKey,
      bundleOf(
        ParameterNames.Common.derivationOptionsJson to derivationOptionsJson
      )
    ).also { intent -> callback?.let{ addRequestAndCallback(getPrivateKeyCallbacks, intent, it) } }
  /**
   * getPrivateKey (same as above) implemented as a Kotlin suspend function
   * in place of callbacks.
   */
  suspend fun getUnsealingKey(
    derivationOptionsJson: String
  ): UnsealingKey = awaitCallback{ getUnsealingKey(
    derivationOptionsJson, it
  ) }

  private val getSymmetricKeyCallbacks = HashMap<String, RequestIntentAndCallback<SymmetricKey>>()
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
  ): Intent =
    call(OperationNames.getSymmetricKey,
      bundleOf(
        ParameterNames.Common.derivationOptionsJson to derivationOptionsJson
      )
    ).also { intent -> callback?.let{ addRequestAndCallback(getSymmetricKeyCallbacks, intent, it) } }
  /**
   * getSymmetricKey (same as above) implemented as a Kotlin suspend function
   * in place of callbacks.
   */
  suspend fun getSymmetricKey(
    derivationOptionsJson: String
  ): SymmetricKey = awaitCallback{ getSymmetricKey(
    derivationOptionsJson, it
  ) }

  private val getSigningKeyCallbacks = HashMap<String, RequestIntentAndCallback<SigningKey>>()
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
  ): Intent =
    call(OperationNames.getSigningKey,
      bundleOf(
        ParameterNames.Common.derivationOptionsJson to derivationOptionsJson
      )
    ).also { intent -> callback?.let{ addRequestAndCallback(getSigningKeyCallbacks, intent, it) } }
  /**
   * getSigningKey (same as above) implemented as a Kotlin suspend function
   * in place of callbacks.
   */
  suspend fun getSigningKey(
    derivationOptionsJson: String
  ): SigningKey = awaitCallback{ getSigningKey(
    derivationOptionsJson, it
  ) }


  private val getPublicKeyCallbacks = HashMap<String, RequestIntentAndCallback<SealingKey>>()
  /**
   * Get a [SealingKey] derived from the user's DiceKey and the [ApiDerivationOptions] specified
   * in [Key-Derivation Options JSON Format](hhttps://dicekeys.github.io/seeded-crypto/derivation_options_format.html)
   * as [derivationOptionsJson].
   */
  fun getSealingKey(
    derivationOptionsJson: String,
    callback: Callback<SealingKey>? = null
  ): Intent =
    call(OperationNames.getSealingKey,
      bundleOf(
        ParameterNames.Common.derivationOptionsJson to derivationOptionsJson
      )
    ).also { intent -> callback?.let{ addRequestAndCallback(getPublicKeyCallbacks, intent, it) } }
  /**
   * getPublicKey (same as above) implemented as a Kotlin suspend function
   * in place of callbacks.
   */
  suspend fun getSealingKey(
    derivationOptionsJson: String
  ): SealingKey = awaitCallback{ getSealingKey(
    derivationOptionsJson, it
  ) }

  private val unsealAsymmetricCallbacks = HashMap<String, RequestIntentAndCallback<ByteArray>>()
  /**
   * Unseal (decrypt & authenticate) a message that was previously sealed with a
   * [SealingKey] to construct a [PackagedSealedMessage].
   * The public/private key pair will be re-derived from the user's seed (DiceKey) and the
   * key-derivation options packaged with the message.  It will also ensure that the
   * post-decryption instructions have not changed since the message was packaged.
   *
   * @throws [CryptographicVerificationFailureException]
   */
  fun unsealWithUnsealingKey(
    packagedSealedMessage: PackagedSealedMessage,
    callback: Callback<ByteArray>? = null
  ): Intent =
    call(OperationNames.unsealWithUnsealingKey,
      bundleOf(
        ParameterNames.UnsealingKey.Unseal.packagedSealedMessageSerializedToBinary to packagedSealedMessage.toSerializedBinaryForm()
      )
    ).also { intent -> callback?.let{ addRequestAndCallback(unsealAsymmetricCallbacks, intent, it) } }
  /**
   * unsealWithPrivateKey (same as above) implemented as a Kotlin suspend function
   * in place of callbacks.
   */
  suspend fun unsealWithUnsealingKey(
    packagedSealedMessage: PackagedSealedMessage
  ): ByteArray = awaitCallback{ unsealWithUnsealingKey(
    packagedSealedMessage, it
  ) }


  private val sealWithSymmetricKeyCallbacks = HashMap<String, RequestIntentAndCallback<PackagedSealedMessage>>()
  /**
   * Seal (encrypt with a message-authentication code) a message ([plaintext]) with a
   * symmetric key derived from the user's DiceKey, the
   * [derivationOptionsJson]
   * in [Key-Derivation Options JSON Format](hhttps://dicekeys.github.io/seeded-crypto/derivation_options_format.html),
   * and [PostDecryptionInstructions] specified via a JSON string as
   * [postDecryptionInstructions] in the
   * in [Post-Decryption Instructions JSON Format](https://dicekeys.github.io/seeded-crypto/post_decryption_instructions_format.html).
   */
  fun sealWithSymmetricKey(
    derivationOptionsJson: String,
    plaintext: ByteArray,
    postDecryptionInstructions: String = "",
    callback: Callback<PackagedSealedMessage>
  ): Intent =
    call(OperationNames.sealWithSymmetricKey,
      bundleOf(
        ParameterNames.Common.derivationOptionsJson to derivationOptionsJson,
        ParameterNames.SymmetricKey.Seal.plaintext to plaintext,
        ParameterNames.SymmetricKey.Seal.postDecryptionInstructions to postDecryptionInstructions
      )
    ).also { intent ->
      addRequestAndCallback(sealWithSymmetricKeyCallbacks, intent, callback)
    }

  /**
   * Seal (same as above) implemented as a Kotlin suspend function in place of callbacks.
   */
  suspend fun sealWithSymmetricKey(
    derivationOptionsJson: String,
    plaintext: ByteArray,
    postDecryptionInstructions: String = ""
  ): PackagedSealedMessage = awaitCallback{ sealWithSymmetricKey(
    derivationOptionsJson, plaintext, postDecryptionInstructions, it
  ) }


  private val unsealWithSymmetricKeyCallbacks = HashMap<String, RequestIntentAndCallback<ByteArray>>()
  /**
   * Unseal (decrypt & authenticate) a [packagedSealedMessage] that was previously sealed with a
   * symmetric key derived from the user's DiceKey, the
   * [ApiDerivationOptions] specified in JSON format via [PackagedSealedMessage.derivationOptionsJson],
   * and any [PostDecryptionInstructions] optionally specified by [PackagedSealedMessage.postDecryptionInstructions]
   * in [Post-Decryption Instructions JSON Format](https://dicekeys.github.io/seeded-crypto/post_decryption_instructions_format.html).
   *
   * If any of those strings change, the wrong key will be derive and the message will
   * not be successfully unsealed, yielding a [org.dicekeys.crypto.seeded.CryptographicVerificationFailureException] exception.
   */
  fun unsealWithSymmetricKey(
    packagedSealedMessage: PackagedSealedMessage,
    callback: Callback<ByteArray>? = null
  ): Intent =
    call(OperationNames.unsealWithSymmetricKey,
      bundleOf(
        ParameterNames.SymmetricKey.Unseal.packagedSealedMessageSerializedToBinary to packagedSealedMessage.toSerializedBinaryForm()
      )
    ).also { intent -> callback?.let{ addRequestAndCallback(unsealWithSymmetricKeyCallbacks, intent, it) } }

  private val getSignatureVerificationKeyCallbacks = HashMap<String, RequestIntentAndCallback<SignatureVerificationKey>>()
  /**
   * unsealWithSymmetricKey (same as above) implemented as a Kotlin suspend function
   * in place of callbacks.
   */
  suspend fun unsealWithSymmetricKey(
    packagedSealedMessage: PackagedSealedMessage
  ): ByteArray = awaitCallback{ unsealWithSymmetricKey(
    packagedSealedMessage, it
  ) }

  /**
   * Get a public [SignatureVerificationKey] derived from the user's DiceKey and the
   * [ApiDerivationOptions] specified in JSON format via [derivationOptionsJson]
   */
  fun getSignatureVerificationKey(
    derivationOptionsJson: String,
    callback: Callback<SignatureVerificationKey>? = null
  ): Intent =
    call(OperationNames.getSignatureVerificationKey,
      bundleOf(
        ParameterNames.Common.derivationOptionsJson to derivationOptionsJson
      )
    ).also { intent -> callback?.let{ addRequestAndCallback(getSignatureVerificationKeyCallbacks, intent, it) } }
  /**
   * getSignatureVerificationKey (same as above) implemented as a Kotlin suspend function
   * in place of callbacks.
   */
  suspend fun getSignatureVerificationKey(
    derivationOptionsJson: String
  ): SignatureVerificationKey = awaitCallback{ getSignatureVerificationKey(
    derivationOptionsJson, it
  ) }

  interface GenerateSignatureResult {
    val signature: ByteArray
    val signatureVerificationKey: SignatureVerificationKey
  }
  private val generateSignatureCallbacks = HashMap<String, RequestIntentAndCallback<GenerateSignatureResult>>()
  /**
   * Sign a [message] using a public/private signing key pair derived
   * from the user's DiceKey and the [ApiDerivationOptions] specified in JSON format via
   * [derivationOptionsJson].
   */
  fun generateSignature(
    derivationOptionsJson: String,
    message: ByteArray,
    callback: Callback<GenerateSignatureResult>? = null
  ): Intent =
    call(OperationNames.generateSignature,
      bundleOf(
        ParameterNames.Common.derivationOptionsJson to derivationOptionsJson,
        ParameterNames.SigningKey.GenerateSignature.message to message
      )
    ).also { intent -> callback?.let{ addRequestAndCallback(generateSignatureCallbacks, intent, it) } }
  /**
   * generateSignature (same as above) implemented as a Kotlin suspend function
   * in place of callbacks.
   */
  suspend fun generateSignature(
    derivationOptionsJson: String,
    message: ByteArray
  ): GenerateSignatureResult = awaitCallback{ generateSignature(
    derivationOptionsJson, message, it
  ) }

  private fun <T>handleResult(
    intentAndCallbackMap: HashMap<String, RequestIntentAndCallback<T>>,
    resultIntent: Intent,
    failureHandler: (callback: Callback<T>, originalIntent: Intent, e: Exception) -> Unit,
    successHandler: (callback: Callback<T>, originalIntent: Intent) -> Unit
  ) {
    resultIntent.getStringExtra(ParameterNames.Common.requestId)?.let{ requestId ->
      intentAndCallbackMap[requestId]?.run {
        if (resultIntent.hasExtra(ParameterNames.Common.exception)) {
          failureHandler(
            callback, requestIntent, resultIntent.getSerializableExtra(ParameterNames.Common.exception) as Exception
          )
        } else {
          successHandler(callback, requestIntent)
        }
      }
    }
  }

  fun <T>requireResult(t: T?): T {
    if (t == null) {
      throw InvalidParameterException("A DiceKeys API method failed to return an expected result parameter.")
    } else {
      return t
    }
  }

  /**
   * Activities and Fragments that use this API should implement onActivityResult and
   * and call handleOnActivityResult with the data/intent (third parameter) received.
   * Doing so allows this class to process results returned to the activity/fragment
   * and then call the appropriate callback functions when an API call has either
   * succeeded or failed.
   */
  fun handleOnActivityResult(resultIntent: Intent?) {
    resultIntent?.getStringExtra(ParameterNames.Common.requestId)?.let{ requestId ->
      when(requestIdToCommand(requestId)) {

        OperationNames.getSecret -> handleResult(getSecretCallbacks, resultIntent,
          { callback, _, e -> callback.onException(e) },
          { callback, _ -> callback.onComplete(
            Secret.fromSerializedBinaryForm(requireResult(resultIntent.getByteArrayExtra( ParameterNames.Secret.Get.secretSerializedToBinary))))
          }
        )

        OperationNames.getSealingKey -> handleResult(getPublicKeyCallbacks, resultIntent,
          { callback, _, e -> callback.onException(e) },
          { callback, _ -> callback.onComplete(
            SealingKey.fromSerializedBinaryForm(
              requireResult(resultIntent.getByteArrayExtra(ParameterNames.UnsealingKey.GetSealingKey.sealingKeySerializedToBinary))
            ))
          }
        )

        OperationNames.unsealWithSymmetricKey -> handleResult(unsealWithSymmetricKeyCallbacks, resultIntent,
          { callback, _, e -> callback.onException(e) },
          { callback, _ -> callback.onComplete(
            requireResult(resultIntent.getByteArrayExtra(ParameterNames.SymmetricKey.Unseal.plaintext))
          )
          }
        )

        OperationNames.unsealWithUnsealingKey -> handleResult(unsealAsymmetricCallbacks, resultIntent,
          { callback, _, e -> callback.onException(e) },
          { callback, _ -> callback.onComplete(
            requireResult(resultIntent.getByteArrayExtra(ParameterNames.UnsealingKey.Unseal.plaintext))
          )
          }
        )

        OperationNames.sealWithSymmetricKey -> handleResult(sealWithSymmetricKeyCallbacks, resultIntent,
          { callback, _, e -> callback.onException(e) },
          { callback, _ ->
            resultIntent.getByteArrayExtra(ParameterNames.SymmetricKey.Seal.packagedSealedMessageSerializedToBinary)?.let { sealedMessageJson ->
              callback.onComplete(PackagedSealedMessage.fromSerializedBinaryForm(sealedMessageJson))
            }
          }
        )

        OperationNames.generateSignature -> handleResult(generateSignatureCallbacks, resultIntent,
          { callback, _, e -> callback.onException(e) },
          { callback, _ -> callback.onComplete(object: GenerateSignatureResult {
                  override val signature = requireResult(
                    resultIntent.getByteArrayExtra(ParameterNames.SigningKey.GenerateSignature.signature)
                  )
                  override val signatureVerificationKey =
                    SignatureVerificationKey.fromSerializedBinaryForm(requireResult(
                      resultIntent.getByteArrayExtra(ParameterNames.SigningKey.GenerateSignature.signatureVerificationKeySerializedToBinary)
                  ))
                })
          }
        )

        OperationNames.getSignatureVerificationKey -> handleResult(getSignatureVerificationKeyCallbacks, resultIntent,
          { callback, _, e -> callback.onException(e) },
          { callback, _ -> callback.onComplete(SignatureVerificationKey.fromSerializedBinaryForm(
              requireResult(resultIntent.getByteArrayExtra(
                ParameterNames.SigningKey.GetSignatureVerificationKey.signatureVerificationKeySerializedToBinary
              ))
            ))
          }
        )

        OperationNames.getSigningKey -> handleResult(getSigningKeyCallbacks, resultIntent,
          { callback, _, e -> callback.onException(e) },
          { callback, _ ->
            callback.onComplete(SigningKey.fromSerializedBinaryForm(
              requireResult(
                resultIntent.getByteArrayExtra(
                  ParameterNames.SigningKey.GetSigningKey.signingKeySerializedToBinary
                )
              )
            ))
          })

        OperationNames.getUnsealingKey -> handleResult(getPrivateKeyCallbacks, resultIntent,
          { callback, _, e -> callback.onException(e) },
          { callback, _ ->
            callback.onComplete(UnsealingKey.fromSerializedBinaryForm(requireResult(
              resultIntent.getByteArrayExtra(
                ParameterNames.UnsealingKey.GetUnsealingKey.unsealingKeySerializedToBinary
              )
            )))
          })

        OperationNames.getSymmetricKey -> handleResult(getSymmetricKeyCallbacks, resultIntent,
          { callback, _, e -> callback.onException(e) },
          { callback, _ -> callback.onComplete(SymmetricKey.fromSerializedBinaryForm(requireResult(
            resultIntent.getByteArrayExtra(
              ParameterNames.SymmetricKey.GetKey.symmetricKeySerializedToBinary
            ))))
          })

        else -> {}
      }
    }
  }
}

