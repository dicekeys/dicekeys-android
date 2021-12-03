package org.dicekeys.app

import androidx.lifecycle.MutableLiveData
import kotlinx.serialization.json.*
import org.dicekeys.api.DerivationRecipe
import org.dicekeys.api.getWildcardOfRegisteredDomainFromCandidateWebUrl
import org.dicekeys.crypto.seeded.DerivationOptions

/*
 * A builder to create custom or template based Recipes
 */
class RecipeBuilder constructor(val type: DerivationOptions.Type, val template: DerivationRecipe?) {
    var domains: String? = null
        private set
    var purpose: String? = null
        private set
    var rawJson: String? = null
        private set

    var sequence = 1
        private set
    var lengthInChars = template?.lengthInChars ?: 0
        private set
    var lengthInBytes = template?.lengthInBytes ?: 0
        private set

    // used only in raw json
    var name: String? = null
        private set

    val derivationRecipeLiveData = MutableLiveData<DerivationRecipe?>()

    init {
        build()
    }

    fun reset() {
        domains = null
        purpose = null
        sequence = 1
        lengthInChars = 0
        lengthInBytes = 0
    }

    fun updateDomains(d: String?) {
        domains = d
        purpose = null
        rawJson = null
    }

    fun updatePurpose(p: String?) {
        purpose = p
        rawJson = null
    }

    fun updateRawJson(json: String?) {
        rawJson = json

        try{
            json?.let { json
                // update sequence from raw json
                Json.parseToJsonElement(json).jsonObject["#"]?.jsonPrimitive?.intOrNull?.let { sequenceFromRaw ->
                    updateSequence(sequenceFromRaw)
                }
            }

        }catch (e: Exception){

        }
    }

    fun updateName(n: String?){
        name = n
    }

    fun updateSequence(s: Int) {
        sequence = if (s >= 1) s else 1
    }

    fun updateLengthInChars(length: Int) {
        lengthInChars = if (length in 8..999) length else 0
    }

    fun updateLengthInBytes(length: Int) {
        lengthInBytes = if (length in 16..999) length else 0
    }

    private fun getDomainList(): List<String> {
        return domains
            ?.split(",")
            ?.map { it -> it.trim() } // trim whitespace
            ?.map { it -> it.trim { it == '.' || it == '/' } } // remove leading and trailing chars
            ?.filter { it.isNotBlank() }
            ?.map { urlOrDomain ->
                getWildcardOfRegisteredDomainFromCandidateWebUrl(urlOrDomain) ?: urlOrDomain
            }
            ?.distinct() // prevent duplicate unique values
            ?.sorted() ?: listOf()
    }

    fun build(): DerivationRecipe? {
        return (try {
            when{
                template != null -> {
                    DerivationRecipe.createRecipeFromTemplate(
                        template = template,
                        sequenceNumber = sequence,
                        lengthInChars = lengthInChars,
                        lengthInBytes = lengthInBytes
                    )
                }
                !rawJson.isNullOrBlank() -> {
                    DerivationRecipe.createCustomRawJsonRecipe(
                        type = type,
                        name = name,
                        rawJson = rawJson ?: "",
                        sequenceNumber = sequence
                    )
                }
                !purpose.isNullOrBlank() -> {
                    DerivationRecipe.createCustomPurposeRecipe(
                        type = type,
                        purpose = purpose ?: "",
                        sequenceNumber = sequence,
                        lengthInChars = lengthInChars,
                        lengthInBytes = lengthInBytes
                    )
                }
                else -> {
                    DerivationRecipe.createCustomOnlineRecipe(
                        type = type,
                        domains = getDomainList(),
                        sequenceNumber = sequence,
                        lengthInChars = lengthInChars,
                        lengthInBytes = lengthInBytes
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }).also {
            derivationRecipeLiveData.value = it
        }
    }

    @JvmName("getDerivationRecipeValue")
    fun getDerivationRecipe(): DerivationRecipe? = derivationRecipeLiveData.value
}