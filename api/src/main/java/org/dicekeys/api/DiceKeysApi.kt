package org.dicekeys.api

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import org.dicekeys.crypto.seeded.SignatureVerificationKey
import org.dicekeys.crypto.seeded.PublicKey
import java.security.InvalidParameterException
import kotlin.collections.HashMap

/**
 * The DiceKeys [DiceKeysApi] allows client applications to access the DiceKeys app to perform
 * cryptographic operations on their behalf.
 *
 * To use it, call [create] and be sure call [handleOnActivityResult] when you, the consumer,
 * receive onActivityResult callbacks.
 */
abstract class DiceKeysApi(
        private val callingContext: Context
) {
    companion object {
        /**
         * Instantiate the API for a use by an [activity].
         *
         * The calling [activity] will need to implement [android.app.Activity.onActivityResult]
         * and have it call [handleOnActivityResult] so that the API can receive results passed
         * back to the API through inter-process communication.
         */
        fun create(activity: android.app.Activity): DiceKeysApi = object: DiceKeysApi(activity) {
            override fun call(command: String, parameters: Bundle, requestCode: Int): Intent =
                    createIntentForCall(command, parameters).also { intent->
                        activity.startActivityForResult(intent, requestCode)
                    }
        }

        /**
         * Instantiate the API for a use by a [fragment].
         *
         * The calling [fragment] will need to implement [android.app.Activity.onActivityResult]
         * and have it call [handleOnActivityResult] so that the API can receive results passed
         * back to the API through inter-process communication.
         */
        fun create(fragment: Fragment): DiceKeysApi = object: DiceKeysApi(fragment.context ?: throw InvalidParameterException("Fragment must have context")) {
            override fun call(command: String, parameters: Bundle, requestCode: Int): Intent =
                    createIntentForCall(command, parameters).also { intent->
                        fragment.startActivityForResult(intent, requestCode)
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
    protected fun createIntentForCall(
            command: String,
            parameters: Bundle = Bundle()
    ): Intent =
//            Intent(callingContext, ExecuteApiCommandActivity::class.java).apply {
        Intent(command).apply {
                // Component name set with (package name in manifest, fully qualified name)
//            component = ComponentName("org.dicekeys","org.dicekeys.activities.ExecuteApiCommandActivity")
            // FIXME -- will break when we separate out API from
            component = android.content.ComponentName(callingContext.packageName ?: "", "org.dicekeys.activities.ExecuteApiCommandActivity")
                action = command
                val parametersWithRequestId = Bundle(parameters)
                parametersWithRequestId.putString(ParameterNames.Common.requestId, uniqueRequestIdForCommand(command))
                putExtras(parametersWithRequestId)
            }

    /**
     * Since call is handle differently for fragments and for activities, the method that actually
     * triggers calling is abstracted and implemented in the [create] call.
     */
    protected abstract fun call(
            command: String,
            parameters: Bundle = Bundle(),
            requestCode: Int = 0
    ): Intent


    /**
     * Abstracts away the names used to marshall parameters into bundles for requests and response.
     */
    object ParameterNames {
        internal object Reused {
            const val postDecryptionInstructionsJson = "postDecryptionInstructionsJson"
            const val plaintext = "plaintext"
            const val ciphertext = "ciphertext"
            const val signatureVerificationKeyJson = "signatureVerificationKeyJson"

        }

        object Common {
            const val requestId  = "requestId"
            const val keyDerivationOptionsJson = "keyDerivationOptionsJson "
            const val exception = "exception"
        }

        object Seed {
            object Get {
                const val seed = "seed"
            }
        }

        object SymmetricKey {

            object Seal {
                const val plaintext = Reused.plaintext
                const val ciphertext = Reused.ciphertext
                const val postDecryptionInstructionsJson = Reused.postDecryptionInstructionsJson
            }
            object Unseal {
                const val plaintext = Reused.plaintext
                const val ciphertext = Reused.ciphertext
                const val postDecryptionInstructionsJson = Reused.postDecryptionInstructionsJson
            }
        }

        object PrivateKey {
            object GetPublic {
                const val publicKeyJson = "publicKeyJson"
            }

            object Unseal {
                const val plaintext = Reused.plaintext
                const val ciphertext = Reused.ciphertext
                const val postDecryptionInstructionsJson = Reused.postDecryptionInstructionsJson
            }
        }

        object SigningKey {
            object GetSignatureVerificationKey {
                const val signatureVerificationKeyJson = Reused.signatureVerificationKeyJson
            }

            object GenerateSignature {
                const val message = "message"
                const val signature = "signature"
                const val signatureVerificationKeyJson = Reused.signatureVerificationKeyJson
            }
        }

    }

    object OperationNames {
        object UI {
            const val ensureKeyLoaded = "org.dicekeys.api.actions.UI.ensureKeyLoaded"
        }

        object Seed {
            const val get = "org.dicekeys.api.actions.Seed.get"
        }

        object SymmetricKey {
            const val seal = "org.dicekeys.api.actions.SymmetricKey.seal"
            const val unseal = "org.dicekeys.api.actions.SymmetricKey.unseal"
        }

        object PrivateKey {
            const val getPublic = "org.dicekeys.api.actions.PrivateKey.getPublic"
            const val unseal = "org.dicekeys.api.actions.PrivateKey.unseal"
        }

        object SigningKey {
            const val getSignatureVerificationKey = "org.dicekeys.api.actions.SigningKey.getSignatureVerificationKey"
            const val generateSignature = "org.dicekeys.api.actions.SigningKey.generateSignature"
        }

        val All = setOf(
                UI.ensureKeyLoaded,
                Seed.get,
                SymmetricKey.seal,
                SymmetricKey.unseal,
                PrivateKey.getPublic,
                PrivateKey.unseal,
                SigningKey.generateSignature,
                SigningKey.getSignatureVerificationKey
        )
    }

    /**
     * Syntactic sugar so that all callbacks for DiceKeys API calls have the same ancestor
     * and are easier to identify.
     */
    interface DiceKeysApiCallback {}

    /**
     * For each API call, we'll track a map of requestId to the intent used to make the
     * request and to the callbacks the caller registered for the response.
     */
    internal data class RequestIntentAndCallback<T: DiceKeysApiCallback> (
            val requestIntent: Intent,
            val callback: T
    )

    /**
     * This helper function adds the intent and callbacks for a request
     * to a map that can retrieve them based on the requestId.
     */
    private fun <T: DiceKeysApiCallback>addRequestAndCallback(
            map: HashMap<String, RequestIntentAndCallback<T>>,
            intent: Intent,
            callback: T
    ): Unit {
        intent.getStringExtra(ParameterNames.Common.requestId)?.let { requestId ->
            map[requestId] = RequestIntentAndCallback<T>(intent, callback)
        }
    }


    /**
     * Open the DiceKeys app and do not return until the user has loaded in a DiceKey.
     * (The DiceKey will NOT be returned.)
     */
    fun ensureKeyLoaded() {
        call(OperationNames.UI.ensureKeyLoaded)
    }
    interface GetSeedCallback : DiceKeysApiCallback {
        fun onGetSeedSuccess(
                seed: ByteArray,
                originalIntent: Intent
        )
        fun onGetSeedFail(
                exception: Exception,
                originalIntent: Intent
        ) {}
    }
    private val getSeedCallbacks = HashMap<String, RequestIntentAndCallback<GetSeedCallback>>()

    /**
     * Derive a pseudo-random cryptographic seed from the user's DiceKey and
     * the [KeyDerivationOptions] specified in JSON format via [keyDerivationOptionsJson].
     */
    fun getSeed(
            keyDerivationOptionsJson: String,
            callback: GetSeedCallback? = null
    ): Intent =
            call(OperationNames.Seed.get,
                    bundleOf(
                            ParameterNames.Common.keyDerivationOptionsJson to keyDerivationOptionsJson
                    )
            ).also { intent -> callback?.let{ addRequestAndCallback(getSeedCallbacks, intent, it) } }

    interface GetPublicKeyCallback : DiceKeysApiCallback {
        fun onGetPublicKeySuccess(
                publicKey: PublicKey,
                originalIntent: Intent
        )
        fun onGetPublicKeyFail(
                exception: Exception,
                originalIntent: Intent
        ) {}
    }
    private val getPublicKeyCallbacks = HashMap<String, RequestIntentAndCallback<GetPublicKeyCallback>>()

    /**
     * Get a public key derived from the user's DiceKey and the [KeyDerivationOptions] specified
     * in JSON format via [keyDerivationOptionsJson]
     */
    fun getPublicKey(
            keyDerivationOptionsJson: String,
            callback: GetPublicKeyCallback? = null
    ): Intent =
            call(OperationNames.PrivateKey.getPublic,
                    bundleOf(
                            ParameterNames.Common.keyDerivationOptionsJson to keyDerivationOptionsJson
                    )
            ).also { intent -> callback?.let{ addRequestAndCallback(getPublicKeyCallbacks, intent, it) } }

    interface UnsealWithPrivateKeyCallback : DiceKeysApiCallback {
        fun onUnsealAsymmetricSuccess(
                plaintext: ByteArray,
                originalIntent: Intent
        )
        fun onUnsealAsymmetricFail(
                exception: Exception,
                originalIntent: Intent
        ) {}
    }
    private val unsealAsymmetricCallbacks = HashMap<String, RequestIntentAndCallback<UnsealWithPrivateKeyCallback>>()
    /**
     * Unseal (decrypt & authenticate) a message ([ciphertext]) that was previously sealed with a
     * [PublicKey].
     * The public/private key pair will be re-derived from the user's DiceKey, the
     * [KeyDerivationOptions] specified in JSON format via [keyDerivationOptionsJson],
     * and any [PostDecryptionInstructions] optionally specified by [postDecryptionInstructionsJson].
     *
     * If any of those strings change, the wrong key will be derive and the message will
     * not be successfully unsealed, yielding a [CryptographicVerificationFailureException] exception.
     */
    fun unsealWithPrivateKey(
            ciphertext: ByteArray,
            keyDerivationOptionsJson: String = "",
            postDecryptionInstructionsJson: String = "",
            callback: UnsealWithPrivateKeyCallback? = null
    ): Intent =
            call(OperationNames.PrivateKey.unseal,
                    bundleOf(
                            ParameterNames.Common.keyDerivationOptionsJson to keyDerivationOptionsJson,
                            ParameterNames.PrivateKey.Unseal.postDecryptionInstructionsJson to postDecryptionInstructionsJson,
                            ParameterNames.PrivateKey.Unseal.ciphertext to ciphertext
                    )
            ).also { intent -> callback?.let{ addRequestAndCallback(unsealAsymmetricCallbacks, intent, it) } }
    /**
     * Unseal (decrypt & authenticate) a message ([ciphertext]) that was previously sealed with
     * [publicKey].
     * The public/private key pair will be re-derived from the user's DiceKey, the [publicKey]'s
     * [PublicKey.keyDerivationOptionsJson] field.
     *
     * The message-specific key will also be seeded by any [PostDecryptionInstructions] optionally
     * specified by [postDecryptionInstructionsJson].
     *
     * If any of those strings change, the wrong key will be derive and the message will
     * not be successfully unsealed, yielding a [CryptographicVerificationFailureException] exception.
     */
    fun unsealWithPrivateKey(
            ciphertext: ByteArray,
            publicKey: PublicKey,
            postDecryptionInstructionsJson: String = "",
            callback: UnsealWithPrivateKeyCallback? = null
    ): Intent = unsealWithPrivateKey(ciphertext, publicKey.keyDerivationOptionsJson, postDecryptionInstructionsJson, callback)

    fun unsealWithPrivateKey(
            ciphertext: ByteArray,
            keyDerivationOptionsJson: String = "",
            callback: UnsealWithPrivateKeyCallback? = null
    ): Intent = unsealWithPrivateKey(ciphertext, keyDerivationOptionsJson, "", callback)

    fun unsealWithPrivateKey(
            ciphertext: ByteArray,
            publicKey: PublicKey,
            callback: UnsealWithPrivateKeyCallback? = null
    ): Intent = unsealWithPrivateKey(ciphertext, publicKey.keyDerivationOptionsJson, "", callback)

    fun unsealWithPrivateKeyUsingPubicKeyAsJson(
            ciphertext: ByteArray,
            publicKeyJson: String,
            callback: UnsealWithPrivateKeyCallback? = null
    ): Intent =
            unsealWithPrivateKey(ciphertext, PublicKey(publicKeyJson), "", callback)


    interface SealWithSymmetricKeyCallback : DiceKeysApiCallback {
        fun onSealWithSymmetricKeySuccess(
                ciphertext: ByteArray,
                originalIntent: Intent
        )
        fun onSealWithSymmetricKeyFail(
                exception: Exception,
                originalIntent: Intent
        ) {}
    }
    private val sealWithSymmetricKeyCallbacks = HashMap<String, RequestIntentAndCallback<SealWithSymmetricKeyCallback>>()
    /**
     * Seal (encrypt with a message-authentication code) a message ([plaintext]) with a
     * symmetric key derived from the user's DiceKey, the
     * [KeyDerivationOptions] specified in JSON format via [keyDerivationOptionsJson],
     * and [PostDecryptionInstructions] specified via a JSON string as
     * [postDecryptionInstructionsJson].
     */
    fun sealWithSymmetricKey(
            keyDerivationOptionsJson: String,
            plaintext: ByteArray,
            postDecryptionInstructionsJson: String = "",
            callback: SealWithSymmetricKeyCallback? = null
    ): Intent =
            call(OperationNames.SymmetricKey.seal,
                    bundleOf(
                            ParameterNames.Common.keyDerivationOptionsJson to keyDerivationOptionsJson,
                            ParameterNames.SymmetricKey.Seal.plaintext to plaintext,
                            ParameterNames.SymmetricKey.Seal.postDecryptionInstructionsJson to postDecryptionInstructionsJson
                    )
            ).also { intent -> callback?.let{ addRequestAndCallback(sealWithSymmetricKeyCallbacks, intent, it) } }
    /**
     * Seal (encrypt with a message-authentication code) a message ([plaintext]) with a
     * symmetric key derived from the user's DiceKey, the
     * [KeyDerivationOptions] specified in JSON format via [keyDerivationOptionsJson],
     * but without any post-decryption instructions.
     */
    fun sealWithSymmetricKey(
            keyDerivationOptionsJson: String,
            plaintext: ByteArray,
            callback: SealWithSymmetricKeyCallback? = null
    ): Intent = sealWithSymmetricKey(keyDerivationOptionsJson, plaintext, "", callback)

    interface UnsealWithSymmetricKeyCallback : DiceKeysApiCallback {
        fun onUnsealSymmetricSuccess(
                plaintext: ByteArray,
                originalIntent: Intent
        )
        fun onUnsealSymmetricFail(
                exception: Exception,
                originalIntent: Intent
        ) {}
    }
    private val unsealWithSymmetricKeyCallbacks = HashMap<String, RequestIntentAndCallback<UnsealWithSymmetricKeyCallback>>()
    /**
     * Unseal (decrypt & authenticate) a message ([ciphertext]) that was previously sealed with a
     * symmetric key derived from the user's DiceKey, the
     * [KeyDerivationOptions] specified in JSON format via [keyDerivationOptionsJson],
     * and any [PostDecryptionInstructions] optionally specified by [postDecryptionInstructionsJson].
     *
     * If any of those strings change, the wrong key will be derive and the message will
     * not be successfully unsealed, yielding a [CryptographicVerificationFailureException] exception.
     */
    fun unsealWithSymmetricKey(
            keyDerivationOptionsJson: String,
            ciphertext: ByteArray,
            postDecryptionInstructionsJson: String = "",
            callback: UnsealWithSymmetricKeyCallback? = null
    ): Intent =
            call(OperationNames.SymmetricKey.unseal,
                    bundleOf(
                            ParameterNames.Common.keyDerivationOptionsJson to keyDerivationOptionsJson,
                            ParameterNames.SymmetricKey.Seal.ciphertext to ciphertext,
                            ParameterNames.SymmetricKey.Seal.postDecryptionInstructionsJson to postDecryptionInstructionsJson
                    )
            ).also { intent -> callback?.let{ addRequestAndCallback(unsealWithSymmetricKeyCallbacks, intent, it) } }
    /**
     * Unseal (decrypt & authenticate) a message ([ciphertext]) that was previously sealed with a
     * symmetric key derived from the user's DiceKey, the
     * [KeyDerivationOptions] specified in JSON format via [keyDerivationOptionsJson],
     * and without any post-decryption instructions.
     *
     * If any of those strings change, the wrong key will be derive and the message will
     * not be successfully unsealed, yielding a [CryptographicVerificationFailureException] exception.
     */
    fun unsealWithSymmetricKey(
            keyDerivationOptionsJson: String,
            ciphertext: ByteArray,
            callback: UnsealWithSymmetricKeyCallback? = null
    ): Intent = unsealWithSymmetricKey(keyDerivationOptionsJson, ciphertext, "", callback)

    interface GetSignatureVerificationKeyCallback : DiceKeysApiCallback {
        fun onGetSignatureVerificationKeySuccess(
                signatureVerificationKey: SignatureVerificationKey,
                originalIntent: Intent
        )
        fun onGetSignatureVerificationKeyFail(
                exception: Exception,
                originalIntent: Intent
        ) {}
    }
    private val getSignatureVerificationKeyCallbacks = HashMap<String, RequestIntentAndCallback<GetSignatureVerificationKeyCallback>>()

    /**
     * Get a public signature-verification key derived from the user's DiceKey and the
     * [KeyDerivationOptions] specified in JSON format via [keyDerivationOptionsJson]
     */
    fun getSignatureVerificationKey(
            keyDerivationOptionsJson: String,
            callback: GetSignatureVerificationKeyCallback? = null
    ): Intent =
            call(OperationNames.SigningKey.getSignatureVerificationKey,
                    bundleOf(
                            ParameterNames.Common.keyDerivationOptionsJson to keyDerivationOptionsJson
                    )
            ).also { intent -> callback?.let{ addRequestAndCallback(getSignatureVerificationKeyCallbacks, intent, it) } }

    interface GenerateSignatureCallback : DiceKeysApiCallback {
        fun onGenerateSignatureCallbackSuccess(
                signature: ByteArray,
                signatureVerificationKey: SignatureVerificationKey,
                originalIntent: Intent
        )
        fun onGenerateSignatureCallbackFail(
                exception: Exception,
                originalIntent: Intent
        ) {}
    }
    private val generateSignatureCallbacks = HashMap<String, RequestIntentAndCallback<GenerateSignatureCallback>>()
    /**
     * Sign a [message] using a public/private signing key pair derived
     * from the user's DiceKey and the [KeyDerivationOptions] specified in JSON format via
     * [keyDerivationOptionsJson].
     */
    fun generateSignature(
            keyDerivationOptionsJson: String,
            message: ByteArray,
            callback: GenerateSignatureCallback? = null
    ): Intent =
            call(OperationNames.SigningKey.generateSignature,
                    bundleOf(
                            ParameterNames.Common.keyDerivationOptionsJson to keyDerivationOptionsJson,
                            ParameterNames.SigningKey.GenerateSignature.message to message
                    )
            ).also { intent -> callback?.let{ addRequestAndCallback(generateSignatureCallbacks, intent, it) } }


    private fun <T: DiceKeysApiCallback>handleResult(
            intentAndCallbackMap: HashMap<String, RequestIntentAndCallback<T>>,
            resultIntent: Intent,
            failureHandler: (callback: T, originalIntent: Intent, e: Exception) -> Unit,
            successHandler: (callback: T, originalIntent: Intent) -> Unit
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

    /**
     * Activities and Fragments that use this API should implement onActivityResult and
     * and call handleOnActivityResult with the data/intent (third parameter) received.
     * Doing so allows this class to process results returned to the activity/fragment
     * and then call the appropriate callback functions when an API call has either
     * succeeded or failed.
     */
    fun handleOnActivityResult(resultIntent: Intent?) {
        if (resultIntent == null)
            return
        if (!resultIntent.hasExtra(ParameterNames.Common.requestId))
            return
        resultIntent.getStringExtra(ParameterNames.Common.requestId)?.let{ requestId ->
            when(requestIdToCommand(requestId)) {

                OperationNames.Seed.get -> handleResult(getSeedCallbacks, resultIntent,
                        { callback, originalIntent, e -> callback.onGetSeedFail(e, originalIntent) },
                        { callback, originalIntent ->
                            resultIntent.getByteArrayExtra(ParameterNames.Seed.Get.seed)?.let{ seed ->
                                callback.onGetSeedSuccess(seed, originalIntent)
                            }
                        }
                )

                OperationNames.PrivateKey.getPublic -> handleResult(getPublicKeyCallbacks, resultIntent,
                        { callback, originalIntent, e -> callback.onGetPublicKeyFail(e, originalIntent) },
                        { callback, originalIntent ->
                            resultIntent.getStringExtra(ParameterNames.PrivateKey.GetPublic.publicKeyJson)?.let { publicKeyJson ->
                                val publicKey = PublicKey(publicKeyJson)
                                callback.onGetPublicKeySuccess(publicKey, originalIntent)
                            }
                        }
                )

                OperationNames.SymmetricKey.unseal -> handleResult(unsealWithSymmetricKeyCallbacks, resultIntent,
                        { callback, originalIntent, e -> callback.onUnsealSymmetricFail(e, originalIntent) },
                        { callback, originalIntent ->
                            resultIntent.getByteArrayExtra(ParameterNames.SymmetricKey.Unseal.plaintext)?.let { plaintext ->
                                callback.onUnsealSymmetricSuccess(plaintext, originalIntent)
                            }
                        }
                )

                OperationNames.PrivateKey.unseal -> handleResult(unsealAsymmetricCallbacks, resultIntent,
                        { callback, originalIntent, e -> callback.onUnsealAsymmetricFail(e, originalIntent) },
                        { callback, originalIntent ->
                            resultIntent.getByteArrayExtra(ParameterNames.PrivateKey.Unseal.plaintext)?.let { plaintext ->
                                callback.onUnsealAsymmetricSuccess(plaintext, originalIntent)
                            }
                        }
                )

                OperationNames.SymmetricKey.seal -> handleResult(sealWithSymmetricKeyCallbacks, resultIntent,
                        { callback, originalIntent, e -> callback.onSealWithSymmetricKeyFail(e, originalIntent) },
                        { callback, originalIntent ->
                            resultIntent.getByteArrayExtra(ParameterNames.SymmetricKey.Seal.ciphertext)?.let { ciphertext ->
                                callback.onSealWithSymmetricKeySuccess(ciphertext, originalIntent)
                            }
                        }
                )

                OperationNames.SigningKey.generateSignature -> handleResult(generateSignatureCallbacks, resultIntent,
                        { callback, originalIntent, e -> callback.onGenerateSignatureCallbackFail(e, originalIntent) },
                        { callback, originalIntent ->
                            resultIntent.getByteArrayExtra(ParameterNames.SigningKey.GenerateSignature.signature)?.let { signature ->
                            resultIntent.getByteArrayExtra(ParameterNames.SigningKey.GenerateSignature.signatureVerificationKeyJson)?.let { signatureVerificationKeyJson ->
                                callback.onGenerateSignatureCallbackSuccess(
                                        signature,
                                        SignatureVerificationKey(signatureVerificationKeyJson),
                                        originalIntent)
                            }}
                        }
                )

                OperationNames.SigningKey.getSignatureVerificationKey -> handleResult(getSignatureVerificationKeyCallbacks, resultIntent,
                        { callback, originalIntent, e -> callback.onGetSignatureVerificationKeyFail(e, originalIntent) },
                        { callback, originalIntent ->
                            resultIntent.getStringExtra(ParameterNames.SigningKey.GetSignatureVerificationKey.signatureVerificationKeyJson)?.let{ signatureVerificationKeyJson ->
                                val signatureVerificationKey = SignatureVerificationKey(signatureVerificationKeyJson)
                                callback.onGetSignatureVerificationKeySuccess(signatureVerificationKey, originalIntent)
                            }
                        }
                )


                else -> {}
            }
        }
    }
}