package org.dicekeys.app.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.dicekeys.api.DerivationRecipe
import org.dicekeys.app.repositories.RecipeRepository
import org.dicekeys.crypto.seeded.Password
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face


class RecipeViewModel @AssistedInject constructor(
        private val recipeRepository: RecipeRepository,
        @Assisted val diceKey: DiceKey<Face>,
        @Assisted val recipe: DerivationRecipe,
        @Assisted val template: DerivationRecipe?,
) : ViewModel() {

    var derivationRecipe = MutableLiveData(recipe);
    var sequenceNumber = MutableLiveData(recipe.sequence.toString())
    var password = MutableLiveData("")
    var recipeIsSaved = MutableLiveData(recipeRepository.exists(recipe))

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
            template?.let { template ->
                derivationRecipe.value = DerivationRecipe(template, sequence)
                sequenceNumber.value = sequence.toString()

                generatePassword()
            }
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
        fun create(diceKey: DiceKey<Face>, recipe: DerivationRecipe, template: DerivationRecipe?): RecipeViewModel
    }

    companion object {
        fun provideFactory(
                assistedFactory: AssistedFactory,
                diceKey: DiceKey<Face>,
                recipe: DerivationRecipe,
                template: DerivationRecipe?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(diceKey, recipe, template) as T
            }
        }
    }
}