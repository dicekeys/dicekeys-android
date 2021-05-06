package org.dicekeys.app.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.dicekeys.api.DerivationRecipe
import org.dicekeys.app.RecipeBuilder
import org.dicekeys.app.repositories.RecipeRepository
import org.dicekeys.crypto.seeded.Password
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face


class RecipeViewModel @AssistedInject constructor(
        private val recipeRepository: RecipeRepository,
        @Assisted val diceKey: DiceKey<Face>,
        @Assisted("recipe") val recipe: DerivationRecipe?,
        @Assisted("template") val template: DerivationRecipe?,
) : ViewModel() {
    private val showRecipe = recipe != null
    val isCustomRecipe = MutableLiveData(recipe == null && template == null)

    var derivationRecipe = MutableLiveData(recipe ?: template)
    var sequenceNumber = MutableLiveData(recipe?.sequence?.toString() ?: "1")
    var password = MutableLiveData<String>(null)
    var recipeIsSaved = MutableLiveData(if(recipe != null) recipeRepository.exists(recipe) else false)

    var recipeBuilder = RecipeBuilder(template)

    init {
        generatePassword()
    }

    private fun generatePassword(){
        derivationRecipe.value?.let{ derivationRecipe ->
            password.value = diceKey.toCanonicalRotation().let { Password.deriveFromSeed(it.toHumanReadableForm(), derivationRecipe.recipeJson).password }
            
            updateSavedState()
        }
    }
    
    fun saveRecipeInMenu(){
        derivationRecipe.value?.let {
            recipeRepository.save(it)
            updateSavedState()
        }
    }
    
    fun removeRecipeFromMenu(){
        derivationRecipe.value?.let {
            recipeRepository.remove(it)
            updateSavedState()
        }
    }

    private fun updateSavedState(){
        derivationRecipe.value?.let {
            recipeIsSaved.value = recipeRepository.exists(it)
        }
    }

    fun updateSequence(sequence: Int){
        if(sequence > 0) {
            sequenceNumber.value = sequence.toString()
            recipeBuilder.updateSequence(sequence)
            updateRecipe()
        }
    }

    fun updateDomains(domains: String){
        recipeBuilder.updateDomains(domains)
        updateRecipe()
    }

    fun updateLengthInChars(length: Int){
        recipeBuilder.updateLengthInChars(length)
        updateRecipe()
    }

    private fun updateRecipe(){
        if(!showRecipe){
            if(template != null){
                derivationRecipe.value = DerivationRecipe(template, sequenceNumber.value!!.toInt())
            }else{
                derivationRecipe.value = recipeBuilder.getDerivationRecipe()
            }

            generatePassword()
        }
    }

    /**
     * Up/down sequence number
     */
    fun sequencUpDown(isUp: Boolean) {
        try{
            sequenceNumber.value?.toInt()?.let { seq ->
                updateSequence(seq + if (isUp) 1 else -1)
            }
        }catch (e : Exception){
            e.printStackTrace()
        }
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(diceKey: DiceKey<Face>,
                   @Assisted("recipe") recipe: DerivationRecipe?,
                   @Assisted("template") template: DerivationRecipe?): RecipeViewModel
    }

    companion object {
        fun provideFactory(
                assistedFactory: AssistedFactory,
                diceKey: DiceKey<Face>,
                recipe: DerivationRecipe?,
                template: DerivationRecipe?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(diceKey = diceKey, recipe = recipe, template = template) as T
            }
        }
    }
}