package org.dicekeys.api

import org.dicekeys.crypto.seeded.*

interface SuspendApi {

  suspend fun generateSignature(
    recipeJson: String,
    message: ByteArray
  ): GenerateSignatureResult

  suspend fun getSealingKey(
    recipeJson: String
  ): SealingKey

  suspend fun getSecret(
    recipeJson: String
  ): Secret

  suspend fun getSignatureVerificationKey(
    recipeJson: String
  ): SignatureVerificationKey

  suspend fun getSigningKey(
    recipeJson: String
  ): SigningKey

  suspend fun getSymmetricKey(
    recipeJson: String
  ): SymmetricKey

  suspend fun getUnsealingKey(
    recipeJson: String
  ): UnsealingKey

  suspend fun sealWithSymmetricKey(
    recipeJson: String,
    plaintext: ByteArray,
    unsealingInstructions: String = ""
  ): PackagedSealedMessage

  suspend fun unsealWithSymmetricKey(
    packagedSealedMessage: PackagedSealedMessage
  ): ByteArray

  suspend fun unsealWithUnsealingKey(
    packagedSealedMessage: PackagedSealedMessage
  ): ByteArray

}