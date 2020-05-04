package org.dicekeys.api

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.dicekeys.crypto.seeded.JsonSerializable

class ApiMarshaller<T>(
  private val commandName: String,
  private val sendCommand: (command: String, marshallParameters: ApiMarshaller.ParameterMarshaller.() -> Unit) -> Unit,
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

  private val requestIdToDeferredApiResult = mutableMapOf<String, CompletableDeferred<T>>()

  private fun getRequestId() = "$commandName:${java.util.UUID.randomUUID()}"

  private fun register(): String = getRequestId().also { requestId ->
    requestIdToDeferredApiResult[requestId] = CompletableDeferred<T>()
  }

  fun <T>callAsync(
    marshallParameters: ApiMarshaller.ParameterMarshaller.() -> Unit
  ): Deferred<T> = CompletableDeferred<T>().also {
    sendCommand(commandName) {
      marshallParameter(ApiStrings::requestId.name, register())
      marshallParameters(this)
    }
  }

  fun handleResponse(
    requestId: String,
    unmarshaller: ParameterUnmarshaller
  ): Boolean {
    val deferredResult = requestIdToDeferredApiResult.get(requestId)
    if (deferredResult == null) {
      // We're not handling this response
      return false
    }
    // We're handling the response to this request, so we no longer need to track it
    requestIdToDeferredApiResult.remove(requestId)
    // If an exception was returned, throw it
    unmarshaller.unmarshallExceptionIfPresent()?.let { deferredResult.completeExceptionally(it) } ?: try {
      deferredResult.complete(getResult(unmarshaller))
    } catch (e: Throwable) {
      deferredResult.completeExceptionally(e)
    }
    return true
  }
}