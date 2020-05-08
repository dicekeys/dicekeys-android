package org.dicekeys.api

import org.json.JSONArray
import org.json.JSONObject

internal object JsonStringListHelpers {
  fun jsonArrayToStringList(jsonArray: JSONArray): List<String> {
    val list: MutableList<String> = ArrayList(jsonArray.length())
    for (i in 0 until jsonArray.length()) {
      list.add(jsonArray.getString(i))
    }
    return list.toList()

  }

  fun getJsonObjectsStringListOrNull(
    jsonObj: JSONObject,
    fieldName: String
  ): List<String>? =
    if (jsonObj.has(fieldName)) jsonArrayToStringList(jsonObj.getJSONArray(fieldName)) else null
}