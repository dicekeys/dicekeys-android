package org.dicekeys.app.apicommands.permissionchecked

import kotlinx.coroutines.Deferred
import org.dicekeys.api.ApiRecipe
import org.dicekeys.api.ApiStrings
import org.dicekeys.api.ClientMayNotRetrieveKeyException
import org.dicekeys.api.UnsealingInstructions
import org.dicekeys.crypto.seeded.DerivationOptions
import org.dicekeys.crypto.seeded.PackagedSealedMessage
import org.dicekeys.dicekey.SimpleDiceKey

/**
 * This class abstracts away all permissions checks AND all access to the diceKey seed,
 * so that the only way the API which inherits from it can get to the seed is by
 * going through the permission checks.
 */
open class PermissionCheckedSeedAccessor(
  private val permissionChecks: ApiPermissionChecks,
  private val loadDiceKey: () -> Deferred<SimpleDiceKey>
) {
  private suspend fun getDiceKey(): SimpleDiceKey = loadDiceKey().await()

  companion object {
    /**
     * Try to construct a permission-checked accessor for DiceKey seeds.
     *
     * If there is a DiceKey loaded into the DiceKeyRepository, this function returns a
     * PermissionCheckedSeedAccessor which will generate seeds for the API while
     * protecting the raw DiceKey.
     *
     */
    fun createForUrlApi(
      respondToUrl: String,
      handshakeAuthenticatedUrl: String?,
      loadDiceKey: () -> Deferred<SimpleDiceKey>,
      requestUsersConsent: (UnsealingInstructions.RequestForUsersConsent
      ) -> Deferred<UnsealingInstructions.RequestForUsersConsent.UsersResponse>
    ): PermissionCheckedSeedAccessor = PermissionCheckedSeedAccessor(
      ApiPermissionChecksForUrls(
        respondToUrl,
        handshakeAuthenticatedUrl,
        requestUsersConsent
      ),
      loadDiceKey
    )
  }


  private suspend fun getSeedOrThrowIfClientNotAuthorized(
    derivationOptions: ApiRecipe,
  ): String {
    permissionChecks.throwIfClientNotAuthorized(derivationOptions)
    return getDiceKey().toKeySeed(derivationOptions.excludeOrientationOfFaces)
  }

  /**
   * Request a seed generated from the user's DiceKey and salted by the
   * recipeJson string, using the ApiRecipe
   * specified by that string. Implements guards to ensure that the
   * recipe allows the client application with the
   * requester's package name to perform operations with this key.
   */
  internal suspend fun getSeedOrThrowIfClientNotAuthorized(
    recipeJson: String?,
    type: DerivationOptions.Type,
    command: String
  ): String {
    val derivationOptions = ApiRecipe(recipeJson, type)
    if (command == ApiStrings.Commands.getSealingKey  &&  (recipeJson == null || recipeJson.isEmpty())) {
      // No permission check is needed in this special case for global sealing/unsealing keys,
      // for which there's no derivationOptionSpecified, indicating a request for any _public_ unsealing key
      // which the user may choose.
    } else if (command == ApiStrings.Commands.unsealWithUnsealingKey && derivationOptions.allow == null) {
      // Attempt to unseal a message with a public key, so we can re-derive the private key
      // since it won't be shared with client and unsealingInstructions can be applied.
    } else {
      // Perform the permission check before returning the seed.
      permissionChecks.throwIfClientNotAuthorized(derivationOptions)
    }
    return getDiceKey().toKeySeed(derivationOptions.excludeOrientationOfFaces)
  }


  /**
   * Used to guard calls to the unseal operation of SymmetricKey and PrivateKey,
   * which not only needs to authorize via recipeJson, but
   * also the postDecryptionOptions in PackagedSealedMessage
   *
   * Requests a seed generated from the user's DiceKey and salted by the
   * recipeJson string, using the ApiRecipe
   * specified by that string. Implements guards to ensure that the
   * recipe allowsthe client application with the
   * requester's package name to perform operations with this key.
   */
  internal suspend fun getSeedOrThrowIfClientNotAuthorizedToUnseal(
    packagedSealedMessage: PackagedSealedMessage,
    type: DerivationOptions.Type,
    command: String
  ): String {
    permissionChecks.throwIfUnsealingInstructionsViolated(packagedSealedMessage.unsealingInstructions)
    return getSeedOrThrowIfClientNotAuthorized(
      packagedSealedMessage.recipe,
      type,
      command
    )
  }

  /**
   * Requests a seed generated from the user's DiceKey and salted by the
   * recipeJson string, using the ApiRecipe
   * specified by that string. Implements guards to ensure that the
   * recipe allowsthe client application with the
   * requester's package name to perform operations with this key.
   *
   * This is used to guard calls to APIs calls used to return raw non-public
   * keys: PrivateKey, SigningKey, or SymmetricKey. The specification only
   * allows the DiceKeys app to return these keys to the requesting app
   * if the recipeJson has `"clientMayRetrieveKey": true`.
   */
  internal suspend fun getSeedOrThrowIfClientsMayNotRetrieveKeysOrThisClientNotAuthorized(
    recipeJson: String?,
    type: DerivationOptions.Type
  ) : String =
    ApiRecipe(recipeJson, type).let { derivationOptions ->
      if (derivationOptions.clientMayRetrieveKey != true) {
        throw ClientMayNotRetrieveKeyException(type.name)
      }
      return getSeedOrThrowIfClientNotAuthorized(derivationOptions)
    }

}