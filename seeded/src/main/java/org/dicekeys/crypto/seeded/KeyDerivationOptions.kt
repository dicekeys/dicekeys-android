package org.dicekeys.crypto.seeded
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

internal fun jsonArrayToStringList(jsonArray: JSONArray) : List<String> {
    val list: MutableList<String> = ArrayList(jsonArray.length())
    for (i in 0 until jsonArray.length()) {
        list.add(jsonArray.getString(i))
    }
    return list.toList()

}

internal fun getJsonObjectsStringListOrNull(obj: JSONObject, fieldName: String): List<String>? =
    if (obj.has(fieldName)) jsonArrayToStringList(obj.getJSONArray(fieldName)) else null

data class KeyDerivationOptionsRestrictions(
    val androidPackagePrefixesAllowed: List<String>?,
    val urlPrefixesAllowed: List<String>?
) {

    companion object {
        fun fromJsonObject(obj: JSONObject): KeyDerivationOptionsRestrictions {
            return KeyDerivationOptionsRestrictions(
                    getJsonObjectsStringListOrNull(obj,
                            KeyDerivationOptionsRestrictions::androidPackagePrefixesAllowed.name),
                    getJsonObjectsStringListOrNull(obj,
                            KeyDerivationOptionsRestrictions::urlPrefixesAllowed.name)
            )
        }
    }

}

class KeyDerivationOptions(
        val keyType: KeyType?,
        val keyLengthInBytes: Int?,
        val algorithm: Algorithm?,
//        val includeOrientationOfFacesInKey: Boolean = true,
        val excludeOrientationOfFaces: Boolean = false,
//        val hashFunction: HashFunction? = null,
//        val restrictToClientApplicationsIdPrefixes: List<String>? = null
        val restrictions: KeyDerivationOptionsRestrictions?
) {
    enum class KeyType {
        Seed, Symmetric, Public, Signing;

        companion object {
            fun valueOfOrNull(name: String): KeyType? = try { valueOf(name) } catch (e: Exception) { null }
        }
    }

    enum class Algorithm {
        XSalsa20Poly1305, X25519, Ed25519;

        companion object {
            fun valueOfOrNull(name: String): Algorithm? = try { valueOf(name) } catch (e: Exception) { null }
        }
    }

//    class HashFunction(
//            val algorithm: HashAlgorithm?, //  "Argon2id" | "Scrypt" | "BLAKE2b" | "SHA256"
//            optionalMemLimit: Long? = null, // default 67108864U, which is crypto_pwhash_MEMLIMIT_INTERACTIVE from LibSodium
//            optionalOpsLimit: Int? = null // default 2, which is crypto_pwhash_OPSLIMIT_INTERACTIVE from LibSodium
//    ) {
//        enum class HashAlgorithm {
//            Argon2id, Scrypt, BLAKE2b, SHA256;
//
//            companion object {
//                fun valueOfOrNull(name: String): HashAlgorithm? = try { valueOf(name) } catch (e: Exception) { null }
//            }
//        }
//
//        val memLimit: Long = optionalMemLimit ?:  67108864L
//        val opsLimit: Int = optionalOpsLimit ?: 2
//    }

    companion object {

        fun fromJson(json: String?, defaultKeyType: KeyType? = null): KeyDerivationOptions =
                fromJsonObject(
                        if (json != null && json.isNotEmpty()) JSONObject(json) else JSONObject(),
                        defaultKeyType
                )

        fun fromJsonObject(obj: JSONObject, defaultKeyType: KeyType? = null): KeyDerivationOptions {
            val keyType: KeyType? = KeyType.valueOfOrNull(obj.optString(KeyDerivationOptions::keyType.name, defaultKeyType?.name
                    ?: ""))
            val defaultAlgorithmName: String = when(keyType) {
                KeyType.Public -> Algorithm.X25519.name
                KeyType.Symmetric -> Algorithm.XSalsa20Poly1305.name
                KeyType.Signing -> Algorithm.Ed25519.name
                else -> ""
            }
            val algorithm: Algorithm? = Algorithm.valueOfOrNull(obj.optString(KeyDerivationOptions::algorithm.name, defaultAlgorithmName))
//            val hashFunction = if (!obj.has(KeyDerivationOptions::hashFunction.name))
//                    // No hash function specified
//                    null
//                else if (obj.optString(KeyDerivationOptions::hashFunction.name, "").isNotEmpty())
//                    // Hash function specified as a string
//                HashFunction(HashFunction.HashAlgorithm.valueOfOrNull(obj.getString(KeyDerivationOptions::hashFunction.name)))
//                else obj.getJSONObject(KeyDerivationOptions::hashFunction.name).run {
//                    // Hash function specified as a JSON object with "algorithm" field
//                HashFunction(
//                        HashFunction.HashAlgorithm.valueOfOrNull(getString(HashFunction::algorithm.name)),
//                        if (has(HashFunction::memLimit.name)) getLong(HashFunction::memLimit.name) else null,
//                        if (has(HashFunction::opsLimit.name)) getInt(HashFunction::opsLimit.name) else null
//                )
//                }
            val excludeOrientationOfFaces = obj.optBoolean(KeyDerivationOptions::excludeOrientationOfFaces.name, true)
            val keyLengthInBytes: Int? = if (obj.has(KeyDerivationOptions::keyLengthInBytes.name))
                obj.getInt(KeyDerivationOptions::keyLengthInBytes.name) else null
            val restrictions: KeyDerivationOptionsRestrictions? =
                    if (obj.has(KeyDerivationOptions::restrictions.name))
                        KeyDerivationOptionsRestrictions.fromJsonObject(obj.getJSONObject(KeyDerivationOptions::restrictions.name))
                    else null

            return KeyDerivationOptions(keyType, keyLengthInBytes, algorithm, excludeOrientationOfFaces, restrictions)
        }

        fun fromValidOrInvalidJson(json: String, defaultKeyType: KeyType? = null): KeyDerivationOptions {
            return try {
                fromJson(json, defaultKeyType)
            } catch (e: Exception) {
                return KeyDerivationOptions(defaultKeyType, null, null, false, null)
            }
        }
    }

}
