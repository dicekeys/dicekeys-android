package org.dicekeys.crypto.seeded

class UnknownException(message: String) : java.lang.Exception(message)

class ClientNotAuthorizedException(message: String) : java.lang.Exception(message)

/**
 * Thrown when a cryptographic operation fails.
 */

class CryptographicVerificationFailureException(message: String) : java.lang.Exception(message)

/**
 * Thrown when a keyDerivationOptionsJson parameter contains a string that is neither
 * empty nor in valid JSON format.
 */
class InvalidKeyDerivationOptionsJsonException(message: String) : java.lang.Exception(message)

/**
 * Thrown when a keyDerivationOptionsJson parameter contains a field that has an invalid
 * or forbidden value.
 */
class InvalidKeyDerivationOptionValueException(message: String) : java.lang.Exception(message)

/**
 * A generic exception for invalid arguments.
 */
class InvalidArgumentException(message: String) : java.lang.Exception(message)

/**
 * Thrown when JSON parsing fails, such as when trying to re-constitute an object
 * in JSON format that was corrupted.
 */
class JsonParsingException(message: String) : java.lang.Exception(message)

/**
 * Thrown when keyDerivationOptionsJson parameter contains an invalid keyLengthInBytes field.
 */
class KeyLengthException(message: String) : java.lang.Exception(message)
