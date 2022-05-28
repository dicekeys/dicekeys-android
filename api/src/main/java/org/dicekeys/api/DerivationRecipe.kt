package org.dicekeys.api

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.dicekeys.crypto.seeded.DerivationOptions
import java.security.MessageDigest

/*
 *   Rebuilding the JsonObject without predefined keys.
 */
fun JsonObject.rebuild(updateJsonObject: JsonObject, skipProperties: List<String>): JsonObject {
    return JsonObject(filterKeys { key ->
        !skipProperties.contains(key)
    }.toMutableMap().apply {
        updateJsonObject.forEach { (key, value) ->
            put(key, value)
        }
    })
}

fun JsonObject.canonicalize(): String{
    return toCanonicalizeRecipeJson(this)
}

fun JsonObjectBuilder.addLengthInCharsToDerivationOptionsJson(lengthInChars: Int){
    if(lengthInChars > 0){
        put("lengthInChars", lengthInChars)
    }
}

fun JsonObjectBuilder.addLengthInBytesToDerivationOptionsJson(lengthInBytes: Int){
    if(lengthInBytes > 0){
        put("lengthInBytes", lengthInBytes)
    }
}

fun JsonObjectBuilder.addSequenceNumberToDerivationOptionsJson(sequenceNumber: Int){
    if(sequenceNumber > 1){
        put("#", sequenceNumber)
    }
}

@Serializable
@Parcelize
data class DerivationRecipe constructor(
        val type: DerivationOptions.Type,
        val name: String,
        @JsonNames("derivation_options_json") // keep backward compatibility
        val recipeJson: String
) : Parcelable {

    @IgnoredOnParcel
    val recipeAsJsonElement: JsonElement = Json.parseToJsonElement(recipeJson)

    @IgnoredOnParcel
    val sequence by lazy {
        recipeAsJsonElement.jsonObject["#"]?.jsonPrimitive?.int ?: 1
    }

    @IgnoredOnParcel
    val lengthInChars by lazy {
        recipeAsJsonElement.jsonObject["lengthInChars"]?.jsonPrimitive?.int
    }

    @IgnoredOnParcel
    val lengthInBytes by lazy {
        recipeAsJsonElement.jsonObject["lengthInBytes"]?.jsonPrimitive?.int
    }

    @IgnoredOnParcel
    val purpose: String? by lazy {
        recipeAsJsonElement.jsonObject["purpose"]?.jsonPrimitive?.content
    }

    fun createDerivationRecipeForSequence(sequenceNumber: Int): DerivationRecipe{
        val jsonObject = recipeAsJsonElement
            .jsonObject
            .rebuild(
                buildJsonObject {
                    addSequenceNumberToDerivationOptionsJson(sequenceNumber)
                }
            , rebuildSkipJsonProperties)

        return DerivationRecipe(type = type, name = createRecipeName(prefix = name, type = type, sequenceNumber = sequenceNumber), recipeJson = jsonObject.canonicalize())
    }

    /*
     * An easy way to have a unique identifier for this Recipe
     */
    @IgnoredOnParcel
    val id by lazy {
        MessageDigest.getInstance("MD5").also {
            it.update(type.toString().toByteArray())
            it.update(recipeJson.toByteArray())
        }.digest().toHexString()
    }

    override fun toString(): String = Json.encodeToString(this)

    companion object{
        val rebuildSkipJsonProperties = listOf("#", "lengthInChars", "lengthInBytes")

        fun createRecipeName(prefix: String, type: DerivationOptions.Type, sequenceNumber: Int): String{
            return prefix +
                    when (type) {
                        DerivationOptions.Type.Password -> " Password"
                        DerivationOptions.Type.Secret -> " Secret"
                        DerivationOptions.Type.SymmetricKey -> " Key"
                        DerivationOptions.Type.UnsealingKey -> " Key Pair"
                        DerivationOptions.Type.SigningKey -> " Signing Key"
                    } + (if (sequenceNumber == 1) "" else " ($sequenceNumber)")
        }

        /*
         * Create a Recipe from Template
         */
        fun createRecipeFromTemplate(template: DerivationRecipe, sequenceNumber: Int, lengthInChars: Int = 0, lengthInBytes: Int = 0): DerivationRecipe {
            return createRecipe(
                name = createRecipeName(prefix = template.name, type = template.type, sequenceNumber = sequenceNumber),
                type = template.type,
                recipeJson = template.recipeAsJsonElement.jsonObject,
                skipJsonProperties = rebuildSkipJsonProperties,
                sequenceNumber = sequenceNumber,
                lengthInChars = lengthInChars,
                lengthInBytes = lengthInBytes
            )
        }

        /*
         * Create a custom Online Recipe
         */
        fun createCustomOnlineRecipe(type: DerivationOptions.Type, domains: List<String>, sequenceNumber: Int, lengthInChars: Int = 0, lengthInBytes: Int = 0): DerivationRecipe? {
            if (domains.isEmpty()) {
                return null
            }

            val jsonObject = buildJsonObject {
                putJsonArray("allow"){
                    domains.forEach { domain ->
                        addJsonObject {
                            put("host", "$domain")
                        }
                    }
                }
            }

            val prefix = domains.joinToString(", ") {
                removeWildcardPrefixIfPresent(it)
            }

            return createRecipe(
                name = createRecipeName(prefix = prefix, type = type, sequenceNumber = sequenceNumber),
                type = type,
                recipeJson = jsonObject,
                skipJsonProperties = rebuildSkipJsonProperties,
                sequenceNumber = sequenceNumber,
                lengthInChars = lengthInChars,
                lengthInBytes = lengthInBytes
            )
        }

        /*
         * Create a custom Recipe with purpose
         */
        fun createCustomPurposeRecipe(type: DerivationOptions.Type, purpose: String, sequenceNumber: Int, lengthInChars: Int = 0, lengthInBytes: Int = 0): DerivationRecipe? {
            if (purpose.isEmpty()) {
                return null
            }

            val jsonObject = buildJsonObject {
                put("purpose", purpose)
            }

            return createRecipe(
                name = createRecipeName(prefix = purpose.replaceFirstChar { it.uppercase() }, type = type, sequenceNumber = sequenceNumber), // capitalize,
                type = type,
                recipeJson = jsonObject,
                skipJsonProperties = rebuildSkipJsonProperties,
                sequenceNumber = sequenceNumber,
                lengthInChars = lengthInChars,
                lengthInBytes = lengthInBytes
            )
        }

        /*
         * Create a custom Recipe with raw json
         */
        fun createCustomRawJsonRecipe(type: DerivationOptions.Type, name: String?, rawJson: String, sequenceNumber: Int): DerivationRecipe? {
            if (rawJson.isEmpty()) {
                return null
            }

            return createRecipe(
                name = createRecipeName(
                    prefix = if (name.isNullOrBlank()) "RawJSON" else name,
                    type = type,
                    sequenceNumber = sequenceNumber
                ),
                type = type,
                recipeJson = Json.parseToJsonElement(rawJson).jsonObject,
                skipJsonProperties = listOf("#"), // allow # to be changed
                sequenceNumber = sequenceNumber,
                lengthInChars = 0,
                lengthInBytes = 0
            )

        }

        private fun createRecipe(
            name : String,
            type: DerivationOptions.Type,
            recipeJson: JsonObject,
            skipJsonProperties: List<String>,
            sequenceNumber: Int,
            lengthInChars: Int,
            lengthInBytes: Int
        ): DerivationRecipe {

            val jsonObject = recipeJson.rebuild(
                buildJsonObject {
                    if (type == DerivationOptions.Type.Password) {
                        addLengthInCharsToDerivationOptionsJson(lengthInChars)
                    }else if (type == DerivationOptions.Type.Secret) {
                        addLengthInBytesToDerivationOptionsJson(lengthInBytes)
                    }
                    addSequenceNumberToDerivationOptionsJson(sequenceNumber)
                }
            , skipJsonProperties)

            return DerivationRecipe(type, name, jsonObject.canonicalize())
        }
    }
}
