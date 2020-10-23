package org.dicekeys.api

import kotlinx.coroutines.Deferred
import org.dicekeys.crypto.seeded.*

interface AsyncApi {

  fun getSecretAsync(
    derivationOptionsJson: String
  ): Deferred<Secret>

  fun getUnsealingKeyAsync(
    derivationOptionsJson: String
  ): Deferred<UnsealingKey>

  fun getSymmetricKeyAsync(
    derivationOptionsJson: String
  ): Deferred<SymmetricKey>

  fun getSigningKeyAsync(
    derivationOptionsJson: String
  ): Deferred<SigningKey>

  fun getSealingKeyAsync(
    derivationOptionsJson: String
  ): Deferred<SealingKey>

  fun unsealWithUnsealingKeyAsync(
    packagedSealedMessage: PackagedSealedMessage
  ): Deferred<ByteArray>

  fun sealWithSymmetricKeyAsync(
    derivationOptionsJson: String,
    plaintext: ByteArray,
    unsealingInstructions: String = ""
  ): Deferred<PackagedSealedMessage>

  fun unsealWithSymmetricKeyAsync(
    packagedSealedMessage: PackagedSealedMessage
  ): Deferred<ByteArray>

  fun getSignatureVerificationKeyAsync(
    derivationOptionsJson: String
  ): Deferred<SignatureVerificationKey>

  fun generateSignatureAsync(
    derivationOptionsJson: String,
    message: ByteArray
  ): Deferred<GenerateSignatureResult>

}