package org.dicekeys.api

class ApiMarshallers(
  private val sendCommand: (command: String, marshallParameters: ApiMarshaller.ParameterMarshaller.() -> Unit) -> Unit
) {
  val marshallers = mutableListOf<ApiMarshaller<Any>>()

  fun <T : Any> add(
    commandName: String,
    getResult: ApiMarshaller.ParameterUnmarshaller.() -> T
  ): ApiMarshaller<T> = ApiMarshaller(commandName, sendCommand, getResult).also {
    marshallers.add(it as ApiMarshaller<Any>)
  }

  fun handleResponse(
    requestId: String,
    unmarshaller: ApiMarshaller.ParameterUnmarshaller
  ): Boolean = marshallers.any { callbackManager ->
    callbackManager.handleResponse(requestId, unmarshaller)
  }
}