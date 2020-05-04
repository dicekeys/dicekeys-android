

/**
 * @supress
 */
/**
 * A sample Android activity using the DiceKeys Client API to seal/unseal a message.
 */
class SampleActivity: AppCompatActivity() {
    private lateinit var diceKeysApiClient: DiceKeysIntentApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create a client for access the DiceKeys API
        diceKeysApiClient = DiceKeysIntentApiClient.create(this)
        // Use the API to perform cryptographic operations
        sealAndUnsealASillyMessage()
    }

    /**
     * All communications in Android travels through your activity/fragment via intents.
     * Without this override, the diceKeysApiClient will not receive response to
     * the requests it issues which are returned via onActivtyResult.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        diceKeysApiClient.handleOnActivityResult(data)
    }

    /**
     * API calls go to the DiceKeys app, which may need to wait for the user to
     * scan their DiceKey into the app, and are therefore asynchronous.
     * This example uses Kotlin co-routines to call the API's suspend functions.
     * Each API call has an non-suspend equivalent where the final parameter is a callback.
     */
    private fun sealAndUnsealASillyMessage() = GlobalScope.launch(Dispatchers.Main) {
        try {
            // Derive keys that other application are forbidden from using.
            // (The DiceKeys app will refuse to (re)derive this key for other apps.)
            val derivationOptionsJson = ApiDerivationOptions().apply {
                restrictions = ApiDerivationOptions.Restrictions().apply {
                    // The activity's packageName field contains the name of this package
                    androidPackagePrefixesAllowed = listOf(packageName)
                }
            }.toJson()
            // Get a public key derived form the user's DiceKey.
            // (Most apps will get this once and store it, rather than ask for it every time.)
            val publicKey = diceKeysApiClient.getPublicKey(derivationOptionsJson)
            // With public key cryptoraphy, sealing a message does not require an API call
            // and is a fully synchronous operation (no waiting needed).
            val packagedSealedMessage = publicKey.seal("You call this a plaintext?")
            // The DiceKeys app will re-derive your private key and unseal data for you.
            val yesICallThisAPlaintext = diceKeysApiClient.unsealWithPrivateKey(
                    packagedSealedMessage
                ).toString(Charsets.UTF_8)
            // Do something with the unsealed message like share it with the user
            ...

        } catch (e: Exception) {
            // Yes, crypto fails if given the wrong key or corrupted/manipulated data,
            // so *you* need to update this code to handle exceptions *gracefully*.
            throw NotImplementedError()
        }
    }
}
/**
 * @suppress
 */
class ApiSamples {

fun sampleOfApiDerivationOptions() {
    val derivationOptionsJson: String =
            ApiDerivationOptions.Symmetric().apply {
                // Ensure the JSON format has the "keyType" field specified
                keyType = requiredKeyType  // sets "keyType": "Symmetric" since this class type is Symmetric
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
            }.toJson()
    // Use this class to parse a JSON string specifying the derivation of a public/private key
    if (ApiDerivationOptions.Public(derivationOptionsJson).clientMayRetrieveKey) {
        // The derivationOptionsJson allows clients not just to use the derived key,
        // but also to retrieve a copy of it (conditional on evaluation of 'requirements')
    } // Converts DerivationOptions to JSON string format
}

}