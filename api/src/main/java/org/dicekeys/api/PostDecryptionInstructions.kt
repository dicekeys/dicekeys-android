package org.dicekeys.api

import org.json.JSONObject

/**
 * Parse or construct
 * [post-decryption instructions JSON format](https://dicekeys.github.io/seeded-crypto/post_decryption_instructions_format.html)
 * strings. If constructing from a JSON string, the class will be populated with the fields
 * specified by that JSON object.  Or, pass an empty string to the constructor, set the
 * fields using `apply`, and then generate a postDecryptionInstructions string.
 *
 * For example:
 * ```kotlin
 * val postDecryptionInstructions = PostDecryptionInstructions().apply{
 *   userMustAcknowledgeThisMessage = "Only allow this message to be unsealed if you want to spoilers for season 6."
 * }.toJson()
 *
 * val message = PostDecryptionInstructions(postDecryptionInstructions).userMustAcknowledgeThisMessage
 * ```
 */
open class PostDecryptionInstructions(
    postDecryptionInstructions: String? = null
): JSONObject(
        if (postDecryptionInstructions == null || postDecryptionInstructions.isEmpty())
            "{}"
        else postDecryptionInstructions
) {

//    val clientApplicationIdMustHavePrefix: List<String>? = null,
//    val alsoPostToUrl: String? = null,
//    val onlyPostToUrl: String? = null,
//    val reEncryptWithPublicKey: String? = null // hex bytes

    var restrictions: ApiKeyDerivationOptions.Restrictions?
        get() =
            if (has(ApiKeyDerivationOptions::restrictions.name))
                ApiKeyDerivationOptions.Restrictions(getJSONObject(ApiKeyDerivationOptions::restrictions.name))
            else null
        set(value) {
            if (value == null)
                remove(ApiKeyDerivationOptions::restrictions.name)
            else
                put(ApiKeyDerivationOptions::restrictions.name, value.jsonObj)
        }

    var userMustAcknowledgeThisMessage: String?
        get() =
            if (has(PostDecryptionInstructions::userMustAcknowledgeThisMessage.name))
                getString(PostDecryptionInstructions::userMustAcknowledgeThisMessage.name)
            else null
        set(value) {
            if (value == null)
                remove(PostDecryptionInstructions::userMustAcknowledgeThisMessage.name)
            else
                put(PostDecryptionInstructions::userMustAcknowledgeThisMessage.name, value)
        }

    fun toJson(indent: Int? = null): String =
            if (indent == null) toString() else toString(indent)


//    var sealResultWithPublicKey:
}
