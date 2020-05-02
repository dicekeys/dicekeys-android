package org.dicekeys.trustedapp.apicommands.permissionchecked

import kotlinx.coroutines.Deferred
import org.dicekeys.api.ApiDerivationOptions
import org.dicekeys.api.ClientPackageNotAuthorizedException
import org.dicekeys.api.UnsealingInstructions


/**
 * This class performs permission checks
 */
open class ApiPermissionChecksForPackages(
  private val clientsApplicationId: String,
  requestUsersConsent: (UnsealingInstructions.RequestForUsersConsent
  ) -> Deferred<UnsealingInstructions.RequestForUsersConsent.UsersResponse>
): ApiPermissionChecks(requestUsersConsent) {
  /**
   * Ensure any non-empty string ends in a "." by appending one if necessary
   */
  private fun terminateWithDot(prefix: String): String =
    if (prefix.isEmpty() || prefix.lastOrNull() == '.')
      prefix
    else
      "${prefix}."

  override fun isClientAuthorizedInFaceOfRestrictions(
    restrictions: ApiDerivationOptions.Restrictions?
  ): Boolean = restrictions == null ||
    restrictions.androidPackagePrefixesAllowed.let { androidPackagePrefixesAllowed ->
      androidPackagePrefixesAllowed != null &&
        terminateWithDot(clientsApplicationId).let { clientsApplicationIdWithTrailingDot ->
          androidPackagePrefixesAllowed.any { prefix ->
            clientsApplicationIdWithTrailingDot.startsWith(terminateWithDot(prefix))
          }
        }
    }

  public override fun throwIfClientNotAuthorized(
    restrictions: ApiDerivationOptions.Restrictions?
  ): Unit {
    if (!isClientAuthorizedInFaceOfRestrictions(restrictions)) {
      // The client application id does not start with any of the specified prefixes
      throw ClientPackageNotAuthorizedException(clientsApplicationId, restrictions?.androidPackagePrefixesAllowed)
    }
  }


  /**
     * Return true if unsealing is allowed
     * Throw if it is forbidden
     * Return false if waiting on the user to make this choice
     */
//  internal fun isUnsealingAllowedByUnsealingInstructions(
//    unsealingInstructions: UnsealingInstructions
//  ) : Boolean? = unsealingInstructions.userMustAcknowledgeThisMessage.let { message ->
//    throwIfUnsealingInstructionsViolated(unsealingInstructions)
//    if (message == null) return true
//    return askUserForApprovalOrReturnResultIfReady(message)
//  }
//
//  internal fun isUnsealingAllowedByUnsealingInstructions(
//    unsealingInstructions: String
//  ) = isUnsealingAllowedByUnsealingInstructions(UnsealingInstructions(unsealingInstructions))
//
//  internal fun isUnsealingAllowedByUnsealingInstructions(
//    packagedSealedMessage: PackagedSealedMessage
//  ) = isUnsealingAllowedByUnsealingInstructions(packagedSealedMessage.unsealingInstructions)
//  }
}