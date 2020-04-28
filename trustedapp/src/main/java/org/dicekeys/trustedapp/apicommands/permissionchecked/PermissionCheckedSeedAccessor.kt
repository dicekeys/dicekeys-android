package org.dicekeys.trustedapp.apicommands.permissionchecked

import android.app.Activity
import android.content.Intent
import org.dicekeys.api.ApiKeyDerivationOptions
import org.dicekeys.api.ClientMayNotRetrieveKeyException
import org.dicekeys.crypto.seeded.KeyDerivationOptions
import org.dicekeys.crypto.seeded.PackagedSealedMessage
import org.dicekeys.keysqr.Face
import org.dicekeys.keysqr.KeySqr
import org.dicekeys.read.ReadKeySqrActivity
import org.dicekeys.trustedapp.state.KeySqrState

/**
 * This class abstracts away all permissions checks AND all access to the keySqr seed,
 * so that the only way the API which inherits from it can get to the seed is by
 * going through the permission checks.
 */
open class PermissionCheckedSeedAccessor(
  private val keySqr: KeySqr<Face>,
  clientsApplicationId: String,
  askUserForApprovalOrReturnResultIfReady: (message: String) -> Boolean
) :  ApiPermissionChecksForPackages(clientsApplicationId, askUserForApprovalOrReturnResultIfReady) {

  companion object {
    private var keySqrReadActivityStarted: Boolean = false

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
    fun create(
      activity: Activity,
      askUserForApprovalOrReturnResultIfReady: (message: String) -> Boolean
    ): PermissionCheckedSeedAccessor? {

      val keySqr = KeySqrState.keySqr
      if (keySqr == null) {
        // We need to first trigger an action to load the key square, then come back to this
        // intent.
        if (!keySqrReadActivityStarted) {
          keySqrReadActivityStarted = true
          val intent = Intent(activity, ReadKeySqrActivity::class.java)
          activity.startActivityForResult(intent, 0)
        }
        return null
      }
      return PermissionCheckedSeedAccessor(
        keySqr,
        activity.callingActivity?.packageName ?: "",
        askUserForApprovalOrReturnResultIfReady
      )
    }
  }

  private fun getSeedOrThrowIfClientNotAuthorized(
    keyDerivationOptions: ApiKeyDerivationOptions
  ): String {
    throwIfClientNotAuthorized(keyDerivationOptions)
    return keySqr.toKeySeed(keyDerivationOptions.excludeOrientationOfFaces)
  }

  /**
   * Request a seed generated from the user's DiceKey and salted by the
   * keyDerivationOptionsJson string, using the ApiKeyDerivationOptions
   * specified by that string. Implements guards to ensure that the
   * keyDerivationOptionJson allow the client application with the
   * requester's package name to perform operations with this key.
   */
  internal fun getSeedOrThrowIfClientNotAuthorized(
    keyDerivationOptionsJson: String?,
    keyType: KeyDerivationOptions.KeyType
  ): String = getSeedOrThrowIfClientNotAuthorized(
    ApiKeyDerivationOptions(keyDerivationOptionsJson, keyType)
  )

  /**
   * Used to guard calls to the unseal operation of SymmetricKey and PrivateKey,
   * which not only needs to authorize via keyDerivationOptionsJson, but
   * also the postDecryptionOptions in PackagedSealedMessage
   *
   * Requests a seed generated from the user's DiceKey and salted by the
   * keyDerivationOptionsJson string, using the ApiKeyDerivationOptions
   * specified by that string. Implements guards to ensure that the
   * keyDerivationOptionJson allow the client application with the
   * requester's package name to perform operations with this key.
   */
  internal fun getSeedOrThrowIfClientNotAuthorizedToUnseal(
    packagedSealedMessage: PackagedSealedMessage,
    keyType: KeyDerivationOptions.KeyType
  ): String {
    throwIfPostDecryptionInstructionsViolated(packagedSealedMessage.postDecryptionInstructions)
    return getSeedOrThrowIfClientNotAuthorized(
      ApiKeyDerivationOptions(packagedSealedMessage.keyDerivationOptionsJson, keyType)
    )
  }

  /**
   * Requests a seed generated from the user's DiceKey and salted by the
   * keyDerivationOptionsJson string, using the ApiKeyDerivationOptions
   * specified by that string. Implements guards to ensure that the
   * keyDerivationOptionJson allow the client application with the
   * requester's package name to perform operations with this key.
   *
   * This is used to guard calls to APIs calls used to return raw non-public
   * keys: PrivateKey, SigningKey, or SymmetricKey. The specification only
   * allows the DiceKeys app to return these keys to the requesting app
   * if the keyDerivationOptionsJson has `"clientMayRetrieveKey": true`.
   */
  internal fun getSeedOrThrowIfClientsMayNotRetrieveKeysOrThisClientNotAuthorized(
    keyDerivationOptionsJson: String?,
    keyType: KeyDerivationOptions.KeyType
  ) : String =
    with(ApiKeyDerivationOptions(keyDerivationOptionsJson, keyType), {
      if (!clientMayRetrieveKey) {
        throw ClientMayNotRetrieveKeyException(keyType.name)
      }
      return getSeedOrThrowIfClientNotAuthorized(this)
    })

}