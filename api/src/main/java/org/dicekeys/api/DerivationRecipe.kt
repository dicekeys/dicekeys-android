package org.dicekeys.api
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
    var derivationOptionsJson = template.derivationOptionsJson
    if (template.type == DerivationOptions.Type.Password && lengthInChars > 0) {
        derivationOptionsJson = addLengthInCharsToDerivationOptionsJson(derivationOptionsJson, lengthInChars)
    }
    derivationOptionsJson =
            addSequenceNumberToDerivationOptionsJson(derivationOptionsJson, sequenceNumber)
    return derivationOptionsJson
}

@Serializable
@Parcelize
data class DerivationRecipe(
        @SerialName("type")
        val type: DerivationOptions.Type,
        @SerialName("name")
        val name: String,
        @SerialName("derivation_options_json")
        val derivationOptionsJson: String
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
        ) {}

    /*
     * An easy way to have a unique identifier for this Recipe
     */
    val id
        get() = derivationOptionsJson.hashCode().toString()

    override fun toString(): String = Json.encodeToString(this)
}