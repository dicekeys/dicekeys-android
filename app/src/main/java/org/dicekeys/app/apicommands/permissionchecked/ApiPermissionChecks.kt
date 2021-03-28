package org.dicekeys.app.apicommands.permissionchecked

import kotlinx.coroutines.Deferred
import org.dicekeys.api.AuthenticationRequirements
import org.dicekeys.api.ClientMayNotRetrieveKeyException
import org.dicekeys.api.UnsealingInstructions
import org.dicekeys.crypto.seeded.ClientNotAuthorizedException

/**
 * Abstract away all permissions checks for the DiceKeys API
 *
 * @param requestUsersConsent You must pass this function, which is
 * called if a message must be shown to the user which will allow them to choose whether
 * to return unsealed data or not.  Your function should return true if the user has
 * already authorized the action, false if they rejected the action, or throw an exception
 * if waiting for the action to complete.
 */
abstract class ApiPermissionChecks(
  private val requestUsersConsent: (UnsealingInstructions.RequestForUsersConsent
    ) -> Deferred<UnsealingInstructions.RequestForUsersConsent.UsersResponse>
) {

  /**
   * Those inheriting this class must implement this test of whether
   * a client is authorized.
   */
  abstract fun doesClientMeetAuthenticationRequirements(
    authenticationRequirements: AuthenticationRequirements
  ): Boolean

  abstract fun throwIfClientNotAuthorized(
    authenticationRequirements: AuthenticationRequirements
  )

  /**
   * Verify that the client is authorized to use a key or secret derived using
   * the [derivationOptions] and, if the client is not authorized,
   * throw a [ClientNotAuthorizedException].
   *
   * @throws ClientNotAuthorizedException
   */
//  fun throwIfClientNotAuthorized(
//    derivationOptions: ApiRecipe
//  ): Unit = throwIfClientNotAuthorized(derivationOptions)


  /**
   * Verify that UnsealingInstructions do not forbid the client from using
   * unsealing a message.  If the client is not authorized,
   * throw a [ClientNotAuthorizedException].
   *
   * @throws ClientNotAuthorizedException
   */
  suspend fun throwIfUnsealingInstructionsViolated(
    unsealingInstructions: UnsealingInstructions
  ) {
    throwIfClientNotAuthorized(unsealingInstructions)
    val requireUsersConsent = unsealingInstructions.requireUsersConsent ?: return;
    if (requestUsersConsent(requireUsersConsent).await() != UnsealingInstructions.RequestForUsersConsent.UsersResponse.Allow) {
      throw ClientMayNotRetrieveKeyException("Operation declined by user")
    }
  }

  /**
   * Verify that UnsealingInstructions do not forbid the client from using
   * unsealing a message.  If the client is not authorized,
   * throw a [ClientNotAuthorizedException].
   *
   * @throws ClientNotAuthorizedException
   */
  suspend fun throwIfUnsealingInstructionsViolated(
    unsealingInstructions: String?
  ): Unit {
    if (unsealingInstructions != null && unsealingInstructions.isNotEmpty())
      throwIfUnsealingInstructionsViolated(UnsealingInstructions(unsealingInstructions)
    )
  }

}