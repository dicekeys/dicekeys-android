package org.dicekeys.trustedapp.apicommands.permissionchecked

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import org.dicekeys.api.ApiStrings

class PermissionCheckedIntentCommands(
  permissionCheckedSeedAccessor: PermissionCheckedSeedAccessor,
  private val activity: Activity
) : PermissionCheckedMarshalledCommands(permissionCheckedSeedAccessor) {
  private val requestIntent = activity.intent
  private val resultIntent = Intent()

  override fun unmarshallBinaryParameter(parameterName: String): ByteArray? =
    requestIntent.getByteArrayExtra(parameterName)

  override fun unmarshallStringParameter(parameterName: String): String? =
    requestIntent.getStringExtra(parameterName)

  override fun marshallResult(responseParameterName: String, value: String): PermissionCheckedIntentCommands {
    resultIntent.putExtra(responseParameterName, value)
    return this
  }

  override fun marshallResult(responseParameterName: String, value: ByteArray): PermissionCheckedIntentCommands {
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

  override fun sendException(exception: Throwable) {
    activity.setResult(Activity.RESULT_CANCELED, Intent().apply{
      putExtra(ApiStrings::requestId.name,
        unmarshallStringParameter(ApiStrings::requestId.name)
      )
      putExtra(ApiStrings.Outputs::exception.name, exception)
    })
    activity.finish()
  }

  override suspend fun executeCommand() {
    executeCommand(requestIntent.action ?: "")
  }

}