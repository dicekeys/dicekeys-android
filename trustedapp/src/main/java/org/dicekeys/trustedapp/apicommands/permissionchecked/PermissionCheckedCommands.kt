package org.dicekeys.trustedapp.apicommands.permissionchecked

import android.util.Base64
import org.dicekeys.crypto.seeded.*
import org.dicekeys.api.*
import java.security.SecureRandom

/**
 * Implements the server-side API calls and the necessary permission checks,
 * using a structure that's locally testable
 * (all intent marshalling and unmarshalling occurs outside this library.)
 *
 * Internally, this class does not have access to the user's raw DiceKey (DiceKeyState).
 * The only way it can get seeds is by calling the [PermissionCheckedSeedAccessor]
 * to get the seeds. ([PermissionCheckedSeedAccessor] acts as a reference monitor.)
 *
 * The caller is responsible for catching exceptions
 */
class PermissionCheckedCommands(
  private val permissionCheckedSeedAccessor: PermissionCheckedSeedAccessor
) {
  fun getAuthToken(respondToUrl: String): String =
    AuthenticationTokens.add(respondToUrl)

  /**
   * Implement [DiceKeysIntentApiClient.getSecret] with the necessary permissions checks
   */
  suspend fun getSecret(derivationOptionsJson: String): Secret =
    Secret.deriveFromSeed(
      permissionCheckedSeedAccessor.getSeedOrThrowIfClientNotAuthorized(
        derivationOptionsJson,
        DerivationOptions.Type.Secret,
        ApiStrings.Commands.getSecret
      ),
      derivationOptionsJson
    )

  /**
   * Implement [DiceKeysIntentApiClient.sealWithSymmetricKey] with the necessary permissions checks
   */
  suspend fun sealWithSymmetricKey(
    derivationOptionsJson: String,
    plaintext: ByteArray,
    unsealingInstructions: String?
  ): PackagedSealedMessage =
    SymmetricKey.deriveFromSeed(
      permissionCheckedSeedAccessor.getSeedOrThrowIfClientNotAuthorized(
        derivationOptionsJson,
        DerivationOptions.Type.SymmetricKey,
        ApiStrings.Commands.sealWithSymmetricKey
      ),
      derivationOptionsJson
    ).seal(plaintext, unsealingInstructions ?: "")

  /**
   * Implement [DiceKeysIntentApiClient.unsealWithSymmetricKey] with the necessary permissions checks
   */
  suspend fun unsealWithSymmetricKey(
    packagedSealedMessage: PackagedSealedMessage
  ) : ByteArray = SymmetricKey.unseal(
      packagedSealedMessage,
      permissionCheckedSeedAccessor.getSeedOrThrowIfClientNotAuthorizedToUnseal(
        packagedSealedMessage,
        DerivationOptions.Type.SymmetricKey,
        ApiStrings.Commands.unsealWithSymmetricKey
      )
    )

  /**
   * Implement [DiceKeysIntentApiClient.getSealingKey] with the necessary permissions checks
   */
  suspend fun getSealingKey(
    derivationOptionsJson: String
  ) : SealingKey =
    UnsealingKey.deriveFromSeed(
      permissionCheckedSeedAccessor.getSeedOrThrowIfClientNotAuthorized(
        derivationOptionsJson,
        DerivationOptions.Type.UnsealingKey,
        ApiStrings.Commands.getSealingKey
      ),
      derivationOptionsJson
    ).getSealingkey()

  /**
   * Implement [DiceKeysIntentApiClient.getUnsealingKey] with the necessary permissions checks
   */
  suspend fun getUnsealingKey(
    derivationOptionsJson: String
  ) : UnsealingKey = UnsealingKey.deriveFromSeed(
    permissionCheckedSeedAccessor.getSeedOrThrowIfClientsMayNotRetrieveKeysOrThisClientNotAuthorized(
      derivationOptionsJson,
      DerivationOptions.Type.UnsealingKey
    ),
    derivationOptionsJson
  )

  /**
   * Implement [DiceKeysIntentApiClient.getSigningKey] with the necessary permissions checks
   */
  suspend fun getSigningKey(
    derivationOptionsJson: String
  ) : SigningKey = SigningKey.deriveFromSeed(
    permissionCheckedSeedAccessor.getSeedOrThrowIfClientsMayNotRetrieveKeysOrThisClientNotAuthorized(
      derivationOptionsJson,
      DerivationOptions.Type.SigningKey
    ),
    derivationOptionsJson
  )

  /**
   * Implement [DiceKeysIntentApiClient.getSymmetricKey] with the necessary permissions checks
   */
  suspend fun getSymmetricKey(
    derivationOptionsJson: String
  ) : SymmetricKey = SymmetricKey.deriveFromSeed(
    permissionCheckedSeedAccessor.getSeedOrThrowIfClientsMayNotRetrieveKeysOrThisClientNotAuthorized(
      derivationOptionsJson,
      DerivationOptions.Type.SymmetricKey
    ),
    derivationOptionsJson
  )

  /**
   * Implement [DiceKeysIntentApiClient.getSignatureVerificationKey] with the necessary permissions checks
   */
  suspend fun getSignatureVerificationKey(
    derivationOptionsJson: String
  ) : SignatureVerificationKey =
    SigningKey.deriveFromSeed(
      permissionCheckedSeedAccessor.getSeedOrThrowIfClientNotAuthorized(
        derivationOptionsJson,
        DerivationOptions.Type.SigningKey,
        ApiStrings.Commands.getSignatureVerificationKey
      ),
      derivationOptionsJson
    ).getSignatureVerificationKey()

  /**
   * Implement [DiceKeysIntentApiClient.unsealWithUnsealingKey] with the necessary permissions checks
   */
  suspend fun unsealWithUnsealingKey(
    packagedSealedMessage: PackagedSealedMessage
  ) : ByteArray =
      UnsealingKey.deriveFromSeed(
        permissionCheckedSeedAccessor.getSeedOrThrowIfClientNotAuthorizedToUnseal(
          packagedSealedMessage,
          DerivationOptions.Type.UnsealingKey,
          ApiStrings.Commands.unsealWithUnsealingKey
        ),
        packagedSealedMessage.recipe
      ).unseal(packagedSealedMessage)

  /**
   * Implement [DiceKeysIntentApiClient.generateSignature] with the necessary permissions checks
   */
  suspend fun generateSignature(
    derivationOptionsJson: String,
    message: ByteArray
  ): Pair<ByteArray, SignatureVerificationKey> = SigningKey.deriveFromSeed(
    permissionCheckedSeedAccessor.getSeedOrThrowIfClientNotAuthorized(
        derivationOptionsJson,
        DerivationOptions.Type.SigningKey,
        ApiStrings.Commands.generateSignature
      ),
      derivationOptionsJson
    ).let{ signingKey ->
      Pair(signingKey.generateSignature(message), signingKey.getSignatureVerificationKey())
    }

}