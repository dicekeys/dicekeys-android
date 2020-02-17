package org.dicekeys

class InvalidKeySqrException(message: String) : Exception(message)
class ClientNotAuthorizedException(message: String) : java.lang.Exception(message)
class InvalidJsonKeyDerivationOptionsException(message: String) : java.lang.Exception(message)
class InvalidKeyDerivationOptionValueException(message: String) : java.lang.Exception(message)
class UnknownKeySqrApiException(message: String) : java.lang.Exception(message)
