package com.keysqr

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.ToJson


//@JsonClass(generateAdapter = true)
//class HashAlgorithm(
//    val algorithm: String, //  "Argon2id" | "Scrypt" | "BLAKE2b" | "SHA256"
//    val memLimit: Long? = null, // default 67108864U, which is crypto_pwhash_MEMLIMIT_INTERACTIVE from LibSodium
//    val opsLimit: Int? = null // default 2, which is crypto_pwhash_OPSLIMIT_INTERACTIVE from LibSodium
//) {
//    fun getMemLimit(): Long {
//        return memLimit ?: 67108864L
//    }
//    fun getOpsLimit: Int {
//        return opsLimit ?: 2
//    }
//
//    @FromJson
//    fun fromString(algorithm: String): HashAlgorithm {
//        return HashAlgorithm(algorithm)
//    }
//}



@JsonClass(generateAdapter = true)
class KeyDerivationOptions(
    //  "Seed", "Symmetric", "Public"
    val keyType: String?,

    val keyLengthInBytes: Int?,

    //  "XSalsa20Poly1305", "X25519"
    val algorithm: String?,

    // "BLAKE2b", "SHA256", or a ParameterizedHashAlgorithm
//    val hashAlgorithm: HashAlgorithm?,

    val includeOrientationOfFacesInKey: Boolean = false,
    val restrictToClientApplicationsIdPrefixes: List<String>? = null
) {
    companion object {
        object KeyType {
            const val Seed = "Seed"
            const val Symmetric = "Symmetric"
            const val Public = "Public"
        }
        object Algorithm {
            const val XSalsa20Poly1305 = "XSalsa20Poly1305"
            const val X25519 = "X25519"
        }

        fun fromJson(json: String): KeyDerivationOptions {
            // FIXME
            return KeyDerivationOptions(null, null, null, true, null)
        }
    }
//
}
