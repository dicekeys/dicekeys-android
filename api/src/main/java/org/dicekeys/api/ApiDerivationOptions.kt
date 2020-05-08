package org.dicekeys.api

import org.dicekeys.crypto.seeded.*
import org.json.JSONArray


/**
 * Used to construct and parse the strings in
 * [key-derivation options JSON format](hhttps://dicekeys.github.io/seeded-crypto/derivation_options_format.html),
 * which specify how to derive cryptographic keys from seed string.
 * These JSON strings appear throughout the API (and in the [DiceKeysIntentApiClient]) as a
 * parameter named _derivationOptionsJson_.
 *
 * This implementation extends of the more general [DerivationOptions] class, which
 * abstracts all the general-purpose key-derivation options that aren't specific
 * to DiceKeys or the DiceKeys App/API.
 *
 * This extension adds the [restrictions] field, and the [Restrictions] class, so that the
 * options string can specify which client apps and URLs are allowed to use the API to
 * perform cryptographic operations (e.g. sealing or signed  data) with the derived key.
 *
 * This extension adds the [clientMayRetrieveKey] option to indicate that clients may
 * use the API to return derived keys back to the client, so that the clients can
 * perform cryptographic operations even when the DiceKeys app or seed string are unavailable.
 *
 * This extension adds support for the [excludeOrientationOfFaces] option, which can create
 * seeds that remain the same even if the orientation of a die within a DiceKey changes.
 *
 *
 * @sample [ApiSamples.sampleOfApiDerivationOptions]
 *

 * ```
 *
 * @see DiceKeysIntentApiClient
 * @see DerivationOptions
 * @see UnsealingInstructions
 *
 * @constructor Construct from a JSON string. When you know the type of key being derived,
 * it's better to use a key class which fills in the [requiredKeyType] field of the constructor.
 * For example [Symmetric] for the derivation options of a symmetric key.  If you pass nothing,
 * empty options will be created, which you can then configure (e.g., via _apply).
 */
open class ApiDerivationOptions constructor(
  derivationOptionsJson: String? = null,
  requiredType: Type? = null
):  AuthenticationRequirements,
    DerivationOptions(
      derivationOptionsJson, requiredType
) {

    /**
     * Unless this value is explicitly set to _true_, the DiceKeys may prevent
     * to obtain a raw derived [SymmetricKey],
     * [UnsealingKey], or
     * [SigningKey].
     * Clients may retrieve a derived [SealingKey],
     * or [SignatureVerificationKey] even if this value
     * is not set or set to false.
     *
     * Even if this value is set to true, requests for keys are not permitted unless
     * the client would be authorized to perform cryptographic operations on those keys.
     * In other words, access is forbidden if the [restrictions] field is set and the
     * specified [Restrictions] are not met.
     */
    var clientMayRetrieveKey: Boolean
        get () = optBoolean(ApiDerivationOptions::clientMayRetrieveKey.name, false)
        set(value)  { put(ApiDerivationOptions::clientMayRetrieveKey.name, value) }


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
    override var androidPackagePrefixesAllowed: List<String>?
        get() = JsonStringListHelpers.getJsonObjectsStringListOrNull(
          this, ApiDerivationOptions::androidPackagePrefixesAllowed.name)
        set(value) { put(
          ApiDerivationOptions::androidPackagePrefixesAllowed.name, JSONArray(value) ) }

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
    override var urlPrefixesAllowed: List<String>?
        get() = JsonStringListHelpers.getJsonObjectsStringListOrNull(this,
          ApiDerivationOptions::urlPrefixesAllowed.name)
        set(value) { put(ApiDerivationOptions::urlPrefixesAllowed.name, JSONArray(value) )}


  override var requireAuthenticationHandshake: Boolean
      get() = optBoolean(ApiDerivationOptions::requireAuthenticationHandshake.name, false)
      set(value) { put(ApiDerivationOptions::requireAuthenticationHandshake.name, value) }

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
        get () = optBoolean(ApiDerivationOptions::excludeOrientationOfFaces.name, false)
        set(value) { put(ApiDerivationOptions::excludeOrientationOfFaces.name, value) }


    /**
     * An extension class that must represent a specification for a public/private key pair
     */
    class UnsealingKey(derivationOptionsJson: String? = null) :
            ApiDerivationOptions(derivationOptionsJson,  Type.UnsealingKey)

    /**
     * An extension class that must represent a specification for a derived seed
     */
    class Secret(derivationOptionsJson: String? = null) :
            ApiDerivationOptions(derivationOptionsJson,  Type.Secret)

    /**
     * An extension class that must represent a specification for a signing/verification key pair
     */
    class SigningKey(derivationOptionsJson: String? = null) :
            ApiDerivationOptions(derivationOptionsJson,  Type.SigningKey)

    /**
     * An extension class that must represent a specification for a symmetric key
     */
    class SymmetricKey(derivationOptionsJson: String? = null) :
            ApiDerivationOptions(derivationOptionsJson,  Type.SymmetricKey)

}
