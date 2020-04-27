package org.dicekeys.trustedapp.apicommands.permissionchecked

import org.dicekeys.api.ApiKeyDerivationOptions
import org.dicekeys.api.ClientPackageNotAuthorizedException
import org.dicekeys.api.PostDecryptionInstructions
import org.dicekeys.crypto.seeded.PackagedSealedMessage

/**
 * This class performs permission checks
 */
open class ApiPermissionChecks(
  private val clientsApplicationId: String,
  private val askUserForApprovalOrReturnResultIfReady: (message: String) -> Boolean?
) {
  /**
   * Ensure any non-empty string ends in a "." by appending one if necessary
   */
  private fun terminateWithDot(prefix: String): String =
    if (prefix.isEmpty() || prefix.lastOrNull() == '.')
      prefix
    else
      "${prefix}."


  internal fun isClientAuthorizedInFaceOfRestrictions(
    restrictions: ApiKeyDerivationOptions.Restrictions?
  ): Boolean = restrictions == null ||
    restrictions.androidPackagePrefixesAllowed.let { androidPackagePrefixesAllowed ->
      androidPackagePrefixesAllowed != null &&
        terminateWithDot(clientsApplicationId).let { clientsApplicationIdWithTrailingDot ->
          androidPackagePrefixesAllowed.any { prefix ->
            clientsApplicationIdWithTrailingDot.startsWith(terminateWithDot(prefix))
          }
        }
    }

  internal fun throwIfClientNotAuthorized(
    restrictions: ApiKeyDerivationOptions.Restrictions?
  ) {
    if (!isClientAuthorizedInFaceOfRestrictions(restrictions)) {
      // The client application id does not start with any of the specified prefixes
      throw ClientPackageNotAuthorizedException(clientsApplicationId, restrictions?.androidPackagePrefixesAllowed)
    }
  }

  /**
   * Verify that either no Android prefixes were specified, or that the
   */
  internal fun throwIfClientNotAuthorized(
    keyDerivationOptions: ApiKeyDerivationOptions
  ): Unit = throwIfClientNotAuthorized(keyDerivationOptions.restrictions)


  internal fun throwIfPostDecryptionInstructionsViolated(
    postDecryptionInstructions: PostDecryptionInstructions
  ) : Unit = throwIfClientNotAuthorized(postDecryptionInstructions.restrictions)

  internal fun throwIfPostDecryptionInstructionsViolated(
    postDecryptionInstructions: String
  ) : Unit = throwIfClientNotAuthorized(
    PostDecryptionInstructions(postDecryptionInstructions).restrictions
  )


  /**
   * Return true if unsealing is allowed
   * Throw if it is forbidden
   * Return false if waiting on the user to make this choice
   */
  internal fun isUnsealingAllowedByPostDecryptionInstructions(
    postDecryptionInstructions: PostDecryptionInstructions
  ) : Boolean? = postDecryptionInstructions.userMustAcknowledgeThisMessage.let { message ->
    throwIfPostDecryptionInstructionsViolated(postDecryptionInstructions)
    if (message == null) return true
    return askUserForApprovalOrReturnResultIfReady(message)
  }

  internal fun isUnsealingAllowedByPostDecryptionInstructions(
    postDecryptionInstructions: String
  ) = isUnsealingAllowedByPostDecryptionInstructions(PostDecryptionInstructions(postDecryptionInstructions))

  internal fun isUnsealingAllowedByPostDecryptionInstructions(
    packagedSealedMessage: PackagedSealedMessage
  ) = isUnsealingAllowedByPostDecryptionInstructions(packagedSealedMessage.postDecryptionInstructions)
}