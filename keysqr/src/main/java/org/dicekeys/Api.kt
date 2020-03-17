package org.dicekeys

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import org.dicekeys.activities.ExecuteApiCommandActivity
import org.dicekeys.keys.PublicKey
import java.security.InvalidParameterException

class ActivityApi(private val callingActivity: AppCompatActivity) : Api(callingActivity) {
    protected override fun call(command: String, parameters: Bundle, requestCode: Int): Intent {
        val intent = super.call(command, parameters, requestCode)
        callingActivity.startActivityForResult(intent, requestCode)
        return intent
    }
}

open class Api(
    private val callingContext: Context
) {

    private fun createIntentForCall(
            command: String,
            parameters: Bundle = Bundle()
    ): Intent =
        Intent(callingContext, ExecuteApiCommandActivity::class.java).apply {
            action = command
            putExtras(parameters)
        }

    protected open fun call(
        command: String,
        parameters: Bundle = Bundle(),
        requestCode: Int = 0
    ): Intent =  createIntentForCall(command, parameters)

    enum class ParameterNames {
        ciphertext,
        keyDerivationOptionsJson,
        message,
        plaintext,
        postDecryptionInstructionsJson,
        publicKeyJson,
        seed,
        signatureVerificationKeyJson,
        signature,
        exception,
        originalAction
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



    fun ensureKeyLoaded(callingActivity: AppCompatActivity) {
        call(OperationNames.UI.ensureKeyLoaded)
    }

    fun getSeed(
            keyDerivationOptionsJson: String
    ): Intent =
        call(OperationNames.Seed.get,
            bundleOf(
                    ParameterNames.keyDerivationOptionsJson.name to keyDerivationOptionsJson
            )
        )

    fun getPublicKey(
            keyDerivationOptionsJson: String
    ): Intent =
        call(OperationNames.PublicPrivateKeyPair.getPublic,
            bundleOf(
                    ParameterNames.keyDerivationOptionsJson.name to keyDerivationOptionsJson
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


    fun unsealWithPrivateKeyUsingPostDecryptionInstructions(
            ciphertext: ByteArray,
            keyDerivationOptionsJson: String = "",
            postDecryptionInstructionsJson: String = ""
    ): Intent =
        call(OperationNames.PublicPrivateKeyPair.unseal,
                bundleOf(
                        ParameterNames.keyDerivationOptionsJson.name to keyDerivationOptionsJson,
                        ParameterNames.postDecryptionInstructionsJson.name to postDecryptionInstructionsJson,
                        ParameterNames.ciphertext.name to ciphertext
                )
        )

    fun unsealUsingPubicKey(
            ciphertext: ByteArray,
            publicKey: PublicKey
    ): Intent =
        call(OperationNames.PublicPrivateKeyPair.unseal,
                bundleOf(
                        ParameterNames.keyDerivationOptionsJson.name to publicKey.jsonKeyDerivationOptions,
                        ParameterNames.ciphertext.name to ciphertext
                )
        )

    fun unsealUsingPubicKeyJson(
            ciphertext: ByteArray,
            publicKeyJson: String
    ): Intent = PublicKey.fromJson(publicKeyJson)?.jsonKeyDerivationOptions?.let { jsonKeyDerivationOptions ->
            call(OperationNames.PublicPrivateKeyPair.unseal,
                    bundleOf(
                            ParameterNames.keyDerivationOptionsJson.name to jsonKeyDerivationOptions,
                            ParameterNames.ciphertext.name to ciphertext
                    )
            )
        } ?: throw InvalidParameterException("Invalid public key JSON")

    fun sealWithSymmetricKey(
            keyDerivationOptionsJson: String,
            plaintext: ByteArray,
            postDecryptionInstructionsJson: String = ""
    ): Intent =
        call(OperationNames.PublicPrivateKeyPair.unseal,
                bundleOf(
                        ParameterNames.keyDerivationOptionsJson.name to keyDerivationOptionsJson,
                        ParameterNames.plaintext.name to plaintext,
                        ParameterNames.postDecryptionInstructionsJson.name to postDecryptionInstructionsJson
                )
        )

    fun unsealWithSymmetricKey(
            keyDerivationOptionsJson: String,
            ciphertext: ByteArray,
            postDecryptionInstructionsJson: String = ""
    ): Intent =
        call(OperationNames.PublicPrivateKeyPair.unseal,
                bundleOf(
                        ParameterNames.keyDerivationOptionsJson.name to keyDerivationOptionsJson,
                        ParameterNames.ciphertext.name to ciphertext,
                        ParameterNames.postDecryptionInstructionsJson.name to postDecryptionInstructionsJson
                )
        )

    fun getSignatureVerificationKey(
            keyDerivationOptionsJson: String
    ): Intent =
            call(OperationNames.SigningKey.getSignatureVerificationKey,
                    bundleOf(
                            ParameterNames.keyDerivationOptionsJson.name to keyDerivationOptionsJson
                    )
            )

    fun generateSignature(
            keyDerivationOptionsJson: String,
            message: ByteArray
    ): Intent =
            call(OperationNames.SigningKey.generateSignature,
                    bundleOf(
                            ParameterNames.keyDerivationOptionsJson.name to keyDerivationOptionsJson,
                            ParameterNames.message.name to message
                    )
            )
}