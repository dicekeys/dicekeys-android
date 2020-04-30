package org.dicekeys.trustedapp.apicommands.permissionchecked

import org.dicekeys.api.ApiDerivationOptions
import org.dicekeys.api.PostDecryptionInstructions
import org.dicekeys.crypto.seeded.ClientNotAuthorizedException

/**
 * Abstract away all permissions checks for the DiceKeys API
 *
 * @param askUserForApprovalOrReturnResultIfReady You must pass this function, which is
 * called if a message must be shown to the user which will allow them to choose whether
 * to return unsealed data or not.  Your function should return true if the user has
 * already authorized the action, false if they rejected the action, or throw an exception
 * if waiting for the action to complete.
 */
abstract class ApiPermissionChecks(
  private val askUserForApprovalOrReturnResultIfReady: (message: String) -> Boolean
) {
  /**
   * Those inheriting this class must implement this test of whether
   * a client is authorized.
   */
  abstract fun isClientAuthorizedInFaceOfRestrictions(
    restrictions: ApiDerivationOptions.Restrictions?
  ): Boolean

  protected abstract fun throwIfClientNotAuthorized(
    restrictions: ApiDerivationOptions.Restrictions?
  )

  /**
   * Verify that the client is authorized to use a key or secret derived using
   * the [derivationOptions] and, if the client is not authorized,
   * throw a [ClientNotAuthorizedException].
   *
   * @throws ClientNotAuthorizedException
   */
  fun throwIfClientNotAuthorized(
    derivationOptions: ApiDerivationOptions
  ): Unit = throwIfClientNotAuthorized(derivationOptions.restrictions)


  /**
   * Verify that PostDecryptionInstructions do not forbid the client from using
   * unsealing a message.  If the client is not authorized,
   * throw a [ClientNotAuthorizedException].
   *
   * @throws ClientNotAuthorizedException
   */
  fun throwIfPostDecryptionInstructionsViolated(
    postDecryptionInstructions: PostDecryptionInstructions
  ) : Unit {
    throwIfClientNotAuthorized(postDecryptionInstructions.restrictions)
    postDecryptionInstructions.userMustAcknowledgeThisMessage?.let{ message ->
      if (!askUserForApprovalOrReturnResultIfReady(message)) {
        throw ClientNotAuthorizedException("Operation declined by user")
      }
    }
  }

  /**
   * Verify that PostDecryptionInstructions do not forbid the client from using
   * unsealing a message.  If the client is not authorized,
   * throw a [ClientNotAuthorizedException].
   *
   * @throws ClientNotAuthorizedException
   */
  fun throwIfPostDecryptionInstructionsViolated(
    postDecryptionInstructions: String
  ) : Unit = throwIfPostDecryptionInstructionsViolated(
    PostDecryptionInstructions(postDecryptionInstructions)
  )


}