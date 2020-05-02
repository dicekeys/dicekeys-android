package org.dicekeys.api

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test

import org.junit.Assert.*


fun compareJson(j1: Any, j2: Any) : Boolean {
    try {
        if (j1 is JSONObject && j2 is JSONObject) {
            // Compare object types
            return (j1.length() == j2.length()) &&
                    j1.keys().asSequence().all{ key -> compareJson(j1.get(key), j2.get(key)) }
        } else if (j1 is JSONArray && j2 is JSONArray) {
            // Compare array types
            if (j1.length() != j2.length()) return false
            for (i in 0 until j1.length()){
                if (!compareJson(j1.get(i), j2.get(i))) return false
            }
            return true
        } else {
            // All other types are compared via their built in equality mechanism
            return j1 == j2
        }
    } catch (e: Throwable) {
        return false;
    }
}

class DerivationOptionsTests {

    @Test
    fun DerivationOptions_toAndBack() {
        val kdo = ApiDerivationOptions.Symmetric().apply {
            // Ensure the JSON format has the "keyType" field specified
            type = requiredType  // sets "keyType": "Symmetric" since this class type is Symmetric
            algorithm = defaultAlgorithm // sets "algorithm": "XSalsa20Poly1305"
            // Set other fields in the spec in a Kotlin/Java friendly way
            clientMayRetrieveKey = true // sets "clientMayRetrieveKey": true
            // The restrictions subclass can be constructed
            restrictions = ApiDerivationOptions.Restrictions().apply {
                androidPackagePrefixesAllowed = listOf("com.example.app")
                urlPrefixesAllowed = listOf("https://example.com/app/")
            }
            // The restrictions subclass can also be modified in place
            restrictions?.apply { urlPrefixesAllowed = listOf("https://example.com/app/", "https://example.com/anotherapp") }
            // You may set JSON fields outside the spec using methods this class inherits from
            // JSONObject, since the spec allows arbitrary fields to support use cases outside
            // its original purpose
            put("salt", "S0d1um Chl0r1d3")
        }
        val derivationOptionsJson = kdo.toJson()
        val replica = ApiDerivationOptions(derivationOptionsJson)
        assertTrue(compareJson(kdo, replica))
        val expectJson = """{
                | "salt": "S0d1um Chl0r1d3",
                | "restrictions": {
                |  "urlPrefixesAllowed": [
                |   "https://example.com/app/",
                |   "https://example.com/anotherapp"
                |  ],
                |  "androidPackagePrefixesAllowed": ["com.example.app"]
                | },
                | "clientMayRetrieveKey": true,
                | "algorithm": "XSalsa20Poly1305",
                | "keyType": "Symmetric"
                |}""".trimMargin()
        assertTrue(compareJson(
            JSONObject(expectJson),
            replica
        ))
    }
}


class UnsealingInstructionsTests {

    @Test
    fun UnsealingInstructions_toAndBack() {
        val kdo = UnsealingInstructions().apply {
            // Ensure the JSON format has the "keyType" field specified
            // The restrictions subclass can be constructed
            restrictions = ApiDerivationOptions.Restrictions().apply {
                androidPackagePrefixesAllowed = listOf("com.example.app")
                urlPrefixesAllowed = listOf("https://example.com/app/", "https://evenworseexample.com")
            }
            userMustAcknowledgeThisMessage = "Only unseal this message if you are trying to " +
                "reset your PoodleMail password."
        }
        val derivationOptionsJson = kdo.toJson()
        val replica = ApiDerivationOptions(derivationOptionsJson)
        assertTrue(compareJson(kdo, replica))
        val expectJson = """{
            | "userMustAcknowledgeThisMessage": "Only unseal this message if you are trying to reset your PoodleMail password.",
            | "restrictions": {
            |   "urlPrefixesAllowed": [
            |       "https://example.com/app/",
            |       "https://evenworseexample.com"
            |   ],
            |   "androidPackagePrefixesAllowed": ["com.example.app"]
            | }
            |}""".trimMargin()
        assertTrue(compareJson(
                JSONObject(expectJson),
                replica
        ))
    }
}