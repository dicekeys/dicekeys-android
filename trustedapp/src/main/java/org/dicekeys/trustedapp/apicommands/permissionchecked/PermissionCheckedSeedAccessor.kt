package org.dicekeys.trustedapp.apicommands.permissionchecked

import android.app.Activity
import kotlinx.coroutines.Deferred
import org.dicekeys.api.ApiDerivationOptions
import org.dicekeys.api.ApiStrings
import org.dicekeys.api.ClientMayNotRetrieveKeyException
import org.dicekeys.api.UnsealingInstructions
import org.dicekeys.crypto.seeded.DerivationOptions
import org.dicekeys.crypto.seeded.PackagedSealedMessage
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.SimpleDiceKey
import org.dicekeys.trustedapp.state.DiceKeyState

/**
 * This class abstracts away all permissions checks AND all access to the diceKey seed,
 * so that the only way the API which inherits from it can get to the seed is by
 * going through the permission checks.
 */
open class PermissionCheckedSeedAccessor(
  private val permissionChecks: ApiPermissionChecks,
  private val loadDiceKey: () -> Deferred<SimpleDiceKey>
) {

  private suspend fun getDiceKey(): SimpleDiceKey =
    DiceKeyState.diceKey
      ?: loadDiceKey().await().also { diceKey -> DiceKeyState.setDiceKey(diceKey) }

  companion object {
    /**
     * Try to construct a permission-checked accessor for DiceKey seeds.
     *
     * If there is a DiceKey loaded into the DiceKeyState, this function returns a
     * PermissionCheckedSeedAccessor which will generate seeds for the API while
     * protecting the raw DiceKey.
     *
     * If there is no DiceKey loaded into memory, this function will launch
     * an activity to load the DiceKey into memory.  The caller should
     * wait for the result with onActivityResult, put the loaded DiceKey
     * into the DiceKeyState, and try to create the seed accessor again.
     */
    fun createForIntentApi(
      activity: Activity,
      loadDiceKey: () -> Deferred<SimpleDiceKey>,
      requestUsersConsent: (UnsealingInstructions.RequestForUsersConsent
      ) -> Deferred<UnsealingInstructions.RequestForUsersConsent.UsersResponse>
    ): PermissionCheckedSeedAccessor = PermissionCheckedSeedAccessor(
      ApiPermissionChecksForPackages(
        activity.callingActivity?.packageName ?: "",
        requestUsersConsent
      ),
      loadDiceKey
    )

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
    derivationOptions: ApiDerivationOptions,
  ): String {
    permissionChecks.throwIfClientNotAuthorized(derivationOptions)
    return getDiceKey().toKeySeed(derivationOptions.excludeOrientationOfFaces)
  }

  /**
   * Request a seed generated from the user's DiceKey and salted by the
   * derivationOptionsJson string, using the ApiDerivationOptions
   * specified by that string. Implements guards to ensure that the
   * keyDerivationOptionsJson allow the client application with the
   * requester's package name to perform operations with this key.
   */
  internal suspend fun getSeedOrThrowIfClientNotAuthorized(
    derivationOptionsJson: String?,
    type: DerivationOptions.Type,
    command: String
  ): String {
    val derivationOptions = ApiDerivationOptions(derivationOptionsJson, type)
    if (command == ApiStrings.Commands.getSealingKey  &&  (derivationOptionsJson == null || derivationOptionsJson.isEmpty())) {
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
   * which not only needs to authorize via derivationOptionsJson, but
   * also the postDecryptionOptions in PackagedSealedMessage
   *
   * Requests a seed generated from the user's DiceKey and salted by the
   * derivationOptionsJson string, using the ApiDerivationOptions
   * specified by that string. Implements guards to ensure that the
   * keyDerivationOptionsJson allow the client application with the
   * requester's package name to perform operations with this key.
   */
  internal suspend fun getSeedOrThrowIfClientNotAuthorizedToUnseal(
    packagedSealedMessage: PackagedSealedMessage,
    type: DerivationOptions.Type,
    command: String
  ): String {
    permissionChecks.throwIfUnsealingInstructionsViolated(packagedSealedMessage.unsealingInstructions)
    return getSeedOrThrowIfClientNotAuthorized(
      packagedSealedMessage.derivationOptionsJson,
      type,
      command
    )
  }

  /**
   * Requests a seed generated from the user's DiceKey and salted by the
   * derivationOptionsJson string, using the ApiDerivationOptions
   * specified by that string. Implements guards to ensure that the
   * keyDerivationOptionsJson allow the client application with the
   * requester's package name to perform operations with this key.
   *
   * This is used to guard calls to APIs calls used to return raw non-public
   * keys: PrivateKey, SigningKey, or SymmetricKey. The specification only
   * allows the DiceKeys app to return these keys to the requesting app
   * if the derivationOptionsJson has `"clientMayRetrieveKey": true`.
   */
  internal suspend fun getSeedOrThrowIfClientsMayNotRetrieveKeysOrThisClientNotAuthorized(
    derivationOptionsJson: String?,
    type: DerivationOptions.Type
  ) : String =
    ApiDerivationOptions(derivationOptionsJson, type).let { derivationOptions ->
      if (derivationOptions.clientMayRetrieveKey != true) {
        throw ClientMayNotRetrieveKeyException(type.name)
      }
      return getSeedOrThrowIfClientNotAuthorized(derivationOptions)
    }

}