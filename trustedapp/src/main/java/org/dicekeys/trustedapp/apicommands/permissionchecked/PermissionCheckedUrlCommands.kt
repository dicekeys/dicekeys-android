package org.dicekeys.trustedapp.apicommands.permissionchecked

import android.app.Activity
import android.content.Intent
import android.net.Uri
import org.dicekeys.api.ApiStrings
import kotlin.reflect.typeOf

class PermissionCheckedUrlCommands(
  private val permissionCheckedSeedAccessor: PermissionCheckedSeedAccessor,
  private val activity: Activity
) : PermissionCheckedCommandsWithUrlSafeBase64Encodings(permissionCheckedSeedAccessor) {
  private val requestUri = activity.intent.data!!
  private val respondTo = requestUri.getQueryParameter(ApiStrings.Inputs::respondTo.name)!!
  private val responseUriBuilder = Uri.parse(respondTo).buildUpon()!!

  override fun unmarshallStringParameter(parameterName: String): String? =
    requestUri.getQueryParameter(parameterName)

  override fun marshallResult(responseParameterName: String, value: String): PermissionCheckedMarshalledCommands {
    responseUriBuilder.appendQueryParameter(responseParameterName, value)
    return this
  }

  private fun sendIntent(intent: Intent) {
    if (intent.resolveActivity(activity.packageManager) != null) {
      activity.startActivity(intent)
    }
  }

  override fun sendSuccess() {
    super.sendSuccess()
    sendIntent(
      Intent(
        Intent.ACTION_VIEW,
        responseUriBuilder.build()
      )
    )
  }

  override fun sendException(exception: Throwable) = sendIntent( Intent(
    Intent.ACTION_VIEW,
    Uri.parse(respondTo).buildUpon()
      .appendQueryParameter(ApiStrings::requestId.name, unmarshallStringParameter(ApiStrings::requestId.name))
      .appendQueryParameter(ApiStrings.Outputs::exception.name, exception::class.qualifiedName)
      .appendQueryParameter(ApiStrings.Outputs::exceptionMessage.name, exception.message)
      .build()
  ))

  override suspend fun executeCommand() {
    val command: String = unmarshallRequiredStringParameter(ApiStrings.Inputs::command.name)
    executeCommand(command)
  }


}