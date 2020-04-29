package org.dicekeys.trustedapp.apicommands.permissionchecked

import org.dicekeys.api.ApiDerivationOptions
import org.dicekeys.api.ClientPackageNotAuthorizedException


/**
 * This class performs permission checks
 */
open class ApiPermissionChecksForPackages(
  private val clientsApplicationId: String,
  private val askUserForApprovalOrReturnResultIfReady: (message: String) -> Boolean
): ApiPermissionChecks(askUserForApprovalOrReturnResultIfReady) {
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
//  internal fun isUnsealingAllowedByPostDecryptionInstructions(
//    postDecryptionInstructions: PostDecryptionInstructions
//  ) : Boolean? = postDecryptionInstructions.userMustAcknowledgeThisMessage.let { message ->
//    throwIfPostDecryptionInstructionsViolated(postDecryptionInstructions)
//    if (message == null) return true
//    return askUserForApprovalOrReturnResultIfReady(message)
//  }
//
//  internal fun isUnsealingAllowedByPostDecryptionInstructions(
//    postDecryptionInstructions: String
//  ) = isUnsealingAllowedByPostDecryptionInstructions(PostDecryptionInstructions(postDecryptionInstructions))
//
//  internal fun isUnsealingAllowedByPostDecryptionInstructions(
//    packagedSealedMessage: PackagedSealedMessage
//  ) = isUnsealingAllowedByPostDecryptionInstructions(packagedSealedMessage.postDecryptionInstructions)
//  }
}