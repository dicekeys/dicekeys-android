package org.dicekeys.api

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
        allowClause.joinToString(",", "'", "'") { wbai -> "${wbai.host} : ${wbai.paths?.joinToString { "," }}" }
      }) matches $clientsUri")
  ) {
  }
}

class ClientMayNotRetrieveKeyException(keyName: String) :
  ClientUriNotAuthorizedException("You cannot generate a $keyName without including clientMayRetrieveKey in your key derivation options.")

class UnknownApiException(message: String?): java.lang.Exception(message) {}

class UserDeclinedToAuthorizeOperation(message: String?): java.lang.Exception(message) {}