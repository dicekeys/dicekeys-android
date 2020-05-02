package org.dicekeys.trustedapp.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import org.dicekeys.keysqr.DiceKey
import org.dicekeys.trustedapp.R
import org.dicekeys.keysqr.FaceRead
import org.dicekeys.keysqr.KeySqr
import org.dicekeys.read.ReadKeySqrActivity
import org.dicekeys.trustedapp.apicommands.permissionchecked.*
import java.lang.Exception
import java.util.*

class ExecuteApiCommandActivity : AppCompatActivity() {

  override fun onCreate(
    savedInstanceState: Bundle? //, persistentState: PersistableBundle?
  ) {
    super.onCreate(savedInstanceState) // , persistentState)
    setContentView(R.layout.activity_execute_api_command)
    executeIntentsCommand()

  }

  private val requestIdParameterName = "requestId"
  /**
   * Handle results of activities to
   *   * Load a DiceKey into memory if it isn't there when the command is executed
   *   * Ask the user to respond to a warning
   */
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (data == null)
      return
    if (data.hasExtra(requestIdParameterName)) {
      data.getStringExtra(requestIdParameterName)?.let { requestId ->
        deferredWarnings.get(requestId)?.let { deferredWarning ->
          // Handle the result
          if (resultCode == Activity.RESULT_CANCELED) {
            // FIXME with better exception
            deferredWarning.completeExceptionally(Exception("Warning message cancelled"))
          }
          if (resultCode == Activity.RESULT_OK) {
            val usersAnswer = data.getBooleanExtra("usersAnswer", false)
            deferredWarning.complete(usersAnswer)
          }
          return@onActivityResult
        }
      }
    }
    // If we've gotten this far, this must be the result of trying to read in the DiceKey
    data.getStringExtra("keySqrAsJson")?.let { keySqrAsJson ->
      FaceRead.keySqrFromJsonFacesRead(keySqrAsJson)?.let { keySqr ->
        // Complete the deferred load operation
        val diceKey = DiceKey(keySqr.faces)
        loadDiceKeyCompletableDeferred?.complete(diceKey)
        return@onActivityResult
      }
    }
    if (resultCode == Activity.RESULT_CANCELED) {
      // This wasn't a failed or successful warning, or a successful load of a DiceKey,
      // so by deduction the remaining possibility is that we failed to load a DiceKey
      // FIXME with better exception
      loadDiceKeyCompletableDeferred?.completeExceptionally(Exception("Failed to read DiceKey"))
    }
  }

  private var deferredWarnings = mutableMapOf<String,CompletableDeferred<Boolean>>()
  private fun warningHandler(message: String): Deferred<Boolean> {
    val requestId = UUID.randomUUID().toString()
    val deferredResult = CompletableDeferred<Boolean>().apply {
      // Register this handler into the mapping of requestIds to deferred warnings and...
      deferredWarnings[requestId]
      // Make sure it is removed from the map when the warning completes
      invokeOnCompletion { deferredWarnings.remove(requestId) }
    }

    startActivityForResult(Intent(this, ReadKeySqrActivity::class.java), 0)
//    startActivityForResult(Intent(this, DisplayWarningActivity::class.java).apply{
//      this.putExtra(requestIdParameterName, requestId)
//      this.putExtra("message", message)
//    }, 0)
    return deferredResult
  }

  private var loadDiceKeyCompletableDeferred : CompletableDeferred<DiceKey>? = null
  private fun loadDiceKey(): Deferred<DiceKey> =
    loadDiceKeyCompletableDeferred ?:
       CompletableDeferred<DiceKey>().also { completableDeferred ->
        val requestId = UUID.randomUUID().toString()
        loadDiceKeyCompletableDeferred = completableDeferred
        // Start the load activity
        startActivityForResult(Intent(this, ReadKeySqrActivity::class.java).apply{
          this.putExtra(requestIdParameterName, requestId)
        }, 0)
        completableDeferred.invokeOnCompletion {
          // When this completes, no new threads should start waiting on it
          loadDiceKeyCompletableDeferred = null
        }
      }

  private fun executeIntentsCommand() {
    // Get a permission-checked accessor for obtaining seeds from the DiceKey.
    // If the user doesn't have a DiceKey loaded into memory yet, this will trigger
    // a load request, return null, and cause this function to return null.
    // When a key is loaded, this function will be called again.
    val permissionCheckedSeedAccessor =
      PermissionCheckedSeedAccessor.createForIntentApi(
        this,
        { this.loadDiceKey() }
      ) {
        warningHandler(it)
      } ?: return

    // Our API commands don't get a copy of the raw DiceKey seed, but only an accessor
    // which must be passed parameters to check.
    val intentMarshalledApi = PermissionCheckedIntentCommands(
      permissionCheckedSeedAccessor, this
    )

    GlobalScope.launch {
      // Start the suspendable command in its own thread
      intentMarshalledApi.executeCommand()
    }
  }
}