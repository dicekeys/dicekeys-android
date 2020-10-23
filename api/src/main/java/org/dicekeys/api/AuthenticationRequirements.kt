package org.dicekeys.api

interface AuthenticationRequirements {
  var androidPackagePrefixesAllowed: List<String>?
  var urlPrefixesAllowed: List<String>?
  var requireAuthenticationHandshake: Boolean
}