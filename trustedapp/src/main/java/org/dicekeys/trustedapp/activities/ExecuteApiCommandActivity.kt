package org.dicekeys.trustedapp.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.dicekeys.api.*
import org.dicekeys.trustedapp.R
import org.dicekeys.keysqr.FaceRead
import org.dicekeys.trustedapp.apicommands.permissionchecked.PermissionCheckedCommands
import org.dicekeys.trustedapp.apicommands.permissionchecked.PermissionCheckedIntentCommands
import org.dicekeys.trustedapp.apicommands.permissionchecked.PermissionCheckedSeedAccessor

class ExecuteApiCommandActivity : AppCompatActivity() {
  private var keySqrReadActivityStarted: Boolean = false

  override fun onCreate(
    savedInstanceState: Bundle? //, persistentState: PersistableBundle?
  ) {
    super.onCreate(savedInstanceState) // , persistentState)
    setContentView(R.layout.activity_execute_api_command)
    executeIntentsCommand()
  }

  /**
   * Handle results of activities to
   *   * Load a DiceKey into memory if it isn't there when the command is executed
   *   * Ask the user to respond to a warning
   */
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
          org.dicekeys.trustedapp.state.KeySqrState.setKeySquareRead(keySqr)
          executeIntentsCommand()
        }
      }
    }

    // FIXME -- handle if result of warning dialog activity
  }


  /**
   * All API calls return values by calling this method and passing a function that
   * populates its extra fields with results.
   */
  private fun returnIntent(fn: (intent: Intent) -> Any) {
    setResult(RESULT_OK, Intent().apply{
      putExtra(DiceKeysApiClient.ParameterNames.Common.requestId,
        intent.getStringExtra(DiceKeysApiClient.ParameterNames.Common.requestId)
      )
      fn(this)
    })
    finish()
  }

  private fun returnError(fn: (intent: Intent) -> Any) {
    setResult(Activity.RESULT_CANCELED, Intent().apply{
      putExtra(DiceKeysApiClient.ParameterNames.Common.requestId,
        intent.getStringExtra(DiceKeysApiClient.ParameterNames.Common.requestId)
      )
      fn(this)
    })
    finish()
  }


  private var postDecryptionInstructionsMessageApproved: String? = null
  private var postDecryptionInstructionsMessageRejected: String? = null
  private fun warningHandler(message: String): Boolean? {
    if (message == postDecryptionInstructionsMessageApproved)
      return true
    else if (message == postDecryptionInstructionsMessageRejected)
      return false
    else {
      // Need to start the warning activity
      throw NotImplementedError()
    }
  }

  private fun executeIntentsCommand() {
    try {
      // First check if the intended action is a valid command
      if (!DiceKeysApiClient.OperationNames.All.contains(intent.action)) {
        throw IllegalArgumentException("Invalid command for DiceKeys API")
      }

      // Get a permission-checked accessor for obtaining seeds from the DiceKey.
      // If the user doesn't have a DiceKey loaded into memory yet, this will trigger
      // a load request, return null, and cause this function to return null.
      // When a key is loaded, this function will be called again.
      val permissionCheckedSeedAccessor = PermissionCheckedSeedAccessor.create(this){
        warningHandler(it)
      } ?: return

      // Our API commands don't get a copy of the raw DiceKey seed, but only an accessor
      // which must be passed parameters to check.
      val apiCommandsWithPermissionChecks = PermissionCheckedCommands(permissionCheckedSeedAccessor)
      val intentMarshalledApi = PermissionCheckedIntentCommands(
        apiCommandsWithPermissionChecks,
        intent
      ) { intent ->
        returnIntent(intent)
      }
      with(intentMarshalledApi){
        when (intent.action) {
          DiceKeysApiClient.OperationNames.Secret.get -> getSecret()
          DiceKeysApiClient.OperationNames.SymmetricKey.seal -> sealWithSymmetricKey()
          DiceKeysApiClient.OperationNames.SymmetricKey.unseal -> unsealWithSymmetricKey()
          DiceKeysApiClient.OperationNames.PrivateKey.getPublic -> getPublicKey()
          DiceKeysApiClient.OperationNames.PrivateKey.unseal -> unsealWithPrivateKey()
          DiceKeysApiClient.OperationNames.SigningKey.getSignatureVerificationKey -> getSignatureVerificationKey()
          DiceKeysApiClient.OperationNames.SigningKey.generateSignature ->generateSignature()
          DiceKeysApiClient.OperationNames.PrivateKey.getPrivate -> getPrivate()
          DiceKeysApiClient.OperationNames.SigningKey.getSigningKey -> getSigningKey()
          DiceKeysApiClient.OperationNames.SymmetricKey.getKey -> getSymmetricKey()
          else -> {
            throw IllegalArgumentException("Invalid command for DiceKeys API")
          }
        }
      }
    } catch (e: Exception) {
      returnError { resultIntent ->
        resultIntent.putExtra(DiceKeysApiClient.ParameterNames.Common.exception, e)
      }
    }
  }

}