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

fun addLengthInBytesToDerivationOptionsJson(derivationOptionsWithoutLengthInBytes: String, lengthInBytes: Int = 0 ): String {
    if (lengthInBytes <= 0) return derivationOptionsWithoutLengthInBytes
    return addFieldToEndOfJsonObjectString(derivationOptionsWithoutLengthInBytes, "lengthInBytes", lengthInBytes.toString())
}

fun addSequenceNumberToDerivationOptionsJson(derivationOptionsWithoutSequenceNumber: String, sequenceNumber: Int): String {
    if (sequenceNumber == 1) return derivationOptionsWithoutSequenceNumber
    return addFieldToEndOfJsonObjectString(derivationOptionsWithoutSequenceNumber, "#", sequenceNumber.toString())
}

private fun augmentRecipeJson(template: DerivationRecipe, sequenceNumber: Int, lengthInChars: Int, lengthInBytes: Int): String {
    var recipeJson = template.recipeJson
    if (template.type == DerivationOptions.Type.Password && lengthInChars > 0) {
        recipeJson = addLengthInCharsToDerivationOptionsJson(recipeJson, lengthInChars)
    }
    if (template.type == DerivationOptions.Type.Secret && lengthInBytes > 0) {
        recipeJson = addLengthInBytesToDerivationOptionsJson(recipeJson, lengthInBytes)
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

    constructor(template: DerivationRecipe, sequenceNumber: Int, lengthInChars: Int = 0, lengthInBytes: Int = 0):
        this(
            template.type,
        template.name +
                when (template.type) {
                    DerivationOptions.Type.Password -> " Password"
                    DerivationOptions.Type.Secret -> " Secret"
                    DerivationOptions.Type.SymmetricKey -> " Key"
                    DerivationOptions.Type.UnsealingKey -> " Key Pair"
                    DerivationOptions.Type.SigningKey -> " Signing Key"
                } + (
                    if (sequenceNumber == 1) "" else " ($sequenceNumber)"
                ),
                augmentRecipeJson(template, sequenceNumber, lengthInChars, lengthInBytes)
        )
    companion object{

        /*
         * Create a custom Online Recipe
         */
        fun createCustomOnlineRecipe(type: DerivationOptions.Type, domains: List<String>, sequenceNumber: Int, lengthInChars: Int = 0, lengthInBytes: Int = 0): DerivationRecipe? {
            if (domains.isEmpty()) {
                return null
            }

            val allowDomainList = domains.map { """{"host":"*.$it"}""" }
            val name = domains.joinToString(", ")
            var recipeJson = """{"allow":[${allowDomainList.joinToString(",")}]}"""

            if (type == DerivationOptions.Type.Password) {
                recipeJson = addLengthInCharsToDerivationOptionsJson(recipeJson, lengthInChars)
            }else if (type == DerivationOptions.Type.Secret) {
                recipeJson = addLengthInBytesToDerivationOptionsJson(recipeJson, lengthInBytes)
            }

            recipeJson = addSequenceNumberToDerivationOptionsJson(recipeJson, sequenceNumber)

            return DerivationRecipe(type, name, recipeJson)
        }

        /*
         * Create a custom Recipe with purpose
         */
        fun createCustomPurposeRecipe(type: DerivationOptions.Type, purpose: String, sequenceNumber: Int, lengthInChars: Int = 0, lengthInBytes: Int = 0): DerivationRecipe? {
            if (purpose.isEmpty()) {
                return null
            }

            var recipeJson = """{"purpose":"$purpose"}"""

            if (type == DerivationOptions.Type.Password) {
                recipeJson = addLengthInCharsToDerivationOptionsJson(recipeJson, lengthInChars)
            }else if (type == DerivationOptions.Type.Secret) {
                recipeJson = addLengthInBytesToDerivationOptionsJson(recipeJson, lengthInBytes)
            }

            recipeJson = addSequenceNumberToDerivationOptionsJson(recipeJson, sequenceNumber)

            val name = purpose.replaceFirstChar { it.uppercase() } // capitalize
            return DerivationRecipe(type, name, recipeJson)
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
