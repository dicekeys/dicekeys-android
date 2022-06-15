package org.dicekeys.app.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.dicekeys.api.ApiStrings
import org.dicekeys.app.repositories.DiceKeyRepository
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face

class ApiRequestViewModel @AssistedInject constructor(
    private val diceKeyRepository: DiceKeyRepository,
    @Assisted val command: String
) : ViewModel() {
    var diceKey = MutableLiveData<DiceKey<Face>>()

    val sequence = MutableLiveData("")

    var title = MutableLiveData("")
    var subtitle = MutableLiveData("")
    var approve = MutableLiveData("")

    var recipe = MutableLiveData("")

    var createLabel = MutableLiveData("")
    var dataCreated = MutableLiveData("")

    val showSequence = MutableLiveData(true)

    val hideFaces = diceKeyRepository.hideFaces

    init {
        showSequence.value = when (command) {
            ApiStrings.Commands.getPassword, ApiStrings.Commands.getSecret -> {
                true
            }
            else -> false
        }
    }

    fun toggleHideFaces() {
        diceKeyRepository.toggleHideFaces()
    }

    fun sequenceUp() {
        updateSequence((sequence.value?.toIntOrNull() ?: 1) + 1)
    }

    fun sequenceDown() {
        updateSequence((sequence.value?.toIntOrNull() ?: 2) - 1)
    }

    private fun updateSequence(seq: Int) {
        sequence.postValue(if (seq > 1) seq.toString() else "")
    }

    override fun onCleared() {
        super.onCleared()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(command: String): ApiRequestViewModel
    }

    companion object{
        fun provideFactory(
            assistedFactory: AssistedFactory,
            command: String,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(command = command) as T
            }
        }
    }
}