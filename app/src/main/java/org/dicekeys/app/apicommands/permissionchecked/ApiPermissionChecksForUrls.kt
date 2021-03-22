package org.dicekeys.app.apicommands.permissionchecked

import kotlinx.coroutines.Deferred
import android.net.Uri
import org.dicekeys.api.*


/**
 * This class performs permission checks
 */
open class ApiPermissionChecksForUrls(
  private val replyToUrlString: String,
  private val handshakeAuthenticatedUrlString: String?,
  requestUsersConsent: (UnsealingInstructions.RequestForUsersConsent
  ) -> Deferred<UnsealingInstructions.RequestForUsersConsent.UsersResponse>
): ApiPermissionChecks(requestUsersConsent) {

  private val replyToUri = Uri.parse(replyToUrlString)
  private val handshakeAuthenticatedUri: Uri? = if (handshakeAuthenticatedUrlString == null) null else Uri.parse(handshakeAuthenticatedUrlString)

  private fun doesPathMatchRequirement(pathExpectedSlashOptional: String, pathObserved: String): Boolean {
    // Paths must start with a "/".  If the path requirement didn't start with a "/",
    // we'll insert one assuming this was a mistake by the developer of the client software
    // that created the recipeJson string.
    val pathExpected = if (pathExpectedSlashOptional.isEmpty() || pathExpectedSlashOptional[0] == '/')
      pathExpectedSlashOptional else "/$pathExpectedSlashOptional"

    return when {
      pathExpected.endsWith("/*") -> {
        // exact prefix match but without the closing "/"
        pathObserved === pathExpected.substring(0, pathExpected.length - 2) ||
          // exact prefix match including the closing "/", with an arbitrary-length suffix
          // as permitted by the "*"
          pathObserved.startsWith(pathExpected.substring(0, pathExpected.length -1))
      }
      pathExpected.endsWith("*") -> {
        // The path requirement specifies a prefix, so test for a prefix match
        pathObserved.startsWith(pathExpected.substring(0, pathExpected.length -1))
      }
      else -> {
        // This path requirement does not specify a prefix, so test for exact match
        pathExpected === pathObserved
      }
    }
  }
  private fun matchesWebBasedApplicationIdentity(
    webBasedApplicationIdentity: WebBasedApplicationIdentity,
    uri: Uri,
  ): Boolean {
    val pathObserved = uri.path ?: ""
    val host = webBasedApplicationIdentity.host;
    val paths = webBasedApplicationIdentity.paths;
    if (host != webBasedApplicationIdentity.host) return false;
    return if (paths == null) {
      doesPathMatchRequirement("/--derived-secret-api--/*", pathObserved)
    } else {
      paths.any { pathExpected -> doesPathMatchRequirement(pathExpected, pathObserved) }
    }
  }

  override fun doesClientMeetAuthenticationRequirements(
    authenticationRequirements: AuthenticationRequirements
  ): Boolean =
      authenticationRequirements.allow.let { allow ->
        allow != null &&
        allow.any { hostAndPaths ->
          // If the prefix appears in the URL associated with the authentication token
          (handshakeAuthenticatedUri != null && matchesWebBasedApplicationIdentity(hostAndPaths, handshakeAuthenticatedUri)) ||
          // Or no handshake is required and the replyUrl starts with the prefix
          (authenticationRequirements.requireAuthenticationHandshake != true && matchesWebBasedApplicationIdentity(hostAndPaths, replyToUri))
        }
      }

  override fun throwIfClientNotAuthorized(
    authenticationRequirements: AuthenticationRequirements
  ): Unit {
    if (!doesClientMeetAuthenticationRequirements(authenticationRequirements)) {
      // The client application id does not start with any of the specified prefixes
      throw ClientUriNotAuthorizedException(
        if (authenticationRequirements.requireAuthenticationHandshake == true) (handshakeAuthenticatedUrlString ?: "") else replyToUrlString,
        authenticationRequirements.allow ?: listOf<WebBasedApplicationIdentity>())
    }
  }
}