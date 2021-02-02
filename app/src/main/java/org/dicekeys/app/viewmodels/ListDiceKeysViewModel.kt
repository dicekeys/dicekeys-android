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


class ListDiceKeysViewModel @AssistedInject constructor(private val encryptedStorage: EncryptedStorage, private val diceKeyRepository: DiceKeyRepository) : ViewModel() {
    fun remove(encryptedDiceKey: EncryptedDiceKey){
        encryptedStorage.remove(encryptedDiceKey)
        diceKeyRepository.remove(encryptedDiceKey.keyId)
    }
}