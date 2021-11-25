package org.dicekeys.app

import androidx.lifecycle.MutableLiveData
import org.dicekeys.api.DerivationRecipe
import org.dicekeys.app.utils.removeWildcardPrefixIfPresent
import org.dicekeys.crypto.seeded.DerivationOptions
import java.net.URL

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

    val derivationRecipe = MutableLiveData<DerivationRecipe?>()

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
        // Reset everything on default values so that we can replace from raw
        reset()

        rawJson = json
    }

    fun updateSequence(s: Int) {
        sequence = if (s >= 1) s else 1
    }

    fun updateLengthInChars(length: Int) {
        lengthInChars = if (length in 16..999) length else 0
    }

    fun updateLengthInBytes(length: Int) {
        lengthInBytes = if (length in 16..999) length else 0
    }

    private fun getDomainList(): List<String> {
        // Check if is valid URL
        try {
            val url = URL(domains)
            return listOf(url.host)
        } catch (e: Exception) { }

        // or domain list
        return domains
            ?.split(",")
            ?.map { it -> it.trim() }
            ?.map { it -> removeWildcardPrefixIfPresent(it) }
            ?.map { it -> it.trim { it == '.' || it == '/' } } // remove leading and trailing chars
            ?.distinct() // prevent duplicate unique values
            ?.filter { it.isNotBlank() }
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
                        rawJson = rawJson ?: "",
                        sequenceNumber = sequence,
                        lengthInChars = lengthInChars,
                        lengthInBytes = lengthInBytes
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
            derivationRecipe.value = it
        }
    }

    @JvmName("getDerivationRecipeValue")
    fun getDerivationRecipe(): DerivationRecipe? = derivationRecipe.value
}