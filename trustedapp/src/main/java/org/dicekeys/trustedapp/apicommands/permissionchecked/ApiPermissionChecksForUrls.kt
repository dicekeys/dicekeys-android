package org.dicekeys.trustedapp.apicommands.permissionchecked

import org.dicekeys.api.ApiDerivationOptions
import org.dicekeys.api.ClientPackageNotAuthorizedException
import org.dicekeys.api.ClientUriNotAuthorizedException


/**
 * This class performs permission checks
 */
open class ApiPermissionChecksForUrls(
  private val clientsUrl: String,
  private val askUserForApprovalOrReturnResultIfReady: (message: String) -> Boolean
): ApiPermissionChecks(askUserForApprovalOrReturnResultIfReady) {
  /**
   * Ensure any non-empty string ends in a "." by appending one if necessary
   */

  override fun isClientAuthorizedInFaceOfRestrictions(
    restrictions: ApiDerivationOptions.Restrictions?
  ): Boolean = restrictions == null ||
    restrictions.urlPrefixesAllowed.let { urlPrefixesAllowed ->
      urlPrefixesAllowed != null &&
      urlPrefixesAllowed.any { prefix ->
        clientsUrl.startsWith(prefix)
      }
    }

  override fun throwIfClientNotAuthorized(
    restrictions: ApiDerivationOptions.Restrictions?
  ): Unit {
    if (!isClientAuthorizedInFaceOfRestrictions(restrictions)) {
      // The client application id does not start with any of the specified prefixes
      throw ClientUriNotAuthorizedException(clientsUrl, restrictions?.urlPrefixesAllowed)
    }
  }
}