package org.dicekeys.app.apicommands.permissionchecked

import android.util.Base64
import java.security.SecureRandom

object AuthenticationTokens {
  val mapOfAuthTokensToUrls = mutableMapOf<String, String>()

  fun add(respondToUrl: String) =
    (Base64.encodeToString(SecureRandom().generateSeed(20), Base64.URL_SAFE or Base64.NO_WRAP)).also {
    mapOfAuthTokensToUrls[it] = respondToUrl
  }

  fun getUrlForAuthToken(authToken: String) : String? = mapOfAuthTokensToUrls[authToken]
}