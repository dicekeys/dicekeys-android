package org.dicekeys.app.viewmodels

import android.annotation.SuppressLint
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dicekeys.api.bip39RandomnessRecipeTemplate
import org.dicekeys.app.RecipeBuilder
import org.dicekeys.app.data.DerivedValue
import org.dicekeys.app.data.DerivedValueView
import org.dicekeys.crypto.seeded.*
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face


class Bip39RandomnessViewModel : ViewModel(), LifecycleOwner {
    val recipeBuilder = RecipeBuilder(
        type = DerivationOptions.Type.Secret,
        scope = viewModelScope,
        template = bip39RandomnessRecipeTemplate
    )
    var derivationRecipe = recipeBuilder.derivationRecipeLiveData

    var derivedValueView: MutableLiveData<DerivedValueView> =
        MutableLiveData(DerivedValueView.BIP39_12())
    var derivedValue: MutableLiveData<DerivedValue> = MutableLiveData()
    var derivedValueAsString = MutableLiveData<String>()

    val isScanned = MutableLiveData(false)
    val diceKey = MutableLiveData(DiceKey.createFromRandom())

    private val _diceKey
        get() = diceKey.value!!

    init {
        recipeBuilder.sequence.value = "1"

        deriveValue()

        recipeBuilder
            .derivationRecipeLiveData
            .asFlow()
            .drop(1) // drop initial value
            .onEach {
                deriveValue()
            }.launchIn(viewModelScope)
    }

    private fun deriveValue() {
        if (_diceKey == null) {


        } else {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {

                    derivedValue.postValue(derivationRecipe.value?.recipeJson?.let { recipeJson ->
                        // Run in IO thread for performance reasons
                        try {
                            _diceKey.toHumanReadableForm().let { seed -> DerivedValue.Secret(Secret.deriveFromSeed(seed, recipeJson)) }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    })
                }

                updateView()
            }
        }
    }

    private fun updateView() {
        derivedValue.value?.valueForView(derivedValueView.value ?: DerivedValueView.BIP39_12())
            ?.let {
                derivedValueAsString.value = it
            }
    }

    fun setView(view: DerivedValueView) {
        derivedValueView.value = view
        updateView()
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

    fun setScannedDiceKey(scannedDiceKey: DiceKey<Face>): Boolean {
        return if (_diceKey != scannedDiceKey) {
            diceKey.value = scannedDiceKey
            isScanned.value = true
            recipeBuilder.sequence.value = "1"
            deriveValue()
            true
        }else {
            false
        }
    }

    val viewLifecycleOwner: LifecycleOwner
        get() = this
}