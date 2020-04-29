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
        DerivationOptions.Type.Symmetric
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
        DerivationOptions.Type.Symmetric
      )
    )

  /**
   * Implement [DiceKeysApiClient.getPublicKey] with the necessary permissions checks
   */
  fun getPublicKey(
    derivationOptionsJson: String
  ) : PublicKey =
    PrivateKey.deriveFromSeed(
      permissionCheckedSeedAccessor.getSeedOrThrowIfClientNotAuthorized(
        derivationOptionsJson,
        DerivationOptions.Type.Public
      ),
      derivationOptionsJson
    ).getPublicKey()

  /**
   * Implement [DiceKeysApiClient.getPrivateKey] with the necessary permissions checks
   */
  fun getPrivateKey(
    derivationOptionsJson: String
  ) : PrivateKey = PrivateKey.deriveFromSeed(
    permissionCheckedSeedAccessor.getSeedOrThrowIfClientsMayNotRetrieveKeysOrThisClientNotAuthorized(
      derivationOptionsJson,
      DerivationOptions.Type.Public
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
      DerivationOptions.Type.Signing
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
      DerivationOptions.Type.Symmetric
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
        DerivationOptions.Type.Signing
      ),
      derivationOptionsJson
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
          DerivationOptions.Type.Public
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
        DerivationOptions.Type.Signing
      ),
      derivationOptionsJson
    ).let{ signingKey ->
      Pair(signingKey.generateSignature(message), signingKey.getSignatureVerificationKey())
    }

}