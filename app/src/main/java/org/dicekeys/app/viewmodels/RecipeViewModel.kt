package org.dicekeys.app.viewmodels

import android.text.Html
import android.text.Spanned
import androidx.core.text.toSpanned
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.dicekeys.api.DerivationRecipe
import org.dicekeys.app.RecipeBuilder
import org.dicekeys.app.data.DeriveType
import org.dicekeys.app.data.DerivedValue
import org.dicekeys.app.data.DerivedValueView
import org.dicekeys.app.data.DerivedValueView.BIP39
import org.dicekeys.app.extensions.toHexString
import org.dicekeys.app.repositories.RecipeRepository
import org.dicekeys.crypto.seeded.*
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face


class RecipeViewModel @AssistedInject constructor(
        private val recipeRepository: RecipeRepository,
        @Assisted val diceKey: DiceKey<Face>,
        @Assisted("recipe") val recipe: DerivationRecipe?,
        @Assisted("template") val template: DerivationRecipe?,
        @Assisted val deriveType: DeriveType,
) : ViewModel() {
    private val showRecipe = recipe != null
    val isCustomRecipe = MutableLiveData(recipe == null && template == null)

    var derivationRecipe = MutableLiveData(recipe ?: template)
    var sequenceNumber = MutableLiveData(recipe?.sequence?.toString() ?: "1")
    var recipeIsSaved = MutableLiveData(if(recipe != null) recipeRepository.exists(recipe) else false)
    var recipeBuilder = RecipeBuilder(template)

    var derivedValueView: MutableLiveData<DerivedValueView> = MutableLiveData()
    var derivedValue: MutableLiveData<DerivedValue> = MutableLiveData()
    var derivedValueAsString = MutableLiveData<String>(null)

    init {
        deriveValue()
    }

    private fun deriveValue(){
        derivationRecipe.value?.recipeJson?.let{ recipeJson ->
            diceKey.toCanonicalRotation().toHumanReadableForm().let { seed ->

                derivedValue.value = when (deriveType) {
                    DeriveType.Password -> DerivedValue.Password(Password.deriveFromSeed(seed, recipeJson))
                    DeriveType.Secret -> DerivedValue.Secret(Secret.deriveFromSeed(seed, recipeJson))
                    DeriveType.SigningKey -> DerivedValue.SigningKey(SigningKey.deriveFromSeed(seed, recipeJson))
                    DeriveType.SymmetricKey -> DerivedValue.SymmetricKey(SymmetricKey.deriveFromSeed(seed, recipeJson))
                    DeriveType.UnsealingKey -> DerivedValue.UnsealingKey(UnsealingKey.deriveFromSeed(seed, recipeJson))
                }

                updateView()
            }

            updateSavedState()
        }
    }

    private fun updateView(){
        derivedValue.value?.let {
            it.valueForView(derivedValueView.value ?: DerivedValueView.JSON()).also {
                derivedValueAsString.value = it
            }
        }
    }

    fun setView(view: DerivedValueView){
        derivedValueView.value = view
        updateView()
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

            deriveValue()
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
                   @Assisted("template") template: DerivationRecipe?,
                   deriveType: DeriveType): RecipeViewModel
    }

    companion object {
        fun provideFactory(
                assistedFactory: AssistedFactory,
                diceKey: DiceKey<Face>,
                recipe: DerivationRecipe?,
                template: DerivationRecipe?,
                deriveType: DeriveType
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(diceKey = diceKey, recipe = recipe, template = template, deriveType = deriveType) as T
            }
        }
    }
}