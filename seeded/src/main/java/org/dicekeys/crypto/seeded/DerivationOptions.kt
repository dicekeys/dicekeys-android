package org.dicekeys.crypto.seeded
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.json.JSONObject


/**
 * A class to parse and construct key-derivation options in _recipeJson_ format.
 *
 * ```
 * val recipeJson: String =
 *   DerivationOptions.Symmetric().apply {
 *       // Ensure the JSON format has the "keyType" field specified
 *       keyType = requiredKeyType  // sets "keyType": "Symmetric" since this class type is Symmetric
 *       algorithm = defaultAlgorithm // sets "algorithm": "XSalsa20Poly1305"
 *       hashFunction = HashFunction.Argon2id // sets "hashFunction": "Argon2id"
 *       hashFunctionIterations = 4L // sets numeric (non-quoted) field "hashFunctionIterations": 4
 *   }.toJson() // converts DerivationOptions to JSON string format
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
open class DerivationOptions(
    recipeJson: String? = null,
    val requiredType: Type? = null
): JSONObject(
    if (recipeJson == null || recipeJson.isEmpty())
        "{}"
    else recipeJson
) {
    /**
     * The keyType values currently supported by this library as an enum,
     * with names matching the string values in the JSON format.
     */
    @Parcelize
    enum class Type : Parcelable {
        Password, Secret, SymmetricKey, UnsealingKey, SigningKey;
    }

    enum class WordList {
        EN_512_words_5_chars_max_ed_4_20200917,
        EN_1024_words_6_chars_max_ed_4_20200917,
        EN_2048_BIPS_39;
    }

    /**
     * Specify whether this JSON object should be used to construct a
     * [Password], ][Secret], [SymmetricKey], [UnsealingKey], or [SigningKey].
     */
    var type: Type?
    get() = optString(DerivationOptions::type.name, "").let{
        if (it.isEmpty()) requiredType else Type.valueOf(it)
    }
    set(value) { put(DerivationOptions::type.name, value?.name) }

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
    val defaultAlgorithm: Algorithm? get() = when(this.type) {
        Type.UnsealingKey -> Algorithm.X25519
        Type.SymmetricKey -> Algorithm.XSalsa20Poly1305
        Type.SigningKey -> Algorithm.Ed25519
        else -> null
    }

    /**
     * Specifies the hash function used to derive the key.
     */
    var algorithm: Algorithm?
        get() = optString(DerivationOptions::algorithm.name, "").let{
            if (it.isEmpty()) defaultAlgorithm else Algorithm.valueOf(it) }
        set(value) { put(DerivationOptions::algorithm.name, value?.name) }

    /**
     * Specifies the key-length in bytes if using a cryptographic operation for which
     * multiple key lengths are supported, or the length of the secret to be derived
     * if `"keyType": "Secret"`.
     */
    var lengthInBytes: Int?
        get() = if (has(DerivationOptions::lengthInBytes.name))
            getInt(DerivationOptions::lengthInBytes.name) else null
        set(value) { put(DerivationOptions::lengthInBytes.name, value) }

    /**
     * Specifies the password length in bits for
     * if `"keyType": "Password"`.
     */
    var lengthInBits: Int?
        get() = if (has(DerivationOptions::lengthInBits.name))
            getInt(DerivationOptions::lengthInBits.name) else null
        set(value) { put(DerivationOptions::lengthInBits.name, value) }

    /**
     * Specifies the password length in words for
     * if `"keyType": "Password"`.
     */
    var lengthInWords: Int?
        get() = if (has(DerivationOptions::lengthInWords.name))
            getInt(DerivationOptions::lengthInWords.name) else null
        set(value) { put(DerivationOptions::lengthInWords.name, value) }

    /**
     * Specify the word list for a password
     */
    var wordList: WordList?
        get() = optString(DerivationOptions::wordList.name, "").let{
            WordList.valueOf(it)
        }
        set(value) { put(DerivationOptions::wordList.name, value?.name) }


    /**
     * The key-derivation hash functions currently supported by this library,
     * with names matching the string values in the JSON format.
     */
    enum class HashFunction {
        BLAKE2b, Argon2id
    }

    /**
     * The default hash function is SHA256
     */
    val defaultHashFunction = HashFunction.BLAKE2b

    /**
     * The hash function the use to derive the secret or the key seed.
     */
    var hashFunction: HashFunction
        get() = HashFunction.valueOf(optString(DerivationOptions::hashFunction.name, HashFunction.BLAKE2b.name))
        set(value) { put(DerivationOptions::hashFunction.name, value.name) }

    /**
     * If using the memory-intensive `Argon2id` or `Scrypt` hash functions, you can set the
     * memory required for the hash function via this value.  The default is 67108864.
     */
    var hashFunctionMemoryLimitInBytes: Long
        get() =
            when(hashFunction) {
                HashFunction.Argon2id ->
                    optLong(DerivationOptions::hashFunctionMemoryLimitInBytes.name, 67108864L)
                else -> throw InvalidDerivationOptionValueException(
                    "hashFunctionMemoryLimit not defined for hash function ${hashFunction.name}")
            }
        set(value) {
            when(hashFunction) {
                HashFunction.Argon2id ->
                    put(DerivationOptions::hashFunctionMemoryLimitInBytes.name, value)
            else -> throw InvalidDerivationOptionValueException(
                "hashFunctionMemoryLimit cannot be set for hash function ${hashFunction.name}")
            }
        }

    /**
     * If using the memory-intensive `Argon2id` or `Scrypt` hash functions, you can set the
     * number of iterative cycles (which those algorithms call opsLimit) via this field.
     */
    var hashFunctionMemoryPasses: Long
        get() =
            when(hashFunction) {
                HashFunction.Argon2id ->
                    optLong(DerivationOptions::hashFunctionMemoryPasses.name, 2L)
                else -> throw InvalidDerivationOptionValueException(
                    "hashFunctionIterations not defined for hash function ${hashFunction.name}")
            }
        set(value) {
            when(hashFunction) {
                HashFunction.Argon2id ->
                    put(DerivationOptions::hashFunctionMemoryPasses.name, value)
                else -> throw InvalidDerivationOptionValueException(
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
     * This library is designed to preserve the recipeJson string for you.
     * All derived keys, including the public [SealingKey] and [SignatureVerificationKey], contain
     * the recipeJson used to derive them so that the corresponding
     * [UnsealingKey] and [SigningKey] can be re-derived if needed.
     * All values sealed by a [SymmetricKey] or [PublicKe] are returned in a
     * [PackagedSealedMessage] class which contains the recipeJson needed
     * to re-derive the keys to unseal the message (but not the seed, as that would
     * allow anyone who intercepts the message to re-derive the key.)
     */
    fun toJson(indent: Int? = null): String =
        if (indent == null) toString() else toString(indent)

    init {
        type?.also{
        if (requiredType != null && it != requiredType) {
            throw InvalidDerivationOptionValueException(
                """Key-derivation options assigned "keyType": "${it.name}" when ${requiredType.name} was required."""
            )
        }}
    }

    class UnsealingKey(recipeJson: String? = null) :
        DerivationOptions(recipeJson,  Type.UnsealingKey)

    class Password(recipeJson: String? = null) :
      DerivationOptions(recipeJson,  Type.Password)

    class Secret(recipeJson: String? = null) :
        DerivationOptions(recipeJson,  Type.Secret)

    class SigningKey(recipeJson: String? = null) :
        DerivationOptions(recipeJson,  Type.SigningKey)

    class SymmetricKey(recipeJson: String? = null) :
        DerivationOptions(recipeJson,  Type.SymmetricKey)

}
