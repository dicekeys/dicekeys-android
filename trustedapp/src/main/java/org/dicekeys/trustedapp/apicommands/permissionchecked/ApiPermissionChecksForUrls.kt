package org.dicekeys.trustedapp.apicommands.permissionchecked

import kotlinx.coroutines.Deferred
import org.dicekeys.api.ApiDerivationOptions
import org.dicekeys.api.ClientUriNotAuthorizedException
import org.dicekeys.api.UnsealingInstructions


/**
 * This class performs permission checks
 */
open class ApiPermissionChecksForUrls(
  private val clientsUrl: String,
  requestUsersConsent: (UnsealingInstructions.RequestForUsersConsent
  ) -> Deferred<UnsealingInstructions.RequestForUsersConsent.UsersResponse>
): ApiPermissionChecks(requestUsersConsent) {

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
      throw ClientUriNotAuthorizedException(clientsUrl, restrictions?.urlPrefixesAllowed ?: listOf<String>())
    }
  }
}