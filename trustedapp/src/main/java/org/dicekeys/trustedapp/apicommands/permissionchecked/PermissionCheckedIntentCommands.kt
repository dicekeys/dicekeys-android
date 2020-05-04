package org.dicekeys.trustedapp.apicommands.permissionchecked

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import org.dicekeys.api.ApiStrings
import org.dicekeys.api.DiceKeysIntentApiClient

class PermissionCheckedIntentCommands(
  permissionCheckedSeedAccessor: PermissionCheckedSeedAccessor,
  private val activity: Activity
) : PermissionCheckedMarshalledCommands(permissionCheckedSeedAccessor) {
  private val requestIntent = activity.intent
  private val resultIntent = Intent()

  override fun binaryParameter(parameterName: String): ByteArray? =
    requestIntent.getByteArrayExtra(parameterName)

  override fun stringParameter(parameterName: String): String? =
    requestIntent.getStringExtra(parameterName)

  override fun respondWith(responseParameterName: String, value: String): PermissionCheckedIntentCommands {
    resultIntent.putExtra(responseParameterName, value)
    return this
  }

  override fun respondWith(responseParameterName: String, value: ByteArray): PermissionCheckedIntentCommands {
    resultIntent.putExtra(responseParameterName, value)
    return this
  }

  override fun sendSuccess() {
    super.sendSuccess()
    // Return the result intent as a success response (OK)
    activity.setResult(AppCompatActivity.RESULT_OK, resultIntent)
    // Finish this activity, which exist(ed) only for the purpose of handling this API request
    activity.finish()
  }

  override fun sendException(exception: Exception) {
    activity.setResult(Activity.RESULT_CANCELED, Intent().apply{
      putExtra(ApiStrings::requestId.name,
        stringParameter(ApiStrings::requestId.name)
      )
      putExtra(ApiStrings.Outputs::exception.name, exception)
    })
    activity.finish()
  }

  override suspend fun executeCommand() {
    executeCommand(requestIntent.action ?: "")
  }

}