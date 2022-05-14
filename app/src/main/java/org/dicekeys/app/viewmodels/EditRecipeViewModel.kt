package org.dicekeys.app.viewmodels

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.dicekeys.app.RecipeBuilder
import org.dicekeys.crypto.seeded.DerivationOptions


class EditRecipeViewModel @AssistedInject constructor(
    @Assisted val type: DerivationOptions.Type?,
    @Assisted val initRecipeBuilder: RecipeBuilder?
) : ViewModel(), LifecycleOwner {
    val recipeBuilder = initRecipeBuilder ?: RecipeBuilder(type = type ?: DerivationOptions.Type.Password, scope = viewModelScope, template = null)

    val deriveType = recipeBuilder.type

    fun editRawJson(isEditRawJson: Boolean) {
        recipeBuilder.setEditRawJson(isEditRawJson)
    }

    private val lifecycleRegistry: LifecycleRegistry by lazy {
        LifecycleRegistry(this).also {
            it.currentState = Lifecycle.State.STARTED
        }
    }

    override fun onCleared() {
        super.onCleared()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    override fun getLifecycle(): Lifecycle = lifecycleRegistry

    val viewLifecycleOwner: LifecycleOwner
        get() = this

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(
            type: DerivationOptions.Type?,
            initRecipeBuilder: RecipeBuilder?
        ): EditRecipeViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            type: DerivationOptions.Type?,
            initRecipeBuilder: RecipeBuilder?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(type = type, initRecipeBuilder = initRecipeBuilder) as T
            }
        }
    }
}