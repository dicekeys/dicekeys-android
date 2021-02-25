package org.dicekeys.app.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
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

    var diceKey = MutableLiveData<DiceKey<Face>?>()

    val isSaved = MutableLiveData(diceKey.value?.let { encryptedStorage.exists(it.keyId) } ?: false)
    private val encryptedStorageObserver = Observer<List<EncryptedDiceKey>> {
        updateIsSaved()
    }

    init {
        // Listen to EncryptedStorage change events
        encryptedStorage
                .getDiceKeysLiveData()
                .observeForever(encryptedStorageObserver)
    }

    private fun updateIsSaved(){
        isSaved.postValue(diceKey.value?.let { encryptedStorage.exists(it.keyId) } ?: false)
    }

    fun remove() {
        diceKey.value?.let { encryptedStorage.remove(it) }
    }

    fun forget() {
        diceKey.value?.let { diceKeyRepository.remove(it) }
    }

    fun setDiceKey(dk: DiceKey<Face>){
        diceKey.value = dk
        updateIsSaved()
    }

    override fun onCleared() {
        super.onCleared()
        encryptedStorage.getDiceKeysLiveData().removeObserver(encryptedStorageObserver)
    }
}