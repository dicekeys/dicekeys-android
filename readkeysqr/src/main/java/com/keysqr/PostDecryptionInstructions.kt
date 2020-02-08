package com.keysqr

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@JsonClass(generateAdapter = true)
class PostDecryptionInstructions(
    val clientApplicationIdMustHavePrefix: List<String>? = null,
    val userMustAcknowledgeThisMessage: String? = null
) {
    companion object {
        private val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

        private val jsonAdapter: JsonAdapter<PostDecryptionInstructions> =
                moshi.adapter(PostDecryptionInstructions::class.java)

        fun fromJsonReturningNullIfInvalid(json: String): PostDecryptionInstructions? {
            try {
                if (json == "null" || json.length < 2)
                    return null
                return jsonAdapter.fromJson(json)
            } catch (e: Exception) {
                return null
            }
        }
        fun fromJsonReturningEmptyObjectIfInvalid(json: String): PostDecryptionInstructions {
            try {
                return jsonAdapter.fromJson(json) ?: PostDecryptionInstructions()
            } catch (e: Exception) {
                return PostDecryptionInstructions()
            }
        }

    }
}
