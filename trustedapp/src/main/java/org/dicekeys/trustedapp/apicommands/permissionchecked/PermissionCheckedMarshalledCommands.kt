package org.dicekeys.trustedapp.apicommands.permissionchecked

import org.dicekeys.api.*
import org.dicekeys.crypto.seeded.PackagedSealedMessage
import org.dicekeys.api.ApiStrings.Inputs
import org.dicekeys.api.ApiStrings.Outputs

/**
 * Wrap the [PermissionCheckedCommands] to unmarshall parameters from the
 * Android Intents (e.g. via `getStringExtra` or `getByteArrayExtra`) and then
 * marshall the Api call's result into a result intent (e.g. via `putExtra`).
 *
 *  The caller is responsible for catching exceptions and marshalling them
 */
abstract class PermissionCheckedMarshalledCommands(
  permissionCheckedSeedAccessor: PermissionCheckedSeedAccessor
) {
  protected val api = PermissionCheckedCommands(permissionCheckedSeedAccessor)

  protected abstract fun unmarshallStringParameter(parameterName: String) : String?

  protected fun unmarshallRequiredStringParameter(parameterName: String) : String =
    unmarshallStringParameter(parameterName)?:
    throw java.lang.IllegalArgumentException("API call must include string parameter: '$parameterName'")

  protected abstract fun unmarshallBinaryParameter(parameterName: String) : ByteArray?

  private fun unmarshallRequiredBinaryParameter(parameterName: String) : ByteArray =
    unmarshallBinaryParameter(parameterName) ?:
    throw java.lang.IllegalArgumentException("API call must include binary parameter '$parameterName'")

  protected abstract fun marshallResult(responseParameterName: String, value: String): PermissionCheckedMarshalledCommands
  protected abstract fun marshallResult(responseParameterName: String, value: ByteArray): PermissionCheckedMarshalledCommands

  protected open fun sendSuccess() {
    marshallResult(
      ApiStrings.MetaInputs.requestId,
      unmarshallRequiredStringParameter(ApiStrings.MetaInputs.requestId)
    )
  }

  abstract fun sendException(exception: Throwable)

  private fun getCommonDerivationOptionsJsonParameter() : String =
    unmarshallRequiredStringParameter((Inputs.getSecret.recipeJson))

  private fun getAuthToken(): Unit = marshallResult(
    ApiStrings.UrlMetaInputs.authToken,
    api.getAuthToken(unmarshallRequiredStringParameter(ApiStrings.UrlMetaInputs.respondTo))
  ).sendSuccess()

  private suspend fun getSecret(): Unit = marshallResult(
    Outputs.getSecret.secretJson,
    api.getSecret(getCommonDerivationOptionsJsonParameter()).toJson()
    ).sendSuccess()

  private suspend fun sealWithSymmetricKey(): Unit = marshallResult(
      Outputs.sealWithSymmetricKey.packagedSealedMessageJson,
      api.sealWithSymmetricKey(
        getCommonDerivationOptionsJsonParameter(),
        unmarshallRequiredBinaryParameter(Inputs.sealWithSymmetricKey::plaintext.name),
        unmarshallStringParameter(Inputs.sealWithSymmetricKey::unsealingInstructions.name)
      ).toJson()
    ).sendSuccess()

  private suspend fun unsealWithSymmetricKey(): Unit = marshallResult(
      Outputs.unsealWithSymmetricKey::plaintext.name,
      api.unsealWithSymmetricKey(
        PackagedSealedMessage.fromJson(
        unmarshallRequiredStringParameter(Inputs.unsealWithSymmetricKey.packagedSealedMessageJson)
      )
    )).sendSuccess()

  private suspend fun getSealingKey(): Unit = marshallResult(
      Outputs.getSealingKey.sealingKeyJson,
      api.getSealingKey(getCommonDerivationOptionsJsonParameter()).toJson()
    ).sendSuccess()

  private suspend fun unsealWithUnsealingKey(): Unit = marshallResult(
      Outputs.unsealWithUnsealingKey::plaintext.name,
      api.unsealWithUnsealingKey(
        PackagedSealedMessage.fromJson(
          unmarshallRequiredStringParameter(Inputs.unsealWithUnsealingKey.packagedSealedMessageJson)
        )
      )
    ).sendSuccess()

  private suspend fun getSignatureVerificationKey(): Unit = marshallResult(
      Outputs.getSignatureVerificationKey.signatureVerificationKeyJson,
      api.getSignatureVerificationKey(getCommonDerivationOptionsJsonParameter())
        .toJson()
    ).sendSuccess()

  private suspend fun generateSignature(): Unit =
    api.generateSignature(
      getCommonDerivationOptionsJsonParameter(),
      unmarshallRequiredBinaryParameter(Inputs.generateSignature::message.name)
    ).let { resultPair ->
      marshallResult(
        Outputs.generateSignature::signature.name, resultPair.first
      ).marshallResult(
        Outputs.generateSignature.signatureVerificationKeyJson,
        resultPair.second.toJson()
      ).sendSuccess()
    }

  private suspend fun getUnsealingKey(): Unit = marshallResult(
    Outputs.getUnsealingKey.unsealingKeyJson,
    api.getUnsealingKey(getCommonDerivationOptionsJsonParameter()).toJson()
  ).sendSuccess()

  private suspend fun getSigningKey(): Unit = marshallResult(
    Outputs.getSigningKey.signingKeyJson,
    api.getSigningKey(getCommonDerivationOptionsJsonParameter()).toJson()
  ).sendSuccess()

  private suspend fun getSymmetricKey(): Unit = marshallResult(
    Outputs.getSymmetricKey.symmetricKeyJson,
    api.getSymmetricKey(getCommonDerivationOptionsJsonParameter()).toJson()
  ).sendSuccess()

  protected suspend fun executeCommand(command: String) {
    try {
      when (command) {
         ::getAuthToken.name -> getAuthToken()
        SuspendApi::generateSignature.name -> generateSignature()
        SuspendApi::getSealingKey.name -> getSealingKey()
        SuspendApi::getSecret.name -> getSecret()
        SuspendApi::getSignatureVerificationKey.name -> getSignatureVerificationKey()
        SuspendApi::getSigningKey.name -> getSigningKey()
        SuspendApi::getSymmetricKey.name -> getSymmetricKey()
        SuspendApi::getUnsealingKey.name -> getUnsealingKey()
        SuspendApi::sealWithSymmetricKey.name -> sealWithSymmetricKey()
        SuspendApi::unsealWithSymmetricKey.name -> unsealWithSymmetricKey()
        SuspendApi::unsealWithUnsealingKey.name -> unsealWithUnsealingKey()
        else -> {
          throw IllegalArgumentException("Invalid command for DiceKeys API")
        }
      }
    } catch (e: Exception) {
      sendException(e)
    }
  }

  abstract suspend fun executeCommand()

}

