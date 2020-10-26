package org.dicekeys.api

import org.json.JSONArray
import org.json.JSONObject


fun getAllow(jsonObj: JSONObject): List<WebBasedApplicationIdentity>? {
  if (!jsonObj.has(AuthenticationRequirements::allow.name)) {
    return null
  }
  val jsonArray = jsonObj.getJSONArray(AuthenticationRequirements::allow.name)
  val list: MutableList<WebBasedApplicationIdentity> = ArrayList(jsonArray.length())
  for (i in 0 until jsonArray.length()) {
    val webBasedApplicationIdentityJsonObject = jsonArray.optJSONObject(i)
    if (webBasedApplicationIdentityJsonObject != null && webBasedApplicationIdentityJsonObject.has(WebBasedApplicationIdentity::host.name)) {
      val host = webBasedApplicationIdentityJsonObject.getString(WebBasedApplicationIdentity::host.name)
      val paths = JsonStringListHelpers.getJsonObjectsStringListOrNull(webBasedApplicationIdentityJsonObject, WebBasedApplicationIdentity::paths.name)
      list.add(WebBasedApplicationIdentity(host, paths))
    }
  }
  return list.toList()
}

fun setAllow(
  jsonObject: JSONObject,
  webBasedApplicationIdentities: List<WebBasedApplicationIdentity>?
) {
  if (webBasedApplicationIdentities == null) {
    jsonObject.remove(AuthenticationRequirements::allow.name);
  } else {
    jsonObject.put(AuthenticationRequirements::allow.name, JSONArray(
      webBasedApplicationIdentities.map{ id -> id.jsonObject }
    ))
  }
}

fun getAndroidApplicationPrefixes(jsonObj: JSONObject): List<String>? =
    JsonStringListHelpers.getJsonObjectsStringListOrNull(
      jsonObj,
      AuthenticationRequirements::allowAndroidPrefixes.name
  )
fun setAndroidApplicationPrefixes(jsonObj: JSONObject, value: List<String>?) {
    jsonObj.put(AuthenticationRequirements::allowAndroidPrefixes.name, JSONArray(value) ) }

interface AuthenticationRequirements {
  var allowAndroidPrefixes: List<String>?
  var allow: List<WebBasedApplicationIdentity>?
  var requireAuthenticationHandshake: Boolean?
}