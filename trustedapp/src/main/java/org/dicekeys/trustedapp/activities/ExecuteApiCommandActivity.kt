package org.dicekeys.trustedapp.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.dicekeys.trustedapp.R
import org.dicekeys.keysqr.FaceRead
import org.dicekeys.trustedapp.apicommands.permissionchecked.*

class ExecuteApiCommandActivity : AppCompatActivity() {
  private var keySqrReadActivityStarted: Boolean = false

  class AwaitingUsersPermissionToUnseal(message: String? = null) :
    PermissionCheckedMarshalledCommands.AwaitingFurtherAction(message)

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

  private var postDecryptionInstructionsMessageApproved: String? = null
  private var postDecryptionInstructionsMessageRejected: String? = null
  private fun warningHandler(message: String): Boolean {
    return when (message) {
      postDecryptionInstructionsMessageApproved -> true
      postDecryptionInstructionsMessageRejected -> false
      else -> {
        // Need to start the warning activity
        throw NotImplementedError()
        throw AwaitingUsersPermissionToUnseal()
      }
    }
  }

  private fun executeIntentsCommand() {
    // Get a permission-checked accessor for obtaining seeds from the DiceKey.
    // If the user doesn't have a DiceKey loaded into memory yet, this will trigger
    // a load request, return null, and cause this function to return null.
    // When a key is loaded, this function will be called again.
    val permissionCheckedSeedAccessor =
      PermissionCheckedSeedAccessor.createForIntentApi(
        this) {
        warningHandler(it)
      } ?: return

    // Our API commands don't get a copy of the raw DiceKey seed, but only an accessor
    // which must be passed parameters to check.
    val intentMarshalledApi = PermissionCheckedIntentCommands(
      permissionCheckedSeedAccessor, this
    )

    intentMarshalledApi.executeCommand()
  }
}