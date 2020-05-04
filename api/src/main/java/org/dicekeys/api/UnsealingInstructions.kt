package org.dicekeys.api

import org.json.JSONObject


/**
 * Parse or construct the
 * [JSON Format for Unsealing Instructions](https://dicekeys.github.io/seeded-crypto/unsealing_instructions_format.html).
 * That format allows the sealer of a message to specify restrictions on who may unseal the
 * message or instruct the DiceKeys trusted app to request the user's consent before revealing
 * the unsealed message to the requesting application.
 * If constructing from a JSON string, the class will be populated with the fields
 * specified by that JSON object.  Or, pass an empty string to the constructor, set the
 * fields using `apply`, and then generate a unsealingInstructions string.
 *
 * For example:
 * ```kotlin
 * val unsealingInstructions = UnsealingInstructions().apply{
 *   requireUsersConsent = RequestForUsersConsent().apply {
 *     question = "Only allow this message to be unsealed if you want to spoilers for season 6."
 *     actionButtonLabels.allow = "Show me the spoilers"
 *     actionButtonLabels.deny = "No spoilers"
 *   }
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
    /**
     * This subclass abstracts the JSON object passed to the optional
     * `requireUsersConsent` field of UnsealingInstructions.
     */
    class RequestForUsersConsent(val jsonObject: JSONObject = JSONObject()) {
        enum class UsersResponse { Allow, Deny }

        /**
         * The text of the consent question to pose to the user
         */
        var question: String
            get() = jsonObject.getString(RequestForUsersConsent::question.name)
            set(value) { jsonObject.put(RequestForUsersConsent::question.name, value) }

        inner class ActionButtonLabels() {
            private fun getRequiredActionButtonLabels(): JSONObject =
              jsonObject.getJSONObject(::actionButtonLabels.name)
            private fun getOrCreateActionButtonLabels(): JSONObject =
              jsonObject.optJSONObject(RequestForUsersConsent::actionButtonLabels.name) ?:
              (JSONObject().also { jsonObject.put(RequestForUsersConsent::actionButtonLabels.name, it) })

            /**
             * The text to display in the button that authorizes unsealing a message
             */
            var allow: String
                get() = getRequiredActionButtonLabels().getString(ActionButtonLabels::allow.name)
                set(value) { getOrCreateActionButtonLabels().put(ActionButtonLabels::allow.name, value) }

            /**
             * The text to display in the button through which the user declines
             * to consent to unseal and release a message.
             */
            var deny: String
                get() = getRequiredActionButtonLabels().getString(ActionButtonLabels::deny.name)
                set(value) { getOrCreateActionButtonLabels().put(ActionButtonLabels::deny.name, value) }
        }

        /**
         * This field will auto-populate, and is never written, so when creating a
         * RequestForUsersConsent, you can read this field and the allow and deny
         * properties without setting the value for this field object itself.
         *
         * ```kotlin
         *   actionButtonLabels.allow = "Heck yeah"
         * ```
         */
        val actionButtonLabels: ActionButtonLabels = ActionButtonLabels()
    }

//    val alsoPostToUrl: String? = null,
//    val onlyPostToUrl: String? = null,
//    val reEncryptWithPublicKey: String? = null // hex bytes

    /**
     * The restrictions field is the same as is used for derivation options in the
     * [ApiDerivationOptions] class
     */
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

    /**
     * This optional field can be set to require the user to consent to the unsealing operation.
     * To set, create a RequestForUsersConsent object and populate its three required fields:
     * `question`, `actionButtonLabels.allow`, and `actionButtonLabels.deny`.
     */
    var requireUsersConsent: RequestForUsersConsent?
        get() = optJSONObject(UnsealingInstructions::requireUsersConsent.name)?.let{ RequestForUsersConsent(it) }
        set(value) { put(UnsealingInstructions::requireUsersConsent.name, value?.jsonObject) }

    /**
     * Turn these UnsealingInstructions into a portable JSON string.
     */
    fun toJson(indent: Int? = null): String =
            if (indent == null) toString() else toString(indent)


//    var sealResultWithPublicKey:
}
