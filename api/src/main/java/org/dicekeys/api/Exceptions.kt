package org.dicekeys.api

/**
 * Thrown when a key to be derived has requirements in keyDerivationOptionsJson that disallow
 * the calling client application from accessing or using the key.
 */
class ClientPackageNotAuthorizedException(
        clientApplicationId: String?,
        authorizedPrefixes: List<String>
): java.lang.Exception("Client $clientApplicationId is not authorized to generate keys as it does not start with one of the following prefixes: ${
authorizedPrefixes.joinToString(",", "'", "'" )
}")
