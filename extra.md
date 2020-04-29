## DiceKeys for Android

<!-- ### What are DiceKeys? -->

### Overview

The DiceKeys app enables mutually-distrusting applications to derive keys and
other secrets from the user's DiceKey without actually seeing the DiceKey.

Your applications can communicate with the DiceKeys app via the [DiceKeysApiClient].
You can ask the DiceKeys app to derive cryptographic keys seeded by the user's DiceKey,
to perform cryptographic operations using the derived keys,
and to give those keys to your application if it is authorized to receive them.
You specify how keys are derived and who can access them via the
[Key-Derivation Options JSON Format](hhttps://dicekeys.github.io/seeded-crypto/derivation_options_format.html/),
which you can construct and parse using the [ApiKeyDerivationOptions] class.

The API builds on the the cross-platform
[Seeded Cryptography C++ Library](https://dicekeys.github.io/seeded-crypto/).
That library implements seeded
symmetric keys ([SymmetricKey]);
assymetric key pairs for public-key encryption ([PublicKey]) and decryption ([PrivateKey]);
assymetric key pairs for digital signatures ([SigningKey]) and their verification [SignatureVerificationKey]),
as well as a general-purpose derived [Secret].
When messages are sealed with the _seal_ operation of [SymmetricKey] or [PublicKey],
the ciphertext is stored within a [PackagedSealedMessage].

The FIDO writer application, [com.dicekeys.fidowriter], is a real-world
use case which is also small enough to be a good sample application.
It seeds FIDO security keys with a secret it has the DiceKeys app derive
from the user's DiceKey.
If the FIDO security key is lost, the user can use this app to re-derive the
secret from their DiceKey, and turn a replacement key into a perfect replica
of the security key they lost.
(Only a limited set of FIDO security keys support this write operation.)

<!-- #### Packages primarily intended for internal use by the DiceKeys App
The DiceKeys app itself uses the [org.dicekeys.read] package to scan in a DiceKey via the
Android devices camera, representing the result in a format represented by [org.dicekeys.keysqr].
They are included here for transparency. -->

### Example
```kotlin
/**
 * A sample Android activity using the DiceKeys Client API to seal/unseal a message.
 */
class SampleActivity: AppCompatActivity() {
    private lateinit var diceKeysApiClient: DiceKeysApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create a client for access the DiceKeys API
        diceKeysApiClient = DiceKeysApiClient.create(this)
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
            val keyDerivationOptionsJson = ApiKeyDerivationOptions().apply {
                restrictions = ApiKeyDerivationOptions.Restrictions().apply {
                    // The activity's packageName field contains the name of this package
                    androidPackagePrefixesAllowed = listOf(packageName)
                }
            }.toJson()
            // Get a public key derived form the user's DiceKey.
            // (Most apps will get this once and store it, rather than ask for it every time.)
            val publicKey = diceKeysApiClient.getPublicKey(keyDerivationOptionsJson)
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
```