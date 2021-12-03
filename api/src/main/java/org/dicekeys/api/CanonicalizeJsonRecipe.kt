package org.dicekeys.api

import kotlinx.serialization.json.*

fun compareObjectFieldNames(a: String, b: String):Int{
    return when{
        // The "#" (sequence number) field always comes last
        a == "#" -> 1
        b == "#" -> -1
        // The "purpose" field always comes first
        a == "purpose" -> - 1
        b == "purpose" -> 1
        // Otherwise, sort in alphabetical order
        else -> a.compareTo(b)
    }
}

fun toCanonicalizeRecipeJson(parsedJson: JsonElement) : String{
    return when(parsedJson){
        is JsonNull -> {
            "null"
        }
        is JsonPrimitive -> {
            val primitive = parsedJson.jsonPrimitive
            if(primitive.isString) "\"${primitive.content}\"" else primitive.content
        }
        is JsonArray -> {
            "[${parsedJson.jsonArray.joinToString(",") { toCanonicalizeRecipeJson(it) }}]"
        }
        is JsonObject -> {
            parsedJson.jsonObject.keys.toSortedSet { a, b ->
                compareObjectFieldNames(a, b)
            }.map { field ->
                "\"$field\":${toCanonicalizeRecipeJson(parsedJson.jsonObject[field]!!)}"
            }.joinToString(",").let {
                "{$it}"
            }
        }
    }
}

fun canonicalizeRecipeJson(recipeJson: String?): String?{
    if(recipeJson == null) return null

    return try {
        val recipeJsonObj = Json{
            isLenient = true
        }.parseToJsonElement(recipeJson)
        return toCanonicalizeRecipeJson(recipeJsonObj.jsonObject)
    }catch (e: Exception){
        e.printStackTrace()
        null
    }
}