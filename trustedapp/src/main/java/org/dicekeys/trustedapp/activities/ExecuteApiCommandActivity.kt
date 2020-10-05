package org.dicekeys.trustedapp.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import org.dicekeys.api.UnsealingInstructions.RequestForUsersConsent
import org.dicekeys.trustedapp.R
import org.dicekeys.dicekey.FaceRead
import org.dicekeys.dicekey.SimpleDiceKey
import org.dicekeys.read.ReadDiceKeyActivity
import org.dicekeys.trustedapp.apicommands.permissionchecked.*
import java.lang.Exception
import java.util.*

class ExecuteApiCommandActivity : AppCompatActivity() {

  override fun onCreate(
    savedInstanceState: Bundle? //, persistentState: PersistableBundle?
  ) {
    super.onCreate(savedInstanceState) // , persistentState)
    setContentView(R.layout.activity_execute_api_command)
    if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
      executeWebApiCommand()
    } else {
      executeIntentApiCommand()
    }
  }

  /**
   * When a DiceKey is needed, a new activity will be spawned to load it and the
   * result will be sent via a call to this method.
   */
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (resultCode == Activity.RESULT_CANCELED) {
      // This wasn't a failed or successful warning, or a successful load of a DiceKey,
      // so by deduction the remaining possibility is that we failed to load a DiceKey
      // FIXME with better exception
      loadDiceKeyCompletableDeferred?.completeExceptionally(Exception("Failed to read DiceKey"))
    }
    // If we've gotten this far, this must be the result of trying to read in the DiceKey
    data?.getStringExtra(ReadDiceKeyActivity.Companion.Parameters.Response.diceKeyAsJson)?.let { diceKeyAsJson ->
      FaceRead.diceKeyFromJsonFacesRead(diceKeyAsJson)?.let { diceKey ->
        // Complete the deferred load operation
        val simpleDiceKey = SimpleDiceKey(diceKey.faces)
        loadDiceKeyCompletableDeferred?.complete(simpleDiceKey)
        return@onActivityResult
      }
    }
  }

  /**
   * Ask the user for consent to unseal a message if the UnsealingInstructions
   * encountered on an `unseal` operation require the user's consent
   */
  private fun requestUsersConsentAsync(
    requestForUsersConsent: RequestForUsersConsent
  ): Deferred<RequestForUsersConsent.UsersResponse> =
    CompletableDeferred<RequestForUsersConsent.UsersResponse>().also {
      deferredDialogResult -> runOnUiThread {
        AlertDialog.Builder(this)
          .setMessage(requestForUsersConsent.question)
          .setPositiveButton(requestForUsersConsent.actionButtonLabels.allow) { _, _ ->
            deferredDialogResult.complete(RequestForUsersConsent.UsersResponse.Allow)
          }
          .setNegativeButton(requestForUsersConsent.actionButtonLabels.deny) { _, _ ->
            deferredDialogResult.complete(RequestForUsersConsent.UsersResponse.Deny)
          }
          .create()
          .show()
      }
  }

  /**
   * Start an action to load in the user's DiceKey so that the requested operation can
   * be performed.
   */
  private var loadDiceKeyCompletableDeferred : CompletableDeferred<SimpleDiceKey>? = null
  private fun loadDiceKeyAsync(): Deferred<SimpleDiceKey> =
    loadDiceKeyCompletableDeferred ?: (
       CompletableDeferred<SimpleDiceKey>().also { completableDeferred ->
        val requestId = UUID.randomUUID().toString()
        loadDiceKeyCompletableDeferred = completableDeferred
        // Start the load activity
        startActivityForResult(Intent(this, ReadDiceKeyActivity::class.java).apply{
          this.putExtra( ReadDiceKeyActivity.Companion.Parameters.Response.diceKeyAsJson, requestId)
        }, 0)
        completableDeferred.invokeOnCompletion {
          // When this completes, no new threads should start waiting on it
          loadDiceKeyCompletableDeferred = null
        }
      }
    )

  /**
   * Execute the API request specified via an intent.
   */
  private fun executeIntentApiCommand() {
    // Get a permission-checked accessor for obtaining seeds from the DiceKey.
    // If the user doesn't have a DiceKey loaded into memory yet, this will trigger
    // a load request, return null, and cause this function to return null.
    // When a key is loaded, this function will be called again.
    // Our API commands don't get a copy of the raw DiceKey seed, but only an accessor
    // which must be passed parameters to check.
    val api = PermissionCheckedIntentCommands(
      this, ::loadDiceKeyAsync, ::requestUsersConsentAsync
    )

    GlobalScope.launch {
      // Start the suspendable command in its own thread
      api.executeCommand()
    }
  }

  private fun executeWebApiCommand() {
    // Our API commands don't get a copy of the raw DiceKey seed, but only an accessor
    // which must be passed parameters to check.
    val api = PermissionCheckedUrlCommands(
      intent.data!!, ::loadDiceKeyAsync, ::requestUsersConsentAsync, this
    )
    GlobalScope.launch {
      // Start the suspendable command in its own thread
      api.executeCommand()
    }
  }
}