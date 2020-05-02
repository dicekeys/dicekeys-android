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
      DiceKeysApiClient.ParameterNames.Common.requestId,
      requiredStringParameter(DiceKeysApiClient.ParameterNames.Common.requestId)
    )
  }
  abstract fun sendException(exception: Exception)


  private fun getCommonDerivationOptionsJsonParameter() : String =
    requiredStringParameter((DiceKeysApiClient.ParameterNames.Common.derivationOptionsJson))

  private suspend fun getSecret(): Unit = respondWith(
      DiceKeysApiClient.ParameterNames.Secret.Get.secretSerializedToBinary,
      api.getSecret(getCommonDerivationOptionsJsonParameter()).toSerializedBinaryForm()
    ).sendSuccess()

  private suspend fun sealWithSymmetricKey(): Unit = respondWith(
      DiceKeysApiClient.ParameterNames.SymmetricKey.Seal.packagedSealedMessageSerializedToBinary,
      api.sealWithSymmetricKey(
        getCommonDerivationOptionsJsonParameter(),
        requiredBinaryParameter(DiceKeysApiClient.ParameterNames.SymmetricKey.Seal.plaintext),
        stringParameter(DiceKeysApiClient.ParameterNames.SymmetricKey.Seal.unsealingInstructions)
      ).toSerializedBinaryForm()
    ).sendSuccess()

  private suspend fun unsealWithSymmetricKey(): Unit = respondWith(
      DiceKeysApiClient.ParameterNames.SymmetricKey.Unseal.plaintext,
      api.unsealWithSymmetricKey(
        PackagedSealedMessage.fromSerializedBinaryForm(
        requiredBinaryParameter(DiceKeysApiClient.ParameterNames.SymmetricKey.Unseal.packagedSealedMessageSerializedToBinary)
      )
    )).sendSuccess()

  private suspend fun getSealingKey(): Unit = respondWith(
      DiceKeysApiClient.ParameterNames.UnsealingKey.GetSealingKey.sealingKeySerializedToBinary,
      api.getSealingKey(getCommonDerivationOptionsJsonParameter()).toSerializedBinaryForm()
    ).sendSuccess()

  private suspend fun unsealWithPrivateKey(): Unit = respondWith(
      DiceKeysApiClient.ParameterNames.UnsealingKey.Unseal.plaintext,
      api.unsealWithUnsealingKey(
        PackagedSealedMessage.fromSerializedBinaryForm(
          requiredBinaryParameter(DiceKeysApiClient.ParameterNames.SymmetricKey.Unseal.packagedSealedMessageSerializedToBinary)
        )
      )
    ).sendSuccess()

  private suspend fun getSignatureVerificationKey(): Unit = respondWith(
      DiceKeysApiClient.ParameterNames.SigningKey.GetSignatureVerificationKey.signatureVerificationKeySerializedToBinary,
      api.getSignatureVerificationKey(getCommonDerivationOptionsJsonParameter())
        .toSerializedBinaryForm()
    ).sendSuccess()

  private suspend fun generateSignature(): Unit =
    api.generateSignature(
      getCommonDerivationOptionsJsonParameter(),
      requiredBinaryParameter(DiceKeysApiClient.ParameterNames.SigningKey.GenerateSignature.message)
    ).let { resultPair ->
      respondWith(
        DiceKeysApiClient.ParameterNames.SigningKey.GenerateSignature.signature, resultPair.first
      ).respondWith(
        DiceKeysApiClient.ParameterNames.SigningKey.GenerateSignature.signatureVerificationKeySerializedToBinary,
        resultPair.second.toSerializedBinaryForm()
      ).sendSuccess()
    }

  private suspend fun getUnsealingKey(): Unit = respondWith(
    DiceKeysApiClient.ParameterNames.UnsealingKey.GetUnsealingKey.unsealingKeySerializedToBinary,
    api.getUnsealingKey(getCommonDerivationOptionsJsonParameter()).toSerializedBinaryForm()
  ).sendSuccess()

  private suspend fun getSigningKey(): Unit = respondWith(
    DiceKeysApiClient.ParameterNames.SigningKey.GetSigningKey.signingKeySerializedToBinary,
    api.getSigningKey(getCommonDerivationOptionsJsonParameter()).toSerializedBinaryForm()
  ).sendSuccess()

  private suspend fun getSymmetricKey(): Unit = respondWith(
    DiceKeysApiClient.ParameterNames.SymmetricKey.GetKey.symmetricKeySerializedToBinary,
    api.getSymmetricKey(getCommonDerivationOptionsJsonParameter()).toSerializedBinaryForm()
  ).sendSuccess()

  protected suspend fun executeCommand(command: String) {
    try {
      when (command) {
        DiceKeysApiClient.OperationNames.getSecret -> getSecret()
        DiceKeysApiClient.OperationNames.sealWithSymmetricKey -> sealWithSymmetricKey()
        DiceKeysApiClient.OperationNames.unsealWithSymmetricKey -> unsealWithSymmetricKey()
        DiceKeysApiClient.OperationNames.getSealingKey -> getSealingKey()
        DiceKeysApiClient.OperationNames.unsealWithUnsealingKey -> unsealWithPrivateKey()
        DiceKeysApiClient.OperationNames.getSignatureVerificationKey -> getSignatureVerificationKey()
        DiceKeysApiClient.OperationNames.generateSignature -> generateSignature()
        DiceKeysApiClient.OperationNames.getUnsealingKey -> getUnsealingKey()
        DiceKeysApiClient.OperationNames.getSigningKey -> getSigningKey()
        DiceKeysApiClient.OperationNames.getSymmetricKey -> getSymmetricKey()
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

