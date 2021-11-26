package org.dicekeys.api

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.dicekeys.crypto.seeded.DerivationOptions

/*
 *   Rebuilding the JsonObject without predefined keys.
 */
fun JsonObject.rebuild(updateJsonObject: JsonObject): JsonObject {
    return JsonObject(filterKeys { key ->
        key != "#" && key != "lengthInChars" && key != "lengthInBytes"
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
        @SerialName("type")
        val type: DerivationOptions.Type,
        @SerialName("name")
        val name: String,
        @SerialName("derivation_options_json")
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

    fun createDerivationRecipeForSequence(sequenceNumber: Int): DerivationRecipe{
        val jsonObject = recipeAsJsonElement
            .jsonObject
            .rebuild(
                buildJsonObject {
                    addSequenceNumberToDerivationOptionsJson(sequenceNumber)
                }
            )

        return DerivationRecipe(type = type, name = createRecipeName(recipe = this, sequenceNumber = sequenceNumber), recipeJson = jsonObject.canonicalize())
    }

    /*
     * An easy way to have a unique identifier for this Recipe
     */
    val id by lazy { recipeJson.hashCode().toString() }

    override fun toString(): String = Json.encodeToString(this)

    companion object{

        fun createRecipeName(recipe: DerivationRecipe, sequenceNumber: Int): String{
            return recipe.name +
                    when (recipe.type) {
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
            val type = template.type
            val name = createRecipeName(template, sequenceNumber)

            val jsonObject = template
                .recipeAsJsonElement
                .jsonObject
                .rebuild(
                    buildJsonObject {
                        if (type == DerivationOptions.Type.Password) {
                            addLengthInCharsToDerivationOptionsJson(lengthInChars)
                        } else if (type == DerivationOptions.Type.Secret) {
                            addLengthInBytesToDerivationOptionsJson(lengthInBytes)
                        }
                        addSequenceNumberToDerivationOptionsJson(sequenceNumber)
                    }
                )


            return DerivationRecipe(type, name, jsonObject.canonicalize())
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
                            put("host", "*.$domain")
                        }
                    }
                }

                if (type == DerivationOptions.Type.Password) {
                    addLengthInCharsToDerivationOptionsJson(lengthInChars)
                }else if (type == DerivationOptions.Type.Secret) {
                    addLengthInBytesToDerivationOptionsJson(lengthInBytes)
                }
                addSequenceNumberToDerivationOptionsJson( sequenceNumber)
            }

            val name = domains.joinToString(", ")

            return DerivationRecipe(type, name, jsonObject.canonicalize())
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

                if (type == DerivationOptions.Type.Password) {
                    addLengthInCharsToDerivationOptionsJson(lengthInChars)
                }else if (type == DerivationOptions.Type.Secret) {
                    addLengthInBytesToDerivationOptionsJson(lengthInBytes)
                }
                addSequenceNumberToDerivationOptionsJson( sequenceNumber)
            }

            val name = purpose.replaceFirstChar { it.uppercase() } // capitalize
            return DerivationRecipe(type, name, jsonObject.canonicalize())
        }

        /*
         * Create a custom Recipe with raw json
         */
        fun createCustomRawJsonRecipe(type: DerivationOptions.Type, rawJson: String, sequenceNumber: Int, lengthInChars: Int = 0, lengthInBytes: Int = 0): DerivationRecipe? {
            if (rawJson.isEmpty()) {
                return null
            }

            val jsonObject = buildJsonObject {
                if (type == DerivationOptions.Type.Password) {
                    addLengthInCharsToDerivationOptionsJson(lengthInChars)
                }else if (type == DerivationOptions.Type.Secret) {
                    addLengthInBytesToDerivationOptionsJson(lengthInBytes)
                }
                addSequenceNumberToDerivationOptionsJson( sequenceNumber)
            }.rebuild(Json.parseToJsonElement(rawJson).jsonObject)

            val name = "RawJSON"// purpose.replaceFirstChar { it.uppercase() } // capitalize
            return DerivationRecipe(type, name, jsonObject.canonicalize())
        }
    }
}
