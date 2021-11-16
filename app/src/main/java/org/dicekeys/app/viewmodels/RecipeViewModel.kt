package org.dicekeys.app.viewmodels

import android.annotation.SuppressLint
import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.dicekeys.api.DerivationRecipe
import org.dicekeys.app.RecipeBuilder
import org.dicekeys.app.data.DerivedValue
import org.dicekeys.app.data.DerivedValueView
import org.dicekeys.app.repositories.RecipeRepository
import org.dicekeys.crypto.seeded.*
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face


class RecipeViewModel @AssistedInject constructor(
        private val recipeRepository: RecipeRepository,
        @Assisted val diceKey: DiceKey<Face>,
        @Assisted("recipe") val recipe: DerivationRecipe?,
        @Assisted("template") val template: DerivationRecipe?,
        @Assisted val deriveType: DerivationOptions.Type,
) : ViewModel(), LifecycleOwner {
    private val showRecipe = recipe != null
    val isCustomRecipe = MutableLiveData(recipe == null && template == null)

    var derivationRecipe = MutableLiveData(recipe ?: template)
    var sequenceNumber = MutableLiveData(recipe?.sequence?.toString() ?: "1")
    var recipeIsSaved = MutableLiveData(if(recipe != null) recipeRepository.exists(recipe) else false)
    var recipeBuilder = RecipeBuilder(deriveType, template)

    var derivedValueView: MutableLiveData<DerivedValueView> = MutableLiveData()
    var derivedValue: MutableLiveData<DerivedValue> = MutableLiveData()
    var derivedValueAsString = MutableLiveData<String>(null)

    val domain: MutableLiveData<String> = MutableLiveData()
    val purpose: MutableLiveData<String> = MutableLiveData()
    val rawJson: MutableLiveData<String> = MutableLiveData()
    val lengthInChars: MutableLiveData<String> = MutableLiveData()
    val lengthInBytes: MutableLiveData<String> = MutableLiveData()

    init {

        domain.observe(viewLifecycleOwner){
            recipeBuilder.updateDomains(it)
            updateRecipe()
        }

        purpose.observe(viewLifecycleOwner){
            recipeBuilder.updatePurpose(it)
            updateRecipe()
        }

        rawJson.observe(viewLifecycleOwner){
            // recipeBuilder.updatePurpose(it)
        }

        lengthInChars.observe(viewLifecycleOwner){
            var lengthInChars = 0
            try {
                lengthInChars = it.toInt()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            recipeBuilder.updateLengthInChars(lengthInChars)
            updateRecipe()
        }

        lengthInBytes.observe(viewLifecycleOwner){
            var lengthInBytes = 0
            try {
                lengthInBytes = it.toInt()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            recipeBuilder.updateLengthInBytes(lengthInBytes)
            updateRecipe()
        }

        deriveValue()
    }

    private fun deriveValue(){
        derivedValue.value = derivationRecipe.value?.recipeJson?.let{ recipeJson ->
            diceKey.toCanonicalRotation().toHumanReadableForm().let { seed ->
                try {
                    when (deriveType) {
                        DerivationOptions.Type.Password -> DerivedValue.Password(
                            Password.deriveFromSeed(
                                seed,
                                recipeJson
                            )
                        )
                        DerivationOptions.Type.Secret -> DerivedValue.Secret(
                            Secret.deriveFromSeed(
                                seed,
                                recipeJson
                            )
                        )
                        DerivationOptions.Type.SigningKey -> DerivedValue.SigningKey(
                            SigningKey.deriveFromSeed(
                                seed,
                                recipeJson
                            )
                        )
                        DerivationOptions.Type.SymmetricKey -> DerivedValue.SymmetricKey(
                            SymmetricKey.deriveFromSeed(seed, recipeJson)
                        )
                        DerivationOptions.Type.UnsealingKey -> DerivedValue.UnsealingKey(
                            UnsealingKey.deriveFromSeed(seed, recipeJson)
                        )
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                    null
                }
            }
        }

        updateSavedState()
        updateView()
    }

    private fun updateView(){
        derivedValueAsString.value = derivedValue.value?.valueForView(derivedValueView.value ?: DerivedValueView.JSON())
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

    @SuppressLint("StaticFieldLeak")
    private var lifecycleRegistry: LifecycleRegistry? = null

    override fun onCleared() {
        super.onCleared()
        lifecycleRegistry?.currentState = Lifecycle.State.DESTROYED
    }

    override fun getLifecycle(): Lifecycle {
        if(lifecycleRegistry == null) {
            lifecycleRegistry = LifecycleRegistry(this)
            lifecycleRegistry?.currentState = Lifecycle.State.STARTED
        }

        return lifecycleRegistry!!
    }

    val viewLifecycleOwner: LifecycleOwner
        get() = this

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(diceKey: DiceKey<Face>,
                   @Assisted("recipe") recipe: DerivationRecipe?,
                   @Assisted("template") template: DerivationRecipe?,
                   deriveType: DerivationOptions.Type): RecipeViewModel
    }

    companion object{
        fun provideFactory(
                assistedFactory: AssistedFactory,
                diceKey: DiceKey<Face>,
                recipe: DerivationRecipe?,
                template: DerivationRecipe?,
                deriveType: DerivationOptions.Type
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(diceKey = diceKey, recipe = recipe, template = template, deriveType = deriveType) as T
            }
        }
    }
}