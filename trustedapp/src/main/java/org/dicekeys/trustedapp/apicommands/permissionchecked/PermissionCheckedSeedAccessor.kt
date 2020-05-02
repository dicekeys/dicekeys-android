package org.dicekeys.trustedapp.apicommands.permissionchecked

import android.app.Activity
import kotlinx.coroutines.Deferred
import org.dicekeys.api.ApiDerivationOptions
import org.dicekeys.api.ClientMayNotRetrieveKeyException
import org.dicekeys.api.UnsealingInstructions
import org.dicekeys.crypto.seeded.DerivationOptions
import org.dicekeys.crypto.seeded.PackagedSealedMessage
import org.dicekeys.keysqr.DiceKey
import org.dicekeys.trustedapp.state.KeySqrState

/**
 * This class abstracts away all permissions checks AND all access to the keySqr seed,
 * so that the only way the API which inherits from it can get to the seed is by
 * going through the permission checks.
 */
open class PermissionCheckedSeedAccessor(
  private val permissionChecks: ApiPermissionChecks,
  private val loadDiceKey: () -> Deferred<DiceKey>
) {

  private suspend fun getDiceKey(): DiceKey =
    KeySqrState.diceKey ?:
      loadDiceKey().await().also { diceKey -> KeySqrState.setDiceKey(diceKey) }

  companion object {
    /**
     * Try to construct a permission-checked accessor for DiceKey seeds.
     *
     * If there is a DiceKey loaded into the KeySqrState, this function returns a
     * PermissionCheckedSeedAccessor which will generate seeds for the API while
     * protecting the raw DiceKey.
     *
     * If there is no DiceKey loaded into memory, this function will launch
     * an activity to load the DiceKey into memory.  The caller should
     * wait for the result with onActivityResult, put the loaded DiceKey
     * into the KeySqrState, and try to create the seed accessor again.
     */
    fun createForIntentApi(
      activity: Activity,
      loadDiceKey: () -> Deferred<DiceKey>,
      requestUsersConsent: (UnsealingInstructions.RequestForUsersConsent
        ) -> Deferred<UnsealingInstructions.RequestForUsersConsent.UsersResponse>
    ): PermissionCheckedSeedAccessor? = PermissionCheckedSeedAccessor(
      ApiPermissionChecksForPackages(
        activity.callingActivity?.packageName ?: "",
        requestUsersConsent
      ),
      loadDiceKey
    )
  }


  private suspend fun getSeedOrThrowIfClientNotAuthorized(
    derivationOptions: ApiDerivationOptions
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
    type: DerivationOptions.Type
  ): String = getSeedOrThrowIfClientNotAuthorized(
    ApiDerivationOptions(derivationOptionsJson, type)
  )

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
    type: DerivationOptions.Type
  ): String {
    permissionChecks.throwIfUnsealingInstructionsViolated(packagedSealedMessage.unsealingInstructions)
    return getSeedOrThrowIfClientNotAuthorized(
      ApiDerivationOptions(packagedSealedMessage.derivationOptionsJson, type)
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
    with(ApiDerivationOptions(derivationOptionsJson, type), {
      if (!clientMayRetrieveKey) {
        throw ClientMayNotRetrieveKeyException(type.name)
      }
      return getSeedOrThrowIfClientNotAuthorized(this)
    })

}