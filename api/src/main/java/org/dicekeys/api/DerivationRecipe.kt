package org.dicekeys.api
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.decodeFromString
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.dicekeys.crypto.seeded.DerivationOptions

fun addFieldToEndOfJsonObjectString(originalJsonObjectString: String, fieldName: String, fieldValue: String): String {
    val lastClosingBraceIndex = originalJsonObjectString.lastIndexOf("}")
    if (lastClosingBraceIndex < 0) return originalJsonObjectString
    val prefixUpToFinalClosingBrace = originalJsonObjectString.substring(0, lastClosingBraceIndex)
    val suffixIncludingFinalCloseBrace = originalJsonObjectString.substring(lastClosingBraceIndex)
    val commaIfObjectNonEmpty = if (prefixUpToFinalClosingBrace.contains (':')) "," else  ""
    return "$prefixUpToFinalClosingBrace$commaIfObjectNonEmpty\"$fieldName\":$fieldValue$suffixIncludingFinalCloseBrace"
}

fun addLengthInCharsToDerivationOptionsJson(derivationOptionsWithoutLengthInChars: String, lengthInChars: Int = 0 ): String {
    if (lengthInChars <= 0) return derivationOptionsWithoutLengthInChars
    return addFieldToEndOfJsonObjectString(derivationOptionsWithoutLengthInChars, "lengthInChars", lengthInChars.toString())
}

fun addSequenceNumberToDerivationOptionsJson(derivationOptionsWithoutSequenceNumber: String, sequenceNumber: Int): String {
    if (sequenceNumber == 1) return derivationOptionsWithoutSequenceNumber
    return addFieldToEndOfJsonObjectString(derivationOptionsWithoutSequenceNumber, "#", sequenceNumber.toString())
}

private fun augmentRecipeJson(template: DerivationRecipe, sequenceNumber: Int, lengthInChars: Int): String {
    var recipeJson = template.recipeJson
    if (template.type == DerivationOptions.Type.Password && lengthInChars > 0) {
        recipeJson = addLengthInCharsToDerivationOptionsJson(recipeJson, lengthInChars)
    }
    recipeJson =
            addSequenceNumberToDerivationOptionsJson(recipeJson, sequenceNumber)
    return recipeJson
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

    constructor(template: DerivationRecipe, sequenceNumber: Int, lengthInChars: Int = 0):
        this(
            template.type,
        template.name +
                when (template.type) {
                    DerivationOptions.Type.Password -> " Password"
                    DerivationOptions.Type.SymmetricKey -> " Key"
                    DerivationOptions.Type.UnsealingKey -> " Key Pair"
                    else -> ""
                } + (
                    if (sequenceNumber == 1) "" else " ($sequenceNumber)"
                ),
                augmentRecipeJson(template, sequenceNumber, lengthInChars)
        )

    companion object{
        fun createCustomOnlineRecipe(recipe: DerivationRecipe, sequenceNumber: Int, lengthInChars: Int = 0): DerivationRecipe {
            return DerivationRecipe(DerivationOptions.Type.Password, recipe.name, augmentRecipeJson(recipe, sequenceNumber, lengthInChars))
        }
    }

    @IgnoredOnParcel
    val sequence by lazy {
        Json.parseToJsonElement(recipeJson).jsonObject["#"]?.jsonPrimitive?.int ?: 1
    }

    /*
     * An easy way to have a unique identifier for this Recipe
     */
    val id
        get() = recipeJson.hashCode().toString()

    override fun toString(): String = Json.encodeToString(this)
}
