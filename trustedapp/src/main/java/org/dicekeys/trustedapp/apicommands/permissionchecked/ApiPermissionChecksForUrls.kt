package org.dicekeys.trustedapp.apicommands.permissionchecked

import kotlinx.coroutines.Deferred
import org.dicekeys.api.AuthenticationRequirements
import org.dicekeys.api.ClientUriNotAuthorizedException
import org.dicekeys.api.UnsealingInstructions


/**
 * This class performs permission checks
 */
open class ApiPermissionChecksForUrls(
  private val replyToUrl: String,
  private val handshakeAuthenticatedUrl: String?,
  requestUsersConsent: (UnsealingInstructions.RequestForUsersConsent
  ) -> Deferred<UnsealingInstructions.RequestForUsersConsent.UsersResponse>
): ApiPermissionChecks(requestUsersConsent) {


  override fun doesClientMeetAuthenticationRequirements(
    authenticationRequirements: AuthenticationRequirements
  ): Boolean =
      authenticationRequirements.urlPrefixesAllowed.let { urlPrefixesAllowed ->
        urlPrefixesAllowed == null ||
        urlPrefixesAllowed.any { prefix ->
          // If the prefix appears in the URL associated with the authentication token
          (handshakeAuthenticatedUrl != null && handshakeAuthenticatedUrl.startsWith(prefix)) ||
          // Or no handshake is required and the replyUrl starts with the prefix
          (!authenticationRequirements.requireAuthenticationHandshake && replyToUrl.startsWith(prefix))
        }
      }

  override fun throwIfClientNotAuthorized(
    authenticationRequirements: AuthenticationRequirements
  ): Unit {
    if (!doesClientMeetAuthenticationRequirements(authenticationRequirements)) {
      // The client application id does not start with any of the specified prefixes
      throw ClientUriNotAuthorizedException(
        if (authenticationRequirements.requireAuthenticationHandshake) (handshakeAuthenticatedUrl ?: "") else replyToUrl,
        authenticationRequirements.urlPrefixesAllowed ?: listOf<String>())
    }
  }
}