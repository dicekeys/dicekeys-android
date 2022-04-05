package org.dicekeys.app.apicommands.permissionchecked

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Deferred
import org.dicekeys.api.ApiStrings
import org.dicekeys.api.UnsealingInstructions
import org.dicekeys.dicekey.SimpleDiceKey
import java.net.URI

class PermissionCheckedUrlCommands(
  private val requestUri: Uri,
  loadDiceKey: () -> Deferred<SimpleDiceKey>,
  requestUsersConsent: (
      UnsealingInstructions.RequestForUsersConsent
    ) -> Deferred<UnsealingInstructions.RequestForUsersConsent.UsersResponse>,
  private val sendResponse: (Uri) -> Unit
) : PermissionCheckedMarshalledCommands(
  PermissionCheckedSeedAccessor.createForUrlApi(
    getRespondToFieldFromFromUri(requestUri),
    getHandshakeAuthenticatedUrl(requestUri),
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
      uri.getQueryParameter(ApiStrings.UrlMetaInputs.respondTo)!!

    fun getAuthTokenFieldFromUri(uri: Uri): String? =
      uri.getQueryParameter(ApiStrings.UrlMetaInputs.authToken)

    fun getHandshakeAuthenticatedUrl(uri: Uri) : String? =
      getAuthTokenFieldFromUri(uri)?.let { AuthenticationTokens.getUrlForAuthToken(it) }
  }

  constructor(
    requestUri: Uri,
    loadDiceKey: () -> Deferred<SimpleDiceKey>,
    requestUsersConsent: (
        UnsealingInstructions.RequestForUsersConsent
      ) -> Deferred<UnsealingInstructions.RequestForUsersConsent.UsersResponse>,
    context: Context
  ): this(
    requestUri, loadDiceKey, requestUsersConsent, { uri ->
      Intent(Intent.ACTION_VIEW, uri).let { intent ->
        context.startActivity(intent)
      }
    }
  )

  val command: String = unmarshallRequiredStringParameter(ApiStrings.MetaInputs.command)
  val recipe: String = unmarshallStringParameter(ApiStrings.MetaInputs.recipe) ?: ""

  private val respondTo: String = getRespondToFieldFromFromUri(requestUri)
  private val responseUriBuilder = Uri.parse(respondTo).buildUpon()!!

  fun getHost(): String? {
    try {
      return URI(respondTo).host
    }catch (e: Exception){
      e.printStackTrace()
    }
    return null
  }

  override fun unmarshallStringParameter(parameterName: String): String? =
    requestUri.getQueryParameter(parameterName)

  override fun unmarshallBinaryParameter(parameterName: String): ByteArray? =
    unmarshallStringParameter(parameterName)?.let{
      Base64.decode(unmarshallRequiredStringParameter(parameterName), Base64.URL_SAFE)
    }

  override fun marshallResult(responseParameterName: String, value: String): PermissionCheckedMarshalledCommands {
    responseUriBuilder.appendQueryParameter(responseParameterName, value)
    return this
  }

  override fun marshallResult(responseParameterName: String, value: ByteArray): PermissionCheckedMarshalledCommands {
    marshallResult(responseParameterName, Base64.encodeToString(value, Base64.URL_SAFE or Base64.NO_WRAP))
    return this
  }

  private fun sendResponse(uriBuilder: Uri.Builder) = sendResponse(uriBuilder.build())

  override fun sendSuccess() {
    super.sendSuccess()
    sendResponse(responseUriBuilder)
  }

  override fun sendException(exception: Throwable) = sendResponse(
    Uri.parse(respondTo).buildUpon()
      .appendQueryParameter(ApiStrings.MetaOutputs.requestId, unmarshallStringParameter(ApiStrings.MetaInputs.requestId))
      .appendQueryParameter(ApiStrings.ExceptionMetaOutputs.exception, exception::class.qualifiedName)
      .appendQueryParameter(ApiStrings.ExceptionMetaOutputs.message, exception.message)
  )

  override suspend fun executeCommand() {
    executeCommand(command)
  }

  fun getResponse(): Uri = responseUriBuilder.build()

  fun sendException() = exception?.let { sendException(it) }
}