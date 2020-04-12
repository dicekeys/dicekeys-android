package org.dicekeys.api

import org.json.JSONObject

open class PostDecryptionInstructions(
    postDecryptionInstructionsJson: String? = null
): JSONObject(
        if (postDecryptionInstructionsJson == null || postDecryptionInstructionsJson.isEmpty())
            "{}"
        else postDecryptionInstructionsJson
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

//    var sealResultWithPublicKey:
}
