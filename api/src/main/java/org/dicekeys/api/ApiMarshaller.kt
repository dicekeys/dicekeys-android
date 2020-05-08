package org.dicekeys.api

import android.util.Base64
import org.dicekeys.crypto.seeded.JsonSerializable
import java.security.SecureRandom
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ApiMarshaller<T>(
  private val commandName: String,
  private val sendCommand: (command: String, marshallParameters: ParameterMarshaller.() -> Unit) -> Unit,
  private val getResult: (ParameterUnmarshaller) -> T
) {
  interface ParameterMarshaller {
    fun marshallParameter(name: String, value: String)
    fun marshallParameter(name: String, value: ByteArray)
    fun marshallParameter(name: String, value: JsonSerializable)
  }

  interface ParameterUnmarshaller {
    fun unmarshallExceptionIfPresent(): Throwable?
    fun unmarshallString(name: String): String
    fun unmarshallByteArray(name: String): ByteArray
  }

  private val requestIdToDeferredApiResult = mutableMapOf<String, Continuation<T>>()

  /**
   * Generate a request ID that contains the command name and also contains a
   * 128-bit cryptographically-secure random value -- strong enough to prevent
   * brute-forcing attacks.
   */
  private fun getRequestId() = "$commandName:${
    Base64.encodeToString(SecureRandom().generateSeed(16), Base64.URL_SAFE)
  }"

  private var authToken: String? = null
  fun setAuthToken(authToken: String) {
    this.authToken = authToken
  }

  suspend fun call(
    authToken: String?,
    marshallParameters: ParameterMarshaller.() -> Unit
  ): T = suspendCoroutine{ apiResultContinuation ->
    sendCommand(commandName) {
      val requestId = getRequestId()
      requestIdToDeferredApiResult[requestId] = apiResultContinuation
      if (authToken != null) {
        marshallParameter(ApiStrings.Inputs::authToken.name, authToken!!)
      }
      marshallParameter(ApiStrings::requestId.name, requestId)
      marshallParameters(this)
    }
  }

  suspend fun call(
    marshallParameters: ParameterMarshaller.() -> Unit
  ) = call(null, marshallParameters)

  fun handleResponse(
    requestId: String,
    unmarshaller: ParameterUnmarshaller
  ): Boolean {
    val deferredResult = requestIdToDeferredApiResult[requestId]
      ?: return false // We're not handling this response
    // We're handling the response to this request, so we no longer need to track it
    requestIdToDeferredApiResult.remove(requestId)
    // If an exception was returned, throw it
    unmarshaller.unmarshallExceptionIfPresent()?.let { deferredResult.resumeWithException(it) } ?: try {
      val result = getResult(unmarshaller)
      deferredResult.resume(result)
    } catch (e: Throwable) {
      deferredResult.resumeWithException(e)
    }
    return true
  }
}