package org.dicekeys.app.viewmodels

import android.annotation.SuppressLint
import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.dicekeys.app.RecipeBuilder


class EditRecipeViewModel @AssistedInject constructor(
    @Assisted val recipeBuilder: RecipeBuilder,
) : ViewModel(), LifecycleOwner {
    val deriveType = recipeBuilder.type
    val isRawJson = MutableLiveData(recipeBuilder.rawJson != null)

    val recipeName = MutableLiveData(if(recipeBuilder.rawJson != null) recipeBuilder.name else "")

    val domains = MutableLiveData(recipeBuilder.domains ?: "")
    val purpose = MutableLiveData(recipeBuilder.purpose ?: "")
    val rawJson = MutableLiveData(if(isRawJson.value!!) recipeBuilder.getDerivationRecipe()?.recipeJson ?: "{}" else recipeBuilder.rawJson)
    val lengthInChars = MutableLiveData(if(recipeBuilder.lengthInChars > 0) recipeBuilder.lengthInChars.toString() else "")
    val lengthInBytes = MutableLiveData(if(recipeBuilder.lengthInBytes > 0) recipeBuilder.lengthInBytes.toString() else "")

    init {
        domains
            .asFlow()
            .drop(1) // drop initial value
            .filterNotNull()
            .onEach { domains ->
                recipeBuilder.updateDomains(domains)
                updateRecipe()
            }.launchIn(viewModelScope)


        purpose
            .asFlow()
            .drop(1) // drop initial value
            .filterNotNull()
            .onEach { purpose ->
                recipeBuilder.updatePurpose(purpose)
                updateRecipe()
            }.launchIn(viewModelScope)

        rawJson
            .asFlow()
            .drop(1) // drop initial value
            .filterNotNull()
            .onEach { rawJson ->
                recipeBuilder.updateRawJson(rawJson)
                updateRecipe()
            }.launchIn(viewModelScope)

        recipeName
            .asFlow()
            .drop(1) // drop initial value
            .filterNotNull()
            .onEach { recipeName ->
                recipeBuilder.updateName(recipeName)
                updateRecipe()
            }.launchIn(viewModelScope)

        lengthInChars
            .asFlow()
            .drop(1) // drop initial value
            .filterNotNull()
            .onEach { lengthInCharsString ->
                var lengthInChars = 0
                try {
                    lengthInChars = lengthInCharsString.toInt()
                } catch (e: Exception) { }
                recipeBuilder.updateLengthInChars(lengthInChars)
                updateRecipe()
            }.launchIn(viewModelScope)

        lengthInBytes
            .asFlow()
            .drop(1) // drop initial value
            .filterNotNull()
            .onEach { lengthInBytesString ->
                var lengthInBytes = 0
                try {
                    lengthInBytes = lengthInBytesString.toInt()
                } catch (e: Exception) { }
                recipeBuilder.updateLengthInBytes(lengthInBytes)
                updateRecipe()
            }.launchIn(viewModelScope)
    }

    fun editRawJson() {
        isRawJson.value = true
        rawJson.value = recipeBuilder.getDerivationRecipe()?.recipeJson ?: "{}"
    }

    private fun updateRecipe() {
        recipeBuilder.build()
    }

    @SuppressLint("StaticFieldLeak")
    private var lifecycleRegistry: LifecycleRegistry? = null

    override fun onCleared() {
        super.onCleared()
        lifecycleRegistry?.currentState = Lifecycle.State.DESTROYED
    }

    override fun getLifecycle(): Lifecycle {
        if (lifecycleRegistry == null) {
            lifecycleRegistry = LifecycleRegistry(this)
            lifecycleRegistry?.currentState = Lifecycle.State.STARTED
        }

        return lifecycleRegistry!!
    }

    val viewLifecycleOwner: LifecycleOwner
        get() = this

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(
            recipeBuilder: RecipeBuilder
        ): EditRecipeViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            recipeBuilder: RecipeBuilder
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(recipeBuilder = recipeBuilder) as T
            }
        }
    }
}