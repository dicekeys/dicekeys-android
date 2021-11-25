package org.dicekeys.app.viewmodels

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import org.dicekeys.api.DerivationRecipe
import org.dicekeys.app.R
import org.dicekeys.app.RecipeBuilder
import org.dicekeys.app.data.DerivedValue
import org.dicekeys.app.data.DerivedValueView
import org.dicekeys.app.databinding.EditRecipeBottomSheetFragmentBinding
import org.dicekeys.app.fragments.dicekey.RecipeFragment
import org.dicekeys.app.repositories.RecipeRepository
import org.dicekeys.crypto.seeded.*
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face


class EditRecipeViewModel @AssistedInject constructor(
    @Assisted val recipeBuilder: RecipeBuilder,
) : ViewModel(), LifecycleOwner {
    val deriveType = recipeBuilder.type
    val isRawJson = MutableLiveData(recipeBuilder.rawJson != null)

    val domains = MutableLiveData(recipeBuilder.domains ?: "")
    val purpose = MutableLiveData(recipeBuilder.purpose ?: "")
    val rawJson = MutableLiveData(recipeBuilder.rawJson ?: "")
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
//        rawJson.value = derivationRecipe.value?.recipeJson
//        recipeBuilder.updateRawJson(recipeBuilder.getDerivationRecipe()?.recipeJson ?: "{}")
        rawJson.value = recipeBuilder.getDerivationRecipe()?.recipeJson ?: "{}"
    }

    private fun updateRecipe() {
        recipeBuilder.build()


//            if (template != null) {
//                derivationRecipe.value = DerivationRecipe.createRecipeFromTemplate(
//                    template,
//                    sequenceNumber.value!!.toInt()
//                )
//            } else {
//                derivationRecipe.value = recipeBuilder.getDerivationRecipe()
//            }

//            if (isRawJson.value == true) {
//                derivationRecipe.value.let { recipe ->
//                    rawJson.value = recipe?.recipeJson
//                    lengthInBytes.value = recipe?.lengthInBytes?.toString(10) ?: ""
//                    lengthInChars.value = recipe?.lengthInChars?.toString(10) ?: ""
//                }
//            }

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