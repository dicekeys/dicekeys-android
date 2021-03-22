package org.dicekeys.app.apicommands.permissionchecked

import org.dicekeys.api.ApiStrings
import org.dicekeys.api.DiceKeysIntentApiClient
import org.dicekeys.crypto.seeded.*

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
   * Implement [DiceKeysIntentApiClient.getPassword] with the necessary permissions checks
   */
  suspend fun getPassword(recipeJson: String): Password =
    Password.deriveFromSeed(
      permissionCheckedSeedAccessor.getSeedOrThrowIfClientNotAuthorized(
        recipeJson,
        DerivationOptions.Type.Password,
        ApiStrings.Commands.getPassword
      ),
      recipeJson
    )

  /**
   * Implement [DiceKeysIntentApiClient.getSecret] with the necessary permissions checks
   */
  suspend fun getSecret(recipeJson: String): Secret =
    Secret.deriveFromSeed(
      permissionCheckedSeedAccessor.getSeedOrThrowIfClientNotAuthorized(
        recipeJson,
        DerivationOptions.Type.Secret,
        ApiStrings.Commands.getSecret
      ),
      recipeJson
    )

  /**
   * Implement [DiceKeysIntentApiClient.sealWithSymmetricKey] with the necessary permissions checks
   */
  suspend fun sealWithSymmetricKey(
    recipeJson: String,
    plaintext: ByteArray,
    unsealingInstructions: String?
  ): PackagedSealedMessage =
    SymmetricKey.deriveFromSeed(
      permissionCheckedSeedAccessor.getSeedOrThrowIfClientNotAuthorized(
        recipeJson,
        DerivationOptions.Type.SymmetricKey,
        ApiStrings.Commands.sealWithSymmetricKey
      ),
      recipeJson
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
    recipeJson: String
  ) : SealingKey =
    UnsealingKey.deriveFromSeed(
      permissionCheckedSeedAccessor.getSeedOrThrowIfClientNotAuthorized(
        recipeJson,
        DerivationOptions.Type.UnsealingKey,
        ApiStrings.Commands.getSealingKey
      ),
      recipeJson
    ).getSealingkey()

  /**
   * Implement [DiceKeysIntentApiClient.getUnsealingKey] with the necessary permissions checks
   */
  suspend fun getUnsealingKey(
    recipeJson: String
  ) : UnsealingKey = UnsealingKey.deriveFromSeed(
    permissionCheckedSeedAccessor.getSeedOrThrowIfClientsMayNotRetrieveKeysOrThisClientNotAuthorized(
      recipeJson,
      DerivationOptions.Type.UnsealingKey
    ),
    recipeJson
  )

  /**
   * Implement [DiceKeysIntentApiClient.getSigningKey] with the necessary permissions checks
   */
  suspend fun getSigningKey(
    recipeJson: String
  ) : SigningKey = SigningKey.deriveFromSeed(
    permissionCheckedSeedAccessor.getSeedOrThrowIfClientsMayNotRetrieveKeysOrThisClientNotAuthorized(
      recipeJson,
      DerivationOptions.Type.SigningKey
    ),
    recipeJson
  )

  /**
   * Implement [DiceKeysIntentApiClient.getSymmetricKey] with the necessary permissions checks
   */
  suspend fun getSymmetricKey(
    recipeJson: String
  ) : SymmetricKey = SymmetricKey.deriveFromSeed(
    permissionCheckedSeedAccessor.getSeedOrThrowIfClientsMayNotRetrieveKeysOrThisClientNotAuthorized(
      recipeJson,
      DerivationOptions.Type.SymmetricKey
    ),
    recipeJson
  )

  /**
   * Implement [DiceKeysIntentApiClient.getSignatureVerificationKey] with the necessary permissions checks
   */
  suspend fun getSignatureVerificationKey(
    recipeJson: String
  ) : SignatureVerificationKey =
    SigningKey.deriveFromSeed(
      permissionCheckedSeedAccessor.getSeedOrThrowIfClientNotAuthorized(
        recipeJson,
        DerivationOptions.Type.SigningKey,
        ApiStrings.Commands.getSignatureVerificationKey
      ),
      recipeJson
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
    recipeJson: String,
    message: ByteArray
  ): Pair<ByteArray, SignatureVerificationKey> = SigningKey.deriveFromSeed(
    permissionCheckedSeedAccessor.getSeedOrThrowIfClientNotAuthorized(
        recipeJson,
        DerivationOptions.Type.SigningKey,
        ApiStrings.Commands.generateSignature
      ),
      recipeJson
    ).let{ signingKey ->
      Pair(signingKey.generateSignature(message), signingKey.getSignatureVerificationKey())
    }

}