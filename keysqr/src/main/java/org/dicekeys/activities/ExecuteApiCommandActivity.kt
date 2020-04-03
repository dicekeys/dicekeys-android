package org.dicekeys.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.dicekeys.api.DiceKeysApi
import org.dicekeys.faces.Face
import org.dicekeys.api.KeyDerivationOptions
import org.dicekeys.KeySqr
import org.dicekeys.state.KeySqrState
import org.dicekeys.readkeysqr.ReadKeySqrActivity


class ExecuteApiCommandActivity : AppCompatActivity() {

    private var keyDerivationOptionsJson: String? = null
    private lateinit var clientsApplicationId: String
    private var keySqrReadActivityStarted: Boolean = false
    private var requestId: String = ""

    override fun onCreate(savedInstanceState: Bundle? //, persistentState: PersistableBundle?
    ) {
        super.onCreate(savedInstanceState) // , persistentState)
        setContentView(org.dicekeys.R.layout.activity_execute_api_command)

        try {
            if (!intent.hasExtra(DiceKeysApi.ParameterNames.Common.requestId)) {
                throw IllegalArgumentException("Command must include a requestId")
            }
            requestId = intent.getStringExtra(DiceKeysApi.ParameterNames.Common.requestId) ?:
                    throw IllegalArgumentException("Command must include a requestId")

            // First check if the intended action is a valid command
            if (!DiceKeysApi.OperationNames.All.contains(intent.action)) {
                throw IllegalArgumentException("Invalid command for DiceKeys API")
            }

            keyDerivationOptionsJson = intent.getStringExtra(DiceKeysApi.ParameterNames.Common.keyDerivationOptionsJson)
            // Note, this will throw exceptions if the JSON is invalid
            clientsApplicationId = callingActivity?.packageName ?: ""

            // Next check if this command is permitted
            throwUnlessIntentCommandIsPermitted()

            tryToExecuteIntentsCommand()

        } catch (e: Exception){
            val newIntent = Intent()
            newIntent.putExtra(DiceKeysApi.ParameterNames.Common.requestId, requestId)
            newIntent.putExtra(DiceKeysApi.ParameterNames.Common.exception, e)
            setResult(Activity.RESULT_CANCELED, newIntent)
            finish()
        }
    }


//    private fun isIntentAValidCommand(): Boolean {
//        return org.dicekeys.activities.Operations.All.contains(intent.action)
//    }

    private fun throwUnlessIntentCommandIsPermitted() {
        if (intent.action == DiceKeysApi.OperationNames.UI.ensureKeyLoaded) {
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
                throw org.dicekeys.api.CliendPackagetNotAuthorizedException(clientsApplicationId, clientApplicationsIdPrefixes)
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
            newIntent.putExtra(DiceKeysApi.ParameterNames.Common.requestId, requestId)
            newIntent.putExtra(DiceKeysApi.ParameterNames.Common.exception, e)
            setResult(Activity.RESULT_CANCELED, newIntent)
            finish()
        }
    }


    private fun executeIntentsCommand(keySqr: KeySqr<Face>) {
        val resultIntent = Intent()
        resultIntent.putExtra(DiceKeysApi.ParameterNames.Common.requestId, requestId)
        when (intent.action) {
            DiceKeysApi.OperationNames.UI.ensureKeyLoaded -> {
                val intent = Intent(this, DisplayDiceKeyActivity::class.java)
                startActivity(intent)
            }
            DiceKeysApi.OperationNames.Seed.get -> {
                // FIXME -- return number of errors in read or if key was manually entered.
                val seed = keySqr.getSeed(keyDerivationOptionsJson, clientsApplicationId)
                resultIntent.putExtra(DiceKeysApi.ParameterNames.Seed.Get.seed, seed)
                setResult(RESULT_OK, resultIntent)
            }
            DiceKeysApi.OperationNames.SymmetricKey.seal -> {
                // FIXME -- validate key read without errors
                val plaintext = intent.getByteArrayExtra(DiceKeysApi.ParameterNames.SymmetricKey.Seal.plaintext) ?:
                    throw IllegalArgumentException("Seal operation must include plaintext byte array")
                val postDecryptionInstructionsJson = intent.getStringExtra(DiceKeysApi.ParameterNames.SymmetricKey.Seal.postDecryptionInstructionsJson) ?: ""

                val ciphertext = keySqr
                        .getSymmetricKey(keyDerivationOptionsJson, clientsApplicationId)
                        .seal(plaintext, postDecryptionInstructionsJson)
                resultIntent.putExtra(DiceKeysApi.ParameterNames.SymmetricKey.Seal.ciphertext, ciphertext)
            }
            DiceKeysApi.OperationNames.SymmetricKey.unseal -> {
                val ciphertext = intent.getByteArrayExtra(DiceKeysApi.ParameterNames.SymmetricKey.Unseal.ciphertext) ?:
                        throw IllegalArgumentException("Seal operation must include ciphertext byte array")
                val postDecryptionInstructionsJson = intent.getStringExtra(DiceKeysApi.ParameterNames.SymmetricKey.Unseal.postDecryptionInstructionsJson) ?: ""
                val plaintext = keySqr
                        .getSymmetricKey(keyDerivationOptionsJson, clientsApplicationId)
                        .unseal(ciphertext, postDecryptionInstructionsJson)
                resultIntent.putExtra(DiceKeysApi.ParameterNames.SymmetricKey.Unseal.plaintext, plaintext)
            }
            DiceKeysApi.OperationNames.PublicPrivateKeyPair.getPublic -> {
                // FIXME -- validate key read without errors
                val publicKeyJson = keySqr
                        .getPublicKey(keyDerivationOptionsJson, clientsApplicationId)
                        .toJson()
                resultIntent.putExtra(DiceKeysApi.ParameterNames.PublicPrivateKeyPair.GetPublic.publicKeyJson, publicKeyJson)
            }
            DiceKeysApi.OperationNames.PublicPrivateKeyPair.unseal -> {
                val ciphertext = intent.getByteArrayExtra(DiceKeysApi.ParameterNames.PublicPrivateKeyPair.Unseal.ciphertext) ?:
                    throw IllegalArgumentException("Seal operation must include ciphertext byte array")
                val postDecryptionInstructionsJson = intent.getStringExtra(DiceKeysApi.ParameterNames.PublicPrivateKeyPair.Unseal.postDecryptionInstructionsJson) ?: ""

                val plaintext = keySqr
                        .getPublicPrivateKeyPair(keyDerivationOptionsJson, clientsApplicationId)
                        .unseal(ciphertext, postDecryptionInstructionsJson)
                resultIntent.putExtra(DiceKeysApi.ParameterNames.PublicPrivateKeyPair.Unseal.plaintext, plaintext)
            }

            DiceKeysApi.OperationNames.SigningKey.getSignatureVerificationKey -> {
                val signatureVerificationKeyJson: String = keySqr
                        .getSignatureVerificationKey(keyDerivationOptionsJson, clientsApplicationId)
                        .toJson()
                resultIntent.putExtra(DiceKeysApi.ParameterNames.SigningKey.GetSignatureVerificationKey.signatureVerificationKeyJson, signatureVerificationKeyJson)
            }

            DiceKeysApi.OperationNames.SigningKey.generateSignature -> {
                val message = intent.getByteArrayExtra(DiceKeysApi.ParameterNames.SigningKey.GenerateSignature.message) ?:
                    throw IllegalArgumentException("Seal operation must include message  byte array")
                val signingKey = keySqr
                        .getSigningKey(keyDerivationOptionsJson, clientsApplicationId)
                val signature = signingKey.generateSignature(message)
                val signatureVerificationKeyJson: String = signingKey.getSignatureVerificationKey().toJson()
                resultIntent.putExtra(DiceKeysApi.ParameterNames.SigningKey.GenerateSignature.signature, signature)
                resultIntent.putExtra(DiceKeysApi.ParameterNames.SigningKey.GenerateSignature.signatureVerificationKeyJson, signatureVerificationKeyJson)
            }
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

}