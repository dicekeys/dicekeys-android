package org.dicekeys.app.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.dicekeys.app.data.DiceKeyDescription
import org.dicekeys.app.encryption.EncryptedDiceKey
import org.dicekeys.app.encryption.EncryptedStorage
import org.dicekeys.app.repositories.DiceKeyRepository
import javax.inject.Inject


@HiltViewModel
class ListDiceKeysViewModel @Inject constructor(private val encryptedStorage: EncryptedStorage, private val diceKeyRepository: DiceKeyRepository) : ViewModel() {

    fun isDiceKeyInMemory(encryptedDiceKey: EncryptedDiceKey) = diceKeyRepository.exists(encryptedDiceKey)

    fun getDiceKey(diceKeyDescription: DiceKeyDescription)  = diceKeyRepository.get(diceKeyDescription.keyId)

    fun remove(diceKeyDescription: DiceKeyDescription) {
        encryptedStorage.getEncryptedData(diceKeyDescription.keyId)?.let {
            // Remove from Storage
            encryptedStorage.remove(it)
        }
        // Remove from Memory
        diceKeyRepository.remove(diceKeyDescription.keyId)
    }
}