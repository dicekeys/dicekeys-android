package org.dicekeys.trustedapp.apicommands.permissionchecked

import android.app.Activity
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Deferred
import org.dicekeys.api.ApiStrings
import org.dicekeys.api.UnsealingInstructions
import org.dicekeys.keysqr.DiceKey

class PermissionCheckedUrlCommands(
  private val requestUri: Uri,
  loadDiceKey: () -> Deferred<DiceKey>,
  requestUsersConsent: (
      UnsealingInstructions.RequestForUsersConsent
    ) -> Deferred<UnsealingInstructions.RequestForUsersConsent.UsersResponse>,
  private val sendResponse: (Uri) -> Unit
) : PermissionCheckedCommandsWithUrlSafeBase64Encodings(
  PermissionCheckedSeedAccessor.createForUrlApi(
    getRespondToFieldFromFromUri(requestUri),
    loadDiceKey,
    requestUsersConsent
  )
) {
//  private val respondTo = requestUri.getQueryParameter(ApiStrings.Inputs::respondTo.name)!!
  // Get a permission-checked accessor for obtaining seeds from the DiceKey.
  // If the user doesn't have a DiceKey loaded into memory yet, this will trigger
  // a load request, return null, and cause this function to return null.
  // When a key is loaded, this function will be called again.

  companion object {
    fun getRespondToFieldFromFromUri(uri: Uri): String =
      uri.getQueryParameter(ApiStrings.Inputs::respondTo.name)!!
  }

  constructor(
    requestUri: Uri,
    loadDiceKey: () -> Deferred<DiceKey>,
    requestUsersConsent: (
        UnsealingInstructions.RequestForUsersConsent
      ) -> Deferred<UnsealingInstructions.RequestForUsersConsent.UsersResponse>,
    activity: Activity
  ): this(
    requestUri, loadDiceKey, requestUsersConsent, { uri ->
      Intent(Intent.ACTION_VIEW, uri).let { intent ->
        intent.resolveActivity(activity.packageManager)?.let {
          activity.startActivity(intent)
        }
      }
    }
  )

  private val respondTo: String = getRespondToFieldFromFromUri(requestUri)
  private val responseUriBuilder = Uri.parse(respondTo).buildUpon()!!

  override fun unmarshallStringParameter(parameterName: String): String? =
    requestUri.getQueryParameter(parameterName)

  override fun marshallResult(responseParameterName: String, value: String): PermissionCheckedMarshalledCommands {
    responseUriBuilder.appendQueryParameter(responseParameterName, value)
    return this
  }

  private fun sendResponse(uriBuilder: Uri.Builder) = sendResponse(uriBuilder.build())

  override fun sendSuccess() {
    super.sendSuccess()
    sendResponse(responseUriBuilder)
  }

  override fun sendException(exception: Throwable) = sendResponse(
    Uri.parse(respondTo).buildUpon()
      .appendQueryParameter(ApiStrings::requestId.name, unmarshallStringParameter(ApiStrings::requestId.name))
      .appendQueryParameter(ApiStrings.Outputs::exception.name, exception::class.qualifiedName)
      .appendQueryParameter(ApiStrings.Outputs::exceptionMessage.name, exception.message)
  )

  override suspend fun executeCommand() {
    val command: String = unmarshallRequiredStringParameter(ApiStrings.Inputs::command.name)
    executeCommand(command)
  }

}
