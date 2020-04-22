package org.dicekeys.trustedapp

import android.content.Intent
import org.dicekeys.api.*
import org.dicekeys.crypto.seeded.PackagedSealedMessage

/**
 * Wrap the [ApiCommandsWithPermissionChecks] to unmarshall parameters from the
 * Android Intents (e.g. via `getStringExtra` or `getByteArrayExtra`) and then
 * marshall the Api call's result into a result intent (e.g. via `putExtra`).
 *
 * Errors are not caught, but trickle up to the caller as Exceptions, and so it's
 * the caller's job to marshall exceptions
 */
class ApiCommandsWithPermissionChecksAndIntentMarhsalling(
  private val api: ApiCommandsWithPermissionChecks,
  private val intent: Intent,
  private val returnIntent: ((fn: (intent: Intent) -> Any) -> Unit)
) {
  private fun getCommonKeyDerivationOptionsJsonParameter() : String =
    intent.getStringExtra(DiceKeysApiClient.ParameterNames.Common.keyDerivationOptionsJson) ?:
    throw java.lang.IllegalArgumentException(
      "API call must include DiceKeysApiClient.ParameterNames.Common.keyDerivationOptionsJson"
    )

  fun getSecret() =
    returnIntent{ resultIntent ->
      resultIntent.putExtra(
        DiceKeysApiClient.ParameterNames.Secret.Get.secretJson,
        api.getSecret(getCommonKeyDerivationOptionsJsonParameter()).toJson()
      )
    }

  fun sealWithSymmetricKey() =
    returnIntent{ resultIntent->
      resultIntent.putExtra(
        DiceKeysApiClient.ParameterNames.SymmetricKey.Seal.packagedSealedMessageSerializedToBinary,
        api.sealWithSymmetricKey(
          getCommonKeyDerivationOptionsJsonParameter(),
          intent.getByteArrayExtra(DiceKeysApiClient.ParameterNames.SymmetricKey.Seal.plaintext)
            ?: throw IllegalArgumentException("Seal operation must include plaintext byte array"),
          intent.getStringExtra(DiceKeysApiClient.ParameterNames.SymmetricKey.Seal.postDecryptionInstructions)
        ).toSerializedBinaryForm())
    }

  fun unsealWithSymmetricKey() =
    api.unsealWithSymmetricKey(
      PackagedSealedMessage.fromSerializedBinaryForm(
        intent.getByteArrayExtra(DiceKeysApiClient.ParameterNames.SymmetricKey.Unseal.packagedSealedMessageSerializedToBinary)
          ?: throw IllegalArgumentException("Unseal operation must include packagedSealedMessageSerializedToBinary")
      )
    )?.let { plaintext ->
      returnIntent{ resultIntent ->
        resultIntent.putExtra(DiceKeysApiClient.ParameterNames.SymmetricKey.Unseal.plaintext, plaintext)
      }
    }

  fun getPublicKey() =
    returnIntent{ resultIntent ->
      resultIntent.putExtra(
        DiceKeysApiClient.ParameterNames.PrivateKey.GetPublic.publicKeySerializedToBinary,
        api.getPublicKey(getCommonKeyDerivationOptionsJsonParameter()).toSerializedBinaryForm()
      )
    }

  fun unsealWithPrivateKey() =
    api.unsealWithPrivateKey(
      PackagedSealedMessage.fromSerializedBinaryForm(
        intent.getByteArrayExtra(DiceKeysApiClient.ParameterNames.SymmetricKey.Unseal.packagedSealedMessageSerializedToBinary)
          ?: throw IllegalArgumentException("Unseal operation must include packagedSealedMessageSerializedToBinary")
      )
    )?.let { plaintext ->
      returnIntent{ resultIntent ->
        resultIntent.putExtra(DiceKeysApiClient.ParameterNames.PrivateKey.Unseal.plaintext, plaintext)
      }
    }

  fun getSignatureVerificationKey() =
    returnIntent{ resultIntent ->
      resultIntent.putExtra(
        DiceKeysApiClient.ParameterNames.SigningKey.GetSignatureVerificationKey.signatureVerificationKeySerializedToBinary,
        api.getSignatureVerificationKey(getCommonKeyDerivationOptionsJsonParameter()).toSerializedBinaryForm())
    }

  fun generateSignature() =
    api.generateSignature(
      getCommonKeyDerivationOptionsJsonParameter(),
      intent.getByteArrayExtra(DiceKeysApiClient.ParameterNames.SigningKey.GenerateSignature.message)
        ?: throw IllegalArgumentException("Seal operation must include message byte array")
    ).let { resultPair ->
      returnIntent{ resultIntent ->
        resultIntent.putExtra(
          DiceKeysApiClient.ParameterNames.SigningKey.GenerateSignature.signature,
          resultPair.first)
        resultIntent.putExtra(
          DiceKeysApiClient.ParameterNames.SigningKey.GenerateSignature.signatureVerificationKeySerializedToBinary,
          resultPair.second.toSerializedBinaryForm()
        )
      }
    }

  fun getPrivate() =
    api.getPrivateKey(getCommonKeyDerivationOptionsJsonParameter()).let { privateKey ->
      returnIntent{ resultIntent ->
        resultIntent.putExtra(
          DiceKeysApiClient.ParameterNames.PrivateKey.GetPrivate.privateKeySerializedToBinary,
          privateKey.toSerializedBinaryForm()
        )
      }
    }

  fun getSigningKey() =
    api.getSigningKey(getCommonKeyDerivationOptionsJsonParameter()).let { signingKey ->
      returnIntent { resultIntent ->
        resultIntent.putExtra(
          DiceKeysApiClient.ParameterNames.SigningKey.GetSigningKey.signingKeySerializedToBinary,
          signingKey.toSerializedBinaryForm()
        )
      }}

  fun getSymmetricKey() =
    api.getSymmetricKey(getCommonKeyDerivationOptionsJsonParameter()).let { symmetricKey ->
      returnIntent { resultIntent ->
        resultIntent.putExtra(
          DiceKeysApiClient.ParameterNames.SymmetricKey.GetKey.symmetricKeySerializedToBinary,
          symmetricKey.toSerializedBinaryForm()
        )
      }
    }

}

