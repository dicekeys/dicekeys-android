package org.dicekeys.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import org.dicekeys.Api
import org.dicekeys.ClientNotAuthorizeException
import org.dicekeys.Face
import org.dicekeys.KeyDerivationOptions
import org.dicekeys.KeySqr
import org.dicekeys.state.KeySqrState
import org.dicekeys.readkeysqr.ReadKeySqrActivity


class ExecuteApiCommandActivity : AppCompatActivity() {

    private var keyDerivationOptionsJson: String? = null
    private lateinit var clientsApplicationId: String
    private var keySqrReadActivityStarted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle? //, persistentState: PersistableBundle?
    ) {
        super.onCreate(savedInstanceState) // , persistentState)
        setContentView(org.dicekeys.R.layout.activity_execute_api_command)

        try {
            // First check if the intended action is a valid command
            if (!Api.OperationNames.All.contains(intent.action)) {
                throw IllegalArgumentException("Invalid command for DiceKeys API")
            }

            keyDerivationOptionsJson = intent.getStringExtra(Api.ParameterNames.keyDerivationOptionsJson)
            // Note, this will throw exceptions if the JSON is invalid
            clientsApplicationId = callingActivity?.packageName ?: ""

            // Next check if this command is permitted
            throwUnlessIntentCommandIsPermitted()

            tryToExecuteIntentsCommand()

        } catch (e: Exception){
            val newIntent = Intent()
            newIntent.putExtra(Api.ParameterNames.exception, e)
            setResult(Activity.RESULT_CANCELED, newIntent)
            finish()
        }
    }


//    private fun isIntentAValidCommand(): Boolean {
//        return org.dicekeys.activities.Operations.All.contains(intent.action)
//    }

    private fun throwUnlessIntentCommandIsPermitted() {
        if (intent.action == Api.OperationNames.UI.ensureKeyLoaded) {
            // You are always allowed to transfer control to the app
            return
        }

        val keyDerivationOptions: KeyDerivationOptions =
                KeyDerivationOptions.fromJson(keyDerivationOptionsJson ?: "{}")

        val clientApplicationsIdPrefixes = keyDerivationOptions.restrictToClientApplicationsIdPrefixes
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
        // if (requestCode == RC_READ_KEYSQR) {
            keySqrReadActivityStarted = false
            if (resultCode == Activity.RESULT_OK && data != null) {
                tryToExecuteIntentsCommand()
            }
        // }
    }

    private fun tryToExecuteIntentsCommand() {
        try {

            // Call this method each time we have a state update that might allow us to
            // execute the command


            // Next check to see if there is a keysqr available for the requested operation,
            // and if not trigger a new action to get a keysqr
            val keySqr = KeySqrState.keySqr
            if (keySqr == null) {
                // We need to first trigger an action to load the key square, then come back to this
                // intent.
                if (!keySqrReadActivityStarted) {
                    keySqrReadActivityStarted = true
                    val intent = Intent(this, ReadKeySqrActivity::class.java)
                    startActivityForResult(intent, 0)
                }
                return
            }

            // Next check to see if there's any additional action required to get user consent
            // for the operation
            if (!triggerUserActionIfRequiredOrReturnTrueIffNoFurtherActionRequired()) {
                return
            }

            // If all the above checks have passed, we are allowed to execute the command.
            executeIntentsCommand(keySqr)

        } catch (e: Exception){
            val newIntent = Intent()
            newIntent.putExtra(Api.ParameterNames.exception, e)
            setResult(Activity.RESULT_CANCELED, newIntent)
            finish()
        }
    }


    private fun executeIntentsCommand(keySqr: KeySqr<Face>) {
        val postDecryptionInstructionsJson = intent.getStringExtra(Api.ParameterNames.postDecryptionInstructionsJson)
        val resultIntent = Intent()
        when (intent.action) {
            Api.OperationNames.UI.ensureKeyLoaded -> {
                val intent = Intent(this, DisplayDiceKeyActivity::class.java)
                startActivity(intent)
            }
            Api.OperationNames.Seed.get -> {
                // FIXME -- return number of errors in read or if key was manually entered.
                val seed = keySqr.getSeed(keyDerivationOptionsJson, clientsApplicationId)
                resultIntent.putExtra(Api.ParameterNames.seed, seed)
                setResult(RESULT_OK, resultIntent)
            }
            Api.OperationNames.SymmetricKey.seal -> {
                // FIXME -- validate key read without errors
                val plaintext = intent.getByteArrayExtra(Api.ParameterNames.plaintext) ?:
                    throw IllegalArgumentException("Seal operation must include plaintext byte array")
                val ciphertext = keySqr
                        .getSymmetricKey(keyDerivationOptionsJson, clientsApplicationId)
                        .seal(plaintext, postDecryptionInstructionsJson)
                resultIntent.putExtra(Api.ParameterNames.ciphertext, ciphertext)
            }
            Api.OperationNames.SymmetricKey.unseal -> {
                val ciphertext = intent.getByteArrayExtra(Api.ParameterNames.ciphertext) ?:
                        throw IllegalArgumentException("Seal operation must include ciphertext byte array")
                val plaintext = keySqr
                        .getSymmetricKey(keyDerivationOptionsJson, clientsApplicationId)
                        .unseal(ciphertext, postDecryptionInstructionsJson)
                resultIntent.putExtra(Api.ParameterNames.plaintext, plaintext)
            }
            Api.OperationNames.PublicPrivateKeyPair.getPublic -> {
                // FIXME -- validate key read without errors
                val publicKeyJson = keySqr
                        .getPublicKey(keyDerivationOptionsJson, clientsApplicationId)
                        .toJson()
                resultIntent.putExtra(Api.ParameterNames.publicKeyJson, publicKeyJson)
            }
            Api.OperationNames.PublicPrivateKeyPair.unseal -> {
                val ciphertext = intent.getByteArrayExtra(Api.ParameterNames.ciphertext) ?:
                    throw IllegalArgumentException("Seal operation must include ciphertext byte array")
                val plaintext = keySqr
                        .getPublicPrivateKeyPair(keyDerivationOptionsJson, clientsApplicationId)
                        .unseal(ciphertext, postDecryptionInstructionsJson)
                resultIntent.putExtra(Api.ParameterNames.plaintext, plaintext)
            }
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

}