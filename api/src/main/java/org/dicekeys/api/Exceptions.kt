package org.dicekeys.api

import java.lang.IllegalArgumentException


/**
 * Thrown when a key to be derived has requirements in recipeJson that disallow
 * the calling client application from accessing or using the key.
 */
class ClientPackageNotAuthorizedException(
        clientApplicationId: String?,
        authorizedPrefixes: List<String>?
): java.lang.Exception(
    "Client $clientApplicationId is not authorized to generate keys " +
        if (authorizedPrefixes == null)
            "as no Android package prefixes have been specified in the key derivation options"
        else ("as it does not start with one of the following prefixes: ${
            authorizedPrefixes.joinToString(",", "'", "'" )}")
)

open class ClientUriNotAuthorizedException(
  message: String?
) : Exception(message) {

  constructor(
    clientsUri: String,
    allowClause: List<WebBasedApplicationIdentity>?
  ) : this(
    "Client is not authorized " +
      if (allowClause == null)
        "as no allow clause is present in the key derivation options"
      else ("as no prefix in {${
        allowClause.joinToString(",", "'", "'") { wbai -> "$wbai.host : ${wbai.paths?.joinToString { "," }}" }
      }) matches $clientsUri")
  ) {
  }
}

class ClientMayNotRetrieveKeyException(keyName: String) :
  ClientUriNotAuthorizedException("You cannot generate a $keyName without including clientMayRetrieveKey in your key derivation options.")

class UnknownApiException(message: String?): java.lang.Exception(message) {}
