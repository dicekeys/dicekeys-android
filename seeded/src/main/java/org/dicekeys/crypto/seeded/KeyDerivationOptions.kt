package org.dicekeys.crypto.seeded
import org.json.JSONArray
import org.json.JSONObject


/**
 * A class to parse and construct key-derivation options in _keyDerivationOptionsJson_ format.
 *
 * FIXME
 * Explain getter/setter
 * Link to
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
    enum class KeyType {
        Seed, Symmetric, Public, Signing;
    }

    var keyType: KeyType?
    get() = optString(KeyDerivationOptions::keyType.name, "").let{
        if (it.isEmpty()) requiredKeyType else KeyType.valueOf(it)
    }
    set(value) { put(KeyDerivationOptions::keyType.name, value?.name) }

    enum class Algorithm {
        XSalsa20Poly1305, X25519, Ed25519;
    }

    val defaultAlgorithm: Algorithm? get() = when(this.keyType) {
        KeyType.Public -> Algorithm.X25519
        KeyType.Symmetric -> Algorithm.XSalsa20Poly1305
        KeyType.Signing -> Algorithm.Ed25519
        else -> null
    }

    var algorithm: Algorithm?
        get() = optString(KeyDerivationOptions::algorithm.name, "").let{
            if (it.isEmpty()) defaultAlgorithm else Algorithm.valueOf(it) }
        set(value) { put(KeyDerivationOptions::algorithm.name, value?.name) }

    var keyLengthInBytes: Int?
        get() = if (has(KeyDerivationOptions::keyLengthInBytes.name))
            getInt(KeyDerivationOptions::keyLengthInBytes.name) else null
        set(value) { put(KeyDerivationOptions::keyLengthInBytes.name, value) }

    enum class HashFunction {
        BLAKE2b, SHA256, Argon2id, Scrypt
    }
    val defaultHashFunction = HashFunction.SHA256
    var hashFunction: HashFunction
        get() = HashFunction.valueOf(optString(KeyDerivationOptions::hashFunction.name, HashFunction.SHA256.name))
        set(value) { put(KeyDerivationOptions::hashFunction.name, value.name) }

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
        KeyDerivationOptions(keyDerivationOptionsJson,  KeyType.Seed)

    class Signing(keyDerivationOptionsJson: String? = null) :
        KeyDerivationOptions(keyDerivationOptionsJson,  KeyType.Signing)

    class Symmetric(keyDerivationOptionsJson: String? = null) :
        KeyDerivationOptions(keyDerivationOptionsJson,  KeyType.Symmetric)

}
