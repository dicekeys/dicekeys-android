package org.dicekeys.trustedapp.apicommands.permissionchecked

import org.dicekeys.api.ApiDerivationOptions
import org.dicekeys.api.PostDecryptionInstructions
import org.dicekeys.crypto.seeded.ClientNotAuthorizedException

abstract class ApiPermissionChecks(
  private val askUserForApprovalOrReturnResultIfReady: (message: String) -> Boolean
) {
  abstract fun isClientAuthorizedInFaceOfRestrictions(
    restrictions: ApiDerivationOptions.Restrictions?
  ): Boolean

  protected abstract fun throwIfClientNotAuthorized(
    restrictions: ApiDerivationOptions.Restrictions?
  )

  /**
   * Verify that either no Android prefixes were specified, or that the
   */
  fun throwIfClientNotAuthorized(
    derivationOptions: ApiDerivationOptions
  ): Unit = throwIfClientNotAuthorized(derivationOptions.restrictions)


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

  fun throwIfPostDecryptionInstructionsViolated(
    postDecryptionInstructions: String
  ) : Unit = throwIfPostDecryptionInstructionsViolated(
    PostDecryptionInstructions(postDecryptionInstructions)
  )


}