package org.dicekeys.app.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.dicekeys.app.encryption.EncryptedDiceKey
import org.dicekeys.app.encryption.EncryptedStorage
import org.dicekeys.app.repositories.DiceKeyRepository
import org.dicekeys.dicekey.DiceKey


class DiceKeyViewModel @AssistedInject constructor(private val encryptedStorage: EncryptedStorage, private val diceKeyRepository: DiceKeyRepository, @Assisted val diceKey: DiceKey<*>) : ViewModel() {
    val isSaved = MutableLiveData(encryptedStorage.exists(diceKey.keyId))
    private val encryptedStorageObserver = Observer<List<EncryptedDiceKey>> { list ->
        isSaved.postValue(list.find { it.keyId == diceKey.keyId } != null)
    }

    init {
        // Listen to EncryptedStorage change events
        encryptedStorage
                .getDiceKeysLiveData()
                .observeForever(encryptedStorageObserver)
    }

    fun remove() {
        encryptedStorage.remove(diceKey)
    }

    fun forget() {
        diceKeyRepository.remove(diceKey)
    }

    override fun onCleared() {
        super.onCleared()
        encryptedStorage.getDiceKeysLiveData().removeObserver(encryptedStorageObserver)
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(diceKey: DiceKey<*>): DiceKeyViewModel
    }

    companion object {
        fun provideFactory(
                assistedFactory: AssistedFactory,
                diceKey: DiceKey<*>
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(diceKey) as T
            }
        }
    }
}