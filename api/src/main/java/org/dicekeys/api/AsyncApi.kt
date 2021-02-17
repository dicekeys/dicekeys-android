package org.dicekeys.api

import kotlinx.coroutines.Deferred
import org.dicekeys.crypto.seeded.*

interface AsyncApi {

  fun getSecretAsync(
    recipeJson: String
  ): Deferred<Secret>

  fun getUnsealingKeyAsync(
    recipeJson: String
  ): Deferred<UnsealingKey>

  fun getSymmetricKeyAsync(
    recipeJson: String
  ): Deferred<SymmetricKey>

  fun getSigningKeyAsync(
    recipeJson: String
  ): Deferred<SigningKey>

  fun getSealingKeyAsync(
    recipeJson: String
  ): Deferred<SealingKey>

  fun unsealWithUnsealingKeyAsync(
    packagedSealedMessage: PackagedSealedMessage
  ): Deferred<ByteArray>

  fun sealWithSymmetricKeyAsync(
    recipeJson: String,
    plaintext: ByteArray,
    unsealingInstructions: String = ""
  ): Deferred<PackagedSealedMessage>

  fun unsealWithSymmetricKeyAsync(
    packagedSealedMessage: PackagedSealedMessage
  ): Deferred<ByteArray>

  fun getSignatureVerificationKeyAsync(
    recipeJson: String
  ): Deferred<SignatureVerificationKey>

  fun generateSignatureAsync(
    recipeJson: String,
    message: ByteArray
  ): Deferred<GenerateSignatureResult>

}