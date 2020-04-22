package org.dicekeys.crypto.seeded
import org.json.JSONObject


/**
 * A class to parse and construct key-derivation options in _keyDerivationOptionsJson_ format.
 *
 * ```
 * val keyDerivationOptionsJson: String =
 *   KeyDerivationOptions.Symmetric().apply {
 *       // Ensure the JSON format has the "keyType" field specified
 *       keyType = requiredKeyType  // sets "keyType": "Symmetric" since this class type is Symmetric
 *       algorithm = defaultAlgorithm // sets "algorithm": "XSalsa20Poly1305"
 *       hashFunction = HashFunction.Argon2id // sets "hashFunction": "Argon2id"
 *       hashFunctionIterations = 4L // sets numeric (non-quoted) field "hashFunctionIterations": 4
 *   }.toJson() // converts KeyDerivationOptions to JSON string format
 * }
 * ```
 *
 * Note: This class inherits from org.json.JSONObject, the documentation of which discourages
 * classes from inheriting from it because overriding methods can lead to undocumented behaviors.
 * In our case, we do not override (change) any methods or functionality of the underlying
 * JSONObject.
 * Rather, we only _augment_ this class, leaving the underlying functionality untouched.
 * We add only
 *   1. Virtual fields, with getters/setters that write/read specified fields into the JSONObject
 *      with a consistent type, ensuring that these fields are not written using the wrong type
 *      (e.g. writing a numeric field as a string or using an invalid enum name in a string field).
 *   2. Classes used by those getters and setters to expose JSON string fields as more-restricted
 *      (better typed) enums and JSON Objects as more-richly typed objects.
 *
 *  Those extending this class should follow the same convention in extending only by
 *  adding virtual fields and the types used to support them.
 */
open class KeyDerivationOptions(
    keyDerivationOptionsJson: String? = null,
    val requiredKeyType: KeyType? = null
): JSONObject(
    if (keyDerivationOptionsJson == null || keyDerivationOptionsJson.isEmpty())
        "{}"
    else keyDerivationOptionsJson
) {
    /**
     * The keyType values currently supported by this library as an enum,
     * with names matching the string values in the JSON format.
     */
    enum class KeyType {
        Secret, Symmetric, Public, Signing;
    }

    /**
     * Specify whether this JSON object should be used to construct a
     * [Secret], [SymmetricKey], [PrivateKey], or [SigningKey].
     */
    var keyType: KeyType?
    get() = optString(KeyDerivationOptions::keyType.name, "").let{
        if (it.isEmpty()) requiredKeyType else KeyType.valueOf(it)
    }
    set(value) { put(KeyDerivationOptions::keyType.name, value?.name) }

    /**
     * Specify the specific algorithm to use for the supported cryptographic operation(s).
     * Do not set if `"keyType": "Secret"`.
     */
    enum class Algorithm {
        XSalsa20Poly1305, X25519, Ed25519;
    }

    /**
     * A read-only field that yields the default algorithm to use for the keyType
     * that has been ste for this object.  If keyType isn't set, or is set to
     * a value with no algorithm (Secret), then the value is null.
     */
    val defaultAlgorithm: Algorithm? get() = when(this.keyType) {
        KeyType.Public -> Algorithm.X25519
        KeyType.Symmetric -> Algorithm.XSalsa20Poly1305
        KeyType.Signing -> Algorithm.Ed25519
        else -> null
    }

    /**
     * Specifies the hash function used to derive the key.
     */
    var algorithm: Algorithm?
        get() = optString(KeyDerivationOptions::algorithm.name, "").let{
            if (it.isEmpty()) defaultAlgorithm else Algorithm.valueOf(it) }
        set(value) { put(KeyDerivationOptions::algorithm.name, value?.name) }

    /**
     * Specifies the key-length in bytes if using a cryptographic operation for which
     * multiple key lengths are supported, or the length of the secret to be derived
     * if `"keyType": "Secret"`.
     */
    var keyLengthInBytes: Int?
        get() = if (has(KeyDerivationOptions::keyLengthInBytes.name))
            getInt(KeyDerivationOptions::keyLengthInBytes.name) else null
        set(value) { put(KeyDerivationOptions::keyLengthInBytes.name, value) }

    /**
     * The key-derivation hash functions currently supported by this library,
     * with names matching the string values in the JSON format.
     */
    enum class HashFunction {
        BLAKE2b, SHA256, Argon2id, Scrypt
    }

    /**
     * The default hash function is SHA256
     */
    val defaultHashFunction = HashFunction.SHA256

    /**
     * The hash function the use to derive the secret or the key seed.
     */
    var hashFunction: HashFunction
        get() = HashFunction.valueOf(optString(KeyDerivationOptions::hashFunction.name, HashFunction.SHA256.name))
        set(value) { put(KeyDerivationOptions::hashFunction.name, value.name) }

    /**
     * If using the memory-intensive `Argon2id` or `Scrypt` hash functions, you can set the
     * memory required for the hash function via this value.  The default is 67108864.
     */
    var hashFunctionMemoryLimit: Long
        get() =
            when(hashFunction) {
                HashFunction.Argon2id, HashFunction.Scrypt ->
                    optLong(KeyDerivationOptions::hashFunctionMemoryLimit.name, 67108864L)
                else -> throw InvalidKeyDerivationOptionValueException(
                    "hashFunctionMemoryLimit not defined for hash function ${hashFunction.name}")
            }
        set(value) {
            when(hashFunction) {
                HashFunction.Argon2id, HashFunction.Scrypt ->
                    put(KeyDerivationOptions::hashFunctionMemoryLimit.name, value)
            else -> throw InvalidKeyDerivationOptionValueException(
                "hashFunctionMemoryLimit cannot be set for hash function ${hashFunction.name}")
            }
        }

    /**
     * If using the memory-intensive `Argon2id` or `Scrypt` hash functions, you can set the
     * number of iterative cycles (which those algorithms call opsLimit) via this field.
     */
    var hashFunctionIterations: Long
        get() =
            when(hashFunction) {
                HashFunction.Argon2id, HashFunction.Scrypt ->
                    optLong(KeyDerivationOptions::hashFunctionIterations.name, 2L)
                else -> throw InvalidKeyDerivationOptionValueException(
                    "hashFunctionIterations not defined for hash function ${hashFunction.name}")
            }
        set(value) {
            when(hashFunction) {
                HashFunction.Argon2id, HashFunction.Scrypt ->
                    put(KeyDerivationOptions::hashFunctionIterations.name, value)
                else -> throw InvalidKeyDerivationOptionValueException(
                    "hashFunctionIterations cannot be set for hash function ${hashFunction.name}")
            }
        }

    /**
     * Generate JSON for these key-derivation options.
     *
     * *DO NOT* assume this will always generate the same JSON string, as the JSON
     * spec allows fields to be placed in different orders.
     * Any change will yield a different key.
     * Rather, the original JSON string should be preserved.
     * This library is designed to preserve the keyDerivationOptionsJson string for you.
     * All derived keys, including the public [PublicKey] and [SignatureVerificationKey], contain
     * the keyDerivationOptionsJson used to derive them so that the corresponding
     * [PrivateKey] and [SigningKey] can be re-derived if needed.
     * All values sealed by a [SymmetricKey] or [PublicKe] are returned in a
     * [PackagedSealedMessage] class which contains the keyDerivationOptionsJson needed
     * to re-derive the keys to unseal the message (but not the seed, as that would
     * allow anyone who intercepts the message to re-derive the key.)
     */
    fun toJson(indent: Int? = null): String =
        if (indent == null) toString() else toString(indent)

    init {
        keyType?.also{
        if (requiredKeyType != null && it != requiredKeyType) {
            throw InvalidKeyDerivationOptionValueException(
                """Key-derivation options assigned "keyType": "${it.name}" when ${requiredKeyType.name} was required."""
            )
        }}
    }

    class Public(keyDerivationOptionsJson: String? = null) :
        KeyDerivationOptions(keyDerivationOptionsJson,  KeyType.Public)

    class Seed(keyDerivationOptionsJson: String? = null) :
        KeyDerivationOptions(keyDerivationOptionsJson,  KeyType.Secret)

    class Signing(keyDerivationOptionsJson: String? = null) :
        KeyDerivationOptions(keyDerivationOptionsJson,  KeyType.Signing)

    class Symmetric(keyDerivationOptionsJson: String? = null) :
        KeyDerivationOptions(keyDerivationOptionsJson,  KeyType.Symmetric)

}
