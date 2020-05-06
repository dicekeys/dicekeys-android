package org.dicekeys.api

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Base64
import androidx.fragment.app.Fragment
import org.dicekeys.crypto.seeded.InvalidArgumentException
import org.dicekeys.crypto.seeded.JsonSerializable

class DiceKeysWebApiClient(
  private val apiUriString: String,
  private val respondToUriString: String,
  private val callUri: (uri: Uri) -> Unit
): Api() {


  override fun call(command: String, marshallParameters: ApiMarshaller.ParameterMarshaller.() -> Unit) {
    callUri(
      Uri.parse(apiUriString).buildUpon().apply {
        appendQueryParameter(ApiStrings.Inputs::command.name, command)
        appendQueryParameter(ApiStrings.Inputs::respondTo.name, respondToUriString)
        marshallParameters(
            object: ApiMarshaller.ParameterMarshaller {
              override fun marshallParameter(name: String, value: ByteArray): Unit {
                appendQueryParameter(name, Base64.encodeToString(value, Base64.URL_SAFE))
              }
              override fun marshallParameter(name: String, value: JsonSerializable): Unit {
                appendQueryParameter(name, value.toJson())
              }
              override fun marshallParameter(name: String, value: String): Unit {
                appendQueryParameter(name, value)
              }
            }
          )
      }.build()
    )
  }

  fun handleResult(uri: Uri) {
    handleResult(object : ApiMarshaller.ParameterUnmarshaller {
      override fun unmarshallByteArray(name: String): ByteArray = Base64.decode(uri.getQueryParameter(name), Base64.URL_SAFE)
        ?: throw InvalidArgumentException("Parameter $name not found")

      override fun unmarshallString(name: String): String = uri.getQueryParameter(name)
        ?: throw InvalidArgumentException("Parameter $name not found")

      override fun unmarshallExceptionIfPresent(): Throwable? =
        uri.getQueryParameter(ApiStrings.Outputs::exception.name)?.let { exceptionName ->
          val message = uri.getQueryParameter(ApiStrings.Outputs::exceptionMessage.name)
          when (exceptionName) {
            ClientUriNotAuthorizedException::class.qualifiedName -> ClientUriNotAuthorizedException(message)
            else -> return UnknownApiException("$exceptionName: $message")
          }
        }
    })
  }

  companion object {
    /**
     * Instantiate an API client for a use within a [Activity].
     *
     * The [Activity] using the [DiceKeysApiClient] must pass a reference
     * to itself via the [activity] parameter.
     *
     * This client will send API requests to the DiceKeys app by creating intents and
     * calling [Activity.startActivityForResult], but it needs your help
     * to relay the results. You must have your activity override
     * [Activity.onActivityResult] and pass the received intent to
     * your [handleResult] method.
     */
    @JvmStatic
    fun create(
      activity: Activity,
      respondToUriString:String,
      apiUriString: String
    ): DiceKeysWebApiClient = DiceKeysWebApiClient(apiUriString, respondToUriString) { uri ->
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (intent.resolveActivity(activity.packageManager) != null) {
          activity.startActivity(intent)
        }
      }


    /**
     * Instantiate an API client for a use within a [Fragment].
     *
     * The [Fragment] using the [DiceKeysApiClient] must pass a reference
     * to itself via the [fragment] parameter.
     *
     * Your activity must run in SingleTask mode (in your Manifest's activity tag,
     * use the attribute android:launchMode="singleTask".
     *
     * Results Urls will arrive in new intents, so you must override your activity's
     * onNewIntent() fun to read the Url.
     *
     * ```kotlin
     *     override fun onNewIntent(intent: Intent?) {
     *       super.onNewIntent(intent)
     *       intent?.data?.let { api.handleResult( it ) }
     *     }
     * ```
     *
     */
    @JvmStatic
    fun create(
      fragment: Fragment,
      respondToUriString:String,
      apiUriString: String
    ): DiceKeysWebApiClient = DiceKeysWebApiClient(apiUriString, respondToUriString) { uri ->
      val intent = Intent(Intent.ACTION_VIEW, uri)
      if (intent.resolveActivity(fragment.activity!!.packageManager) != null) {
        fragment.startActivity(intent)
      }
    }
  }

}