package org.dicekeys

class InvalidKeySqrException(message: String) : Exception(message)
class ClientNotAuthorizedException(message: String) : java.lang.Exception(message)
class InvalidJsonKeyDerivationOptionsException(message: String) : java.lang.Exception(message)
class InvalidKeyDerivationOptionValueException(message: String) : java.lang.Exception(message)
class InvalidArgumentException(message: String) : java.lang.Exception(message)
class JsonParsingException(message: String) : java.lang.Exception(message)
class UnknownKeySqrApiException(message: String) : java.lang.Exception(message)
class CryptographicVerificationFailure(message: String) : java.lang.Exception(message)

class ClientNotAuthorizeException(
        clientApplicationId: String?,
        authorizedPrefixes: List<String>
): java.lang.Exception("Client $clientApplicationId is not authorized to generate key as it does not start with one of the following prefixes: ${
authorizedPrefixes.joinToString(",", "'", "'" )
}")
