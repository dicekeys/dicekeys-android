package org.dicekeys.app.apicommands.permissionchecked

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
  private val permissionCheckedSeedAccessor: PermissionCheckedSeedAccessor
) {
  protected var result: PermissionCheckedMarshalledCommands? = null
  var createdDataOrPlainText: String? = null

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

  protected open suspend fun sendSuccess(sendCenterLetterAndDigit: Boolean) {
    marshallResult(
      ApiStrings.MetaOutputs.requestId,
      unmarshallRequiredStringParameter(ApiStrings.MetaInputs.requestId)
    )
    if(sendCenterLetterAndDigit){
      marshallResult(
        ApiStrings.MetaOutputs.centerLetterAndDigit,
        permissionCheckedSeedAccessor.getDiceKey().centerFace().toHumanReadableForm(false)
      )
    }
  }

  abstract fun sendException(exception: Throwable)

  private fun getCommonDerivationOptionsJsonParameter() : String =
    unmarshallRequiredStringParameter((Inputs.getSecret.recipeJson))

  private fun getAuthToken(): PermissionCheckedMarshalledCommands = marshallResult(
    ApiStrings.UrlMetaInputs.authToken,
    api.getAuthToken(unmarshallRequiredStringParameter(ApiStrings.UrlMetaInputs.respondTo))
  )

  private suspend fun getPassword(): PermissionCheckedMarshalledCommands = marshallResult(
          Outputs.getPassword.passwordJson,
          api.getPassword(unmarshallRequiredStringParameter((Inputs.getPassword.recipeJson))).also {
            createdDataOrPlainText = it.password
          }.toJson()
  )

  private suspend fun getSecret(): PermissionCheckedMarshalledCommands = marshallResult(
    Outputs.getSecret.secretJson,
    api.getSecret(getCommonDerivationOptionsJsonParameter()).also {
      createdDataOrPlainText = it.secretBytes.toHexString()
    }.toJson()
    )

  private suspend fun sealWithSymmetricKey(): PermissionCheckedMarshalledCommands = marshallResult(
      Outputs.sealWithSymmetricKey.packagedSealedMessageJson,
      api.sealWithSymmetricKey(
        getCommonDerivationOptionsJsonParameter(),
        unmarshallRequiredBinaryParameter(Inputs.sealWithSymmetricKey::plaintext.name),
        unmarshallStringParameter(Inputs.sealWithSymmetricKey::unsealingInstructions.name)
      ).also {
        createdDataOrPlainText = String(unmarshallRequiredBinaryParameter(Inputs.sealWithSymmetricKey::plaintext.name))
      }.toJson()
    )

  private suspend fun unsealWithSymmetricKey(): PermissionCheckedMarshalledCommands = marshallResult(
      Outputs.unsealWithSymmetricKey::plaintext.name,
      api.unsealWithSymmetricKey(
        PackagedSealedMessage.fromJson(
        unmarshallRequiredStringParameter(Inputs.unsealWithSymmetricKey.packagedSealedMessageJson)
      )
    ))

  private suspend fun getSealingKey(): PermissionCheckedMarshalledCommands = marshallResult(
      Outputs.getSealingKey.sealingKeyJson,
      api.getSealingKey(getCommonDerivationOptionsJsonParameter()).also {
        createdDataOrPlainText = it.keyBytes.toHexString()
      }.toJson()
    )

  private suspend fun unsealWithUnsealingKey(): PermissionCheckedMarshalledCommands = marshallResult(
      Outputs.unsealWithUnsealingKey::plaintext.name,
      api.unsealWithUnsealingKey(
        PackagedSealedMessage.fromJson(
          unmarshallRequiredStringParameter(Inputs.unsealWithUnsealingKey.packagedSealedMessageJson)
        )
      )
    )

  private suspend fun getSignatureVerificationKey(): PermissionCheckedMarshalledCommands = marshallResult(
      Outputs.getSignatureVerificationKey.signatureVerificationKeyJson,
      api.getSignatureVerificationKey(getCommonDerivationOptionsJsonParameter()).also {
        createdDataOrPlainText = it.keyBytes.toHexString()
      }.toJson()
    )

  private suspend fun generateSignature(): PermissionCheckedMarshalledCommands =
    api.generateSignature(
      getCommonDerivationOptionsJsonParameter(),
      unmarshallRequiredBinaryParameter(Inputs.generateSignature::message.name)
    ).let { resultPair ->
      marshallResult(
        Outputs.generateSignature::signature.name, resultPair.first
      ).marshallResult(
        Outputs.generateSignature.signatureVerificationKeyJson,
        resultPair.second.also {
          createdDataOrPlainText = it.keyBytes.toHexString()
        }.toJson()
      )
    }

  private suspend fun getUnsealingKey(): PermissionCheckedMarshalledCommands = marshallResult(
    Outputs.getUnsealingKey.unsealingKeyJson,
    api.getUnsealingKey(getCommonDerivationOptionsJsonParameter()).also {
      createdDataOrPlainText = it.unsealingKeyBytes.toHexString()
    }.toJson()
  )

  private suspend fun getSigningKey(): PermissionCheckedMarshalledCommands = marshallResult(
    Outputs.getSigningKey.signingKeyJson,
    api.getSigningKey(getCommonDerivationOptionsJsonParameter()).also {
      createdDataOrPlainText = it.signingKeyBytes.toHexString()
    }.toJson()
  )

  private suspend fun getSymmetricKey(): PermissionCheckedMarshalledCommands = marshallResult(
    Outputs.getSymmetricKey.symmetricKeyJson,
    api.getSymmetricKey(getCommonDerivationOptionsJsonParameter()).also {
              createdDataOrPlainText = it.keyBytes.toHexString()
            }.toJson()
  )

  var exception: Exception? = null

  fun hasException() = exception != null

  protected suspend fun executeCommand(command: String) {
    try {
      result = when (command) {
         ::getAuthToken.name -> getAuthToken()
        SuspendApi::getPassword.name -> getPassword()
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
      exception = e
    }
  }

  suspend fun send(sendCenterLetterAndDigit: Boolean) {
    try {
      result!!.sendSuccess(sendCenterLetterAndDigit = sendCenterLetterAndDigit)
    } catch (e: Exception) {
      e.printStackTrace()
      sendException(e)
    }
  }

  abstract suspend fun executeCommand()

}

