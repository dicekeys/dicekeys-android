package org.dicekeys.app.viewmodels

import android.annotation.SuppressLint
import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val recipeBuilder = if(isEditable) RecipeBuilder(type = deriveType, scope = viewModelScope, template = recipe) else null
    val isCustomRecipe = recipe == null

    var derivationRecipe = if(isEditable) recipeBuilder!!.derivationRecipeLiveData else MutableLiveData(recipe)
    var recipeIsSaved = MutableLiveData(if(recipe != null) recipeRepository.exists(recipe) else false)

    var derivedValueView: MutableLiveData<DerivedValueView> = MutableLiveData()
    var derivedValue: MutableLiveData<DerivedValue> = MutableLiveData()
    var derivedValueAsString = MutableLiveData<String>(null)
    var derivedQrCodeTextAsString = MutableLiveData<String>(null)

    init {
        deriveValue()

        recipeBuilder
            ?.derivationRecipeLiveData
            ?.asFlow()
            ?.drop(1) // drop initial value
            ?.onEach {
                deriveValue()
            }?.launchIn(viewModelScope)
    }

    private fun deriveValue(){
        viewModelScope.launch {
            derivedValue.value = derivationRecipe.value?.recipeJson?.let { recipeJson ->
                // Run in IO thread for performance reasons
                withContext(Dispatchers.IO) {
                    try {
                        diceKey.toCanonicalRotation().toHumanReadableForm().let { seed ->
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
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
            }

            updateSavedState()
            updateView()
        }
    }

    private fun updateView(){
        val view = derivedValueView.value ?: DerivedValueView.JSON()
        derivedValue.value?.valueForView(view).let { value ->
            derivedValueAsString.value = value

            // Provide a different content for QR codes if required in the future
            derivedQrCodeTextAsString.value = when(view){
                else -> { value }
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

    /**
     * Up/down sequence number
     */
    fun sequenceUpDown(isUp: Boolean) {
        if (isUp) {
            recipeBuilder?.sequenceUp()
        } else {
            recipeBuilder?.sequenceDown()
        }
        recipeBuilder?.build()
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