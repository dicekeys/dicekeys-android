package org.dicekeys.app.encryption

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.dicekeys.dicekey.DiceKey

class EncryptedStorage(private val sharedPreferences: SharedPreferences) {
    private val diceKeysLiveData : MutableLiveData<List<EncryptedDiceKey>> = MutableLiveData(listOf())

    private val sharedPreferencesListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        updateDiceKeys()
    }

    init {
        updateDiceKeys()
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }

    private fun updateDiceKeys(){
        GlobalScope.launch {
            val list = mutableListOf<EncryptedDiceKey>()
            for(key in sharedPreferences.all.keys){
                getEncryptedData(key)?.let {
                    list += it
                }
            }
            diceKeysLiveData.postValue(list)
        }
    }

    fun getDiceKeysLiveData(): LiveData<List<EncryptedDiceKey>> = diceKeysLiveData

    fun save(diceKey: DiceKey<*>, encryptedData: EncryptedData){
        // TODO add center face
        val encryptedDiceKey = EncryptedDiceKey(keyId = diceKey.keyId, centerFace = diceKey.centerFace().toHumanReadableForm(true), encryptedData = encryptedData)

        sharedPreferences
                .edit()
                .putString(encryptedDiceKey.keyId, encryptedDiceKey.toString())
                .apply()
    }

    fun remove(encryptedDiceKey: EncryptedDiceKey){
        remove(encryptedDiceKey.keyId)
    }

    fun remove(diceKey: DiceKey<*>){
        remove(diceKey.keyId)
    }

    private fun remove(id: String){
        sharedPreferences.edit().remove(id).apply()
    }

    fun getEncryptedData(id: String): EncryptedDiceKey? {
        return sharedPreferences.getString(id, null)?.let{
            return Json.decodeFromString(it)
        }
    }


}