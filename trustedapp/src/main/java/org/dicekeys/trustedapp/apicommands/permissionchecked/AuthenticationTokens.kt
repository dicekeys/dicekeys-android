package org.dicekeys.trustedapp.apicommands.permissionchecked

object AuthenticationTokens {
  val mapOfAuthTokensToUrls = mutableMapOf<String, String>()

  fun add(authToken: String, sentToRespondToUrl: String) {
    mapOfAuthTokensToUrls[authToken] = sentToRespondToUrl
  }

  fun getUrlForAuthToken(authToken: String) : String? = mapOfAuthTokensToUrls[authToken]
}