package org.dicekeys.api

import org.dicekeys.crypto.seeded.*
import org.json.JSONArray
import org.json.JSONObject

internal fun jsonArrayToStringList(jsonArray: JSONArray) : List<String> {
    val list: MutableList<String> = ArrayList(jsonArray.length())
    for (i in 0 until jsonArray.length()) {
        list.add(jsonArray.getString(i))
    }
    return list.toList()

}

internal fun getJsonObjectsStringListOrNull(
        jsonObj: JSONObject,
        fieldName: String
): List<String>? =
        if (jsonObj.has(fieldName)) jsonArrayToStringList(jsonObj.getJSONArray(fieldName)) else null

internal fun example_a() {
    val keyDerivationOptionsJson: String =
            ApiKeyDerivationOptions.Symmetric().apply {
                // Ensure the JSON format has the "keyType" field specified
                keyType = requiredKeyType  // sets "keyType": "Symmetric" since this class type is Symmetric
                algorithm = defaultAlgorithm // sets "algorithm": "XSalsa20Poly1305"
                // Set other fields in the spec in a Kotlin/Java friendly way
                clientMayRetrieveKey = true // sets "clientMayRetrieveKey": true
                // The restrictions subclass can be constructed
                restrictions = ApiKeyDerivationOptions.Restrictions().apply {
                    androidPackagePrefixesAllowed = listOf("com.example.app")
                    urlPrefixesAllowed = listOf("https://example.com/app/")
                }
                // The restrictions subclass can also be modified in place
                restrictions?.apply { urlPrefixesAllowed = listOf("https://example.com/app/", "https://example.com/anotherapp") }
                // You may set JSON fields outside the spec using methods this class inherits from
                // JSONObject, since the spec allows arbitrary fields to support use cases outside
                // its original purpose
                put("salt", "S0d1um Chl0r1d3")
            }.toJson()
    // Use this class to parse a JSON string specifying the derivation of a public/private key
    if (ApiKeyDerivationOptions.Public(keyDerivationOptionsJson).clientMayRetrieveKey ) {
        // The keyDerivationOptionsJson allows clients not just to use the derived key,
        // but also to retrieve a copy of it (conditional on evaluation of 'requirements')
    } // Converts KeyDerivationOptions to JSON string format
}

/**
 * An extension of the more general [KeyDerivationOptions] class, which supports the
 * constructing and parsing the strings used to derive cryptographic keys from seed strings,
 * which typically appear in the API with the name _keyDerivationOptionsJson_.
 *
 * This extension adds support for features specific to the use of DiceKeys as seeds via the
 * [excludeOrientationOfFaces] option.
 *
 * This extension adds the [restrictions] field, and the [Restrictions] class, so that the
 * options string can specify which client apps and URLs are allowed to use the API to
 * perform cryptographic operations (e.g. sealing or signingd data) with the derived key.
 *
 * This extension adds the [clientMayRetrieveKey] option to indicate that clients may
 * use the API to return derived keys back to the client, so that the clients can
 * perform cryptographic operations even when the DiceKeys app or seed string are unavailable.
 *
 * ```
 * // Use this class (and its key-specific extension) to construct a JSON string specifying
 * // the derivation of a Symmetric Key that a client may retrieve.
 * val keyDerivationOptionsJson: String =
 *   ApiKeyDerivationOptions.Symmetric().apply {
 *        // Ensure the JSON format has the "keyType" field specified
 *        keyType = requiredKeyType  // sets "keyType": "Symmetric" since this class type is Symmetric
 *        algorithm = defaultAlgorithm // sets "algorithm": "XSalsa20Poly1305"
 *        // Set other fields in the spec in a Kotlin/Java friendly way
 *        clientMayRetrieveKey = true // sets "clientMayRetrieveKey": true
 *        // The restrictions subclass can be constructed
 *        restrictions = ApiKeyDerivationOptions.Restrictions().apply {
 *            androidPackagePrefixesAllowed = listOf("com.example.app")
 *            urlPrefixesAllowed = listOf("https://example.com/app/")
 *        }
 *        // The restrictions subclass can also be modified in place
 *        restrictions?.apply { urlPrefixesAllowed = listOf("https://example.com/app/", "https://example.com/anotherapp") }
 *        // You may set JSON fields outside the spec using methods this class inherits from
 *        // JSONObject, since the spec allows arbitrary fields to support use cases outside
 *        // its original purpose
 *        put("salt", "S0d1um Chl0r1d3")
 *    }.toJson()  // Converts KeyDerivationOptions to JSON string format
 *
 * // Use this class to parse a JSON string specifying the derivation of a public/private key
 * if (ApiKeyDerivationOptions.Public(keyDerivationOptionsJson).clientMayRetrieveKey ) {
 *   // The keyDerivationOptionsJson allows clients not just to use the derived key,
 *   // but also to retrieve a copy of it (conditional on evaluation of 'requirements')
 *   doSomething(...)
 * }
 * ```
 */
open class ApiKeyDerivationOptions constructor(
        keyDerivationOptionsJson: String? = null,
        requiredKeyType: KeyType? = null
): KeyDerivationOptions(
    keyDerivationOptionsJson, requiredKeyType
) {

    /**
     * This subclass is used to determine which clients/sites are permitted to use keys
     * derived from a keyDerivationOptionsJson string.
     */
    class Restrictions(
            var jsonObj: JSONObject = JSONObject()
    ) {
        /**
         * In Android, client applications are identified by their package name,
         * which must be cryptographically signed before an application can enter the
         * Google play store.
         *
         * If this value is specified, Android apps must have a package name that begins
         * with one of the provided prefixes if they are to use a derived key.
         *
         * Note that all prefixes, and the client package names they are compared to,
         * have an implicit '.' appended to to prevent attackers from registering the
         * suffix of a package name.  Hence the package name "com.example.app" is treated
         * as "com.example.app." and the prefix "com.example" is treated as
         * "com.example." so that an attacker cannot generate a key by registering
         * "com.examplesignedbyattacker".
         */
        var androidPackagePrefixesAllowed: List<String>?
            get() = getJsonObjectsStringListOrNull(
                    jsonObj, Restrictions::androidPackagePrefixesAllowed.name)
            set(value) { jsonObj.put(
                    Restrictions::androidPackagePrefixesAllowed.name, value ) }

        /**
         * On Apple platforms, applications are specified by a URL containing a domain name
         * from the Internet's Domain Name System (DNS).
         *
         * If this value is specified, applications must come from clients that have a URL prefix
         * starting with one of the items on this list if they are to use a derived key.
         *
         * Since some platforms, including iOS, do not allow the DiceKeys app to authenticate
         * the sender of an API request, the app may perform a cryptographic operation
         * only if it has been instructed to send the result to a URL that starts with
         * one of the permitted prefixes.
         */
        var urlPrefixesAllowed: List<String>?
            get() = getJsonObjectsStringListOrNull(jsonObj,
                    Restrictions::urlPrefixesAllowed.name)
            set(value) { jsonObj.put(Restrictions::urlPrefixesAllowed.name, value )}
    }

    /**
     * Unless this value is explicitly set to _true_, the DiceKeys may prevent
     * to obtain a raw derived [SymmetricKey],
     * [PrivateKey], or
     * [SigningKey].
     * Clients may retrieve a derived [PublicKey],
     * or [SignatureVerificationKey] even if this value
     * is not set or set to false.
     *
     * Even if this value is set to true, requests for keys are not permitted unless
     * the client would be authorized to perform cryptographic operations on those keys.
     * In other words, access is forbidden if the [restrictions] field is set and the
     * specified [Restrictions] are not met.
     */
    var clientMayRetrieveKey: Boolean
        get () = optBoolean(ApiKeyDerivationOptions::clientMayRetrieveKey.name, false)
        set(value)  { put(ApiKeyDerivationOptions::clientMayRetrieveKey.name, value) }

    /**
     * When using a DiceKey as a seed, setting this value to true will exclude the orientation
     * of each face from the key, so that the seed is unchanged even if orientations are misread.
     * This reduces the lieklihood that, if a user copies their DiceKey manually and does not verify
     * it, an error in copying orientation would prevent them from re-generating their key.
     * It also reduces the security of the key.
     *
     * For a key of 25 dice, it reduces the entropy by 50 (2x25) bits, from ~196 bits to ~146 bits.
     */
    var excludeOrientationOfFaces: Boolean
        get () = optBoolean(ApiKeyDerivationOptions::excludeOrientationOfFaces.name, false)
        set(value) { put(ApiKeyDerivationOptions::excludeOrientationOfFaces.name, value) }

    /**
     * Restrict which clients are permitted to use the API to work with the derived key.
     * See the documentation for [Restrcitions].
     */
    var restrictions: Restrictions?
        get() =
            if (has(ApiKeyDerivationOptions::restrictions.name))
                Restrictions(getJSONObject(ApiKeyDerivationOptions::restrictions.name))
            else null
        set(value) {
            if (value == null)
                remove(ApiKeyDerivationOptions::restrictions.name)
            else
                put(ApiKeyDerivationOptions::restrictions.name, value.jsonObj)
        }

    /**
     * An extension class that must represent a specification for a public/private key pair
     */
    class Public(keyDerivationOptionsJson: String? = null) :
            ApiKeyDerivationOptions(keyDerivationOptionsJson,  KeyType.Public)

    /**
     * An extension class that must represent a specification for a derived seed
     */
    class Seed(keyDerivationOptionsJson: String? = null) :
            ApiKeyDerivationOptions(keyDerivationOptionsJson,  KeyType.Seed)

    /**
     * An extension class that must represent a specification for a signing/verification key pair
     */
    class Signing(keyDerivationOptionsJson: String? = null) :
            ApiKeyDerivationOptions(keyDerivationOptionsJson,  KeyType.Signing)

    /**
     * An extension class that must represent a specification for a symmetric key
     */
    class Symmetric(keyDerivationOptionsJson: String? = null) :
            ApiKeyDerivationOptions(keyDerivationOptionsJson,  KeyType.Symmetric)

}
