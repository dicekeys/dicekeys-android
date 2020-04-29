package org.dicekeys.trustedapp.apicommands.permissionchecked

import org.dicekeys.crypto.seeded.*
import org.dicekeys.api.*

/**
 * Implements the server-side API calls and the necessary permission checks,
 * using a structure that's locally testable
 * (all intent marshalling and unmarshalling occurs outside this library.)
 *
 * Internally, this class does not have access to the user's raw DiceKey (KeySqrState).
 * The only way it can get seeds is by calling the [PermissionCheckedSeedAccessor]
 * to get the seeds. ([PermissionCheckedSeedAccessor] acts as a reference monitor.)
 *
 * The caller is responsible for catching exceptions
 */
class PermissionCheckedCommands(
  private val permissionCheckedSeedAccessor: PermissionCheckedSeedAccessor
) {
  /**
   * Implement [DiceKeysApiClient.getSecret] with the necessary permissions checks
   */
  fun getSecret(derivationOptionsJson: String): Secret =
    Secret.deriveFromSeed(
      permissionCheckedSeedAccessor.getSeedOrThrowIfClientNotAuthorized(
        derivationOptionsJson,
        DerivationOptions.Type.Secret
      ),
      derivationOptionsJson
    )

  /**
   * Implement [DiceKeysApiClient.sealWithSymmetricKey] with the necessary permissions checks
   */
  fun sealWithSymmetricKey(
    derivationOptionsJson: String,
    plaintext: ByteArray,
    postDecryptionInstructions: String?
  ): PackagedSealedMessage =
    SymmetricKey.deriveFromSeed(
      permissionCheckedSeedAccessor.getSeedOrThrowIfClientNotAuthorized(
        derivationOptionsJson,
        DerivationOptions.Type.SymmetricKey
      ),
      derivationOptionsJson
    ).seal(plaintext, postDecryptionInstructions ?: "")

  /**
   * Implement [DiceKeysApiClient.unsealWithSymmetricKey] with the necessary permissions checks
   */
  fun unsealWithSymmetricKey(
    packagedSealedMessage: PackagedSealedMessage
  ) : ByteArray = SymmetricKey.unseal(
      packagedSealedMessage,
      permissionCheckedSeedAccessor.getSeedOrThrowIfClientNotAuthorizedToUnseal(
        packagedSealedMessage,
        DerivationOptions.Type.SymmetricKey
      )
    )

  /**
   * Implement [DiceKeysApiClient.getSealingKey] with the necessary permissions checks
   */
  fun getSealingKey(
    derivationOptionsJson: String
  ) : SealingKey =
    UnsealingKey.deriveFromSeed(
      permissionCheckedSeedAccessor.getSeedOrThrowIfClientNotAuthorized(
        derivationOptionsJson,
        DerivationOptions.Type.UnsealingKey
      ),
      derivationOptionsJson
    ).getPublicKey()

  /**
   * Implement [DiceKeysApiClient.getUnsealingKey] with the necessary permissions checks
   */
  fun getUnsealingKey(
    derivationOptionsJson: String
  ) : UnsealingKey = UnsealingKey.deriveFromSeed(
    permissionCheckedSeedAccessor.getSeedOrThrowIfClientsMayNotRetrieveKeysOrThisClientNotAuthorized(
      derivationOptionsJson,
      DerivationOptions.Type.UnsealingKey
    ),
    derivationOptionsJson
  )

  /**
   * Implement [DiceKeysApiClient.getSigningKey] with the necessary permissions checks
   */
  fun getSigningKey(
    derivationOptionsJson: String
  ) : SigningKey = SigningKey.deriveFromSeed(
    permissionCheckedSeedAccessor.getSeedOrThrowIfClientsMayNotRetrieveKeysOrThisClientNotAuthorized(
      derivationOptionsJson,
      DerivationOptions.Type.SigningKey
    ),
    derivationOptionsJson
  )

  /**
   * Implement [DiceKeysApiClient.getSymmetricKey] with the necessary permissions checks
   */
  fun getSymmetricKey(
    derivationOptionsJson: String
  ) : SymmetricKey = SymmetricKey.deriveFromSeed(
    permissionCheckedSeedAccessor.getSeedOrThrowIfClientsMayNotRetrieveKeysOrThisClientNotAuthorized(
      derivationOptionsJson,
      DerivationOptions.Type.SymmetricKey
    ),
    derivationOptionsJson
  )

  /**
   * Implement [DiceKeysApiClient.getSignatureVerificationKey] with the necessary permissions checks
   */
  fun getSignatureVerificationKey(
    derivationOptionsJson: String
  ) : SignatureVerificationKey =
    SigningKey.deriveFromSeed(
      permissionCheckedSeedAccessor.getSeedOrThrowIfClientNotAuthorized(
        derivationOptionsJson,
        DerivationOptions.Type.SigningKey
      ),
      derivationOptionsJson
    ).getSignatureVerificationKey()

  /**
   * Implement [DiceKeysApiClient.unsealWithUnsealingKey] with the necessary permissions checks
   */
  fun unsealWithUnsealingKey(
    packagedSealedMessage: PackagedSealedMessage
  ) : ByteArray =
      UnsealingKey.deriveFromSeed(
        permissionCheckedSeedAccessor.getSeedOrThrowIfClientNotAuthorizedToUnseal(
          packagedSealedMessage,
          DerivationOptions.Type.UnsealingKey
        ),
        packagedSealedMessage.derivationOptionsJson
      ).unseal(packagedSealedMessage)

  /**
   * Implement [DiceKeysApiClient.generateSignature] with the necessary permissions checks
   */
  fun generateSignature(
    derivationOptionsJson: String,
    message: ByteArray
  ): Pair<ByteArray, SignatureVerificationKey> = SigningKey.deriveFromSeed(
    permissionCheckedSeedAccessor.getSeedOrThrowIfClientNotAuthorized(
        derivationOptionsJson,
        DerivationOptions.Type.SigningKey
      ),
      derivationOptionsJson
    ).let{ signingKey ->
      Pair(signingKey.generateSignature(message), signingKey.getSignatureVerificationKey())
    }

}