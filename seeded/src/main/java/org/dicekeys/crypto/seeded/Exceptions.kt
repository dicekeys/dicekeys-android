package org.dicekeys.crypto.seeded

class UnknownException(message: String) : java.lang.Exception(message)

class ClientNotAuthorizedException(message: String) : java.lang.Exception(message)
class CryptographicVerificationFailureException(message: String) : java.lang.Exception(message)
class InvalidKeyDerivationOptionsJsonException(message: String) : java.lang.Exception(message)
class InvalidKeyDerivationOptionValueException(message: String) : java.lang.Exception(message)
class InvalidArgumentException(message: String) : java.lang.Exception(message)
class JsonParsingException(message: String) : java.lang.Exception(message)
class KeyLengthException(message: String) : java.lang.Exception(message)

class ClientPackageNotAuthorizedException(
        clientApplicationId: String?,
        authorizedPrefixes: List<String>
): java.lang.Exception("Client $clientApplicationId is not authorized to generate keys as it does not start with one of the following prefixes: ${
authorizedPrefixes.joinToString(",", "'", "'" )
}")
