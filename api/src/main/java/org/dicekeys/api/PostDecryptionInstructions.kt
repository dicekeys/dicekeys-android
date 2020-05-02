package org.dicekeys.api

import org.json.JSONObject

/**
 * Parse or construct
 * [unsealing_instructions instructions JSON format](https://dicekeys.github.io/seeded-crypto/unsealing_instructions_format.html)
 * strings. If constructing from a JSON string, the class will be populated with the fields
 * specified by that JSON object.  Or, pass an empty string to the constructor, set the
 * fields using `apply`, and then generate a unsealingInstructions string.
 *
 * For example:
 * ```kotlin
 * val unsealingInstructions = UnsealingInstructions().apply{
 *   userMustAcknowledgeThisMessage = "Only allow this message to be unsealed if you want to spoilers for season 6."
 * }.toJson()
 *
 * val message = UnsealingInstructions(unsealingInstructions).userMustAcknowledgeThisMessage
 * ```
 */
open class UnsealingInstructions(
    unsealingInstructions: String? = null
): JSONObject(
        if (unsealingInstructions == null || unsealingInstructions.isEmpty())
            "{}"
        else unsealingInstructions
) {

//    val clientApplicationIdMustHavePrefix: List<String>? = null,
//    val alsoPostToUrl: String? = null,
//    val onlyPostToUrl: String? = null,
//    val reEncryptWithPublicKey: String? = null // hex bytes

    var restrictions: ApiDerivationOptions.Restrictions?
        get() =
            if (has(ApiDerivationOptions::restrictions.name))
                ApiDerivationOptions.Restrictions(getJSONObject(ApiDerivationOptions::restrictions.name))
            else null
        set(value) {
            if (value == null)
                remove(ApiDerivationOptions::restrictions.name)
            else
                put(ApiDerivationOptions::restrictions.name, value.jsonObj)
        }

    var userMustAcknowledgeThisMessage: String?
        get() =
            if (has(UnsealingInstructions::userMustAcknowledgeThisMessage.name))
                getString(UnsealingInstructions::userMustAcknowledgeThisMessage.name)
            else null
        set(value) {
            if (value == null)
                remove(UnsealingInstructions::userMustAcknowledgeThisMessage.name)
            else
                put(UnsealingInstructions::userMustAcknowledgeThisMessage.name, value)
        }

    fun toJson(indent: Int? = null): String =
            if (indent == null) toString() else toString(indent)


//    var sealResultWithPublicKey:
}
