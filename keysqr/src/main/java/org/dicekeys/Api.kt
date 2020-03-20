package org.dicekeys

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import org.dicekeys.activities.ExecuteApiCommandActivity
import org.dicekeys.keys.PublicKey
import org.dicekeys.keys.SignatureVerificationKey
import java.security.InvalidParameterException
import kotlin.collections.HashMap

open class ActivityApi(private val activity: android.app.Activity) : Api(activity) {
    protected override fun call(command: String, parameters: Bundle, requestCode: Int): Intent =
        super.call(command, parameters, requestCode).also { intent->
            activity.startActivityForResult(intent, requestCode)
        }
}

class FragmentApi(private val fragment: Fragment) : Api(
    fragment.context ?: throw InvalidParameterException("Fragment must have context")
) {
    protected override fun call(command: String, parameters: Bundle, requestCode: Int): Intent =
        super.call(command, parameters, requestCode).also { intent->
            fragment.startActivityForResult(intent, requestCode)
        }
}

open class Api(
    private val callingContext: Context
) {

    private fun uniqueRequestIdForCommand(command: String) = "$command:${java.util.UUID.randomUUID()}"

    private fun requestIdToCommand(requestId: String) =
        requestId.substringBefore(':', "")

    private fun createIntentForCall(
            command: String,
            parameters: Bundle = Bundle()
    ): Intent =
        Intent(callingContext, ExecuteApiCommandActivity::class.java).apply {
            action = command
            val parametersWithRequestId = Bundle(parameters)
            parametersWithRequestId.putString(ParameterNames.Global.requestId, uniqueRequestIdForCommand(command))
            putExtras(parametersWithRequestId)
        }


    protected open fun call(
        command: String,
        parameters: Bundle = Bundle(),
        requestCode: Int = 0
    ): Intent =  createIntentForCall(command, parameters)


    object ParameterNames {

        internal const val _postDecryptionInstructionsJson = "postDecryptionInstructionsJson"

        object Global {
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
            const val plaintext = "plaintext"
            const val ciphertext = "ciphertext"
            const val postDecryptionInstructionsJson = _postDecryptionInstructionsJson
        }

        object PublicPrivateKeyPair {
            object GetPublic {
                const val publicKeyJson = "publicKeyJson"
            }

            object Unseal {
                const val plaintext = "plaintext"
                const val ciphertext = "ciphertext"
                const val postDecryptionInstructionsJson = _postDecryptionInstructionsJson
            }
        }

        object SigningKey {
            object GetSignatureVerificationKey {
                const val signatureVerificationKeyJson = "signatureVerificationKeyJson"
            }

            object GenerateSignature {
                const val message = "message"
                const val signature = "signature"
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

        object PublicPrivateKeyPair {
            const val getPublic = "org.dicekeys.api.actions.PublicPrivateKeyPair.getPublic"
            const val unseal = "org.dicekeys.api.actions.PublicPrivateKeyPair.unseal"
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
            PublicPrivateKeyPair.getPublic,
            PublicPrivateKeyPair.unseal,
            SigningKey.generateSignature,
            SigningKey.getSignatureVerificationKey
        )
    }

    interface DiceKeysApiCallback {
    }
    data class IntentAndCallback<T: DiceKeysApiCallback> (
        val originalIntent: Intent,
        val callback: T
    )

    private fun <T: DiceKeysApiCallback>addCallback(
        map: HashMap<String, IntentAndCallback<T>>,
        intent: Intent,
        callback: T
    ): Unit {
        intent.getStringExtra(ParameterNames.Global.requestId)?.let { requestId ->
            map[requestId] = IntentAndCallback<T>(intent, callback)
        }
    }


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
    private val getSeedCallbacks = HashMap<String, IntentAndCallback<GetSeedCallback>>()
    fun getSeed(
            keyDerivationOptionsJson: String,
            callback: GetSeedCallback? = null
    ): Intent =
        call(OperationNames.Seed.get,
            bundleOf(
                ParameterNames.Global.keyDerivationOptionsJson to keyDerivationOptionsJson
            )
        ).also { intent -> callback?.let{ addCallback(getSeedCallbacks, intent, it) } }

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
    private val getPublicKeyCallbacks = HashMap<String, IntentAndCallback<GetPublicKeyCallback>>()
    fun getPublicKey(
            keyDerivationOptionsJson: String
    ): Intent =
        call(OperationNames.PublicPrivateKeyPair.getPublic,
            bundleOf(
                ParameterNames.Global.keyDerivationOptionsJson to keyDerivationOptionsJson
            )
        )

    fun sealWithPublicKey(
        plaintext: ByteArray,
        publicKey: PublicKey,
        postDecryptionInstructionsJson: String = ""
    ): ByteArray {
        return publicKey.seal(plaintext, postDecryptionInstructionsJson)
    }

    fun sealWithPublicKey(
            plaintext: ByteArray,
            publicKeyJson: String,
            postDecryptionInstructionsJson: String = ""
    ): ByteArray =
        PublicKey.fromJson(publicKeyJson)?.let { publicKey ->
            sealWithPublicKey(plaintext, publicKey, postDecryptionInstructionsJson)
        } ?: throw InvalidParameterException("Invalid public key JSON")

    interface UnsealAsymmetricCallback : DiceKeysApiCallback {
        fun onUnsealAsymmetricSuccess(
                plaintext: ByteArray,
                originalIntent: Intent
        )
        fun onUnsealAsymmetricFail(
                exception: Exception,
                originalIntent: Intent
        ) {}
    }
    private val unsealAsymmetricCallbacks = HashMap<String, IntentAndCallback<UnsealAsymmetricCallback>>()
    fun unsealAsymmetric(
            ciphertext: ByteArray,
            keyDerivationOptionsJson: String = "",
            postDecryptionInstructionsJson: String = ""
    ): Intent =
        call(OperationNames.PublicPrivateKeyPair.unseal,
                bundleOf(
                        ParameterNames.Global.keyDerivationOptionsJson to keyDerivationOptionsJson,
                        ParameterNames.PublicPrivateKeyPair.Unseal.postDecryptionInstructionsJson to postDecryptionInstructionsJson,
                        ParameterNames.PublicPrivateKeyPair.Unseal.ciphertext to ciphertext
                )
        )

    fun unsealAsymmetric(
            ciphertext: ByteArray,
            publicKey: PublicKey,
            postDecryptionInstructionsJson: String = ""
    ): Intent =
        call(OperationNames.PublicPrivateKeyPair.unseal,
                bundleOf(
                        ParameterNames.Global.keyDerivationOptionsJson to publicKey.jsonKeyDerivationOptions,
                        ParameterNames.PublicPrivateKeyPair.Unseal.postDecryptionInstructionsJson to postDecryptionInstructionsJson,
                        ParameterNames.PublicPrivateKeyPair.Unseal.ciphertext to ciphertext
                )
        )

    fun unsealAsymmetricWithPubicKeyAsJson(
            ciphertext: ByteArray,
            publicKeyJson: String,
            postDecryptionInstructionsJson: String = ""
    ): Intent = PublicKey.fromJson(publicKeyJson)?.jsonKeyDerivationOptions?.let { jsonKeyDerivationOptions ->
            call(OperationNames.PublicPrivateKeyPair.unseal,
                    bundleOf(
                            ParameterNames.Global.keyDerivationOptionsJson to jsonKeyDerivationOptions,
                            ParameterNames.PublicPrivateKeyPair.Unseal.postDecryptionInstructionsJson to postDecryptionInstructionsJson,
                            ParameterNames.PublicPrivateKeyPair.Unseal.ciphertext to ciphertext
                    )
            )
        } ?: throw InvalidParameterException("Invalid public key JSON")

    interface SealCallback : DiceKeysApiCallback {
        fun onSealSuccess(
            ciphertext: ByteArray,
            originalIntent: Intent
        )
        fun onSealFail(
            exception: Exception,
            originalIntent: Intent
        ) {}
    }
    private val sealCallbacks = HashMap<String, IntentAndCallback<SealCallback>>()
    fun sealWithSymmetricKey(
            keyDerivationOptionsJson: String,
            plaintext: ByteArray,
            postDecryptionInstructionsJson: String = ""
    ): Intent =
        call(OperationNames.SymmetricKey.seal,
                bundleOf(
                        ParameterNames.Global.keyDerivationOptionsJson to keyDerivationOptionsJson,
                        ParameterNames.SymmetricKey.plaintext to plaintext,
                        ParameterNames.SymmetricKey.postDecryptionInstructionsJson to postDecryptionInstructionsJson
                )
        )

    interface UnsealSymmetricCallback : DiceKeysApiCallback {
        fun onUnsealSymmetricSuccess(
                plaintext: ByteArray,
                originalIntent: Intent
        )
        fun onUnsealSymmetricFail(
                exception: Exception,
                originalIntent: Intent
        ) {}
    }
    private val unsealSymmetricCallbacks = HashMap<String, IntentAndCallback<UnsealSymmetricCallback>>()
    fun unsealWithSymmetricKey(
            keyDerivationOptionsJson: String,
            ciphertext: ByteArray,
            postDecryptionInstructionsJson: String = ""
    ): Intent =
        call(OperationNames.SymmetricKey.unseal,
                bundleOf(
                        ParameterNames.Global.keyDerivationOptionsJson to keyDerivationOptionsJson,
                        ParameterNames.SymmetricKey.ciphertext to ciphertext,
                        ParameterNames.SymmetricKey.postDecryptionInstructionsJson to postDecryptionInstructionsJson
                )
        )

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
    private val getSignatureVerificationKeyCallbacks = HashMap<String, IntentAndCallback<GetSignatureVerificationKeyCallback>>()
    fun getSignatureVerificationKey(
            keyDerivationOptionsJson: String
    ): Intent =
            call(OperationNames.SigningKey.getSignatureVerificationKey,
                    bundleOf(
                            ParameterNames.Global.keyDerivationOptionsJson to keyDerivationOptionsJson
                    )
            )

    interface GenerateSignatureCallback : DiceKeysApiCallback {
        fun onGenerateSignatureCallbackSuccess(
                signature: ByteArray,
                originalIntent: Intent
        )
        fun onGenerateSignatureCallbackFail(
                exception: Exception,
                originalIntent: Intent
        ) {}
    }
    private val generateSignatureCallbacks = HashMap<String, IntentAndCallback<GenerateSignatureCallback>>()
    fun generateSignature(
            keyDerivationOptionsJson: String,
            message: ByteArray
    ): Intent =
            call(OperationNames.SigningKey.generateSignature,
                    bundleOf(
                            ParameterNames.Global.keyDerivationOptionsJson to keyDerivationOptionsJson,
                            ParameterNames.SigningKey.GenerateSignature.message to message
                    )
            )


    private fun <T: DiceKeysApiCallback>handleResult(
            intentAndCallbackMap: HashMap<String, IntentAndCallback<T>>,
            resultIntent: Intent,
            failureHandler: (callback: T, originalIntent: Intent, e: Exception) -> Unit,
            successHandler: (callback: T, originalIntent: Intent) -> Unit
    ) {
        resultIntent.getStringExtra(ParameterNames.Global.requestId)?.let{ requestId ->
            intentAndCallbackMap[requestId]?.run {
                if (resultIntent.hasExtra(ParameterNames.Global.exception)) {
                    failureHandler(
                            callback, originalIntent, resultIntent.getSerializableExtra(ParameterNames.Global.exception) as Exception
                    )
                } else {
                    successHandler(callback, originalIntent)
                }
            }
        }
    }

    fun handleOnActivityResult(resultIntent: Intent?) {
        if (resultIntent == null)
            return
        if (!resultIntent.hasExtra(ParameterNames.Global.requestId))
            return
        resultIntent.getStringExtra(ParameterNames.Global.requestId)?.let{ requestId ->
            when(requestIdToCommand(requestId)) {

                OperationNames.Seed.get -> handleResult(getSeedCallbacks, resultIntent,
                        { callback, originalIntent, e -> callback.onGetSeedFail(e, originalIntent) },
                        { callback, originalIntent ->
                            resultIntent.getByteArrayExtra(ParameterNames.Seed.Get.seed)?.let{ seed ->
                                callback.onGetSeedSuccess(seed, originalIntent)
                            }
                        }
                )

                OperationNames.PublicPrivateKeyPair.getPublic -> handleResult(getPublicKeyCallbacks, resultIntent,
                        { callback, originalIntent, e -> callback.onGetPublicKeyFail(e, originalIntent) },
                        { callback, originalIntent ->
                            resultIntent.getStringExtra(ParameterNames.PublicPrivateKeyPair.GetPublic.publicKeyJson)?.let { publicKeyJson ->
                                val publicKey = PublicKey.fromJsonOrThrow(publicKeyJson)
                                callback.onGetPublicKeySuccess(publicKey, originalIntent)
                            }
                        }
                )

                OperationNames.SymmetricKey.unseal -> handleResult(unsealSymmetricCallbacks, resultIntent,
                        { callback, originalIntent, e -> callback.onUnsealSymmetricFail(e, originalIntent) },
                        { callback, originalIntent ->
                            resultIntent.getByteArrayExtra(ParameterNames.SymmetricKey.plaintext)?.let { plaintext ->
                                callback.onUnsealSymmetricSuccess(plaintext, originalIntent)
                            }
                        }
                )

                OperationNames.PublicPrivateKeyPair.unseal -> handleResult(unsealAsymmetricCallbacks, resultIntent,
                        { callback, originalIntent, e -> callback.onUnsealAsymmetricFail(e, originalIntent) },
                        { callback, originalIntent ->
                            resultIntent.getByteArrayExtra(ParameterNames.PublicPrivateKeyPair.Unseal.plaintext)?.let { plaintext ->
                                callback.onUnsealAsymmetricSuccess(plaintext, originalIntent)
                            }
                        }
                )

                OperationNames.SymmetricKey.seal -> handleResult(sealCallbacks, resultIntent,
                        { callback, originalIntent, e -> callback.onSealFail(e, originalIntent) },
                        { callback, originalIntent ->
                            resultIntent.getByteArrayExtra(ParameterNames.SymmetricKey.ciphertext)?.let { ciphertext ->
                                callback.onSealSuccess(ciphertext, originalIntent)
                            }
                        }
                )

                OperationNames.SigningKey.generateSignature -> handleResult(generateSignatureCallbacks, resultIntent,
                        { callback, originalIntent, e -> callback.onGenerateSignatureCallbackFail(e, originalIntent) },
                        { callback, originalIntent ->
                            resultIntent.getByteArrayExtra(ParameterNames.SigningKey.GenerateSignature.signature)?.let { signature ->
                                callback.onGenerateSignatureCallbackSuccess(signature, originalIntent)
                            }
                        }
                )

                OperationNames.SigningKey.getSignatureVerificationKey -> handleResult(getSignatureVerificationKeyCallbacks, resultIntent,
                        { callback, originalIntent, e -> callback.onGetSignatureVerificationKeyFail(e, originalIntent) },
                        { callback, originalIntent ->
                            resultIntent.getStringExtra(ParameterNames.SigningKey.GetSignatureVerificationKey.signatureVerificationKeyJson)?.let{ signatureVerificationKeyJson ->
                                val signatureVerificationKey = SignatureVerificationKey.fromJsonOrThrow(signatureVerificationKeyJson)
                                callback.onGetSignatureVerificationKeySuccess(signatureVerificationKey, originalIntent)
                            }
                        }
                )


                else -> {}
            }
        }
    }
}