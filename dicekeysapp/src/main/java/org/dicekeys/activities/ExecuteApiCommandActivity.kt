package org.dicekeys.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.dicekeys.api.DiceKeysApiClient
import org.dicekeys.keysqr.Face
import org.dicekeys.keysqr.KeySqr
import org.dicekeys.api.ClientMayNotRetrieveKeyException
import org.dicekeys.api.ApiKeyDerivationOptions
import org.dicekeys.api.ClientPackageNotAuthorizedException
import org.dicekeys.crypto.seeded.PackagedSealedMessage
import org.dicekeys.crypto.seeded.SymmetricKey
import org.dicekeys.crypto.seeded.PrivateKey
import org.dicekeys.dicekeysapp.R
import org.dicekeys.keysqr.FaceRead
import org.dicekeys.state.KeySqrState
import org.dicekeys.read.ReadKeySqrActivity


class ExecuteApiCommandActivity : AppCompatActivity() {

    private lateinit var clientsApplicationId: String
    private var keySqrReadActivityStarted: Boolean = false
    private var requestId: String = ""

    override fun onCreate(savedInstanceState: Bundle? //, persistentState: PersistableBundle?
    ) {
        super.onCreate(savedInstanceState) // , persistentState)
        setContentView(R.layout.activity_execute_api_command)

        try {
            if (!intent.hasExtra(DiceKeysApiClient.ParameterNames.Common.requestId)) {
                throw IllegalArgumentException("Command must include a requestId")
            }
            requestId = intent.getStringExtra(DiceKeysApiClient.ParameterNames.Common.requestId) ?:
                    throw IllegalArgumentException("Command must include a requestId")

            // First check if the intended action is a valid command
            if (!DiceKeysApiClient.OperationNames.All.contains(intent.action)) {
                throw IllegalArgumentException("Invalid command for DiceKeys API")
            }

            clientsApplicationId = callingActivity?.packageName ?: ""

            tryToExecuteIntentsCommand()

        } catch (e: Exception){
            val newIntent = Intent()
            newIntent.putExtra(DiceKeysApiClient.ParameterNames.Common.requestId, requestId)
            newIntent.putExtra(DiceKeysApiClient.ParameterNames.Common.exception, e)
            setResult(Activity.RESULT_CANCELED, newIntent)
            finish()
        }
    }


//    private fun isIntentAValidCommand(): Boolean {
//        return org.dicekeys.activities.Operations.All.contains(intent.action)
//    }
//
    /**
     * Ensure any non-empty string ends in a "." by appending one if necessary
     */
    private fun terminateWithDot(prefix: String): String =
        if (prefix.isEmpty() || prefix.lastOrNull() == '.')
            prefix
        else
            "${prefix}."

    // protect against 'com.dicekeys' prefix being attacked by 'com.dicekeywithsuffixattached'
    // we'll append the a dot to the package name to ensure full matches work as well
    /**
     * Verify that either no Android prefixes were specified, or that the
     */
    private fun throwUnlessIntentCommandIsPermitted(
        keyDerivationOptions: ApiKeyDerivationOptions
    ): Unit {
        keyDerivationOptions.restrictions?.androidPackagePrefixesAllowed.let { androidPackagePrefixesAllowed ->
            // The key derivation options require us to ensure that the client's application/package
            // starts with one of the included prefixes.
            val clientsApplicationIdWithTrailingDot: String = terminateWithDot(clientsApplicationId)
            if (androidPackagePrefixesAllowed == null ||
                androidPackagePrefixesAllowed.none{ prefix ->
                    clientsApplicationIdWithTrailingDot.startsWith(terminateWithDot(prefix))
            }) {
                // The client application id does not start with any of the specified prefixes
                throw ClientPackageNotAuthorizedException(clientsApplicationId, androidPackagePrefixesAllowed)
            }
        }
    }
    private fun throwUnlessIntentCommandIsPermitted(
        keyDerivationOptionsJson: String
    ): Unit = throwUnlessIntentCommandIsPermitted(ApiKeyDerivationOptions(keyDerivationOptionsJson))

    private var requiredUserActionsCompleted: Boolean = false
    private fun triggerUserActionIfRequiredOrReturnTrueIffNoFurtherActionRequired(): Boolean {
        requiredUserActionsCompleted = true
        return requiredUserActionsCompleted
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        keySqrReadActivityStarted = false
        if (
            resultCode == Activity.RESULT_OK &&
            data != null &&
            data.hasExtra("keySqrAsJson")
        ) {
            data.getStringExtra("keySqrAsJson")?.let { keySqrAsJson ->
                FaceRead.keySqrFromJsonFacesRead(keySqrAsJson)?.let { keySqr ->
                    KeySqrState.setKeySquareRead(keySqr)
                    tryToExecuteIntentsCommand()
                }
            }
        }
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
                    // FIXME -- must add result to state
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
            newIntent.putExtra(DiceKeysApiClient.ParameterNames.Common.requestId, requestId)
            newIntent.putExtra(DiceKeysApiClient.ParameterNames.Common.exception, e)
            setResult(Activity.RESULT_CANCELED, newIntent)
            finish()
        }
    }


    private fun executeIntentsCommand(keySqr: KeySqr<Face>) {
        val resultIntent = Intent()
        resultIntent.putExtra(DiceKeysApiClient.ParameterNames.Common.requestId, requestId)
        when (intent.action) {
            DiceKeysApiClient.OperationNames.Seed.get -> {
                // FIXME -- return number of errors in read or if key was manually entered.
                val keyDerivationOptionsJson: String = intent.getStringExtra(DiceKeysApiClient.ParameterNames.Common.keyDerivationOptionsJson) ?: "{}"
                throwUnlessIntentCommandIsPermitted(keyDerivationOptionsJson)
                val seed = keySqr.getSeed(keyDerivationOptionsJson)
                resultIntent.putExtra(DiceKeysApiClient.ParameterNames.Seed.Get.seedJson, seed.toJson())
                setResult(RESULT_OK, resultIntent)
            }
            DiceKeysApiClient.OperationNames.SymmetricKey.seal -> {
                // FIXME -- validate key read without errors
                val keyDerivationOptionsJson: String = intent.getStringExtra(DiceKeysApiClient.ParameterNames.Common.keyDerivationOptionsJson) ?: "{}"
                throwUnlessIntentCommandIsPermitted(keyDerivationOptionsJson)
                val plaintext = intent.getByteArrayExtra(DiceKeysApiClient.ParameterNames.SymmetricKey.Seal.plaintext) ?:
                    throw IllegalArgumentException("Seal operation must include plaintext byte array")
                val postDecryptionInstructionsJson = intent.getStringExtra(DiceKeysApiClient.ParameterNames.SymmetricKey.Seal.postDecryptionInstructionsJson) ?: ""

                val sealedMessagePackage = keySqr
                    .getSymmetricKey(keyDerivationOptionsJson)
                    .seal(plaintext, postDecryptionInstructionsJson)
                resultIntent.putExtra(DiceKeysApiClient.ParameterNames.SymmetricKey.Seal.packagedSealedMessageSerializedToBinary, sealedMessagePackage.toSerializedBinaryForm())
            }
            DiceKeysApiClient.OperationNames.SymmetricKey.unseal -> {
                val packagedSealedMessage = PackagedSealedMessage.fromSerializedBinaryForm(
                        intent.getByteArrayExtra(DiceKeysApiClient.ParameterNames.SymmetricKey.Unseal.packagedSealedMessageSerializedToBinary) ?:
                        throw IllegalArgumentException("Unseal operation must include packagedSealedMessageSerializedToBinary")
                )
                throwUnlessIntentCommandIsPermitted(packagedSealedMessage.keyDerivationOptionsJson)
                val plaintext: ByteArray = SymmetricKey.unseal(
                    packagedSealedMessage,
                    keySqr.toKeySeed(packagedSealedMessage.keyDerivationOptionsJson)
                )
                resultIntent.putExtra(DiceKeysApiClient.ParameterNames.SymmetricKey.Unseal.plaintext, plaintext)
            }
            DiceKeysApiClient.OperationNames.PrivateKey.getPublic -> {
                // FIXME -- validate key read without errors
                val keyDerivationOptionsJson: String = intent.getStringExtra(DiceKeysApiClient.ParameterNames.Common.keyDerivationOptionsJson) ?: "{}"
                throwUnlessIntentCommandIsPermitted(keyDerivationOptionsJson)
                val publicKey = keySqr.getPublicKey(keyDerivationOptionsJson)
                resultIntent.putExtra(
                    DiceKeysApiClient.ParameterNames.PrivateKey.GetPublic.publicKeySerializedToBinary,
                    publicKey.toSerializedBinaryForm()
                )
            }
            DiceKeysApiClient.OperationNames.PrivateKey.unseal -> {
                val packagedSealedMessage = PackagedSealedMessage.fromSerializedBinaryForm(
                        intent.getByteArrayExtra(DiceKeysApiClient.ParameterNames.SymmetricKey.Unseal.packagedSealedMessageSerializedToBinary) ?:
                        throw IllegalArgumentException("Unseal operation must include packagedSealedMessageSerializedToBinary")
                )
                throwUnlessIntentCommandIsPermitted(packagedSealedMessage.keyDerivationOptionsJson)
                val plaintext : ByteArray = PrivateKey.unseal(
                    keySqr.toKeySeed(packagedSealedMessage.keyDerivationOptionsJson),
                    packagedSealedMessage
                )
                resultIntent.putExtra(DiceKeysApiClient.ParameterNames.PrivateKey.Unseal.plaintext, plaintext)
            }

            DiceKeysApiClient.OperationNames.SigningKey.getSignatureVerificationKey -> {
                val keyDerivationOptionsJson: String = intent.getStringExtra(DiceKeysApiClient.ParameterNames.Common.keyDerivationOptionsJson) ?: "{}"
                throwUnlessIntentCommandIsPermitted(keyDerivationOptionsJson)
                val signatureVerificationKey = keySqr
                        .getSignatureVerificationKey(keyDerivationOptionsJson)
                resultIntent.putExtra(
                        DiceKeysApiClient.ParameterNames.SigningKey.GetSignatureVerificationKey.signatureVerificationKeySerializedToBinary,
                        signatureVerificationKey.toSerializedBinaryForm())
            }

            DiceKeysApiClient.OperationNames.SigningKey.generateSignature -> {
                val keyDerivationOptionsJson: String = intent.getStringExtra(DiceKeysApiClient.ParameterNames.Common.keyDerivationOptionsJson) ?: "{}"
                throwUnlessIntentCommandIsPermitted(keyDerivationOptionsJson)
                val message = intent.getByteArrayExtra(DiceKeysApiClient.ParameterNames.SigningKey.GenerateSignature.message) ?:
                    throw IllegalArgumentException("Seal operation must include message byte array")
                val signingKey = keySqr
                        .getSigningKey(keyDerivationOptionsJson)
                val signature = signingKey.generateSignature(message)
                val signatureVerificationKey = signingKey.getSignatureVerificationKey()
                resultIntent.putExtra(DiceKeysApiClient.ParameterNames.SigningKey.GenerateSignature.signature, signature)
                resultIntent.putExtra(
                    DiceKeysApiClient.ParameterNames.SigningKey.GenerateSignature.signatureVerificationKeySerializedToBinary,
                    signatureVerificationKey.toSerializedBinaryForm()
                )
            }
            DiceKeysApiClient.OperationNames.PrivateKey.getPrivate -> {
                // FIXME -- validate key read without errors
                val keyDerivationOptionsJson: String = intent.getStringExtra(DiceKeysApiClient.ParameterNames.Common.keyDerivationOptionsJson) ?: "{}"
                val kdo = ApiKeyDerivationOptions(keyDerivationOptionsJson)
                throwUnlessIntentCommandIsPermitted(kdo)
                if (!kdo.clientMayRetrieveKey) {
                    throw ClientMayNotRetrieveKeyException("Private")
                }
                val privateKey = keySqr.getPrivateKey(keyDerivationOptionsJson)
                resultIntent.putExtra(
                        DiceKeysApiClient.ParameterNames.PrivateKey.GetPrivate.privateKeySerializedToBinary,
                        privateKey.toSerializedBinaryForm()
                )
            }
            DiceKeysApiClient.OperationNames.SigningKey.getSigningKey -> {
                // FIXME -- validate key read without errors
                val keyDerivationOptionsJson: String = intent.getStringExtra(DiceKeysApiClient.ParameterNames.Common.keyDerivationOptionsJson) ?: "{}"
                val kdo = ApiKeyDerivationOptions(keyDerivationOptionsJson)
                throwUnlessIntentCommandIsPermitted(kdo)
                if (!kdo.clientMayRetrieveKey) {
                    throw ClientMayNotRetrieveKeyException("Signing")
                }
                val signingKey = keySqr.getSigningKey(keyDerivationOptionsJson)
                resultIntent.putExtra(
                        DiceKeysApiClient.ParameterNames.SigningKey.GetSigningKey.signingKeySerializedToBinary,
                        signingKey.toSerializedBinaryForm()
                )
            }

            DiceKeysApiClient.OperationNames.SymmetricKey.getKey -> {
                // FIXME -- validate key read without errors
                val keyDerivationOptionsJson: String = intent.getStringExtra(DiceKeysApiClient.ParameterNames.Common.keyDerivationOptionsJson) ?: "{}"
                val kdo = ApiKeyDerivationOptions(keyDerivationOptionsJson)
                throwUnlessIntentCommandIsPermitted(kdo)
                if (!kdo.clientMayRetrieveKey) {
                    throw ClientMayNotRetrieveKeyException("Symmetric")
                }
                val symmetricKey = keySqr.getSymmetricKey(keyDerivationOptionsJson)
                resultIntent.putExtra(
                        DiceKeysApiClient.ParameterNames.SymmetricKey.GetKey.symmetricKeySerializedToBinary,
                        symmetricKey.toSerializedBinaryForm()
                )
            }
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

}