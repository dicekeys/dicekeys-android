package org.dicekeys.api

import java.lang.IllegalArgumentException

/**
 * Thrown when a key to be derived has requirements in derivationOptionsJson that disallow
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

class ClientUriNotAuthorizedException(
  clientsUri: String?,
  authorizedPrefixes: List<String>?
): java.lang.Exception(
  "Client is not authorized " +
    if (authorizedPrefixes == null)
      "as no Uri prefixes have been specified in the key derivation options"
    else ("as its Uri does not start with one of the following prefixes: ${
    authorizedPrefixes.joinToString(",", "'", "'" )}")
)

class ClientMayNotRetrieveKeyException(keyName: String) :
        IllegalArgumentException("You cannot generate a $keyName without including clientMayRetrieveKey in your key derivation options.")

class DiceKeysAppNotPresentException() :
  IllegalArgumentException("The DiceKeys App is not installed")