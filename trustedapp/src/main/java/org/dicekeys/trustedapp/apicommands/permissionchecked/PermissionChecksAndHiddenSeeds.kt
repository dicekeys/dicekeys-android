package org.dicekeys.trustedapp.apicommands.permissionchecked

import org.dicekeys.api.ApiKeyDerivationOptions
import org.dicekeys.api.ClientMayNotRetrieveKeyException
import org.dicekeys.crypto.seeded.KeyDerivationOptions
import org.dicekeys.keysqr.Face
import org.dicekeys.keysqr.KeySqr

/**
 * This class abstracts away all permissions checks AND all access to the keySqr seed,
 * so that the only way the Api which inherits from it can get to the seed is by
 * going through the permission checks.
 */
open class PermissionChecksAndHiddenSeeds(
  private val keySqr: KeySqr<Face>,
  clientsApplicationId: String,
  askUserForApprovalOrReturnResultIfReady: (message: String) -> Boolean?
) :  ApiPermissionChecks(clientsApplicationId, askUserForApprovalOrReturnResultIfReady) {
  internal fun getSeedOrThrowIfClientNotAuthorized(
    keyDerivationOptions: ApiKeyDerivationOptions
  ): String {
    throwIfClientNotAuthorized(keyDerivationOptions)
    return keySqr.toKeySeed(keyDerivationOptions.excludeOrientationOfFaces)
  }

  internal fun getSeedOrThrowIfClientNotAuthorized(
    keyDerivationOptionsJson: String?,
    keyType: KeyDerivationOptions.KeyType
  ): String = getSeedOrThrowIfClientNotAuthorized(
    ApiKeyDerivationOptions(keyDerivationOptionsJson, keyType)
  )

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