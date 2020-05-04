package org.dicekeys.trustedapp.apicommands.permissionchecked

import org.dicekeys.api.*
import org.dicekeys.crypto.seeded.PackagedSealedMessage


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

  protected abstract fun stringParameter(parameterName: String) : String?

  protected fun requiredStringParameter(parameterName: String) : String =
    stringParameter(parameterName)?:
    throw java.lang.IllegalArgumentException("API call must include string parameter: '$parameterName'")

  protected abstract fun binaryParameter(parameterName: String) : ByteArray?

  private fun requiredBinaryParameter(parameterName: String) : ByteArray =
    binaryParameter(parameterName) ?:
    throw java.lang.IllegalArgumentException("API call must include binary parameter '$parameterName'")

  protected abstract fun respondWith(responseParameterName: String, value: String): PermissionCheckedMarshalledCommands
  protected abstract fun respondWith(responseParameterName: String, value: ByteArray): PermissionCheckedMarshalledCommands

  protected open fun sendSuccess() {
    respondWith(
      ApiStrings::requestId.name,
      requiredStringParameter(ApiStrings::requestId.name)
    )
  }
  abstract fun sendException(exception: Exception)


  private fun getCommonDerivationOptionsJsonParameter() : String =
    requiredStringParameter((ApiStrings.Inputs.withDerivationOptions::derivationOptionsJson.name))

  private suspend fun getSecret(): Unit = respondWith(
      ApiStrings.Outputs.getSecret::secret.name,
      api.getSecret(getCommonDerivationOptionsJsonParameter()).toJson()
    ).sendSuccess()

  private suspend fun sealWithSymmetricKey(): Unit = respondWith(
      ApiStrings.Outputs.sealWithSymmetricKey::packagedSealedMessage.name,
      api.sealWithSymmetricKey(
        getCommonDerivationOptionsJsonParameter(),
        requiredBinaryParameter(ApiStrings.Inputs.sealWithSymmetricKey::plaintext.name),
        stringParameter(ApiStrings.Inputs.sealWithSymmetricKey::unsealingInstructions.name)
      ).toJson()
    ).sendSuccess()

  private suspend fun unsealWithSymmetricKey(): Unit = respondWith(
      ApiStrings.Outputs.unsealWithSymmetricKey::plaintext.name,
      api.unsealWithSymmetricKey(
        PackagedSealedMessage.fromJson(
        requiredStringParameter(ApiStrings.Inputs.unsealWithSymmetricKey::packagedSealedMessage.name)
      )
    )).sendSuccess()

  private suspend fun getSealingKey(): Unit = respondWith(
      ApiStrings.Outputs.getSealingKey::sealingKey.name,
      api.getSealingKey(getCommonDerivationOptionsJsonParameter()).toJson()
    ).sendSuccess()

  private suspend fun unsealWithUnsealingKey(): Unit = respondWith(
      ApiStrings.Outputs.unsealWithUnsealingKey::plaintext.name,
      api.unsealWithUnsealingKey(
        PackagedSealedMessage.fromJson(
          requiredStringParameter(ApiStrings.Inputs.unsealWithUnsealingKey::packagedSealedMessage.name)
        )
      )
    ).sendSuccess()

  private suspend fun getSignatureVerificationKey(): Unit = respondWith(
      ApiStrings.Outputs.getSignatureVerificationKey::signatureVerificationKey.name,
      api.getSignatureVerificationKey(getCommonDerivationOptionsJsonParameter())
        .toJson()
    ).sendSuccess()

  private suspend fun generateSignature(): Unit =
    api.generateSignature(
      getCommonDerivationOptionsJsonParameter(),
      requiredBinaryParameter(ApiStrings.Inputs.generateSignature::message.name)
    ).let { resultPair ->
      respondWith(
        ApiStrings.Outputs.generateSignature::signature.name, resultPair.first
      ).respondWith(
        ApiStrings.Outputs.generateSignature::signatureVerificationKey.name,
        resultPair.second.toJson()
      ).sendSuccess()
    }

  private suspend fun getUnsealingKey(): Unit = respondWith(
    ApiStrings.Outputs.getUnsealingKey::unsealingKey.name,
    api.getUnsealingKey(getCommonDerivationOptionsJsonParameter()).toJson()
  ).sendSuccess()

  private suspend fun getSigningKey(): Unit = respondWith(
    ApiStrings.Outputs.getSigningKey::signingKey.name,
    api.getSigningKey(getCommonDerivationOptionsJsonParameter()).toJson()
  ).sendSuccess()

  private suspend fun getSymmetricKey(): Unit = respondWith(
    ApiStrings.Outputs.getSymmetricKey::symmetricKey.name,
    api.getSymmetricKey(getCommonDerivationOptionsJsonParameter()).toJson()
  ).sendSuccess()

  protected suspend fun executeCommand(command: String) {
    try {
      when (command) {
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
    }catch (e: Exception) {
      sendException(e)
    }
  }

  abstract suspend fun executeCommand()

}

