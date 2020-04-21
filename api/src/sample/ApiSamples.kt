
/**
 * @suppress
 */
class ApiSamples {

fun sampleOfApiKeyDerivationOptions() {
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
    if (ApiKeyDerivationOptions.Public(keyDerivationOptionsJson).clientMayRetrieveKey) {
        // The keyDerivationOptionsJson allows clients not just to use the derived key,
        // but also to retrieve a copy of it (conditional on evaluation of 'requirements')
    } // Converts KeyDerivationOptions to JSON string format
}

}