package org.dicekeys.app

import org.dicekeys.api.DerivationRecipe
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
    private var sequence = 1
    private var lengthInChars = 0
    private var lengthInBytes = 0

    fun reset() {
        domains = null
        purpose = null
        sequence = 1
        lengthInChars = 0
        lengthInBytes = 0
    }

    fun updateDomains(d: String) {
        domains = d
        purpose = null
    }

    fun updatePurpose(p: String?) {
        purpose = p
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
        // On URLs remove the subdomain eg. keep last 2 elements of host, subdomain1.subdomain2.google.com -> google.com
        try {
            val url = URL(domains)
            return listOf(
                url.host
                    .split(".").let {
                        it.filterIndexed { i, _ -> i >= (it.size - 2) }
                            .joinToString(".")
                    }
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // or domain list
        return domains
            ?.split(",")
            ?.map { it -> it.trim { it == '.' || it == '/' } } // remove leading and trailing chars
            ?.filter { it.isNotBlank() }
            ?.sorted() ?: listOf()
    }

    fun getDerivationRecipe(): DerivationRecipe? {
        return if (template == null) {

            if (purpose.isNullOrBlank()) {
                DerivationRecipe.createCustomOnlineRecipe(
                    type = type,
                    domains = getDomainList(),
                    sequenceNumber = sequence,
                    lengthInChars = lengthInChars,
                    lengthInBytes = lengthInBytes
                )
            } else {
                DerivationRecipe.createCustomPurposeRecipe(
                    type = type,
                    purpose = purpose ?: "",
                    sequenceNumber = sequence,
                    lengthInChars = lengthInChars,
                    lengthInBytes = lengthInBytes
                )
            }
        } else {
            DerivationRecipe(
                template = template,
                sequenceNumber = sequence,
                lengthInChars = lengthInChars,
                lengthInBytes = lengthInBytes
            )
        }
    }
}