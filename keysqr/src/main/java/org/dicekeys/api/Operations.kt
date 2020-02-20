package org.dicekeys.api

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import org.dicekeys.KeyDerivationOptions
import org.dicekeys.readkeysqr.ReadKeySqrActivity
import java.lang.Exception

object Operations {
    object ParameterNames {
        const val ciphertext = "ciphertext"
        const val keyDerivationOptionsJson = "keyDerivationOptionsJson"
        const val plaintext = "plaintext"
        const val postDecryptionInstructionsJson = "postDecryptionInstructionsJson"
        const val publicKeyJson = "publicKeyJson"
        const val seed = "seed"
        const val exception = "exception"
    }

    object Seed {
        const val get = "org.keysqr.api.actions.Seed.get"
    }
    object SymmetricKey {
        const val seal = "org.keysqr.api.actions.SymmetricKey.seal"
        const val unseal = "org.keysqr.api.actions.SymmetricKey.unseal"
    }
    object PublicPrivateKeyPair {
        const val getPublic = "org.keysqr.api.actions.PublicPrivateKeyPair.getPublic"
        const val unseal = "org.keysqr.api.actions.PublicPrivateKeyPair.unseal"
    }

    val All = setOf(
        Seed.get,
        SymmetricKey.seal,
        SymmetricKey.unseal,
        PublicPrivateKeyPair.getPublic,
        PublicPrivateKeyPair.unseal
    )
}


class ClientNotAuthorizeException(
        clientApplicationId: String?,
        authorizedPrefixes: List<String>
): Exception("Client $clientApplicationId is not authorized to generate key as it does not start with one of the following prefixes: ${
    authorizedPrefixes.joinToString(",", "'", "'" )
}")

class ExecuteApiCommandActivity : AppCompatActivity() {
    companion object {
        const val RC_READ_KEYSQR = 1
    }

    private var keyDerivationOptionsJson: String? = null
    private lateinit var keyDerivationOptions: KeyDerivationOptions
    private var restrictToClientApplicationsIdPrefixes: List<String>? = null
    private lateinit var clientsApplicationId: String
    private var keySqrReadActivityStarted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        try {
            // First check if the intended action is a valid command
            if (!Operations.All.contains(intent.action)) {
                throw IllegalArgumentException("Invalid command for DiceKeys API")
            }

            keyDerivationOptionsJson = intent.getStringExtra(Operations.ParameterNames.keyDerivationOptionsJson)
            keyDerivationOptions = KeyDerivationOptions.fromJson(keyDerivationOptionsJson ?: "{}")
            restrictToClientApplicationsIdPrefixes = keyDerivationOptions.restrictToClientApplicationsIdPrefixes
            clientsApplicationId = callingActivity?.packageName ?: ""

            // Next check if this command is permitted
            throwUnlessIntentCommandIsPermitted()

            tryToExecuteIntentsCommand()

        } catch (e: Exception){
            val newIntent = Intent()
            newIntent.putExtra(Operations.ParameterNames.exception, e)
            setResult(Activity.RESULT_CANCELED, newIntent)
            finish()
        }
    }


//    private fun isIntentAValidCommand(): Boolean {
//        return org.dicekeys.api.Operations.All.contains(intent.action)
//    }

    private fun throwUnlessIntentCommandIsPermitted() {
        val clientApplicationsIdPrefixes = restrictToClientApplicationsIdPrefixes
        if (clientApplicationsIdPrefixes != null && clientApplicationsIdPrefixes.isNotEmpty()) {
            // The key derivation options require us to ensure that the client's application/package
            // starts with one of the included prefixes.

            val clientsApplicationIdWithTrailingDot: String =
                if (clientsApplicationId.isEmpty() || clientsApplicationId.lastOrNull() == '.')
                    clientsApplicationId
                else
                    """$clientsApplicationId."""
            val numberOfValidPrefixes = clientApplicationsIdPrefixes.count{ prefix ->
                // FIXME - document that prefixes are assumed to end with "." even if none is provided
                // protect against 'com.dicekeys' prefix being attacked by 'com.dicekeywithsuffixattached'
                val prefixWithTrailingDot: String =
                        if (prefix.isEmpty() || prefix.lastOrNull() == '.')
                            prefix
                        else
                            """$prefix."""
                // we'll append the a dot to the package name to ensure full matches work as well
                return@count clientsApplicationIdWithTrailingDot.startsWith(prefixWithTrailingDot)
            }
            if (numberOfValidPrefixes == 0) {
                // The client application id does not start with any of the specified prefixes
                // throw ClientNotAuthorizeException(clientsApplicationId, clientApplicationsIdPrefixes)
                throw ClientNotAuthorizeException(clientsApplicationId, clientApplicationsIdPrefixes)
            }
        }
    }


    private var requiredUserActionsCompleted: Boolean = false
    private fun triggerUserActionIfRequiredOrReturnTrueIffNoFurtherActionRequired(): Boolean {
        requiredUserActionsCompleted = true
        return requiredUserActionsCompleted
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_READ_KEYSQR) {
            keySqrReadActivityStarted = false
            if (resultCode == Activity.RESULT_OK && data != null) {
                tryToExecuteIntentsCommand()
            }
        }
    }

    private fun tryToExecuteIntentsCommand() {
        try {

            // Call this method each time we have a state update that might allow us to
            // execute the command


            // Next check to see if there is a keysqr available for the requested operation,
            // and if not trigger a new action to get a keysqr
            if (KeySqrState.keySqr == null) {
                // We need to first trigger an action to load the key square, then come back to this
                // intent.
                if (!keySqrReadActivityStarted) {
                    keySqrReadActivityStarted = true
                    val intent = Intent(this, ReadKeySqrActivity::class.java)
                    startActivityForResult(intent, RC_READ_KEYSQR)
                }
                return
            }

            // Next check to see if there's any additional action required to get user consent
            // for the operation
            if (triggerUserActionIfRequiredOrReturnTrueIffNoFurtherActionRequired()) {
                return
            }

            // If all the above checks have passed, we are allowed to execute the command.
            executeIntentsCommand()

        } catch (e: Exception){
            val newIntent = Intent()
            newIntent.putExtra(Operations.ParameterNames.exception, e)
            setResult(Activity.RESULT_CANCELED, newIntent)
            finish()
        }
    }


    private fun executeIntentsCommand() {
        val postDecryptionInstructionsJson = intent.getStringExtra(Operations.ParameterNames.postDecryptionInstructionsJson)
        val resultIntent = Intent()
        when (intent.action) {
            Operations.Seed.get -> {
                // FIXME -- return number of errors in read or if key was manually entered.
                val seed = KeySqrState.keySqr?.getSeed(keyDerivationOptionsJson, clientsApplicationId)
                resultIntent.putExtra(Operations.ParameterNames.seed, seed)
                setResult(RESULT_OK, resultIntent)
            }
            Operations.SymmetricKey.seal -> {
                // FIXME -- validate key read without errors
                val plaintext = intent.getByteArrayExtra(Operations.ParameterNames.plaintext) ?:
                    throw IllegalArgumentException("Seal operation must include plaintext byte array")
                val ciphertext = KeySqrState.keySqr
                        ?.getSymmetricKey(keyDerivationOptionsJson, clientsApplicationId)
                        ?.seal(plaintext, postDecryptionInstructionsJson)
                resultIntent.putExtra(Operations.ParameterNames.ciphertext, ciphertext)
            }
            Operations.SymmetricKey.unseal -> {
                val ciphertext = intent.getByteArrayExtra(Operations.ParameterNames.ciphertext) ?:
                        throw IllegalArgumentException("Seal operation must include ciphertext byte array")
                val plaintext = KeySqrState.keySqr
                        ?.getSymmetricKey(keyDerivationOptionsJson, clientsApplicationId)
                        ?.unseal(ciphertext, postDecryptionInstructionsJson)
                resultIntent.putExtra(Operations.ParameterNames.plaintext, plaintext)
            }
            Operations.PublicPrivateKeyPair.getPublic -> {
                // FIXME -- validate key read without errors
                val publicKeyJson = KeySqrState.keySqr
                        ?.getPublicKey(keyDerivationOptionsJson, clientsApplicationId)
                        ?.toJson()
                resultIntent.putExtra(Operations.ParameterNames.publicKeyJson, publicKeyJson)
            }
            Operations.PublicPrivateKeyPair.unseal -> {
                val ciphertext = intent.getByteArrayExtra(Operations.ParameterNames.ciphertext) ?:
                    throw IllegalArgumentException("Seal operation must include ciphertext byte array")
                val plaintext = KeySqrState.keySqr
                        ?.getPublicPrivateKeyPair(keyDerivationOptionsJson, clientsApplicationId)
                        ?.unseal(ciphertext, postDecryptionInstructionsJson)
                resultIntent.putExtra(Operations.ParameterNames.plaintext, plaintext)
            }
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

}