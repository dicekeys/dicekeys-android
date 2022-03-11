package org.dicekeys.app.apicommands.permissionchecked

import android.app.Activity
import android.content.Intent
import kotlinx.coroutines.Deferred
import org.dicekeys.api.ApiStrings
import org.dicekeys.api.UnsealingInstructions
import org.dicekeys.dicekey.SimpleDiceKey

class PermissionCheckedIntentCommands(
  private val activity: Activity,
  loadDiceKey: () -> Deferred<SimpleDiceKey>,
  requestUsersConsent: (UnsealingInstructions.RequestForUsersConsent
        ) -> Deferred<UnsealingInstructions.RequestForUsersConsent.UsersResponse>
) : PermissionCheckedMarshalledCommands(
  PermissionCheckedSeedAccessor.createForIntentApi(activity, loadDiceKey, requestUsersConsent)
) {
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

   private fun sendResult(resultType: Int, intent: Intent) {
    activity.setResult(resultType, intent)
    activity.finish()
  }

  override fun sendSuccess() {
    super.sendSuccess()
    // Return the result intent as a success response (OK)
    sendResult(Activity.RESULT_OK, resultIntent)
  }

  override fun sendException(exception: Throwable) {
    sendResult(
      Activity.RESULT_CANCELED,
      Intent().apply{
        putExtra(ApiStrings.MetaOutputs.requestId,
          unmarshallStringParameter(ApiStrings.MetaInputs.requestId)
        )
        putExtra(ApiStrings.ExceptionMetaOutputs.exception, exception)
      }
    )
  }

  override suspend fun executeCommand() {
    executeCommand(requestIntent.action ?: "")
  }

}