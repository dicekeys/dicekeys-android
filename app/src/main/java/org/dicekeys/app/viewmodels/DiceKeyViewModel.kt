package org.dicekeys.app.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.dicekeys.app.encryption.EncryptedDiceKey
import org.dicekeys.app.encryption.EncryptedStorage
import org.dicekeys.app.repositories.DiceKeyRepository
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import javax.inject.Inject

@HiltViewModel
class DiceKeyViewModel @Inject constructor(
        private val encryptedStorage: EncryptedStorage,
        private val diceKeyRepository: DiceKeyRepository
) : ViewModel() {

    var diceKey = MutableLiveData<DiceKey<Face>>()
    val hideFaces = diceKeyRepository.hideFaces

    val isSaved = MutableLiveData(diceKey.value?.let { encryptedStorage.exists(it.keyId) } ?: false)
    private val encryptedStorageObserver = Observer<List<EncryptedDiceKey>> {
        updateIsSaved()
    }

    init {
        // Get Active DiceKey
        diceKey.value = diceKeyRepository.getActiveDiceKey()

        // Listen to EncryptedStorage change events
        encryptedStorage
                .getDiceKeysLiveData()
                .observeForever(encryptedStorageObserver)
    }

    private fun updateIsSaved(){
        isSaved.postValue(diceKey.value?.let { encryptedStorage.exists(it.keyId) } ?: false)
    }

    fun toggleHideFaces(){
        diceKeyRepository.toggleHideFaces()
    }

    fun remove() {
        diceKey.value?.let { encryptedStorage.remove(it) }
    }

    fun forget() {
        diceKey.value?.let { diceKeyRepository.remove(it) }
    }

    override fun onCleared() {
        super.onCleared()
        encryptedStorage.getDiceKeysLiveData().removeObserver(encryptedStorageObserver)
    }
}