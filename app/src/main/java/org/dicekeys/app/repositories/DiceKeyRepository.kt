package org.dicekeys.app.repositories

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import org.dicekeys.app.encryption.EncryptedDiceKey
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face

/*
 * DiceKeyRepository
 *
 * A Repository handling in-memory DiceKeys.
 * The app currently allows only one unlocked (in-memory) DiceKey, the repo can handle multiple.
 *
 */

class DiceKeyRepository(private val sharedPreferences: SharedPreferences) {
    private var diceKeys = mutableMapOf<String, DiceKey<Face>>()
    private var activeDiceKeyId : String? = null

    val hideFaces = MutableLiveData(sharedPreferences.getBoolean(HIDE_FACES, false))

    fun toggleHideFaces(){
        hideFaces.value = hideFaces.value!!.let {
            val hideFaces = !it
            sharedPreferences.edit().putBoolean(HIDE_FACES, hideFaces).apply()
            return@let hideFaces
        }
    }

    fun exists(encryptedDiceKey: EncryptedDiceKey) = exists(encryptedDiceKey.keyId)
    fun exists(diceKey: DiceKey<*>) = exists(diceKey.keyId)
    fun exists(keyId: String) = diceKeys.containsKey(keyId)

    fun set(diceKey: DiceKey<Face>) {
        diceKeys[diceKey.keyId] = diceKey
        activeDiceKeyId = diceKey.keyId
    }

    fun get(keyId: String) = diceKeys[keyId]

    fun remove(diceKey: DiceKey<*>) = remove(diceKey.keyId)
    fun remove(keyId: String){
        diceKeys.remove(keyId)
    }

    fun clear(){
        diceKeys.clear()
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