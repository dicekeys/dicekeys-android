package org.dicekeys.api



import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import androidx.fragment.app.Fragment
import org.dicekeys.crypto.seeded.InvalidArgumentException
import org.dicekeys.crypto.seeded.JsonSerializable

abstract class DiceKeysIntentApiClient: Api(false) {

  abstract fun callIntent(intent: Intent)

  override fun call(command: String, marshallParameters: ApiMarshaller.ParameterMarshaller.() -> Unit) {
    callIntent(
      Intent().apply {
        component = ComponentName(ApiStrings.AndroidIntent.packageName, ApiStrings.AndroidIntent.className)
        action = command
        marshallParameters(
            object: ApiMarshaller.ParameterMarshaller {
              override fun marshallParameter(name: String, value: ByteArray): Unit {
                putExtra(name, value)
              }
              override fun marshallParameter(name: String, value: JsonSerializable): Unit {
                putExtra(name, value.toJson())
              }
              override fun marshallParameter(name: String, value: String): Unit {
                putExtra(name, value)
              }
            }
          )
        }
    )
  }

  fun handleOnActivityResult(intent: Intent) {
    handleResult(object : ApiMarshaller.ParameterUnmarshaller {
      override fun unmarshallByteArray(name: String): ByteArray = intent.getByteArrayExtra(name)
        ?: throw InvalidArgumentException("Parameter $name not found")

      override fun unmarshallString(name: String): String = intent.getStringExtra(name)
        ?: throw InvalidArgumentException("Parameter $name not found")

      override fun unmarshallExceptionIfPresent(): Throwable? = intent.getSerializableExtra(ApiStrings.ExceptionMetaOutputs.exception) as Throwable?
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
     * your [DiceKeysApiClient]'s [handleOnActivityResult] method.
     */
    @JvmStatic
    fun create(activity: Activity): DiceKeysIntentApiClient = object : DiceKeysIntentApiClient() {
      override fun callIntent(intent: Intent) {
        activity.startActivityForResult(intent, 0)
      }
    }

    /**
     * Instantiate an API client for a use within a [Fragment].
     *
     * The [Fragment] using the [DiceKeysApiClient] must pass a reference
     * to itself via the [fragment] parameter.
     *
     * This client will send API requests to the DiceKeys app by creating intents and
     * calling [Fragment.startActivityForResult], but it needs your help
     * to relay the results. You must have your activity override
     * [Fragment.onActivityResult] and pass the received intent to
     * your [DiceKeysApiClient]'s [handleOnActivityResult] method.
     */
    @JvmStatic
    fun create(fragment: Fragment): DiceKeysIntentApiClient = object : DiceKeysIntentApiClient() {
      override fun callIntent(intent: Intent) {
        fragment.startActivityForResult(intent, 0)
      }
    }
  }

}