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

    fun editRawJson(isEditRawJson: Boolean) {
        recipeBuilder.setEditRawJson(isEditRawJson)
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