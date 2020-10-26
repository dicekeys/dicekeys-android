package org.dicekeys.api

import org.dicekeys.crypto.seeded.DerivationOptions
import org.json.JSONArray
import org.json.JSONObject


class WebBasedApplicationIdentity(
  val jsonObject: JSONObject = JSONObject()
) {

  constructor(host: String, paths: List<String>?) : this() {
    this.host = host
    this.paths = paths
  }

  /**
   * The host, which is the same as a hostname unless a non-standard https port is used (not recommended).
   * Start it with a "*." to match a domain and any of its subdomains.
   *
   * > origin = <scheme> "://" <hostname> [ ":" <port> ] = <scheme> "://" <host> = "https://" <host>
   * So, `host = origin.substr(8)`
   */
  var host: String?
    get() = this.jsonObject.optString(WebBasedApplicationIdentity::host.name, "")
    set(value) { jsonObject.put(WebBasedApplicationIdentity::host.name, value) }

  /**
   * Restrict URL-based access to the API to a limited set of URL paths.
   *
   * If the specified path on the list ends in "/"+"*", a path will validate it if it either shares
   * the same prefix (including the "/" just before the *) or if is exactly equal to prefix preceding the "/".
   * In other words, the requirement "/here/"+"*" is satisfied by "/here/and/there", "/here/", and "/here",
   * but not, "/hereandthere/", "/her", "/her/", or "/thereandhere".
   *
   * If the path does not end in "/"+"*" but does end in "*", any path that starts with the string preceding the "*"
   * will be validated.
   * In other words, the requirement "/here*" is satisfied by "/here/and/there", "/here/", "/here", and "/hereandthere/",
   * but not "/her", "/her/", "/thereandhere", or "/thereandhere/".
   *
   * If the path does not end in "*", it must match exactly.
   *
   * If paths is undefined, no path validation is ever performed.
   *
   * If paths is et to an empty list is  empty, no path will match and the URL interface operations are disallowed.
   *
   * Paths are not evaluated for the postMessage-based API as all authentication is done by
   * origin and postMessage not subject to cross-site request forgery.
   *
   */
  var paths: List<String>?
    get() = JsonStringListHelpers.getJsonObjectsStringListOrNull(
      jsonObject, WebBasedApplicationIdentity::paths.name)
    set(value) {
      if (value == null) {
        jsonObject.remove(WebBasedApplicationIdentity::paths.name)
      } else {
        jsonObject.put(
          WebBasedApplicationIdentity::paths.name,
          JSONArray(value)
        )
      }
    }
}