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


class DiceKeyViewModel @AssistedInject constructor(val encryptedStorage: EncryptedStorage, val diceKeyRepository: DiceKeyRepository, @Assisted val keyId: String) : ViewModel() {
    val isSaved = MutableLiveData<Boolean>(false)
    val diceKey = MutableLiveData<DiceKey<*>>()

    private val encryptedStorageObserver = Observer<List<EncryptedDiceKey>> {
        diceKey.value?.let { diceKey ->
            isSaved.postValue(it.find { it.keyId == diceKey.keyId } != null)
        }
    }

    init {
        diceKeyRepository.get(keyId)?.let {
            diceKey.value = it
        }

        encryptedStorage.getDiceKeysLiveData().observeForever(encryptedStorageObserver)
    }

    fun remove() {
        diceKey.value?.let { encryptedStorage.remove(it) }
    }

    fun forget() {
        diceKey.value?.let { diceKeyRepository.remove(it) }
    }

    fun save(encryptedDiceKey: EncryptedDiceKey) {
        diceKey.value?.let { encryptedStorage.save(it, encryptedDiceKey.encryptedData) }
    }

    override fun onCleared() {
        super.onCleared()
        encryptedStorage.getDiceKeysLiveData().removeObserver(encryptedStorageObserver)
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(keyId: String): DiceKeyViewModel
    }

    companion object {
        fun provideFactory(
                assistedFactory: AssistedFactory,
                keyId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(keyId) as T
            }
        }
    }
}