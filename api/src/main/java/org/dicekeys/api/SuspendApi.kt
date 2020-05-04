package org.dicekeys.api

import org.dicekeys.crypto.seeded.*

interface SuspendApi {

  suspend fun generateSignature(
    derivationOptionsJson: String,
    message: ByteArray
  ): GenerateSignatureResult

  suspend fun getSealingKey(
    derivationOptionsJson: String
  ): SealingKey

  suspend fun getSecret(
    derivationOptionsJson: String
  ): Secret

  suspend fun getSignatureVerificationKey(
    derivationOptionsJson: String
  ): SignatureVerificationKey

  suspend fun getSigningKey(
    derivationOptionsJson: String
  ): SigningKey

  suspend fun getSymmetricKey(
    derivationOptionsJson: String
  ): SymmetricKey

  suspend fun getUnsealingKey(
    derivationOptionsJson: String
  ): UnsealingKey

  suspend fun sealWithSymmetricKey(
    derivationOptionsJson: String,
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