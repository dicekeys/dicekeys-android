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
  private val permissionCheckedSeedAccessor: PermissionCheckedSeedAccessor
) {
  val api = PermissionCheckedCommands(permissionCheckedSeedAccessor)

  open class AwaitingFurtherAction(message: String? = null): Exception(message)

  protected abstract fun stringParameter(parameterName: String) : String?

  protected fun requiredStringParameter(parameterName: String) : String =
    stringParameter(parameterName)?:
    throw java.lang.IllegalArgumentException("API call must include string parameter: '$parameterName'")

  protected abstract fun binaryParameter(parameterName: String) : ByteArray?

  protected fun requiredBinaryParameter(parameterName: String) : ByteArray =
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

  fun getSecret(): Unit = respondWith(
      DiceKeysApiClient.ParameterNames.Secret.Get.secretSerializedToBinary,
      api.getSecret(getCommonDerivationOptionsJsonParameter()).toSerializedBinaryForm()
    ).sendSuccess()

  fun sealWithSymmetricKey(): Unit = respondWith(
      DiceKeysApiClient.ParameterNames.SymmetricKey.Seal.packagedSealedMessageSerializedToBinary,
      api.sealWithSymmetricKey(
        getCommonDerivationOptionsJsonParameter(),
        requiredBinaryParameter(DiceKeysApiClient.ParameterNames.SymmetricKey.Seal.plaintext),
        stringParameter(DiceKeysApiClient.ParameterNames.SymmetricKey.Seal.postDecryptionInstructions)
      ).toSerializedBinaryForm()
    ).sendSuccess()

  fun unsealWithSymmetricKey(): Unit = respondWith(
      DiceKeysApiClient.ParameterNames.SymmetricKey.Unseal.plaintext,
      api.unsealWithSymmetricKey(
        PackagedSealedMessage.fromSerializedBinaryForm(
        requiredBinaryParameter(DiceKeysApiClient.ParameterNames.SymmetricKey.Unseal.packagedSealedMessageSerializedToBinary)
      )
    )).sendSuccess()

  fun getPublicKey(): Unit = respondWith(
      DiceKeysApiClient.ParameterNames.PrivateKey.GetPublic.publicKeySerializedToBinary,
      api.getPublicKey(getCommonDerivationOptionsJsonParameter()).toSerializedBinaryForm()
    ).sendSuccess()

  fun unsealWithPrivateKey(): Unit = respondWith(
      DiceKeysApiClient.ParameterNames.PrivateKey.Unseal.plaintext,
      api.unsealWithPrivateKey(
        PackagedSealedMessage.fromSerializedBinaryForm(
          requiredBinaryParameter(DiceKeysApiClient.ParameterNames.SymmetricKey.Unseal.packagedSealedMessageSerializedToBinary)
        )
      )
    ).sendSuccess()

  fun getSignatureVerificationKey(): Unit = respondWith(
      DiceKeysApiClient.ParameterNames.SigningKey.GetSignatureVerificationKey.signatureVerificationKeySerializedToBinary,
      api.getSignatureVerificationKey(getCommonDerivationOptionsJsonParameter())
        .toSerializedBinaryForm()
    ).sendSuccess()

  fun generateSignature(): Unit =
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

  fun getPrivate(): Unit = respondWith(
    DiceKeysApiClient.ParameterNames.PrivateKey.GetPrivate.privateKeySerializedToBinary,
    api.getPrivateKey(getCommonDerivationOptionsJsonParameter()).toSerializedBinaryForm()
  ).sendSuccess()

  fun getSigningKey(): Unit = respondWith(
    DiceKeysApiClient.ParameterNames.SigningKey.GetSigningKey.signingKeySerializedToBinary,
    api.getSigningKey(getCommonDerivationOptionsJsonParameter()).toSerializedBinaryForm()
  ).sendSuccess()

  fun getSymmetricKey(): Unit = respondWith(
    DiceKeysApiClient.ParameterNames.SymmetricKey.GetKey.symmetricKeySerializedToBinary,
    api.getSymmetricKey(getCommonDerivationOptionsJsonParameter()).toSerializedBinaryForm()
  ).sendSuccess()

  fun executeCommand(command: String) {
    try {
      when (command) {
        DiceKeysApiClient.OperationNames.Secret.get -> getSecret()
        DiceKeysApiClient.OperationNames.SymmetricKey.seal -> sealWithSymmetricKey()
        DiceKeysApiClient.OperationNames.SymmetricKey.unseal -> unsealWithSymmetricKey()
        DiceKeysApiClient.OperationNames.PrivateKey.getPublic -> getPublicKey()
        DiceKeysApiClient.OperationNames.PrivateKey.unseal -> unsealWithPrivateKey()
        DiceKeysApiClient.OperationNames.SigningKey.getSignatureVerificationKey -> getSignatureVerificationKey()
        DiceKeysApiClient.OperationNames.SigningKey.generateSignature -> generateSignature()
        DiceKeysApiClient.OperationNames.PrivateKey.getPrivate -> getPrivate()
        DiceKeysApiClient.OperationNames.SigningKey.getSigningKey -> getSigningKey()
        DiceKeysApiClient.OperationNames.SymmetricKey.getKey -> getSymmetricKey()
        else -> {
          throw IllegalArgumentException("Invalid command for DiceKeys API")
        }
      }
    }catch (e: AwaitingFurtherAction) {
      return
      // The above exception indicates that the operation can't be performed right now
      // because we're awaiting the user's response to a request to permission to complete
      // the operation.  When the user responds to the permission request, this function
      // will be called again and succeed or fail based on the user's response.
      // FUTURE - perhaps use suspend operations instead?

    } catch (e: Exception) {
      sendException(e)
    }
  }

  abstract fun executeCommand(): Unit

}

