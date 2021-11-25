package org.dicekeys.app.viewmodels

import android.annotation.SuppressLint
import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
    @Assisted val recipe: DerivationRecipe?,
    @Assisted val isEditable: Boolean,
    @Assisted val deriveType: DerivationOptions.Type,
) : ViewModel(), LifecycleOwner {
    val recipeBuilder = RecipeBuilder(type = deriveType, template = recipe)
    val isCustomRecipe = recipe == null

    var derivationRecipe = recipeBuilder.derivationRecipe//MutableLiveData(recipe)
    var sequenceNumber = MutableLiveData(recipe?.sequence?.toString() ?: "1")
    var recipeIsSaved = MutableLiveData(if(recipe != null) recipeRepository.exists(recipe) else false)

    var derivedValueView: MutableLiveData<DerivedValueView> = MutableLiveData()
    var derivedValue: MutableLiveData<DerivedValue> = MutableLiveData()
    var derivedValueAsString = MutableLiveData<String>(null)

    init {
        deriveValue()

        recipeBuilder
            .derivationRecipe
            .asFlow()
            .drop(1) // drop initial value
            .onEach {
                deriveValue()
            }.launchIn(viewModelScope)
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
            updateRecipe()
        }
    }

    private fun updateRecipe(){
            recipe?.let { recipe ->
                derivationRecipe.value = recipe.createDerivationRecipeForSequence(sequenceNumber.value!!.toInt())
            }
            deriveValue()
    }

    /**
     * Up/down sequence number
     */
    fun sequenceUpDown(isUp: Boolean) {
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
                   recipe: DerivationRecipe?,
                   isEditable: Boolean,
                   deriveType: DerivationOptions.Type): RecipeViewModel
    }

    companion object{
        fun provideFactory(
                assistedFactory: AssistedFactory,
                diceKey: DiceKey<Face>,
                recipe: DerivationRecipe?,
                isEditable: Boolean,
                deriveType: DerivationOptions.Type
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(diceKey = diceKey, recipe = recipe, isEditable = isEditable, deriveType = deriveType) as T
            }
        }
    }
}