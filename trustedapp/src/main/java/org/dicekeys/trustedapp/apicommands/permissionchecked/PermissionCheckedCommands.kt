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
  fun getSecret(keyDerivationOptionsJson: String): Secret =
    Secret.deriveFromSeed(
      permissionCheckedSeedAccessor.getSeedOrThrowIfClientNotAuthorized(
        keyDerivationOptionsJson,
        KeyDerivationOptions.KeyType.Secret
      ),
      keyDerivationOptionsJson
    )

  /**
   * Implement [DiceKeysApiClient.sealWithSymmetricKey] with the necessary permissions checks
   */
  fun sealWithSymmetricKey(
    keyDerivationOptionsJson: String,
    plaintext: ByteArray,
    postDecryptionInstructions: String?
  ): PackagedSealedMessage =
    SymmetricKey.deriveFromSeed(
      permissionCheckedSeedAccessor.getSeedOrThrowIfClientNotAuthorized(
        keyDerivationOptionsJson,
        KeyDerivationOptions.KeyType.Symmetric
      ),
      keyDerivationOptionsJson
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
        KeyDerivationOptions.KeyType.Symmetric
      )
    )

  /**
   * Implement [DiceKeysApiClient.getPublicKey] with the necessary permissions checks
   */
  fun getPublicKey(
    keyDerivationOptionsJson: String
  ) : PublicKey =
    PrivateKey.deriveFromSeed(
      permissionCheckedSeedAccessor.getSeedOrThrowIfClientNotAuthorized(
        keyDerivationOptionsJson,
        KeyDerivationOptions.KeyType.Public
      ),
      keyDerivationOptionsJson
    ).getPublicKey()

  /**
   * Implement [DiceKeysApiClient.getPrivateKey] with the necessary permissions checks
   */
  fun getPrivateKey(
    keyDerivationOptionsJson: String
  ) : PrivateKey = PrivateKey.deriveFromSeed(
    permissionCheckedSeedAccessor.getSeedOrThrowIfClientsMayNotRetrieveKeysOrThisClientNotAuthorized(
      keyDerivationOptionsJson,
      KeyDerivationOptions.KeyType.Public
    ),
    keyDerivationOptionsJson
  )

  /**
   * Implement [DiceKeysApiClient.getSigningKey] with the necessary permissions checks
   */
  fun getSigningKey(
    keyDerivationOptionsJson: String
  ) : SigningKey = SigningKey.deriveFromSeed(
    permissionCheckedSeedAccessor.getSeedOrThrowIfClientsMayNotRetrieveKeysOrThisClientNotAuthorized(
      keyDerivationOptionsJson,
      KeyDerivationOptions.KeyType.Signing
    ),
    keyDerivationOptionsJson
  )

  /**
   * Implement [DiceKeysApiClient.getSymmetricKey] with the necessary permissions checks
   */
  fun getSymmetricKey(
    keyDerivationOptionsJson: String
  ) : SymmetricKey = SymmetricKey.deriveFromSeed(
    permissionCheckedSeedAccessor.getSeedOrThrowIfClientsMayNotRetrieveKeysOrThisClientNotAuthorized(
      keyDerivationOptionsJson,
      KeyDerivationOptions.KeyType.Symmetric
    ),
    keyDerivationOptionsJson
  )

  /**
   * Implement [DiceKeysApiClient.getSignatureVerificationKey] with the necessary permissions checks
   */
  fun getSignatureVerificationKey(
    keyDerivationOptionsJson: String
  ) : SignatureVerificationKey =
    SigningKey.deriveFromSeed(
      permissionCheckedSeedAccessor.getSeedOrThrowIfClientNotAuthorized(
        keyDerivationOptionsJson,
        KeyDerivationOptions.KeyType.Signing
      ),
      keyDerivationOptionsJson
    ).getSignatureVerificationKey()

  /**
   * Implement [DiceKeysApiClient.unsealWithPrivateKey] with the necessary permissions checks
   */
  fun unsealWithPrivateKey(
    packagedSealedMessage: PackagedSealedMessage
  ) : ByteArray =
      PrivateKey.deriveFromSeed(
        permissionCheckedSeedAccessor.getSeedOrThrowIfClientNotAuthorizedToUnseal(
          packagedSealedMessage,
          KeyDerivationOptions.KeyType.Public
        ),
        packagedSealedMessage.keyDerivationOptionsJson
      ).unseal(packagedSealedMessage)

  /**
   * Implement [DiceKeysApiClient.generateSignature] with the necessary permissions checks
   */
  fun generateSignature(
    keyDerivationOptionsJson: String,
    message: ByteArray
  ): Pair<ByteArray, SignatureVerificationKey> = SigningKey.deriveFromSeed(
    permissionCheckedSeedAccessor.getSeedOrThrowIfClientNotAuthorized(
        keyDerivationOptionsJson,
        KeyDerivationOptions.KeyType.Signing
      ),
      keyDerivationOptionsJson
    ).let{ signingKey ->
      Pair(signingKey.generateSignature(message), signingKey.getSignatureVerificationKey())
    }

}