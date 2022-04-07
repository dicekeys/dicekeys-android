package org.dicekeys.app.repositories

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import org.dicekeys.app.data.DiceKeyDescription
import org.dicekeys.app.encryption.EncryptedDiceKey
import org.dicekeys.app.encryption.EncryptedStorage
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face

/*
 * DiceKeyRepository
 *
 * A Repository handling DiceKeys, in-memory or backed by EncryptedStorage.
 *
 */

class DiceKeyRepository constructor(
    private val sharedPreferences: SharedPreferences,
    private val encryptedStorage: EncryptedStorage
) {
    private var diceKeys = mutableMapOf<String, DiceKey<Face>>()
    private var activeDiceKeyId : String? = null

    val hideFaces = MutableLiveData(sharedPreferences.getBoolean(HIDE_FACES, false))

    val availableDiceKeys = MutableLiveData(listOf<DiceKeyDescription>())

    init {
        encryptedStorage.getDiceKeysLiveData().observeForever {
            updateAvailableDiceKeys()
        }
    }

    private fun updateAvailableDiceKeys(){
        val encryptedDiceKeys = encryptedStorage.getDiceKeysLiveData().value ?: listOf()

        // Filter unique values
        val inMemoryOnlyDiceKeys = diceKeys.values.filter { inMemory -> encryptedDiceKeys.find { encrypted -> inMemory.keyId == encrypted.keyId } == null }

        // Combine both
        availableDiceKeys.value = inMemoryOnlyDiceKeys.map { DiceKeyDescription(it) } + encryptedDiceKeys.map { DiceKeyDescription(it) }
    }

    fun toggleHideFaces(){
        hideFaces.value = hideFaces.value!!.let {
            val hideFaces = !it
            sharedPreferences.edit().putBoolean(HIDE_FACES, hideFaces).apply()
            return@let hideFaces
        }
    }

    fun exists(diceKeyDescription: DiceKeyDescription) = exists(diceKeyDescription.keyId)
    fun exists(encryptedDiceKey: EncryptedDiceKey) = exists(encryptedDiceKey.keyId)
    fun exists(diceKey: DiceKey<*>) = exists(diceKey.keyId)
    fun exists(keyId: String) = diceKeys.containsKey(keyId)

    fun set(diceKey: DiceKey<Face>) {
        diceKeys[diceKey.keyId] = diceKey
        activeDiceKeyId = diceKey.keyId
        updateAvailableDiceKeys()
    }

    fun get(keyId: String) = diceKeys[keyId]

    fun remove(diceKey: DiceKey<*>) = remove(diceKey.keyId)
    fun remove(keyId: String){
        diceKeys.remove(keyId)
        updateAvailableDiceKeys()
    }

    fun clear(){
        diceKeys.clear()
        updateAvailableDiceKeys()
    }

    // Get the Active DiceKey
    fun getActiveDiceKey(): DiceKey<Face>? {
        return diceKeys[activeDiceKeyId]
    }

    fun size() = diceKeys.size

    companion object{
        const val HIDE_FACES = "hide_faces"
    }
}