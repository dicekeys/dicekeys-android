package org.dicekeys.app

import org.dicekeys.api.DerivationRecipe
import org.dicekeys.crypto.seeded.DerivationOptions
import java.net.URL

/*
 * A builder to create custom or template based Recipes
 */
class RecipeBuilder(val template: DerivationRecipe?) {
    private var domainList: List<String> = listOf()
    private var sequence = 1
    private var lengthInChars = 0

    fun reset(){
        domainList = listOf()
        sequence = 1
        lengthInChars = 0
    }

    fun updateDomains(domains: String){

        // Check if is valid URL
        try{
            val url = URL(domains)
            domainList = listOf(url.host)
            return
        }catch (e: Exception){
            e.printStackTrace()
        }

        // or domain list
        domainList = domains
                .split(",")
                .map { it -> it.trim { it == '.' || it == '/' } } // remove leading and trailing chars
                .filter { it.isNotBlank() }
                .sorted()
    }

    fun updateSequence(s: Int){
        sequence = if(s >= 1) s else 1
    }

    fun updateLengthInChars(length: Int) {
        lengthInChars = if(length in 6..999) length else 0
    }

    fun getDerivationRecipe(): DerivationRecipe? {
        return if(template == null) {
            DerivationRecipe.createCustomOnlineRecipe(DerivationOptions.Type.Password, domainList, sequence, lengthInChars)
        } else {
            DerivationRecipe(template, sequence)
        }
    }
}